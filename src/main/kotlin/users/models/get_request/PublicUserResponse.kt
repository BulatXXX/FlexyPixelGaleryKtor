package users.models.get_request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PublicUserResponse(
    @Contextual val publicId: UUID,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val bio: String?,
    @Contextual val createdAt: LocalDateTime,
)
