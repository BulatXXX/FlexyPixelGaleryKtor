package configurations.gallery.models.search_request


import configurations.common.models.AuthorInfo
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class GalleryCardResponse(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String?,
    val author: AuthorInfo,
    val tags: List<String>,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val publishedAt: LocalDateTime,
    val averageRating: Double,
    val addedCount: Int
)
