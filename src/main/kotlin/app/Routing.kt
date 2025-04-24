package com.flexypixelgalleryapi.routes

import auth.authRoutes
import configurations.library.usersConfigurationRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import users.userRoutes



fun Application.configureRouting() {

    routing {
        authRoutes()
        route("/configurations"){
            usersConfigurationRoutes()
        }
        userRoutes()

    }
}
