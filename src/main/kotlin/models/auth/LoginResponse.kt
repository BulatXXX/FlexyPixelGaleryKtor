package com.flexypixelgalleryapi.models.auth

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class LoginResponse(
    val token: String,
    @Contextual val publicId: UUID
)
