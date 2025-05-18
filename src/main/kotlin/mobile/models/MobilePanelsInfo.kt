package mobile.models

import kotlinx.serialization.Serializable

@Serializable
data class MobilePanelsInfo(
    val x: Int,
    val y: Int,
    val uid: String,
    val direction: String,
    val palette: List<Palette>,
)

@Serializable
data class Palette (
    val r: Short,
    val g: Short,
    val b: Short,
)

const val PALETTE_LENGTH = 12
