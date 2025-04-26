package auth

import auth.models.login_request.LoginRequest
import auth.models.login_request.LoginResult
import auth.models.register_request.RegisterRequest
import auth.models.register_request.RegisterResponse
import auth.models.register_request.RegisterResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

suspend fun ApplicationCall.respondRegister(result: RegisterResult) = when (result) {
    is RegisterResult.Success -> {
        respond(HttpStatusCode.Created, RegisterResponse(result.publicId))
    }

    is RegisterResult.ValidationError -> {
        respond(HttpStatusCode.BadRequest, mapOf("error" to result.error))
    }

    is RegisterResult.Conflict -> {
        respond(HttpStatusCode.Conflict, mapOf("error" to "User already exists"))
    }

    is RegisterResult.DatabaseError -> {
        respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error"))
    }
}

suspend fun ApplicationCall.respondLogin(result: LoginResult) = when (result) {
    is LoginResult.Success -> {
        respond(HttpStatusCode.OK, result.loginResponse)
    }

    is LoginResult.NotFound -> {
        respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
    }

    is LoginResult.IncorrectPassword -> {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect password"))
    }
}

fun Route.authRoutes() {

    val authService: AuthService by inject<AuthService>()
    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(request)
            call.respondRegister(result)
        }
        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.login(request)
            call.respondLogin(result)
        }


        post("/refresh") {
            val request = call.receive<Map<String, String>>()
            val refreshToken = request["refreshToken"]
            if (refreshToken == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing refreshToken")
                return@post
            }
            val response = authService.refreshToken(refreshToken) ?: call.respond(
                HttpStatusCode.BadRequest,
                "Invalid refresh token"
            )
            call.respond(HttpStatusCode.OK, response)

        }


    }


}
