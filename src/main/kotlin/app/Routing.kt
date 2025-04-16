package com.flexypixelgalleryapi.routes

import com.flexypixelgalleryapi.configurations.userLibrary.usersConfigurationRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        authRoutes()
        route("/configurations"){
            usersConfigurationRoutes()
        }
        userRoutes()

    }
}
