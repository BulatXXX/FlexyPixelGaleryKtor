package com.flexypixelgalleryapi.app.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtClaims {
    const val PUBLIC_ID = "publicId"
    const val USER_ID = "userId"
}


object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private var accessValidityInMs:Long = 36_000_00 * 24
    private var refreshValidityInMs: Long = 7 * accessValidityInMs
    private lateinit var algorithm: Algorithm

    fun init(secret: String, issuer: String, validityMs: Long = 36_000_00 * 24) {
        JwtConfig.secret = secret
        JwtConfig.issuer = issuer
        accessValidityInMs = validityMs
        algorithm = Algorithm.HMAC256(secret)
    }

    fun generateAccessToken(userId: Int, publicId: UUID): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(JwtClaims.PUBLIC_ID, publicId.toString())
            .withClaim(JwtClaims.USER_ID, userId)
            .withExpiresAt(Date(System.currentTimeMillis() + accessValidityInMs))
            .sign(algorithm)
    }


    fun generateRefreshToken(userId: Int,publicId: UUID): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(JwtClaims.PUBLIC_ID, publicId.toString())
            .withClaim(JwtClaims.USER_ID, userId)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshValidityInMs))
            .sign(algorithm)
    }

    fun getVerifier() = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()
}
