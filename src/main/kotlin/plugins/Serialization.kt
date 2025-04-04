package com.flexypixelgalleryapi.plugins

import com.flexypixelgalleryapi.utils.UUIDSerializer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.UUID

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json{
                encodeDefaults = true
                ignoreUnknownKeys = true
                prettyPrint = false
                serializersModule = SerializersModule {
                    contextual(UUID::class,UUIDSerializer as KSerializer<UUID>)
                }
            }
        )
    }
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
