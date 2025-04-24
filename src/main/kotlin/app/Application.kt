package com.flexypixelgalleryapi.app.app

import app.plugins.configureDi
import app.plugins.configureSecurity
import com.flexypixelgalleryapi.app.config.configureDatabases
import com.flexypixelgalleryapi.plugins.*
import com.flexypixelgalleryapi.routes.configureRouting
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureCompression()
    configureSerialization()
    configureSecurity()
    configureMonitoring()
    configureDatabases()
    configureRouting()
    configureDi()
}
