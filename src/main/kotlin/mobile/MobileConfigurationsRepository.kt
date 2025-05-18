package mobile

import app.entities.Frame
import app.entities.LEDPanelsConfiguration
import app.entities.Panel
import kotlinx.serialization.json.Json
import mobile.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*


class MobileConfigurationsRepository {
    fun getAll(ownerId: Int) = transaction {
        LEDPanelsConfiguration.selectAll().orderBy(LEDPanelsConfiguration.createdAt, SortOrder.DESC).where {
            (LEDPanelsConfiguration.ownerId eq ownerId) and
                    (LEDPanelsConfiguration.isPublic eq false)
        }.map {
            ConfigData(
                publicId = it[LEDPanelsConfiguration.publicId],
                name = it[LEDPanelsConfiguration.name],
                createdAt = it[LEDPanelsConfiguration.createdAt],
                miniPreviewPanelUid = it[LEDPanelsConfiguration.miniPreviewPanelUid],
                miniPreviewImageUrl = it[LEDPanelsConfiguration.miniPreviewImageUrl],
            )
        }
    }

    fun isOwner(publicId: UUID, ownerId: Int): Boolean =
        LEDPanelsConfiguration.selectAll().where(
            (LEDPanelsConfiguration.publicId eq publicId)
                    and (LEDPanelsConfiguration.ownerId eq ownerId)
        ).count() > 0

    fun exists(publicId: UUID) = LEDPanelsConfiguration.selectAll().where(
        LEDPanelsConfiguration.publicId eq publicId
    ).count() > 0

    fun getConfigurations(publicId: UUID): MobileConfiguration? {
        val configRow = LEDPanelsConfiguration.selectAll().where {
            (LEDPanelsConfiguration.publicId eq publicId)
        }.singleOrNull() ?: return null

        val configId = configRow[LEDPanelsConfiguration.id]

        val panels = Panel
            .selectAll().where { Panel.configurationId eq configId }
            .map { row ->
                val raw = row[Panel.palette].orEmpty()
                val paletteList = if (raw.isNotBlank()) {
                    Json.decodeFromString<List<Palette>>(raw)
                } else {
                    emptyList()
                }
                MobilePanelsInfo(
                    x = row[Panel.x],
                    y = row[Panel.y],
                    uid = row[Panel.uid],
                    direction = row[Panel.direction],
                    palette = paletteList
                )
            }
        val frames = Frame.selectAll().where { Frame.configurationId eq configId }
            .orderBy(Frame.index to SortOrder.ASC)
            .map { row ->
                MobileFramesInfo(
                    index = row[Frame.index],
                    panelPixelColors = row[Frame.panelPixelColors]
                )
            }
        return MobileConfiguration(
            publicId = configRow[LEDPanelsConfiguration.publicId],
            name = configRow[LEDPanelsConfiguration.name],
            panelsInfo = panels,
            frameInfo = frames,
            interFrameDelay = configRow[LEDPanelsConfiguration.interFrameDelay],
        )
    }

    fun createConfiguration(
        ownerId: Int,
        configId: UUID,
        mobileConfiguration: MobileConfiguration,
        previewUrl: String,
        miniPreviewUrl: String,
    ): Boolean = transaction {
        val id = LEDPanelsConfiguration.insert {
            it[publicId] = configId
            it[LEDPanelsConfiguration.ownerId] = ownerId
            it[name] = mobileConfiguration.name
            it[description] = ""
            mobileConfiguration.interFrameDelay?.let { interFrameDelay ->
                it[LEDPanelsConfiguration.interFrameDelay] = interFrameDelay
            }
            it[miniPreviewImageUrl] = miniPreviewUrl
            it[miniPreviewPanelUid] = mobileConfiguration.miniPreviewPanelUid
            it[previewImageUrl] = previewUrl
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)
            ?: return@transaction false

        insertPanels(id, mobileConfiguration.panelsInfo)
        insertFrames(id, mobileConfiguration.frameInfo)

        true
    }

    private fun insertPanels(configId: Int, panels: List<MobilePanelsInfo>) {
        panels.forEach { panel ->
            Panel.insert {
                it[configurationId] = configId
                it[uid] = panel.uid
                it[x] = panel.x
                it[y] = panel.y
                it[direction] = panel.direction
                it[palette] = Json.encodeToString(panel.palette)
            }
        }
    }

    private fun deletePanels(configId: Int) {
        Panel.deleteWhere { configurationId eq configId }
    }

    private fun deleteFrames(configId: Int) {
        Frame.deleteWhere { configurationId eq configId }
    }

    private fun insertFrames(configId: Int, frames: List<MobileFramesInfo>) {
        frames.forEach { frame ->
            Frame.insert {
                it[configurationId] = configId
                it[index] = frame.index
                it[panelPixelColors] = frame.panelPixelColors
            }
        }
    }

    fun updateConfiguration(
        mobileConfiguration: MobileConfiguration,
        previewUrl: String,
        miniPreviewUrl: String,
    ): Boolean {
        if (mobileConfiguration.publicId == null) return false

        LEDPanelsConfiguration.update(
            { LEDPanelsConfiguration.publicId eq mobileConfiguration.publicId }
        ) {
            it[name] = mobileConfiguration.name
            it[miniPreviewImageUrl] = miniPreviewUrl
            it[miniPreviewPanelUid] = mobileConfiguration.miniPreviewPanelUid
            it[previewImageUrl] = previewUrl
            it[updatedAt] = LocalDateTime.now()
            mobileConfiguration.interFrameDelay?.let { interFrameDelay ->
                it[LEDPanelsConfiguration.interFrameDelay] = interFrameDelay
            }

        }
        val row = LEDPanelsConfiguration
            .selectAll()
            .where { LEDPanelsConfiguration.publicId eq mobileConfiguration.publicId }.singleOrNull() ?: return false

        val configInnerId = row[LEDPanelsConfiguration.id]
        deleteFrames(configInnerId)
        deletePanels(configInnerId)
        insertFrames(configInnerId, mobileConfiguration.frameInfo)
        insertPanels(configInnerId, mobileConfiguration.panelsInfo)
        return true
    }

    fun deleteConfiguration(configId: UUID): Boolean {
        val row =
            LEDPanelsConfiguration.selectAll().where { LEDPanelsConfiguration.publicId eq configId }.singleOrNull()
                ?: return false

        val configInnerId = row[LEDPanelsConfiguration.id]
        deleteFrames(configInnerId)
        deletePanels(configInnerId)
        LEDPanelsConfiguration.deleteWhere { LEDPanelsConfiguration.id eq configInnerId }
        return true
    }

    fun getPreviewUrls(configId: UUID): List<String?> {
        val row = LEDPanelsConfiguration.selectAll().where{
            LEDPanelsConfiguration.publicId eq configId
        }.singleOrNull() ?: return emptyList()
        val previewUrl = row[LEDPanelsConfiguration.previewImageUrl]
        val miniPreviewUrl = row[LEDPanelsConfiguration.miniPreviewImageUrl]

        return listOf(previewUrl,miniPreviewUrl)
    }


}