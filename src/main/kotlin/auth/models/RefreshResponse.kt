package auth.models

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
