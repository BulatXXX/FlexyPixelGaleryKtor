package auth.models.login_request

import app.entities.UserRole
import java.util.*

data class LoginCredentials(
    val id:Int,
    val publicId: UUID,
    val passwordHash: String,
    val role: UserRole
)
