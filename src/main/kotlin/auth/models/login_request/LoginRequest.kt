package auth.models.login_request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val loginOrEmail: String,
    val password: String
)
