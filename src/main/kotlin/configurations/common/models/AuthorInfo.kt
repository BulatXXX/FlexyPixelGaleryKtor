package configurations.common.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AuthorInfo(
    @Contextual val publicId: UUID,
    val username: String,
    val avatarUrl: String? = null,
    val displayName: String
)
