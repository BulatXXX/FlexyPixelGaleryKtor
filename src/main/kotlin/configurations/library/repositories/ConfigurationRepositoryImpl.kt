package configurations.library.repositories

import app.entities.*
import configurations.common.models.AuthorInfo
import configurations.library.models.ConfigurationFullResponse
import configurations.common.models.FrameData
import configurations.common.models.PanelData
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
            }) { configRow ->
                request.name?.let { configRow[LEDPanelsConfiguration.name] = it }
                request.description?.let { configRow[LEDPanelsConfiguration.description] = it }
                request.previewUrl?.let {
                    configRow[LEDPanelsConfiguration.previewImageUrl] = it
                }
                request.miniPreviewUrl?.let { configRow[LEDPanelsConfiguration.miniPreviewImageUrl] = it }
                request.useMiniPreview?.let { configRow[LEDPanelsConfiguration.useMiniPreview] = it }
                configRow[updatedAt] = LocalDateTime.now()
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
                        ((LEDPanelsConfiguration.ownerId eq requesterId) or (LEDPanelsConfiguration.isPublic eq true))
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
                miniPreviewImageUrl = configRow[LEDPanelsConfiguration.miniPreviewImageUrl],
                miniPreviewPanelUid = configRow[LEDPanelsConfiguration.miniPreviewPanelUid],
                forkStatus = configRow[LEDPanelsConfiguration.forkStatus],
                useMiniPreview = configRow[LEDPanelsConfiguration.useMiniPreview],
                createdAt = configRow[LEDPanelsConfiguration.createdAt],
                updatedAt = configRow[LEDPanelsConfiguration.updatedAt],
                panels = panels,
                frames = frames
            )
        }
    }

    override fun getConfigInfo(publicId: UUID): ConfigInfoResponse? = transaction {
        LEDPanelsConfiguration.selectAll().where {
            LEDPanelsConfiguration.publicId eq publicId
        }.map { configRow ->
            ConfigInfoResponse(
               name = configRow[LEDPanelsConfiguration.name],
                description = configRow[LEDPanelsConfiguration.description],
                previewImageUrl = configRow[LEDPanelsConfiguration.previewImageUrl],
                miniPreviewImageUrl = configRow[LEDPanelsConfiguration.miniPreviewImageUrl],
                forkStatus = configRow[LEDPanelsConfiguration.forkStatus]
            )
        }.singleOrNull()
    }

    override fun getConfigurationsByOwner(
        ownerId: Int,
        offset: Long,
        size: Int,
        forkStatus: ForkStatus?,
        isPublic: Boolean?
    ): List<ConfigurationSummaryResponse> {
        return transaction {
            val baseQuery = LEDPanelsConfiguration.selectAll().where {
                LEDPanelsConfiguration.ownerId eq ownerId
            }

            val filteredQuery = baseQuery.andWhere {
                Op.build {
                    listOfNotNull(
                        forkStatus?.let { LEDPanelsConfiguration.forkStatus eq it },
                        isPublic?.let { LEDPanelsConfiguration.isPublic eq it }
                    ).reduceOrNull { acc, expr -> acc and expr } ?: Op.TRUE
                }
            }

            filteredQuery.orderBy(LEDPanelsConfiguration.createdAt, SortOrder.DESC).limit(size).offset(offset)
                .map { configRow ->
                    val forkStatusRow = configRow[LEDPanelsConfiguration.forkStatus]
                    var forkInfo: ForkInfo? = null

                    if (forkStatusRow != ForkStatus.ORIGINAL) {
                        val sourceConfigId = configRow[LEDPanelsConfiguration.sourceConfigurationId]
                        if (sourceConfigId != null) {
                            val origRow = LEDPanelsConfiguration.selectAll().where {
                                LEDPanelsConfiguration.id eq sourceConfigId
                            }.singleOrNull()

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
                        miniPreviewImageUrl = configRow[LEDPanelsConfiguration.miniPreviewImageUrl],
                        miniPreviewPanelUid = configRow[LEDPanelsConfiguration.miniPreviewPanelUid],
                        useMiniPreview = configRow[LEDPanelsConfiguration.useMiniPreview],
                        createdAt = configRow[LEDPanelsConfiguration.createdAt],
                        updatedAt = configRow[LEDPanelsConfiguration.updatedAt],
                        forkStatus = forkStatusRow,
                        forkInfo = forkInfo,
                        isPublic = configRow[LEDPanelsConfiguration.isPublic],
                    )
                }
        }
    }


    private fun getUserShortInfoById(ownerId: Int): AuthorInfo? = transaction {
        val userRow = User.selectAll().where {
            User.id eq ownerId
        }.singleOrNull()
        if (userRow != null) {
            AuthorInfo(
                publicId = userRow[User.publicId],
                username = userRow[User.username],
                displayName = userRow[User.displayName],
                avatarUrl = userRow[User.avatarUrl]
            )
        } else {
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