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
import org.koin.ktor.ext.inject
import password_recovery.EmailService
import password_recovery.passwordRecoveryRoutes
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
        get("/hello") {
            val version = "0.2.0"
            val html = """
    <!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="UTF-8"/>
        <title>Hello</title>
        <style>
          body { background: #f0f0f0; font-family: sans-serif; }
          h1 { color: beige; text-align: center; margin-top: 4rem; }
        </style>
      </head>
      <body>
        <h1>Hello FlexyPixel $version!</h1>
      </body>
    </html>
  """.trimIndent()

            call.respondText(html, ContentType.Text.Html, HttpStatusCode.OK)
        }
        staticFiles("/uploads", File("uploads"))
        staticFiles("/previews", File("previews"))
        authRoutes()
        route("/configurations"){
            usersConfigurationRoutes()
            galleryRoutes()
        }
        userRoutes()
        passwordRecoveryRoutes()
    }
}
