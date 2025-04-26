package configurations.gallery.models.publish_request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PublishResponse (
    @Contextual val publicId: UUID
)