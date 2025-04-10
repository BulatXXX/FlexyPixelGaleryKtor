package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ConfigurationResponse (
    @Contextual
    val publicId: UUID,
    val ownerId: Int,
    val name: String,
    val description: String,
    val previewImageUrl: String?,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)