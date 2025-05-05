package app.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object PasswordRecoveryTokens : Table(name = "password_recovery_tokens") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(
        User.id,
        onDelete = ReferenceOption.CASCADE
    )            // ссылка на таблицу пользователей
    val tokenHash = varchar("token_hash", length = 128).uniqueIndex()
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id, name = "pk_password_recovery_tokens")
}
