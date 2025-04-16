package com.flexypixelgalleryapi.configurations.userLibrary.models

import kotlinx.serialization.Serializable

@Serializable
data class PanelData(
    val uid: String,
    val x: Int,
    val y: Int,
    val direction: String
)
