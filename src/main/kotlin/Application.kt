package com.flexypixelgalleryapi

import com.flexypixelgalleryapi.config.configureDatabases
import com.flexypixelgalleryapi.plugins.configureHTTP
import com.flexypixelgalleryapi.plugins.configureMonitoring
import com.flexypixelgalleryapi.plugins.configureSecurity
import com.flexypixelgalleryapi.plugins.configureSerialization
import com.flexypixelgalleryapi.routes.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureMonitoring()
    configureDatabases()
    configureHTTP()
    configureRouting()
}
