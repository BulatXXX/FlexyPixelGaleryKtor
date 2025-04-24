package auth

import at.favre.lib.crypto.bcrypt.BCrypt
import app.config.JwtClaims
import app.config.JwtConfig
import auth.models.login_request.LoginRequest
import auth.models.login_request.LoginResponse
import auth.models.login_request.LoginResult
import auth.models.refresh_request.RefreshResponse
import auth.models.register_request.RegisterRequest
import auth.models.register_request.RegisterResult
import auth.repositories.AuthRepository
import java.util.*

class AuthService(private val authRepository: AuthRepository) {

    fun register(request: RegisterRequest): RegisterResult = when {
        authRepository.exists(request.email, request.login) -> RegisterResult.Conflict
        else -> {
            val hashed = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
            val publicId = UUID.randomUUID()

            val result = authRepository.registerUser(publicId, request, hashed)
            if (!result) RegisterResult.DatabaseError

            RegisterResult.Success(publicId)
        }

    }

    fun login(request: LoginRequest): LoginResult {
        val user = authRepository.findByLoginOrEmail(request.loginOrEmail)
            ?: return LoginResult.NotFound

        val isPasswordCorrect = BCrypt.verifyer()
            .verify(request.password.toCharArray(), user.passwordHash)
            .verified

        if (!isPasswordCorrect) return LoginResult.IncorrectPassword

        val token = JwtConfig.generateAccessToken(user.id, user.publicId)
        val refreshToken = JwtConfig.generateRefreshToken(user.id, user.publicId)

        val result = LoginResponse(
            accessToken = token,
            refreshToken = refreshToken,
            publicId = user.publicId
        )

        return LoginResult.Success(result)
    }

    fun refreshToken(refreshToken: String): RefreshResponse? {
        return try {
            val decodedJWT = JwtConfig.getVerifier().verify(refreshToken)
            val publicIdStr = decodedJWT.getClaim(JwtClaims.PUBLIC_ID).asString()
            val userId = decodedJWT.getClaim(JwtClaims.USER_ID).asInt()
            if (publicIdStr.isNullOrBlank()) null
            else {
                val publicId = UUID.fromString(publicIdStr)
                val newAccessToken = JwtConfig.generateAccessToken(userId, publicId)
                val newRefreshToken = JwtConfig.generateRefreshToken(userId, publicId)
                RefreshResponse(newAccessToken, newRefreshToken)
            }
        } catch (ex: Exception) {
            null
        }
    }
}
