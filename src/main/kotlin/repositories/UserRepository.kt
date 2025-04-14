package com.flexypixelgalleryapi.repositories

import com.flexypixelgalleryapi.entities.UserRole
import com.flexypixelgalleryapi.models.auth.RegisterRequest
import com.flexypixelgalleryapi.models.auth.LoginCredentials
import com.flexypixelgalleryapi.models.UserResponse
import java.util.UUID

interface UserRepository {
    fun createUser(
        publicId: UUID,
        request: RegisterRequest,
        hashedPassword: String,
        role: UserRole = UserRole.USER
    )
    fun exist(email: String, login: String): Boolean
    fun findByPublicId(publicId: UUID): UserResponse?
    fun findByLoginOrEmail(loginOrEmail: String): LoginCredentials?
    fun getUserIdByPublicId(publicId: UUID): Int?

}


