package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class ConfigurationRequest(
    val name: String,
    val description: String,
    val previewImageUrl: String? = null,
    val panels: List<PanelData> = emptyList(),   // Используем, если создается полная конфигурация
    val frames: List<FrameData> = emptyList()      // Используем, если создается полная конфигурация
)
