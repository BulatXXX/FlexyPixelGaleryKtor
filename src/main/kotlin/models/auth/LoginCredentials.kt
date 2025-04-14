package com.flexypixelgalleryapi.models.auth

import java.util.*

data class LoginCredentials(
    val publicId: UUID,
    val passwordHash: String
)
