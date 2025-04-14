package com.flexypixelgalleryapi.models.auth

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
