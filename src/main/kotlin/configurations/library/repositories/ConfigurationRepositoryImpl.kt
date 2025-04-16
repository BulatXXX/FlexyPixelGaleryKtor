package com.flexypixelgalleryapi.configurations.userLibrary.repositories

import com.flexypixelgalleryapi.configurations.userLibrary.models.ConfigurationForEditorFullData
import com.flexypixelgalleryapi.configurations.userLibrary.models.*
import com.flexypixelgalleryapi.app.entities.ForkStatus
import com.flexypixelgalleryapi.app.entities.Frame
import com.flexypixelgalleryapi.app.entities.LEDPanelsConfiguration
import com.flexypixelgalleryapi.app.entities.Panel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class ConfigurationRepositoryImpl : ConfigurationRepository {
    override fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
    ) {
        transaction {
            LEDPanelsConfiguration.insert {
                it[this.publicId] = publicId
                it[this.ownerId] = ownerId
                it[this.name] = name
                it[this.description] = description
                it[this.createdAt] = LocalDateTime.now()
                it[this.updatedAt] = LocalDateTime.now()
            }
        }
    }

    override fun updateFrameByPublicId(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String,
        requesterId: Int
    ): Boolean {
        return transaction {

            val configRow = LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }
                .singleOrNull() ?: return@transaction false
            if (configRow[LEDPanelsConfiguration.ownerId] != requesterId) return@transaction false
            val configId = configRow[LEDPanelsConfiguration.id]

            val updatedCount = Frame.update({
                (Frame.configurationId eq configId) and (Frame.index eq frameIndex)
            }) {
                it[panelPixelColors] = newFrameJson
            }
            updatedCount > 0
        }
    }

    override fun updateConfiguration(
        publicId: UUID,
        name: String?,
        description: String?,
        requesterId: Int
    ): Boolean {
        return transaction {
            val configRow =
                LEDPanelsConfiguration.selectAll().where(LEDPanelsConfiguration.publicId eq publicId).singleOrNull()
                    ?: return@transaction false
            if (configRow[LEDPanelsConfiguration.ownerId] != requesterId) return@transaction false

            val updatedCount =
                LEDPanelsConfiguration.update({ LEDPanelsConfiguration.id eq configRow[LEDPanelsConfiguration.id] }) {
                    if (name != null) it[this.name] = name
                    if (description != null) it[this.description] = description
                    it[this.updatedAt] = LocalDateTime.now()
                }
            updatedCount > 0
        }
    }

    override fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>,
        requesterId: Int
    ): Boolean {
        return transaction {
            val configRow =
                LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }.singleOrNull()
                    ?: return@transaction false
            if (configRow[LEDPanelsConfiguration.ownerId] != requesterId) return@transaction false
            val configId = configRow[LEDPanelsConfiguration.id]

            deletePanels(configId)
            insertPanels(configId, panels)

            deleteFrames(configId)
            insertFrames(configId, frames)

            LEDPanelsConfiguration.update({ LEDPanelsConfiguration.id eq configId }) {
                it[updatedAt] = LocalDateTime.now()
            }
            true
        }
    }

    private fun deletePanels(configId: Int) {
        Panel.deleteWhere { Panel.configurationId eq configId }
    }

    private fun insertPanels(configId: Int, panels: List<PanelData>) {
        panels.forEach { panel ->
            Panel.insert {
                it[Panel.configurationId] = configId
                it[Panel.uid] = panel.uid
                it[Panel.x] = panel.x
                it[Panel.y] = panel.y
                it[Panel.direction] = panel.direction
            }
        }
    }

    private fun deleteFrames(configId: Int) {
        Frame.deleteWhere { Frame.configurationId eq configId }
    }

    private fun insertFrames(configId: Int, frames: List<FrameData>) {
        frames.forEach { frame ->
            Frame.insert {
                it[Frame.configurationId] = configId
                it[Frame.index] = frame.index
                it[Frame.panelPixelColors] = frame.panelPixelColors
            }
        }
    }

    override fun deleteConfiguration(publicId: UUID, requesterId: Int): Boolean {
        return transaction {
            val configRow =
                LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }.singleOrNull()
                    ?: return@transaction false
            if (configRow[LEDPanelsConfiguration.ownerId] != requesterId) return@transaction false
            val configId = configRow[LEDPanelsConfiguration.id]
            deletePanels(configId)
            deleteFrames(configId)
            LEDPanelsConfiguration.deleteWhere { LEDPanelsConfiguration.id eq configId } > 0
        }
    }

    override fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        requestData: CreateConfigurationData
    ) {
        transaction {
            val configId = LEDPanelsConfiguration.insert {
                it[this.publicId] = publicId
                it[this.ownerId] = ownerId
                it[this.name] = requestData.name
                it[this.description] = requestData.description
                it[this.createdAt] = LocalDateTime.now()
                it[this.updatedAt] = LocalDateTime.now()
            }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)
                ?: error("Insert failed")

            insertPanels(configId, requestData.panels)
            insertFrames(configId, requestData.frames)

        }
    }

    override fun getFullConfiguration(publicId: UUID, requesterId: Int): ConfigurationForEditorFullData? {
        return transaction {
            val configRow = LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }
                .singleOrNull() ?: return@transaction null
            if (configRow[LEDPanelsConfiguration.ownerId] != requesterId) return@transaction null
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

            ConfigurationForEditorFullData(
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
                LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.ownerId eq ownerId }.map {
                    configRow->
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

                                //TODO getUserShortInfoById(ownerId: Int): AuthorInfo?
                                val dummyAuthor = AuthorInfo(
                                    publicId = UUID.randomUUID(),
                                    username = "dummyUser",
                                    displayName = "Dummy User",
                                    avatarUrl = null
                                )
                                forkInfo = ForkInfo(
                                    sourceConfigurationPublicId = sourceConfigurationPublicId,
                                    author = dummyAuthor
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


}