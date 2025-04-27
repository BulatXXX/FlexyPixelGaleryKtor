package com.flexypixelgalleryapi

import app.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "jwt.audience" to "test-audience",
                "jwt.issuer" to "test-issuer",
                "jwt.secret" to "test-secret",
                "jwt.domain" to "test-domain",
                "jwt.realm" to "test-realm",
            )
        }
        application {
            module()
        }
        client.get("/hello").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
