package com.flexypixelgalleryapi.routes

import com.flexypixelgalleryapi.models.LoginRequest
import com.flexypixelgalleryapi.models.RegisterRequest
import com.flexypixelgalleryapi.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {

    val userService: UserService by inject<UserService>()
    route("/users") {
        post("register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = userService.register(request)
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
                val response = userService.login(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to e.message))
            }
        }


        authenticate("auth-jwt") {
            get("me") {
                val principal = call.principal<JWTPrincipal>()
                val pubicId = UUID.fromString(principal!!.payload.getClaim("publicId").asString())

                val user = userService.getByPublicId(pubicId)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "No user found"))
                }
            }
        }
    }



}
