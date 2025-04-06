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
    ): Unit

    fun exist(email: String, login: String): Boolean
    fun findByPublicId(publicId: UUID): UserResponse?
    fun findByLoginOrEmail(loginOrEmail: String): LoginCredentials?


}

class UserRepositoryImpl : UserRepository {

    override fun createUser(
        publicId: UUID,
        request: RegisterRequest,
        hashedPassword: String,
        role: UserRole
    ) {
        transaction {
            User.insert {
                it[User.publicId] = publicId
                it[User.email] = request.email
                it[User.login] = request.login
                it[User.username] = generateUsername(request.displayName)
                it[User.displayName] = request.displayName
                it[User.passwordHash] = hashedPassword
                it[User.phone] = request.phone
                it[User.role] = role
                it[User.mobileRole] = MobileRole.USER
                it[User.isVerified] = false
                it[User.createdAt] = LocalDateTime.now()
                it[User.updatedAt] = LocalDateTime.now()
            }
        }
    }

    override fun exist(email: String, login: String): Boolean {
        return transaction {
            User.selectAll().where {
                (User.email eq email) or (User.login eq login)
            }.count() > 0
        }
    }

    override fun findByLoginOrEmail(loginOrEmail: String): LoginCredentials? {
        return transaction {
            User.selectAll().where {
                (User.email eq loginOrEmail) or (User.login eq loginOrEmail)
            }.map {
                LoginCredentials(
                    publicId = it[User.publicId],
                    passwordHash = it[User.passwordHash]
                )
            }.singleOrNull()
        }
    }


    override fun findByPublicId(publicId: UUID): UserResponse? {
        return transaction {
            User.selectAll().where { User.publicId eq publicId }
                .map {
                    UserResponse(
                        publicId = it[User.publicId],
                        email = it[User.email],
                        username = it[User.username],
                        displayName = it[User.displayName],
                        phone = it[User.phone],
                        avatarUrl = it[User.avatarUrl],
                        bio = it[User.bio],
                        isVerified = it[User.isVerified],
                        role = it[User.role],
                        mobileRole = it[User.mobileRole],
                        createdAt = it[User.createdAt]
                    )
                }
                .singleOrNull()
        }
    }


    private fun generateUsername(displayName: String): String {
        val base = displayName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(20)

        var username = base
        var suffix = 1

        transaction {
            while (!User.selectAll().where { User.username eq username }.empty()) {
                username = "$base$suffix"
                suffix++
            }
        }

        return username
    }
}
