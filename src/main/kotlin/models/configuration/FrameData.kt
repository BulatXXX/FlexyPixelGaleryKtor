package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class FrameData(
    val index: Int,
    val panelPixelColors: String
)
