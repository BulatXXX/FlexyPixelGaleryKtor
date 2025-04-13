package com.flexypixelgalleryapi.repositories

import com.flexypixelgalleryapi.entities.MobileRole
import com.flexypixelgalleryapi.entities.UserRole
import com.flexypixelgalleryapi.models.RegisterRequest
import com.flexypixelgalleryapi.entities.User
import com.flexypixelgalleryapi.models.LoginCredentials
import com.flexypixelgalleryapi.models.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
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


