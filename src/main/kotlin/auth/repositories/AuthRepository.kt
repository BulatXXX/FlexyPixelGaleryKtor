package auth.repositories

import auth.models.login_request.LoginCredentials
import auth.models.register_request.RegisterRequest
import app.entities.UserRole
import java.util.*

interface AuthRepository {
    fun exists(email:String,login:String): Boolean
    fun registerUser(
        publicId: UUID,
        request: RegisterRequest,
        hashedPassword: String,
        role: UserRole = UserRole.USER
    ):Boolean
    fun findByLoginOrEmail(loginOrEmail: String): LoginCredentials?
}