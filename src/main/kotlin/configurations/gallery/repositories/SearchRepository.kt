package configurations.gallery.repositories

import configurations.gallery.models.search_request.GalleryCardResponse
import configurations.gallery.models.search_request.SearchFilters

interface SearchRepository {
    fun searchGallery(filters: SearchFilters): List<GalleryCardResponse>
}