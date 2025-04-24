package users.models.update_request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRequest(
    val email:        String? = null,
    val username:     String? = null,
    val displayName:  String? = null,
    val phone:        String? = null,
    val avatarUrl:    String? = null,
    val bio:          String? = null
)
