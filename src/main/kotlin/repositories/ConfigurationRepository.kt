package com.flexypixelgalleryapi.repositories

import com.flexypixelgalleryapi.entities.Frame
import com.flexypixelgalleryapi.entities.LEDPanelsConfiguration
import com.flexypixelgalleryapi.entities.Panel
import com.flexypixelgalleryapi.models.ConfigurationFullData
import com.flexypixelgalleryapi.models.configuration.FrameData
import com.flexypixelgalleryapi.models.configuration.PanelData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

interface ConfigurationRepository {
    fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
        previewImageUrl: String? = null
    )

    fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
        previewImageUrl: String? = null,
        panels: List<PanelData>,
        frames: List<FrameData>
    )

    fun updateConfiguration(publicId: UUID, name: String?, description: String?, previewImageUrl: String?): Boolean

    fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>
    ): Boolean

    fun deleteConfiguration(publicId: UUID): Boolean

    fun getFullConfiguration(publicId: UUID): ConfigurationFullData?

    fun updateFrameByPublicId(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String
    ): Boolean
}


class ConfigurationRepositoryImpl : ConfigurationRepository {
    override fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
        previewImageUrl: String?
    ) {
        transaction {
            LEDPanelsConfiguration.insert {
                it[this.publicId] = publicId
                it[this.ownerId] = ownerId
                it[this.name] = name
                it[this.description] = description
                it[this.previewImageUrl] = previewImageUrl
                it[this.createdAt] = LocalDateTime.now()
                it[this.updatedAt] = LocalDateTime.now()
            }
        }
    }

    override fun updateFrameByPublicId(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String
    ): Boolean {
        return transaction {

            val configRow = LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }
                .singleOrNull() ?: return@transaction false
            val configId = configRow[LEDPanelsConfiguration.id]

            val updatedCount = Frame.update({
                (Frame.configurationId eq configId) and (Frame.index eq frameIndex)
            }) {
                it[panelPixelColors] = newFrameJson
            }
            updatedCount > 0
        }
    }

    override fun updateConfiguration(publicId: UUID, name: String?, description: String?, previewImageUrl: String?): Boolean {
        return transaction {
            val updatedCount = LEDPanelsConfiguration.update({ LEDPanelsConfiguration.publicId eq publicId }) {
                if (name != null) it[this.name] = name
                if (description != null) it[this.description] = description
                if (previewImageUrl != null) it[this.previewImageUrl] = previewImageUrl
                it[this.updatedAt] = LocalDateTime.now()
            }
            updatedCount > 0
        }
    }

    override fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>
    ): Boolean {
        return transaction {
            val configRow = LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }.singleOrNull()
                ?: return@transaction false

            val configId = configRow[LEDPanelsConfiguration.id]

            deletePanels(configId)
            insertPanels(configId, panels)

            deleteFrames(configId)
            insertFrames(configId, frames)

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
    override fun deleteConfiguration(publicId: UUID): Boolean {
        return transaction {
            LEDPanelsConfiguration.deleteWhere { LEDPanelsConfiguration.publicId eq publicId } > 0
        }
    }

    override fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
        previewImageUrl: String?,
        panels: List<PanelData>,
        frames: List<FrameData>
    ) {
        transaction {
            // Вставляем конфигурацию и получаем внутреннее id
            val configId = LEDPanelsConfiguration.insert {
                it[this.publicId] = publicId
                it[this.ownerId] = ownerId
                it[this.name] = name
                it[this.description] = description
                it[this.previewImageUrl] = previewImageUrl
                it[this.createdAt] = LocalDateTime.now()
                it[this.updatedAt] = LocalDateTime.now()
            }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)
                ?: error("Insert failed")

            insertPanels(configId, panels)
            insertFrames(configId, frames)

        }
    }
    override fun getFullConfiguration(publicId: UUID): ConfigurationFullData? {
        return transaction {
            val configRow = LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq publicId }
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

            ConfigurationFullData(
                publicId = configRow[LEDPanelsConfiguration.publicId],
                ownerId = configRow[LEDPanelsConfiguration.ownerId],
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
}
