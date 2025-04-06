package com.flexypixelgalleryapi.models

import java.util.*

data class LoginCredentials(
    val publicId: UUID,
    val passwordHash: String
)
