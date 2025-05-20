package configurations.newlibrary

import configurations.util.SvgPreviewGenerator
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.create_request.CreateResult
import java.util.*

class LibraryConfigurationService(
    private val libraryConfigurationRepository: LibraryConfigurationRepository,
    private val previewGenerator: SvgPreviewGenerator
) {
    fun createConfigurationData(
        ownerId: Int,
        request: CreateConfigurationData
    ): CreateResult = UUID.randomUUID().let { publicId ->
        val created = libraryConfigurationRepository.createConfiguration(
            ownerId = ownerId,
            publicId = publicId,
            request = request
        )
        if (created) CreateResult.Success(publicId)
        else CreateResult.DatabaseError
    }

    fun createConfigurationFull() {}
    fun getConfigurationData() {}
    fun getConfigurationFull() {}
    fun updateConfigurationData() {}
    fun updateConfigurationFull() {}
    fun updateConfigurationFrame() {}
    fun deleteConfiguration() {}
    fun getAllConfigurations() {}


    fun banConfiguration() {}

    private fun isOwner() {}
}
