package com.flexypixelgalleryapi.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private var validityInMs:Long = 36_000_00 * 24 // 24 часа

    private lateinit var algorithm: Algorithm

    fun init(secret: String, issuer: String, validityMs: Long = 36_000_00 * 24) {
        this.secret = secret
        this.issuer = issuer
        this.validityInMs = validityMs
        this.algorithm = Algorithm.HMAC256(secret)
    }
    fun generateToken(publicId: UUID): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("publicId", publicId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }

    fun getVerifier() = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()
}
