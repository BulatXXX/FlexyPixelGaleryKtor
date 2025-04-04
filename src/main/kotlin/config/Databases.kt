package com.flexypixelgalleryapi.config


import at.favre.lib.crypto.bcrypt.BCrypt
import com.flexypixelgalleryapi.entities.User
import com.flexypixelgalleryapi.entities.UserRole
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/flexydb",
        driver = "org.postgresql.Driver",
        user = "flexyuser",
        password = "flexapipass2025"
    )
    transaction {
        SchemaUtils.create(User)

        if(User.selectAll().empty()){
            val hashedPassword = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray())
            User.insert {
                it[publicId] = UUID.randomUUID()
                it[email] = "admin@example.com"
                it[login] = "admin"
                it[username] = "admin"
                it[displayName] = "Admin"
                it[passwordHash] = hashedPassword
                it[role] = UserRole.ADMIN
                it[isVerified] = true
                it[createdAt] = LocalDateTime.now()
                it[updatedAt] = LocalDateTime.now()
            }
            println("✅ Тестовый пользователь 'admin' добавлен")
        }
    }
}
