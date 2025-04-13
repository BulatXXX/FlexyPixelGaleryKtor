package com.flexypixelgalleryapi.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        userRoutes()
        route("/configurations"){
            usersConfigurationRoutes()
        }

    }
}
