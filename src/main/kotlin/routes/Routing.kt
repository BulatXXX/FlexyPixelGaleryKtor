package com.flexypixelgalleryapi.routes

import com.flexypixelgalleryapi.services.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        val userService = UserService()
        userRoutes(userService)
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
