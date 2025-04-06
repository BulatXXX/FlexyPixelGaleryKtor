package com.flexypixelgalleryapi

import com.flexypixelgalleryapi.config.configureDatabases
import com.flexypixelgalleryapi.plugins.*
import com.flexypixelgalleryapi.routes.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureSecurity()
    configureMonitoring()
    configureDatabases()
    configureRouting()
    configureDi()
}
