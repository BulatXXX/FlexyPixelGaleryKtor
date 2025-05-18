import app.configureRouting
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HelloRouteTest {
    @Test
    fun `GET hello should return HTML greeting`() = testApplication {
        application {
            install(Authentication) {
                jwt("auth-jwt") {
                    realm = "ktor-test"
                    skipWhen { true }
                    verifier(
                        JWT
                            .require(Algorithm.HMAC256("dummy-secret"))
                            .build()
                    )
                    validate { JWTPrincipal(it.payload) }
                }
            }
            configureRouting()
        }

        val response = client.get("/hello")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        assertTrue(body.contains("<h1>Hello FlexyPixel"))
    }
}
