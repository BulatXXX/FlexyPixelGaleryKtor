package com.flexypixelgalleryapi.routes

import com.flexypixelgalleryapi.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.server.response.*
import java.util.*


fun Route.userRoutes() {
    val userService: UserService by inject<UserService>()
    authenticate("auth-jwt") {
        route("/users") {
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