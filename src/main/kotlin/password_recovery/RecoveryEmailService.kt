package password_recovery

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Интерфейс сервиса отправки email
 */
interface EmailService {
    suspend fun sendPasswordResetEmail(to: String, link: String)
}

/**
 * Реализация EmailService через Resend API
 */
class ResendEmailService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val fromEmail: String
) : EmailService {
    override suspend fun sendPasswordResetEmail(to: String, link: String) {
        println("Authorization $apiKey")
        val request = ResendEmailRequest(
            from = fromEmail,
            to = to,
            subject = "Восстановление пароля",
            html = "<p>Для сброса пароля перейдите по ссылке:</p>" +
                    "<p><a href=\"$link\">$link</a></p>"
        )

        val r = httpClient.post("https://api.resend.com/emails") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        println("RESEND REQUEST STATUS ------->>>>>>> $r.status")
    }
}

@Serializable
private data class ResendEmailRequest(
    val from: String,
    val to: String,
    val subject: String,
    val html: String
)
