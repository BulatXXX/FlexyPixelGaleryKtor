package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val previewImageUrl: String? = null
)
