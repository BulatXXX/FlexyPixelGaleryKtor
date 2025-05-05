package password_recovery

import at.favre.lib.crypto.bcrypt.BCrypt

import java.util.UUID
import java.security.MessageDigest

import users.repositories.UserRepository
import java.time.LocalDateTime

/**
 * Сервис для восстановления пароля
 */
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetRepository: PasswordResetRepository,
    private val emailService: EmailService,
    private val resetLinkBaseUrl: String,
    private val tokenExpiryMinutes: Long = 15
) {

    /**
     * Шаг 1: запрос сброса пароля
     */
    suspend fun requestReset(email: String) {
        val userId = userRepository.getUserId(email) ?: return
        val rawToken = UUID.randomUUID().toString()
        val tokenHash = hashToken(rawToken)
        val expiresAt = LocalDateTime.now().plusMinutes(tokenExpiryMinutes)

        passwordResetRepository.create(
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt
        )

        val link = "$resetLinkBaseUrl?token=$rawToken"
        emailService.sendPasswordResetEmail(email, link)
    }

    /**
     * Шаг 2: подтверждение и смена пароля
     */
    fun confirmReset(token: String, newPassword: String): Boolean {
        val tokenHash = hashToken(token)
        val resetToken = passwordResetRepository.findValidByHash(tokenHash) ?: return false

        // <-- здесь поправлено:
        val hashedPassword = BCrypt.withDefaults()
            .hashToString(12, newPassword.toCharArray())
        passwordResetRepository.updatePassword(resetToken.userId, hashedPassword)
        passwordResetRepository.markUsed(resetToken.id)

        return true
    }

    /**
     * Отдельный метод для SHA-256 хеширования токена
     */
    private fun hashToken(token: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(token.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
