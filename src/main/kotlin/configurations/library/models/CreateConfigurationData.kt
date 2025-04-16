package com.flexypixelgalleryapi.configurations.userLibrary.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateConfigurationData(
    val name: String,
    val description: String,
    val panels: List<PanelData> = emptyList(),
    val frames: List<FrameData> = emptyList()
)
