package configurations.gallery

import configurations.gallery.models.publish_request.PublishRequest
import configurations.gallery.models.publish_request.PublishResponse
import configurations.gallery.models.publish_request.PublishResult
import configurations.gallery.models.search_request.GalleryCardResponse
import configurations.gallery.models.search_request.SearchFilters
import configurations.gallery.models.subscribe_request.SubscribeResult
import configurations.gallery.repositories.GalleryRepository
import configurations.gallery.repositories.SearchRepository
import configurations.util.SvgPreviewGenerator
import java.util.*


class GalleryService(
    private val galleryRepository: GalleryRepository,
    private val searchRepository: SearchRepository,
    private val previewGenerator: SvgPreviewGenerator
) {
    fun publishConfiguration(request: PublishRequest, requesterId: Int, configId: UUID): PublishResult {
        val configurationData = galleryRepository.getConfigurationPublishingInfo(configId, requesterId)
            ?: return if (galleryRepository.exists(configId)) PublishResult.Forbidden
            else PublishResult.NotFound
        if (configurationData.isPublic) return PublishResult.AlreadyPublished
        val newPublicId = UUID.randomUUID()
        val previewUrls = galleryRepository.getPreviewUrl(configId)

        val (newFullUrl, newMiniUrl) = if (previewUrls != null) {
            previewGenerator.duplicate(previewUrls, newPublicId)
        }else{
            "" to "" //TODO generateNew
        }
        return if (galleryRepository.publish(configId, newPublicId, request) && galleryRepository.updatePreviewUrls(
                publicId = newPublicId,
                fullPreviewUrl = newFullUrl,
                miniPreviewUrl = newMiniUrl
            )
        )
            PublishResult.Success(
                PublishResponse(
                    newPublicId
                )
            ) else {
            PublishResult.DatabaseError
        }
    }

    fun subscribeConfiguration(requesterId: Int, configId: UUID): SubscribeResult {
        val newPublicId = UUID.randomUUID()
        val previewUrls = galleryRepository.getPreviewUrl(configId)
        val (newFullUrl, newMiniUrl) = if (previewUrls != null) {
            previewGenerator.duplicate(previewUrls, newPublicId)
        }else{
            "" to "" //TODO generateNew
        }
        return if (galleryRepository.subscribeConfiguration(
                configId,
                newPublicId,
                requesterId
            ) && galleryRepository.updatePreviewUrls(
                publicId = newPublicId,
                fullPreviewUrl = newFullUrl,
                miniPreviewUrl = newMiniUrl
            )
        ) SubscribeResult.Success(newPublicId)
        else {
            if (galleryRepository.exists(configId)) SubscribeResult.IsNotPublic
            else SubscribeResult.NotFound
        }
    }

    fun searchGallery(filters: SearchFilters): List<GalleryCardResponse> {
        return searchRepository.searchGallery(filters)
    }
}