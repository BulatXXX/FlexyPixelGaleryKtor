package app.plugins

import app.config.JwtConfig
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureSecurity() {

    val jwtAudience = System.getenv("JWT_AUDIENCE")
        ?: environment.config.propertyOrNull("jwt.audience")?.getString()
        ?: "jwt-audience"
    val jwtIssuer = System.getenv("JWT_DOMAIN")
        ?: environment.config.propertyOrNull("jwt.domain")?.getString()
        ?: "flexypixelapi"
    val jwtRealm = System.getenv("JWT_REALM")
        ?: environment.config.propertyOrNull("jwt.realm")?.getString()
        ?: "flexypixelapp"
    val jwtSecret = System.getenv("JWT_SECRET")
        ?: environment.config.propertyOrNull("jwt.secret")?.getString()
        ?: "flexypixel2025"

    JwtConfig.init(jwtSecret, jwtIssuer)
    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JwtConfig.getVerifier()
            )
            validate { credential ->
                val publicId =  credential.payload.getClaim("publicId").asString()
                if (publicId != null) JWTPrincipal(credential.payload) else null
            }
        }
    }

    authentication {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                )
            }
            client = HttpClient(Apache)
        }
    }

    routing {
        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/hello")
            }
        }
    }
}

class UserSession(accessToken: String)
