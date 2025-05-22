package configurations.gallery.repositories


import configurations.gallery.models.PreviewUrls
import configurations.gallery.models.publish_request.ConfigurationInfo
import configurations.gallery.models.publish_request.PublishRequest
import java.util.*

interface GalleryRepository {
    fun getConfigurationPublishingInfo(configId: UUID, requesterId: Int): ConfigurationInfo?
    fun exists(publicConfigId: UUID): Boolean
    fun publish(
        oldConfigID: UUID,
        publicConfigId: UUID,
        request: PublishRequest
    ): Boolean
    fun subscribeConfiguration(
        sourcePublicConfigID: UUID,
        newConfigId: UUID,
        requesterId: Int,
    ):Boolean
    fun updatePreviewUrls(
        publicId: UUID,
        fullPreviewUrl: String,
        miniPreviewUrl: String
    ): Boolean

    fun getPreviewUrl(publicConfigID: UUID): PreviewUrls?
    fun banConfiguration(publicId: UUID): Boolean
}