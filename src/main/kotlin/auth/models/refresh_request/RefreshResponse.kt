package auth.models.refresh_request

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
