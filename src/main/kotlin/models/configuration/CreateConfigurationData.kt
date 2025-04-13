package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class CreateConfigurationData(
    val name: String,
    val description: String,
    val panels: List<PanelData> = emptyList(),
    val frames: List<FrameData> = emptyList()
)
