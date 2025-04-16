package auth.models

import java.util.*

data class LoginCredentials(
    val id:Int,
    val publicId: UUID,
    val passwordHash: String
)
