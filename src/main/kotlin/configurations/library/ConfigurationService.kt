package configurations.library

import configurations.library.models.*
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.create_request.CreateResult
import configurations.library.models.delete_request.DeleteResult
import configurations.library.models.get_request.GetResult
import configurations.library.models.update_request.UpdateConfigurationDataRequest
import configurations.library.models.update_request.UpdateConfigurationStructureRequest
import configurations.library.models.update_request.UpdateResult
import configurations.library.repositories.ConfigurationRepository
import java.util.UUID

class ConfigurationService(private val configurationRepository: ConfigurationRepository) {

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

    fun updateFrame(publicId: UUID, frameIndex: Int, newFrameJson: String, requesterId: Int): UpdateResult = when {
        configurationRepository.updateFrame(publicId, frameIndex, newFrameJson, requesterId) ->
            UpdateResult.Success("Frame $frameIndex updated")

        configurationRepository.exists(publicId) ->
            UpdateResult.Forbidden

        else -> UpdateResult.NotFound
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
            CreateResult.Success(configurationPublicId)
        } else {
            CreateResult.DatabaseError
        }
    }

    fun updateConfigurationStructure(
        publicId: UUID,
        request: UpdateConfigurationStructureRequest,
        requesterId: Int,
    ): UpdateResult = when {
        configurationRepository.updateConfigurationStructure(publicId, request, requesterId) ->
            UpdateResult.Success("Configuration structure (panels and frames) updated")

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


    fun deleteConfiguration(publicId: UUID, requesterId: Int): DeleteResult = when {
        configurationRepository.deleteConfiguration(publicId, requesterId) ->
            DeleteResult.Success("Deleted configuration")

        configurationRepository.exists(publicId) ->
            DeleteResult.Forbidden

        else ->
            DeleteResult.NotFound
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

    fun getConfigurationSummary(ownerId: Int): List<ConfigurationSummaryResponse> {
        val summary = configurationRepository.getConfigurationsByOwner(ownerId)
        return summary
    }
}
