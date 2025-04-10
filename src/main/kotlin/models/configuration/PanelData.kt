package com.flexypixelgalleryapi.models.configuration

import kotlinx.serialization.Serializable

@Serializable
data class PanelData(
    val uid: String,
    val x: Int,
    val y: Int,
    val direction: String
)
