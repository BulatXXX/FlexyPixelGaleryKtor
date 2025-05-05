package password_recovery

import app.entities.PasswordRecoveryTokens
import app.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class PasswordResetRepository() {
    fun create(userId: Int, tokenHash: String, expiresAt: LocalDateTime)= transaction {
        PasswordRecoveryTokens.insert {
            it[PasswordRecoveryTokens.userId] = userId
            it[PasswordRecoveryTokens.tokenHash] = tokenHash
            it[PasswordRecoveryTokens.expiresAt] = expiresAt
            it[PasswordRecoveryTokens.createdAt] = LocalDateTime.now()
        }.insertedCount>0
    }

    fun updatePassword(userId: Int, password: String) = transaction {
        User.update({ User.id eq userId }) {
            it[passwordHash] = password
        }>0
    }

    fun findValidByHash(hash: String): TokenInfo? = transaction {
        val now = LocalDateTime.now()
        PasswordRecoveryTokens
            .selectAll().where {
                (PasswordRecoveryTokens.tokenHash eq hash) and
                        (PasswordRecoveryTokens.usedAt.isNull()) and
                        (PasswordRecoveryTokens.expiresAt greaterEq now)
            }
            .singleOrNull()
            ?.let { row ->
                TokenInfo(
                    userId = row[PasswordRecoveryTokens.userId],
                    id     = row[PasswordRecoveryTokens.id]
                )
            }
    }

    fun markUsed(id: Int): Boolean = transaction {
        PasswordRecoveryTokens.update({ PasswordRecoveryTokens.id eq id }) {
            it[PasswordRecoveryTokens.usedAt] = LocalDateTime.now()
        } > 0
    }
}

data class TokenInfo(
    val userId: Int,
    val id: Int,
)