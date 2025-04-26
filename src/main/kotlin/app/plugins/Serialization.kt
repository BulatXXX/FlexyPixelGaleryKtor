package app.plugins

import app.utils.LocalDateTimeSerializer
import app.utils.UUIDSerializer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import java.util.UUID

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json{
                encodeDefaults = true
                ignoreUnknownKeys = true
                prettyPrint = false
                serializersModule = SerializersModule {
                    contextual(UUID::class, UUIDSerializer as KSerializer<UUID>)
                    contextual(LocalDateTime::class, LocalDateTimeSerializer as KSerializer<LocalDateTime>)
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
