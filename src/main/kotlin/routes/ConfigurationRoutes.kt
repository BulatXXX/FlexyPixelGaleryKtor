package com.flexypixelgalleryapi.routes


import com.flexypixelgalleryapi.models.configuration.ConfigurationRequest
import com.flexypixelgalleryapi.models.configuration.ConfigurationUpdateRequest
import com.flexypixelgalleryapi.models.configuration.UpdateConfigurationStructureRequest
import com.flexypixelgalleryapi.services.ConfigurationService
import com.flexypixelgalleryapi.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.configurationRoutes() {
    val configurationService: ConfigurationService by inject<ConfigurationService>()
    val userService by inject<UserService>()
    authenticate("auth-jwt") {
        route("/configurations") {

            patch("/full/{publicId}/structure") {
                val publicIdParam = call.parameters["publicId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing publicId")
                val publicId = try {
                    UUID.fromString(publicIdParam)
                } catch (e: IllegalArgumentException) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }

                val request = call.receive<UpdateConfigurationStructureRequest>()

                val success = configurationService.updatePanelsAndFrames(publicId, request.panels, request.frames)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Structure updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Configuration not found")
                }
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                val ownerId = principal.payload.getClaim("ownerId").asInt()

                val request = call.receive<ConfigurationRequest>()

                val publicId = configurationService.createConfiguration(
                    ownerId = ownerId,
                    name = request.name,
                    description = request.description,
                    previewImageUrl = request.previewImageUrl
                )
                call.respond(HttpStatusCode.Created, mapOf("publicId" to publicId))
            }

            post("/full") {

                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                val ownerPublicId = UUID.fromString(principal.payload.getClaim("publicId").asString())
                val ownerId = userService.getUserIdByPublicId(ownerPublicId)?: return@post call.respond(HttpStatusCode.Forbidden, "Unauthorized")

                val request = call.receive<ConfigurationRequest>()
                // Panels и Frames находятся внутри запроса – типизированные данные
                val publicId = configurationService.createFullConfiguration(
                    ownerId = ownerId,
                    name = request.name,
                    description = request.description,
                    previewImageUrl = request.previewImageUrl,
                    panels = request.panels,
                    frames = request.frames
                )
                call.respond(HttpStatusCode.Created, mapOf("publicId" to publicId))
            }

            // 3. Получение полной конфигурации по publicId
            get("{publicId}") {
                val publicIdParam = call.parameters["publicId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing publicId"
                )
                try {
                    val publicId = UUID.fromString(publicIdParam)
                    val configuration = configurationService.getFullConfiguration(publicId)
                    if (configuration != null) {
                        call.respond(configuration)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Configuration not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }
            }

            // 4. Обновление конфигурации
            patch("{publicId}") {
                val publicIdParam = call.parameters["publicId"] ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, "Missing publicId"
                )
                try {
                    val publicId = UUID.fromString(publicIdParam)
                    val updateRequest = call.receive<ConfigurationUpdateRequest>()
                    val success = configurationService.updateConfiguration(
                        publicId = publicId,
                        name = updateRequest.name,
                        description = updateRequest.description,
                        previewImageUrl = updateRequest.previewImageUrl
                    )
                    if (success) {
                        call.respond(HttpStatusCode.OK, "Configuration updated")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Configuration not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }
            }

            // 5. Удаление конфигурации
            delete("{publicId}") {
                val publicIdParam = call.parameters["publicId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, "Missing publicId"
                )
                try {
                    val publicId = UUID.fromString(publicIdParam)
                    val success = configurationService.deleteConfiguration(publicId)
                    if (success) {
                        call.respond(HttpStatusCode.OK, "Configuration deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Configuration not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }
            }

            patch("{publicId}/frames/{frameIndex}") {
                // Извлекаем publicId из URL
                val publicIdParam = call.parameters["publicId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing publicId")
                val publicId = try {
                    UUID.fromString(publicIdParam)
                } catch (e: IllegalArgumentException) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }

                // Извлекаем frameIndex из URL
                val frameIndexParam = call.parameters["frameIndex"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing frameIndex")
                val frameIndex = try {
                    frameIndexParam.toInt()
                } catch (e: NumberFormatException) {
                    return@patch call.respond(HttpStatusCode.BadRequest, "Invalid frameIndex")
                }

                // Ожидаем тело запроса – новый полный JSON для кадра
                val newFrameJson = call.receiveText()

                // Вызываем сервис
                val success = configurationService.updateFrame(publicId, frameIndex, newFrameJson)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Frame updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Frame not found")
                }
            }
        }
    }
}
