package com.flexypixelgalleryapi.repositories

import com.flexypixelgalleryapi.app.entities.UserRole
import auth.models.RegisterRequest
import auth.models.LoginCredentials
import users.UserResponse
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


