package configurations.common

import kotlinx.serialization.Serializable

@Serializable
data class FrameData(
    val index: Int,
    val panelPixelColors: String
)
