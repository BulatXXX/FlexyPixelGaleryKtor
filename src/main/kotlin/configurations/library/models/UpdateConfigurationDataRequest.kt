package com.flexypixelgalleryapi.configurations.userLibrary.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationDataRequest(
    val name: String? = null,
    val description: String? = null
)
