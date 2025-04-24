package users

import app.config.JwtClaims
import configurations.library.getPublicIdFromParams
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import io.ktor.server.response.*
import users.models.get_request.GetResult
import users.models.update_request.UpdateRequest
import users.models.update_request.UpdateResult
import java.util.*


suspend fun ApplicationCall.requireUserId(): Int? {
    val principal = this.principal<JWTPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized)
        return null
    }
    return principal.payload.getClaim(JwtClaims.USER_ID).asInt()
}


suspend fun ApplicationCall.respondGet(result: GetResult) = when (result) {
    is GetResult.SuccessFull -> respond(HttpStatusCode.OK, result.userResponse)
    is GetResult.SuccessPublic -> respond(HttpStatusCode.OK, result.userResponse)
    is GetResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
    is GetResult.DataBaseError -> respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error"))
}

suspend fun ApplicationCall.respondUpdate(result: UpdateResult) = when (result) {
    is UpdateResult.Success -> respond(HttpStatusCode.OK, mapOf("message" to "Info updated"))
    is UpdateResult.ValidationError -> respond(HttpStatusCode.BadRequest, mapOf("error" to result.message))
    is UpdateResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
    is UpdateResult.DatabaseError -> respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database error"))
}

fun Route.userRoutes() {
    val userService: UserService by inject<UserService>()
    authenticate("auth-jwt") {
        route("/users") {

            route("/me") {
                get {
                    val userId = call.requireUserId() ?: return@get
                    val result = userService.getUserById(userId)
                    call.respondGet(result)
                }
                patch {
                    val userId = call.requireUserId() ?: return@patch
                    val request = call.receive<UpdateRequest>()
                    val result = userService.updateUserInfo(userId, request)
                    call.respondUpdate(result)
                }
            }

            get("{publicId}") {
                val publicIdParam = call.parameters["publicId"]
                if (publicIdParam == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing required parameter"))
                    return@get
                }
                val publicId = UUID.fromString(publicIdParam)
                val result = userService.getByPublicId(publicId)
                call.respondGet(result = result)
            }


        }
    }
}