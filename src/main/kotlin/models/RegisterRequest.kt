package com.flexypixelgalleryapi.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val login: String,
    val displayName: String,
    val password: String,
    val phone: String? = null
)
