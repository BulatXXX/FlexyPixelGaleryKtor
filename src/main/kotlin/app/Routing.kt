package app

import app.config.JwtClaims
import auth.authRoutes
import configurations.gallery.galleryRoutes
import configurations.library.usersConfigurationRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import users.userRoutes
import java.io.File

suspend fun ApplicationCall.requireUserId(): Int? {
    val principal = this.principal<JWTPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized)
        return null
    }
    return principal.payload.getClaim(JwtClaims.USER_ID).asInt()
}

suspend inline fun <T> ApplicationCall.requireParam(
    name: String,
    crossinline parser: (String) -> T?
): T? {
    val raw = parameters[name]
        ?: run {
            respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing parameter '$name'"))
            return null
        }
    val value = try {
        parser(raw)
    } catch (e: Exception) {
        null
    }
    if (value == null) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid '$name': '$raw'"))
        return null
    }
    return value
}

inline fun <T> ApplicationCall.optionalParam(
    name: String,
    parser: (String) -> T?
): T? {
    val raw = request.queryParameters[name] ?: return null
    return try {
        parser(raw)
    } catch (e: Exception) {
        null
    }
}



fun Application.configureRouting() {
    routing {
        get("/hello"){
            call.respond(HttpStatusCode.OK,"Hello FlexyPixel 0.1.8!")
        }
        staticFiles("/uploads", File("uploads"))
        staticFiles("/previews", File("previews"))
        authRoutes()
        route("/configurations"){
            usersConfigurationRoutes()
            galleryRoutes()
        }
        userRoutes()
    }
}
