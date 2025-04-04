package com.flexypixelgalleryapi

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
