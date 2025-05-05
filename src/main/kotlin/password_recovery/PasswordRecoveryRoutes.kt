package password_recovery



import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

fun Route.passwordRecoveryRoutes() {
    val recoveryService:PasswordResetService by inject<PasswordResetService>()
    route("/password-recovery") {
        post("/request") {
            val request = call.receive<PasswordResetRequest>()
            recoveryService.requestReset(request.email)
            call.respond(HttpStatusCode.Accepted)
        }
        post("/confirm"){
            val req = call.receive<PasswordResetConfirmRequest>()
            val ok = recoveryService.confirmReset(req.token, req.newPassword)
            if (ok) call.respond(HttpStatusCode.OK)
            else   call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or expired token"))

        }
    }
}

@Serializable
private data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetConfirmRequest(
    val token: String,
    val newPassword: String
)