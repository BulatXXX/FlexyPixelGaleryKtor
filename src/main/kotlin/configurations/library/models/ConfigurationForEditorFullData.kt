package com.flexypixelgalleryapi.configurations.userLibrary.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ConfigurationForEditorFullData(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String?,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val panels: List<PanelData>,
    val frames: List<FrameData>
)
