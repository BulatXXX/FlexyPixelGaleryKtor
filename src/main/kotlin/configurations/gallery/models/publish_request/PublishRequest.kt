package configurations.gallery.models.publish_request
import kotlinx.serialization.Serializable

@Serializable
data class PublishRequest(
    val newName: String? = null,
    val newDescription: String? = null,
    val tagIds: List<Int> = emptyList()
)
