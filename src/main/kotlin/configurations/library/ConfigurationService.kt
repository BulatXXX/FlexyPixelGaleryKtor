package configurations.library

import app.entities.ForkStatus
import configurations.library.models.*
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.create_request.CreateResult
import configurations.library.models.delete_request.DeleteResult
import configurations.library.models.get_request.GetResult
import configurations.library.models.update_request.UpdateConfigurationDataRequest
import configurations.library.models.update_request.UpdateConfigurationStructureRequest
import configurations.library.models.update_request.UpdateResult
import configurations.library.repositories.ConfigurationRepository
import configurations.util.SvgPreviewGenerator
import java.io.File
import java.util.UUID

class ConfigurationService(
    private val configurationRepository: ConfigurationRepository,
    private val previewGenerator: SvgPreviewGenerator
) {

    fun createConfiguration(
        ownerId: Int,
        name: String,
        description: String,
    ): CreateResult = UUID.randomUUID().let { configurationPublicId ->
        val result = configurationRepository.createConfiguration(
            ownerId,
            configurationPublicId,
            name,
            description,
        )
        if (result) {
            CreateResult.Success(configurationPublicId)
        } else {
            CreateResult.DatabaseError
        }
    }

    fun createFullConfiguration(
        ownerId: Int,
        requestData: CreateConfigurationData
    ): CreateResult = UUID.randomUUID().let { configurationPublicId ->
        val result = configurationRepository.createFullConfiguration(
            ownerId,
            configurationPublicId,
            requestData
        )
        if (result) {
            val panels = requestData.panels
            val firstFrame = requestData.frames.firstOrNull()
            if (firstFrame != null) {
                val previewUrl = previewGenerator.generate(panels, firstFrame, configurationPublicId)
                val miniPreviewUrl = previewGenerator.generate(panels, firstFrame, configurationPublicId, "1")
                val finalResult = configurationRepository.updateConfigurationData(
                    configurationPublicId,
                    UpdateConfigurationDataRequest(previewUrl = previewUrl, miniPreviewUrl = miniPreviewUrl), ownerId
                )
            }
            CreateResult.Success(configurationPublicId)
        } else {
            CreateResult.DatabaseError
        }
    }

    fun updateFrame(publicId: UUID, frameIndex: Int, newFrameJson: String, requesterId: Int): UpdateResult = when {
        configurationRepository.updateFrame(publicId, frameIndex, newFrameJson, requesterId) ->
            UpdateResult.Success("Frame $frameIndex updated")

        configurationRepository.exists(publicId) ->
            UpdateResult.Forbidden

        else -> UpdateResult.NotFound
    }

    fun updateConfigurationStructure(
        publicId: UUID,
        request: UpdateConfigurationStructureRequest,
        requesterId: Int,
    ): UpdateResult = when {
        configurationRepository.updateConfigurationStructure(publicId, request, requesterId) -> {
            val panels = request.panels
            val firstFrame = request.frames.firstOrNull()
            firstFrame?.let {
                deleteOldPreviews(publicId)

                val previewUrl =
                    previewGenerator.generate(configurationId = publicId, panels = panels, frame = firstFrame)
                val miniPreviewUrl = previewGenerator.generate(
                    configurationId = publicId,
                    panels = panels,
                    frame = firstFrame,
                    miniPanelUID = "1"
                )

                val res = configurationRepository.updateConfigurationData(
                    publicId = publicId, UpdateConfigurationDataRequest(
                        previewUrl = previewUrl, miniPreviewUrl = miniPreviewUrl,
                    ), requesterId = requesterId
                )

            }

            UpdateResult.Success("Configuration structure (panels and frames) updated")
        }


        configurationRepository.exists(publicId) ->
            UpdateResult.Forbidden

        else ->
            UpdateResult.NotFound
    }

    fun updateConfigurationData(
        publicId: UUID,
        request: UpdateConfigurationDataRequest,
        requesterId: Int
    ): UpdateResult = when {
        configurationRepository.updateConfigurationData(publicId, request, requesterId) ->
            UpdateResult.Success("Configuration data updated")

        configurationRepository.exists(publicId) ->
            UpdateResult.Forbidden

        else ->
            UpdateResult.NotFound
    }

    fun deleteConfiguration(publicId: UUID, requesterId: Int): DeleteResult {
        deleteOldPreviews(publicId)
        return when {
            configurationRepository.deleteConfiguration(publicId, requesterId) ->
                DeleteResult.Success("Deleted configuration")

            configurationRepository.exists(publicId) ->
                DeleteResult.Forbidden

            else ->
                DeleteResult.NotFound
        }
    }

    private fun deleteOldPreviews(publicId: UUID) {
        val oldMeta = configurationRepository.getConfigInfo(publicId)

        oldMeta?.previewImageUrl?.let {
            deletePreviewFile(it)
        }
        oldMeta?.miniPreviewImageUrl?.let {
            deletePreviewFile(it)
        }
    }

    fun getFullConfiguration(publicId: UUID, requesterId: Int): GetResult {
        val res = configurationRepository.getFullConfiguration(publicId, requesterId)

        return when {
            res != null -> GetResult.Success(res)

            configurationRepository.exists(publicId) ->
                GetResult.Forbidden

            else ->
                GetResult.NotFound
        }
    }

    fun getConfigurationSummary(
        ownerId: Int,
        offset: Long = 0,
        size: Int = 20,
        forkStatus: ForkStatus? = null,
        isPublic: Boolean? = null,
    ): List<ConfigurationSummaryResponse> {
        val summary = configurationRepository.getConfigurationsByOwner(ownerId, offset, size, forkStatus, isPublic)
        return summary
    }

    private fun deletePreviewFile(url: String) {
        val fileName = url.substringAfterLast("/")
        val fullFile = File(previewGenerator.outputDir, fileName)
        val miniFile = File(previewGenerator.outputDir, fileName) // если у вас разные папки, корректируйте путь
        if (fullFile.exists()) fullFile.delete()
        if (miniFile.exists()) miniFile.delete()
    }
}
