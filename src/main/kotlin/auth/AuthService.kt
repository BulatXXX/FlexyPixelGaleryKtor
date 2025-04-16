package auth

import at.favre.lib.crypto.bcrypt.BCrypt
import auth.models.*
import com.flexypixelgalleryapi.app.config.JwtClaims
import com.flexypixelgalleryapi.app.config.JwtConfig
import com.flexypixelgalleryapi.repositories.UserRepository
import java.util.*

class AuthService(private val userRepository: UserRepository) {

    fun register(request: RegisterRequest): RegisterResponse {
        val exists = userRepository.exist(request.email, request.login)
        if (exists) throw IllegalArgumentException("User already exists")

        val hashed = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
        val publicId = UUID.randomUUID()

        userRepository.createUser(publicId, request, hashed)

        return RegisterResponse(publicId)

    }


    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByLoginOrEmail(request.loginOrEmail)
            ?: throw IllegalArgumentException("User not found")

        val isPasswordCorrect = BCrypt.verifyer()
            .verify(request.password.toCharArray(), user.passwordHash)
            .verified

        if (!isPasswordCorrect) throw IllegalArgumentException("Incorrect password")

        val token = JwtConfig.generateAccessToken(user.id, user.publicId)
        val refreshToken = JwtConfig.generateRefreshToken(user.id, user.publicId)


        return LoginResponse(
            accessToken = token,
            refreshToken = refreshToken,
            publicId = user.publicId
        )
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
