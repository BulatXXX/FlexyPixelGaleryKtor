package com.flexypixelgalleryapi.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val loginOrEmail: String,
    val password: String
)
