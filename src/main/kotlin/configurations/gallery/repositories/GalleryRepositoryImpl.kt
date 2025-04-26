package configurations.gallery.repositories

import app.entities.*
import configurations.gallery.models.publish_request.ConfigurationInfo
import configurations.common.models.FrameData
import configurations.common.models.PanelData
import configurations.gallery.models.publish_request.PublishRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class GalleryRepositoryImpl : GalleryRepository {

    override fun getConfigurationPublishingInfo(configId: UUID, requesterId: Int): ConfigurationInfo? = transaction {
        LEDPanelsConfiguration.selectAll().where {
            (LEDPanelsConfiguration.publicId eq configId) and
                    (LEDPanelsConfiguration.ownerId eq requesterId)
        }.map { row ->
            ConfigurationInfo(
                isPublic = row[LEDPanelsConfiguration.isPublic],
                sourceConfigurationId = row[LEDPanelsConfiguration.sourceConfigurationId],
                forkStatus = row[LEDPanelsConfiguration.forkStatus],
            )
        }.singleOrNull()
    }

    override fun exists(publicConfigId: UUID): Boolean = transaction {
        LEDPanelsConfiguration.selectAll().where {
            LEDPanelsConfiguration.publicId eq publicConfigId
        }.count() > 0
    }

    override fun publish(
        oldConfigID: UUID,
        publicConfigId: UUID,
        request: PublishRequest
    ): Boolean = transaction {

        val origRow = LEDPanelsConfiguration.selectAll().where {
            LEDPanelsConfiguration.publicId eq oldConfigID
        }.singleOrNull() ?: return@transaction false

        val origId = origRow[LEDPanelsConfiguration.id]

        val newId = insertPublishedConfiguration(publicConfigId, origRow, request) ?: return@transaction false

        insertDuplicatedPanels(origId, newId)

        insertDuplicatedFrames(origId, newId)

        LEDPanelsConfigurationMetadata.insert {
            it[configurationId] = newId
        }

        request.tagIds.forEach { tagId ->
            ConfigurationTags.insert {
                it[configurationId] = newId
                it[ConfigurationTags.tagId] = tagId
            }
        }

        return@transaction true
    }

    override fun subscribeConfiguration(sourcePublicConfigID: UUID, newConfigId: UUID, requesterId: Int): Boolean = transaction {
        val origRow = LEDPanelsConfiguration.selectAll().where {
            (LEDPanelsConfiguration.publicId eq sourcePublicConfigID) and
                    (LEDPanelsConfiguration.isPublic eq true)
        }.singleOrNull() ?: return@transaction false

        val origId = origRow[LEDPanelsConfiguration.id]
        val newId = insertSubscribedConfiguration(newConfigId, requesterId, origRow) ?: return@transaction false

        insertDuplicatedPanels(origId, newId)
        insertDuplicatedFrames(origId, newId)

        return@transaction true
    }

    private fun insertSubscribedConfiguration(
        newConfigId: UUID,
        requesterId: Int,
        origRow: ResultRow
    ) = LEDPanelsConfiguration.insert {
        it[publicId] = newConfigId
        it[ownerId] = requesterId
        it[name] = origRow[name]
        it[description] = origRow[description]
        it[sourceConfigurationId] = origRow[id]
        it[forkStatus] = ForkStatus.ADDED
        it[createdAt] = LocalDateTime.now()
        it[updatedAt] = LocalDateTime.now()
    }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)


    private fun insertPublishedConfiguration(
        publicConfigId: UUID,
        origRow: ResultRow,
        request: PublishRequest
    ) = LEDPanelsConfiguration.insert {
        it[publicId] = publicConfigId
        it[ownerId] = origRow[ownerId]
        it[name] = request.newName ?: origRow[name]
        it[description] = request.newDescription ?: origRow[description]
        it[isPublic] = true
        it[createdAt] = LocalDateTime.now()
        it[updatedAt] = LocalDateTime.now()
    }.resultedValues?.first()?.get(LEDPanelsConfiguration.id)

    private fun insertDuplicatedFrames(origId: Int, newId: Int) {
        val frameRows = Frame.selectAll().where { Frame.configurationId eq origId }
            .orderBy(Frame.index to SortOrder.ASC).toList()
        Frame.batchInsert(frameRows) { row ->
            this[Frame.configurationId] = newId
            this[Frame.index] = row[Frame.index]
            this[Frame.panelPixelColors] = row[Frame.panelPixelColors]
        }
    }

    private fun insertDuplicatedPanels(origId: Int, newId: Int) {
        val panelRows = Panel.selectAll().where {
            Panel.configurationId eq origId
        }.toList()
        Panel.batchInsert(panelRows) { row ->
            this[Panel.configurationId] = newId
            this[Panel.uid] = row[Panel.uid]
            this[Panel.x] = row[Panel.x]
            this[Panel.y] = row[Panel.y]
            this[Panel.direction] = row[Panel.direction]

        }
    }


    private fun getPanels(configId: Int): List<PanelData> =
        Panel.selectAll().where {
            Panel.configurationId eq configId
        }.map { r ->
            PanelData(
                uid = r[Panel.uid],
                x = r[Panel.x],
                y = r[Panel.y],
                direction = r[Panel.direction],
            )
        }

    private fun getFrames(configId: Int): List<FrameData> =
        Frame.selectAll().where {
            Frame.configurationId eq configId
        }.orderBy(Frame.index to SortOrder.ASC).map { r ->
            FrameData(
                index = r[Frame.index],
                panelPixelColors = r[Frame.panelPixelColors],
            )
        }
}
