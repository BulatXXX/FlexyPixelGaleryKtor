package com.flexypixelgalleryapi.routes

import auth.models.LoginRequest
import auth.models.RegisterRequest
import auth.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {

    val authService: AuthService by inject<AuthService>()
    route("/auth") {
        post("register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = authService.register(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
            }
        }
        post("login") {
            val request = call.receive<LoginRequest>()
            try {
                val response = authService.login(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            }
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
