package com.flexypixelgalleryapi.routes

import com.flexypixelgalleryapi.models.RegisterRequest
import com.flexypixelgalleryapi.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    post("/register") {
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
}
