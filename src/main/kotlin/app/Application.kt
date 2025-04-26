package app

import app.config.configureDatabases
import app.plugins.*
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
