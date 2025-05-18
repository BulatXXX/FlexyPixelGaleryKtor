package mobile

import app.requireParam
import app.requireUserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.server.response.*
import mobile.models.MobileConfiguration
import mobile.models.results.DeleteResult
import mobile.models.results.GetResult
import mobile.models.results.PostResult
import mobile.models.results.UpdateResult
import java.util.*

suspend fun ApplicationCall.respondGet(getResult: GetResult) = when (getResult) {
    is GetResult.Success -> respond(HttpStatusCode.OK, getResult.mobileConfiguration)
    is GetResult.Forbidden -> respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the Owner"))
    is GetResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "The config does not exist"))
    else -> respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
}

suspend fun ApplicationCall.respondPost(postResult: PostResult) = when (postResult) {
    is PostResult.Success -> respond(HttpStatusCode.Created, mapOf("publicId" to postResult.publicId))
    else -> respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal Server Error"))
}

suspend fun ApplicationCall.respondUpdate(updateResult: UpdateResult) = when (updateResult) {
    is UpdateResult.Success -> respond(HttpStatusCode.OK, mapOf("message" to "Update successful"))
    is UpdateResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "The config not found"))
    is UpdateResult.Forbidden -> respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the Owner"))
    is UpdateResult.NoConfigId -> respond(HttpStatusCode.BadRequest, mapOf("error" to "No configId provided"))
    is UpdateResult.UpdateError -> respond(
        HttpStatusCode.InternalServerError,
        mapOf("error" to "Internal Server Error")
    )
}
suspend fun ApplicationCall.respondDelete(deleteResult: DeleteResult) = when (deleteResult) {
    is DeleteResult.Success -> respond(HttpStatusCode.OK, mapOf("message" to "Deleted successfully"))
    is DeleteResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "The config does not exist"))
    is DeleteResult.Forbidden -> respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the Owner"))
    is DeleteResult.DeleteFailed -> respond(HttpStatusCode.BadRequest, mapOf("error" to "The config deletion failed"))
}

fun Route.mobileRoutes() {
    val mobileConfigurationsService by inject<MobileConfigurationsService>()
    authenticate("auth-jwt") {
        route("mobile/configurations") {
            get("{publicId}") {
                val configId = call.requireParam("publicId") { UUID.fromString(it) } ?: return@get
                val ownerId = call.requireUserId() ?: return@get
                val result = mobileConfigurationsService.getConfiguration(configId, ownerId)
                call.respondGet(result)
            }
            post {
                val ownerId = call.requireUserId() ?: return@post
                val mobileConfiguration = call.receive<MobileConfiguration>()
                val result = mobileConfigurationsService.createConfiguration(ownerId, mobileConfiguration)
                call.respondPost(result)
            }
            patch("{publicId}") {
                val ownerId = call.requireUserId() ?: return@patch
                val mobileConfiguration = call.receive<MobileConfiguration>()
                val result = mobileConfigurationsService.updateConfiguration(ownerId, mobileConfiguration)
                call.respondUpdate(result)
            }

            delete("{publicId}") {
                val ownerId = call.requireUserId() ?: return@delete
                val configId = call.requireParam("publicId") { UUID.fromString(it) } ?: return@delete
                val result = mobileConfigurationsService.deleteConfiguration(ownerId,configId)
                call.respondDelete(result)

            }

            get("/all") {
                val ownerId = call.requireUserId() ?: return@get
                val result = mobileConfigurationsService.getAll(ownerId)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}