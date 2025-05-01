package configurations.library.repositories

import app.entities.ForkStatus
import configurations.library.models.ConfigurationFullResponse
import configurations.library.models.*
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.update_request.UpdateConfigurationDataRequest
import configurations.library.models.update_request.UpdateConfigurationStructureRequest
import java.util.*


interface ConfigurationRepository {
    fun exists(publicId: UUID): Boolean

    fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
    ): Boolean

    fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        requestData: CreateConfigurationData
    ): Boolean

    fun updateConfigurationData(
        publicId: UUID,
        request: UpdateConfigurationDataRequest,
        requesterId: Int
    ): Boolean

    fun updateConfigurationStructure(
        publicId: UUID,
        request: UpdateConfigurationStructureRequest,
        requesterId: Int
    ): Boolean

    fun deleteConfiguration(publicId: UUID, requesterId: Int): Boolean

    fun getFullConfiguration(publicId: UUID, requesterId: Int): ConfigurationFullResponse?

    fun updateFrame(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String,
        requesterId: Int
    ): Boolean

    fun getConfigurationsByOwner(
        ownerId: Int,
        offset: Long = 0,
        size: Int = 20,
        forkStatus: ForkStatus? = null,
        isPublic: Boolean? = null,
    ): List<ConfigurationSummaryResponse>

}




