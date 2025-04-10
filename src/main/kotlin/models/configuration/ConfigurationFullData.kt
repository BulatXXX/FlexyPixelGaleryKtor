package com.flexypixelgalleryapi.models

import com.flexypixelgalleryapi.models.configuration.FrameData
import com.flexypixelgalleryapi.models.configuration.PanelData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ConfigurationFullData(
    @Contextual val publicId: UUID,
    val ownerId: Int,
    val name: String,
    val description: String,
    val previewImageUrl: String?,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val panels: List<PanelData>,
    val frames: List<FrameData>
)
