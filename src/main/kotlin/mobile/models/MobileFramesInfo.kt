package mobile.models

import kotlinx.serialization.Serializable

@Serializable
data class MobileFramesInfo (
    val index: Int,
    val panelPixelColors: String
)
