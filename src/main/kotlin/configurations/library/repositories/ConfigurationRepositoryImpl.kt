package configurations.library.repositories

import com.flexypixelgalleryapi.app.entities.*
import configurations.common.FrameData
import configurations.common.PanelData
import configurations.library.models.*
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.update_request.UpdateConfigurationDataRequest
import configurations.library.models.update_request.UpdateConfigurationStructureRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*


class ConfigurationRepositoryImpl : ConfigurationRepository {

    override fun exists(
        publicId: UUID,
    ): Boolean = transaction {
        LEDPanelsConfiguration.selectAll()
            .where(LEDPanelsConfiguration.publicId eq publicId)
            .firstOrNull() != null
    }

    override fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
    ): Boolean = transaction {
        LEDPanelsConfiguration.insert {
            it[this.publicId] = publicId
            it[this.ownerId] = ownerId
            it[this.name] = name
            it[this.description] = description
            it[this.createdAt] = LocalDateTime.now()
            it[this.updatedAt] = LocalDateTime.now()
        }.insertedCount > 0
    }

    override fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        requestData: CreateConfigurationData
    ): Boolean = transaction {
        val configId = LEDPanelsConfiguration.insert {
            it[this.publicId] = publicId
            it[this.ownerId] = ownerId
            it[this.name] = requestData.name
            it[this.description] = requestData.description
            it[this.createdAt] = LocalDateTime.now()
            it[this.updatedAt] = LocalDateTime.now()
        }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)
            ?: return@transaction false

        insertPanels(configId, requestData.panels)
        insertFrames(configId, requestData.frames)
        true
    }

    override fun updateFrame(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String,
        requesterId: Int
    ): Boolean = transaction {
        val configId = findOwnerConfigId(publicId, requesterId) ?: return@transaction false

        val updatedCount = Frame.update({
            (Frame.configurationId eq configId) and (Frame.index eq frameIndex)
        }) {
            it[panelPixelColors] = newFrameJson
        }
        updatedCount > 0
    }

    override fun updateConfigurationData(
        publicId: UUID,
        request: UpdateConfigurationDataRequest,
        requesterId: Int
    ): Boolean = transaction {
        val updated =
            LEDPanelsConfiguration.update({
                (LEDPanelsConfiguration.publicId eq publicId) and
                        (LEDPanelsConfiguration.ownerId eq requesterId)
            }) {
                it[name] = request.name
                it[description] = request.description
                it[updatedAt] = LocalDateTime.now()
            }
        updated > 0
    }

    override fun updateConfigurationStructure(
        publicId: UUID,
        request: UpdateConfigurationStructureRequest,
        requesterId: Int
    ): Boolean = transaction {
        val configId = findOwnerConfigId(publicId, requesterId) ?: return@transaction false

        LEDPanelsConfiguration.update({ LEDPanelsConfiguration.id eq configId }) {
            it[updatedAt] = LocalDateTime.now()
        }

        deletePanels(configId)
        insertPanels(configId, request.panels)

        deleteFrames(configId)
        insertFrames(configId, request.frames)

        true
    }

    override fun deleteConfiguration(publicId: UUID, requesterId: Int): Boolean {
        return transaction {
            val configId = findOwnerConfigId(publicId, requesterId) ?: return@transaction false
            deletePanels(configId)
            deleteFrames(configId)
            LEDPanelsConfiguration.deleteWhere { id eq configId } > 0
        }
    }

    override fun getFullConfiguration(publicId: UUID, requesterId: Int): ConfigurationFullResponse? {
        return transaction {
            val configRow = LEDPanelsConfiguration.selectAll().where {
                (LEDPanelsConfiguration.publicId eq publicId) and
                        (LEDPanelsConfiguration.ownerId eq requesterId)
            }
                .singleOrNull() ?: return@transaction null
            val configId = configRow[LEDPanelsConfiguration.id]

            val panels = Panel.selectAll().where { Panel.configurationId eq configId }
                .map { row ->
                    PanelData(
                        uid = row[Panel.uid],
                        x = row[Panel.x],
                        y = row[Panel.y],
                        direction = row[Panel.direction]
                    )
                }


            val frames = Frame.selectAll().where { Frame.configurationId eq configId }
                .orderBy(Frame.index to SortOrder.ASC)
                .map { row ->
                    FrameData(
                        index = row[Frame.index],
                        panelPixelColors = row[Frame.panelPixelColors]
                    )
                }

            ConfigurationFullResponse(
                publicId = configRow[LEDPanelsConfiguration.publicId],
                name = configRow[LEDPanelsConfiguration.name],
                description = configRow[LEDPanelsConfiguration.description],
                previewImageUrl = configRow[LEDPanelsConfiguration.previewImageUrl],
                createdAt = configRow[LEDPanelsConfiguration.createdAt],
                updatedAt = configRow[LEDPanelsConfiguration.updatedAt],
                panels = panels,
                frames = frames
            )
        }
    }

    override fun getConfigurationsByOwner(ownerId: Int): List<ConfigurationSummaryResponse> {
        return transaction {
            LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.ownerId eq ownerId }.map { configRow ->
                val forkStatus = configRow[LEDPanelsConfiguration.forkStatus]
                var forkInfo: ForkInfo? = null
                if (forkStatus != ForkStatus.ORIGINAL) {
                    val sourceConfigId = configRow[LEDPanelsConfiguration.sourceConfigurationId]
                    if (sourceConfigId != null) {
                        val origRow =
                            LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.id eq sourceConfigId }
                                .singleOrNull()
                        if (origRow != null) {
                            val sourceConfigurationPublicId = origRow[LEDPanelsConfiguration.publicId]
                            val authorId = origRow[LEDPanelsConfiguration.ownerId]
                            val authorInfo = getUserShortInfoById(authorId)
                            forkInfo = ForkInfo(
                                sourceConfigurationPublicId = sourceConfigurationPublicId,
                                author = authorInfo
                            )
                        }
                    }
                }

                ConfigurationSummaryResponse(
                    publicId = configRow[LEDPanelsConfiguration.publicId],
                    name = configRow[LEDPanelsConfiguration.name],
                    description = configRow[LEDPanelsConfiguration.description],
                    previewImageUrl = configRow[LEDPanelsConfiguration.previewImageUrl],
                    createdAt = configRow[LEDPanelsConfiguration.createdAt],
                    updatedAt = configRow[LEDPanelsConfiguration.updatedAt],
                    forkStatus = forkStatus,
                    forkInfo = forkInfo
                )
            }
        }
    }

    private fun getUserShortInfoById(ownerId: Int): AuthorInfo? = transaction {
        val userRow = User.selectAll().where{
            User.id eq ownerId
        }.singleOrNull()
        if (userRow!=null){
            AuthorInfo(
                publicId = userRow[User.publicId],
                username = userRow[User.username],
                displayName = userRow[User.displayName],
                avatarUrl = userRow[User.avatarUrl]
            )
        }else{
            null
        }
    }

    private fun deletePanels(configId: Int) {
        Panel.deleteWhere { configurationId eq configId }
    }

    private fun insertPanels(configId: Int, panels: List<PanelData>) {
        panels.forEach { panel ->
            Panel.insert {
                it[configurationId] = configId
                it[uid] = panel.uid
                it[x] = panel.x
                it[y] = panel.y
                it[direction] = panel.direction
            }
        }
    }

    private fun deleteFrames(configId: Int) {
        Frame.deleteWhere { configurationId eq configId }
    }

    private fun insertFrames(configId: Int, frames: List<FrameData>) {
        frames.forEach { frame ->
            Frame.insert {
                it[configurationId] = configId
                it[index] = frame.index
                it[panelPixelColors] = frame.panelPixelColors
            }
        }
    }

    private fun findOwnerConfigId(publicId: UUID, requesterId: Int): Int? =
        LEDPanelsConfiguration
            .selectAll()
            .where {
                (LEDPanelsConfiguration.publicId eq publicId) and
                        (LEDPanelsConfiguration.ownerId eq requesterId)
            }.singleOrNull()?.get(LEDPanelsConfiguration.id)

}