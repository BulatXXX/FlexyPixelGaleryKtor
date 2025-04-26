package configurations.gallery

import configurations.gallery.models.publish_request.PublishRequest
import configurations.gallery.models.publish_request.PublishResponse
import configurations.gallery.models.publish_request.PublishResult
import configurations.gallery.models.search_request.GalleryCardResponse
import configurations.gallery.models.search_request.SearchFilters
import configurations.gallery.models.subscribe_request.SubscribeResult
import configurations.gallery.repositories.GalleryRepository
import configurations.gallery.repositories.SearchRepository
import java.util.*


class GalleryService(
    private val galleryRepository: GalleryRepository,
    private val searchRepository: SearchRepository
) {
    fun publishConfiguration(request: PublishRequest, requesterId: Int, configId: UUID): PublishResult {
        val configurationData = galleryRepository.getConfigurationPublishingInfo(configId, requesterId)
            ?: return if (galleryRepository.exists(configId)) PublishResult.Forbidden
            else PublishResult.NotFound
        if (configurationData.isPublic) return PublishResult.AlreadyPublished
        val newPublicId = UUID.randomUUID()
        return if (galleryRepository.publish(configId, newPublicId, request)) PublishResult.Success(
            PublishResponse(
                newPublicId
            )
        ) else {
            PublishResult.DatabaseError
        }
    }

    fun subscribeConfiguration(requesterId: Int, configId: UUID): SubscribeResult {
        val newPublicId = UUID.randomUUID()
        return if (galleryRepository.subscribeConfiguration(
                configId,
                newPublicId,
                requesterId
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