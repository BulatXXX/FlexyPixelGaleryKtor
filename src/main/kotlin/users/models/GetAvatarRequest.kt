package users.models

import kotlinx.serialization.Serializable

@Serializable
data class GetAvatarRequest(
    val loginOrEmail: String,
)
