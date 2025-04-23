package configurations.library


import app.config.JwtClaims
import configurations.library.models.create_request.CreateConfigurationData
import configurations.library.models.update_request.UpdateConfigurationDataRequest
import configurations.library.models.update_request.UpdateConfigurationStructureRequest
import configurations.library.models.create_request.CreateResult
import configurations.library.models.delete_request.DeleteResult
import configurations.library.models.get_request.GetResult
import configurations.library.models.update_request.UpdateResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

suspend fun ApplicationCall.getConfigurationPublicIdFromParams(): UUID? {
    val publicIdParam = this.parameters["publicId"]
    if (publicIdParam == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing required parameter"))
        return null
    }
    val configId = UUID.fromString(publicIdParam)

    return configId
}

suspend fun ApplicationCall.requireUserId(): Int? {
    val principal = this.principal<JWTPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized)
        return null
    }
    return principal.payload.getClaim(JwtClaims.USER_ID).asInt()
}

suspend fun ApplicationCall.respondCreate(result: CreateResult) {
    when (result) {
        is CreateResult.Success -> respond(HttpStatusCode.Created, mapOf("publicId" to result.publicId))
        is CreateResult.DatabaseError -> respond(HttpStatusCode.Conflict, mapOf("error" to "Database error"))
        is CreateResult.ValidationError -> respond(HttpStatusCode.BadRequest, mapOf("error" to result.message))
    }
}

suspend fun ApplicationCall.respondUpdate(result: UpdateResult) {
    when (result) {
        is UpdateResult.Success -> respond(HttpStatusCode.OK, mapOf("message" to result.message))
        is UpdateResult.DatabaseError -> respond(HttpStatusCode.Conflict, mapOf("error" to "Database error"))
        is UpdateResult.ValidationError -> respond(HttpStatusCode.BadRequest, mapOf("error" to result.message))
        is UpdateResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "Configuration not found"))
        is UpdateResult.Forbidden -> respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the owner"))
    }
}

fun Route.usersConfigurationRoutes() {
    val configurationService: ConfigurationService by inject<ConfigurationService>()
    authenticate("auth-jwt") {
        route("/my") {

            post("/create") {
                val ownerId = call.requireUserId() ?: return@post

                val request = call.receive<CreateConfigurationData>()

                val result = configurationService.createConfiguration(
                    ownerId = ownerId,
                    name = request.name,
                    description = request.description,
                )
                call.respondCreate(result)
            }

            post("/create/full") {
                val ownerId = call.requireUserId() ?: return@post

                val request = call.receive<CreateConfigurationData>()

                val result = configurationService.createFullConfiguration(
                    ownerId = ownerId,
                    requestData = request
                )
                call.respondCreate(result)
            }

            patch("{publicId}/data") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch

                val requesterId = call.requireUserId() ?: return@patch

                val updateRequest = call.receive<UpdateConfigurationDataRequest>()

                val result = configurationService.updateConfigurationData(
                    publicId = configurationPublicId,
                    request = updateRequest,
                    requesterId = requesterId,
                )
                call.respondUpdate(result)
            }

            patch("{publicId}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch

                val requesterId = call.requireUserId() ?: return@patch

                val request = call.receive<UpdateConfigurationStructureRequest>()

                val result =
                    configurationService.updateConfigurationStructure(
                        publicId = configurationPublicId,
                        request = request,
                        requesterId = requesterId
                    )
                call.respondUpdate(result)
            }

            patch("{publicId}/frames/{frameIndex}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@patch

                val requesterId = call.requireUserId() ?: return@patch

                val frameIndexParam = call.parameters["frameIndex"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Missing frameIndex")
                val frameIndex = frameIndexParam.toInt()

                val updatedFrame = call.receiveText()

                val result =
                    configurationService.updateFrame(configurationPublicId, frameIndex, updatedFrame, requesterId)
                call.respondUpdate(result)
            }
            get("{publicId}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@get

                val requesterId = call.requireUserId() ?: return@get

                when (val result = configurationService.getFullConfiguration(configurationPublicId, requesterId)) {
                    is GetResult.Success -> call.respond(HttpStatusCode.OK, result.configurationFullResponse)
                    is GetResult.Forbidden -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the owner"))
                    is GetResult.NotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Configuration is not found"))
                }
            }

            delete("{publicId}") {
                val configurationPublicId =
                    call.getConfigurationPublicIdFromParams() ?: return@delete

                val requesterId = call.requireUserId() ?: return@delete

                when (val result = configurationService.deleteConfiguration(configurationPublicId, requesterId)) {
                    is DeleteResult.Success -> call.respond(HttpStatusCode.NoContent, mapOf("message" to result.message))
                    is DeleteResult.Forbidden -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the owner"))
                    is DeleteResult.NotFound -> call.respond(HttpStatusCode.NotFound, mapOf("error" to "Configuration not found"))
                    is DeleteResult.DatabaseError -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Database error"))
                }
            }

            get("all") {
                val ownerId = call.requireUserId() ?: return@get
                val configurations = configurationService.getConfigurationSummary(ownerId)
                call.respond(HttpStatusCode.OK, configurations)
            }
        }
    }
}

