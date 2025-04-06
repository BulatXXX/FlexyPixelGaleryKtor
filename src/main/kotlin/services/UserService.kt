package com.flexypixelgalleryapi.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.flexypixelgalleryapi.config.JwtConfig
import com.flexypixelgalleryapi.models.*
import com.flexypixelgalleryapi.repositories.UserRepository
import java.util.*

class UserService(private val userRepository: UserRepository) {

    fun register(request: RegisterRequest): RegisterResponse {
        val exists = userRepository.exist(request.email, request.login)
        if (exists) throw IllegalArgumentException("User already exists")

        val hashed = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
        val publicId = UUID.randomUUID()

        userRepository.createUser(publicId, request, hashed)

        return RegisterResponse(publicId)

    }

    fun getByPublicId(id: UUID): UserResponse? {
        val user = userRepository.findByPublicId(id) ?: return null
        return user
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByLoginOrEmail(request.loginOrEmail)
            ?: throw IllegalArgumentException("User not found")

        val isPasswordCorrect = BCrypt.verifyer()
            .verify(request.password.toCharArray(), user.passwordHash)
            .verified

        if (!isPasswordCorrect) throw IllegalArgumentException("Incorrect password")

        val token = JwtConfig.generateToken(user.publicId)

        return LoginResponse(
            token = token,
            publicId = user.publicId
        )
    }


}
