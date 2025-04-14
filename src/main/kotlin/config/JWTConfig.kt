package com.flexypixelgalleryapi.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private var accessValidityInMs:Long = 36_000_00 * 24
    private var refreshValidityInMs: Long = 7 * accessValidityInMs
    private lateinit var algorithm: Algorithm

    fun init(secret: String, issuer: String, validityMs: Long = 36_000_00 * 24) {
        this.secret = secret
        this.issuer = issuer
        this.accessValidityInMs = validityMs
        this.algorithm = Algorithm.HMAC256(secret)
    }

    fun generateAccessToken(publicId: UUID): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("publicId", publicId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + accessValidityInMs))
            .sign(algorithm)
    }


    fun generateRefreshToken(publicId: UUID): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("publicId", publicId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + refreshValidityInMs))
            .sign(algorithm)
    }

    fun getVerifier() = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()
}
