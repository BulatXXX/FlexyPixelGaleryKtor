package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationStructureRequest(
    val panels: List<PanelData>,
    val frames: List<FrameData>
)

