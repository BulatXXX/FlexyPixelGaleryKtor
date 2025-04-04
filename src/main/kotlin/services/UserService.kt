package com.flexypixelgalleryapi.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.flexypixelgalleryapi.entities.User
import com.flexypixelgalleryapi.entities.UserRole
import com.flexypixelgalleryapi.models.RegisterRequest
import com.flexypixelgalleryapi.models.RegisterResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class UserService {

    fun register(request: RegisterRequest): RegisterResponse {
        // Генерируем username
        print("reg1")
        val username = generateUniqueUsername(request.displayName)

        // Проверка по email и login
        val existing = transaction {
            User.selectAll().where(
                (User.email eq request.email) or (User.login eq request.login)
            ).count()
        }
        print("reg2")
        if (existing > 0) throw IllegalArgumentException("User with same email or login already exists")

        val hashed = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
        val publicId = UUID.randomUUID()
        print("reg3")
        transaction {
            User.insert {
                it[User.publicId] = publicId
                it[User.email] = request.email
                it[User.login] = request.login
                it[User.username] = username
                it[User.displayName] = request.displayName
                it[User.passwordHash] = hashed
                it[User.phone] = request.phone
                it[User.role] = UserRole.USER
                it[User.isVerified] = false
                it[User.createdAt] = LocalDateTime.now()
                it[User.updatedAt] = LocalDateTime.now()
            }
        }
        print("reg4")

        return RegisterResponse(publicId = publicId)
    }

    private fun generateUniqueUsername(displayName: String): String {
        print("gen0")
        val base = displayName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(20)
        print("gen1")
        var username = base
        var suffix = 1
        print("gen2")
        transaction {
            while (!User.selectAll().where(User.username eq username).empty()) {
                username = "$base$suffix"
                suffix++
            }
        }
        print("gen3")
        return username
    }
}
