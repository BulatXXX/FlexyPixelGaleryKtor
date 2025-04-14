package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ConfigurationSummaryData(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String? = null,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime
)
