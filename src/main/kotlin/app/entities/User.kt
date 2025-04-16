package com.flexypixelgalleryapi.app.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime


enum class UserRole { USER, ADMIN }
enum class MobileRole {USER, DEVELOPER}

object User : Table("users") {
    val id = integer("id").autoIncrement()
    val publicId = uuid("public_id").uniqueIndex()

    val email = varchar("email", 255).uniqueIndex()
    val login = varchar("login", 50).uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val displayName = varchar("display_name", 255)
    val passwordHash = varchar("password_hash", 255)

    val phone = varchar("phone", 20).nullable()
    val avatarUrl = varchar("avatar_url", 512).nullable()
    val bio = text("bio").nullable()

    val isVerified = bool("is_verified").default(false)
    val role = enumerationByName("role", 10, UserRole::class).default(UserRole.USER)
    val mobileRole = enumerationByName("mobile_role", 10, MobileRole::class).default(MobileRole.USER)
    val isBanned = bool("is_banned").default(false)
    val isDeleted = bool("is_deleted").default(false)

    val lastLoginAt = datetime("last_login_at").nullable()

    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}
