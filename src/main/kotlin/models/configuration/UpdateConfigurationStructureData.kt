package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationStructureData(
    val panels: List<PanelData>,
    val frames: List<FrameData>
)

