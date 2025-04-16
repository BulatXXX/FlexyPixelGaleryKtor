package com.flexypixelgalleryapi.configurations.userLibrary


import com.flexypixelgalleryapi.app.config.JwtClaims
import com.flexypixelgalleryapi.configurations.userLibrary.models.CreateConfigurationData
import com.flexypixelgalleryapi.configurations.userLibrary.models.UpdateConfigurationDataRequest
import com.flexypixelgalleryapi.configurations.userLibrary.models.UpdateConfigurationStructureData
import users.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID


fun ApplicationCall.getOwnerIdByPrincipal(userService: UserService): Int? {
    val principal = this.principal<JWTPrincipal>() ?: return null
    val publicId = UUID.fromString(principal.payload.getClaim("publicId").asString())
    return userService.getUserIdByPublicId(publicId)
}

fun ApplicationCall.getConfigurationPublicIdFromParams(): UUID? {
    val publicIdParam = this.parameters["publicId"] ?: return null
    return UUID.fromString(publicIdParam)

}


fun Route.usersConfigurationRoutes() {
    val configurationService: ConfigurationService by inject<ConfigurationService>()
    val userService by inject<UserService>()
    authenticate("auth-jwt") {
        route("/my") {

            post("/create") {
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val ownerId = principal.payload.getClaim(JwtClaims.USER_ID).asInt()

                val request = call.receive<CreateConfigurationData>()

                val publicId = configurationService.createConfiguration(
                    ownerId = ownerId,
                    name = request.name,
                    description = request.description,
                )
                call.respond(HttpStatusCode.Created, mapOf("publicId" to publicId))
            }

            patch("{publicId}/data") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val requesterId =
                    call.getOwnerIdByPrincipal(userService) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                try {
                    val updateRequest = call.receive<UpdateConfigurationDataRequest>()
                    val success = configurationService.updateConfiguration(
                        publicId = configurationPublicId,
                        name = updateRequest.name,
                        description = updateRequest.description,
                        requesterId = requesterId
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

            post("/create/full") {
                val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val ownerId = principal.payload.getClaim(JwtClaims.USER_ID).asInt()

                val request = call.receive<CreateConfigurationData>()
                val publicId = configurationService.createFullConfiguration(
                    ownerId = ownerId,
                    requestData = request
                )
                call.respond(HttpStatusCode.Created, mapOf("publicId" to publicId))
            }

            patch("{publicId}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        "Bad config public id"
                    )
                val requesterId =
                    call.getOwnerIdByPrincipal(userService) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val request = call.receive<UpdateConfigurationStructureData>()

                val success =
                    configurationService.updatePanelsAndFrames(
                        publicId = configurationPublicId,
                        panels = request.panels,
                        frames = request.frames,
                        requesterId = requesterId
                    )
                if (success) {
                    call.respond(HttpStatusCode.OK, "Structure updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Configuration not found")
                }
            }

            get("{publicId}") {

                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val requesterId =
                    call.getOwnerIdByPrincipal(userService) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                try {
                    val configuration = configurationService.getFullConfiguration(configurationPublicId, requesterId)
                    if (configuration != null) {
                        call.respond(HttpStatusCode.OK, configuration)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Configuration not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }
            }



            delete("{publicId}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val requesterId =
                    call.getOwnerIdByPrincipal(userService) ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                try {
                    val success = configurationService.deleteConfiguration(configurationPublicId, requesterId)
                    if (success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Configuration not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid publicId format")
                }
            }

            patch("{publicId}/frames/{frameIndex}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val requesterId =
                    call.getOwnerIdByPrincipal(userService) ?: return@patch call.respond(HttpStatusCode.Unauthorized)
                val frameIndexParam = call.parameters["frameIndex"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing frameIndex")
                val frameIndex = frameIndexParam.toInt()

                val updatedFrame = call.receiveText()

                val success =
                    configurationService.updateFrame(configurationPublicId, frameIndex, updatedFrame, requesterId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Frame updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Frame not found")
                }
            }
            get("all") {
                val ownerId = call.getOwnerIdByPrincipal(userService)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                val configurations = configurationService.getConfigurationSummary(ownerId)
                call.respond(HttpStatusCode.OK, configurations)
            }
        }
    }
}

