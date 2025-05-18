package mobile.models


import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.Color
import java.util.*

@Serializable
data class MobileConfiguration(
    @Contextual val publicId: UUID? = null,
    val name: String,
    var panelsInfo: List<MobilePanelsInfo>,
    val frameInfo: List<MobileFramesInfo>,
    val interFrameDelay: Int?,
    val miniPreviewImageUrl: String? = null,
    val miniPreviewPanelUid: String? = null,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        private fun quantizeChannel(v: Int): Short =
            (((v + 1) * 10 - 1) / 256).toShort()

        private fun quantHex(hex: String): String {
            val c =   Color.decode(hex)
            val qr = quantizeChannel(c.red)
            val qg = quantizeChannel(c.green)
            val qb = quantizeChannel(c.blue)
            return "%d%d%d".format(qr, qg, qb)
        }
        private fun dequantizeChannel(q: Int): Int =
            if (q == 9) 255 else (q * 256 / 10)

        private fun dequantHex(quant: String): String {
            require(quant.length == 3) { "Ожидаю строку из трёх цифр, например \"259\"" }
            val qr = quant[0].digitToInt()
            val qg = quant[1].digitToInt()
            val qb = quant[2].digitToInt()
            val r = dequantizeChannel(qr)
            val g = dequantizeChannel(qg)
            val b = dequantizeChannel(qb)
            return String.format("#%02X%02X%02X", r, g, b)
        }
    }

    fun parseToMobile(): MobileConfiguration {
        val newPanels = normalizePanelsCoordinates()
        val newFrames = parseFramesToMobile()
        return copy(frameInfo = newFrames, panelsInfo = newPanels)
    }

    fun parseFromMobile(configId:UUID? = null): MobileConfiguration {
        val newFrames = parseFramesFromMobile()
        val newPanels = denormalizeCoordinates()
        return if (configId != null) {
            copy(frameInfo = newFrames, panelsInfo = newPanels, publicId = configId)
        }
        else copy(frameInfo = newFrames, panelsInfo = newPanels)
    }
    private fun MobileConfiguration.denormalizeCoordinates(
        stepX: Int = 8,
        stepY: Int = 8,
        offsetX: Int = 185,
        offsetY: Int = 95
    ): List<MobilePanelsInfo>{
        val denormPanels = panelsInfo.map { panel ->
            panel.copy(
                x = offsetX + panel.x * stepX,
                y = offsetY + panel.y * stepY
            )
        }
        return denormPanels
    }

    private fun MobileConfiguration.normalizePanelsCoordinates(): List<MobilePanelsInfo> {
        val xs = panelsInfo.map { it.x }.distinct().sorted()
        val ys = panelsInfo.map { it.y }.distinct().sorted()


        val xIndex = xs.withIndex().associate { (idx, x) -> x to idx }
        val yIndex = ys.withIndex().associate { (idx, y) -> y to idx }

        val normalizedPanels = panelsInfo.map { panel ->
            panel.copy(
                x = xIndex.getValue(panel.x),
                y = yIndex.getValue(panel.y)
            )
        }

        return normalizedPanels
    }


    private fun parseFramesToMobile(): List<MobileFramesInfo> {
        val newFrames = frameInfo.map { frame ->
            val raw: Map<String, List<List<String>>> =
                json.decodeFromString(frame.panelPixelColors)

            val quantized: Map<String, List<List<String>>> = raw.mapValues { (_, rows) ->
                rows.map { row -> row.map { hex -> quantHex(hex) } }
            }

            frame.copy(panelPixelColors = Json.encodeToString(quantized))
        }
        return newFrames
    }

    private fun parseFramesFromMobile(): List<MobileFramesInfo>{
        val restored = frameInfo.map { frame ->
            val raw: Map<String, List<List<String>>> =
                json.decodeFromString(frame.panelPixelColors)
            val deq: Map<String, List<List<String>>> = raw.mapValues { (_, rows) ->
                rows.map { row ->
                    row.map { quant ->
                        dequantHex(quant)
                    }
                }
            }
            frame.copy(panelPixelColors = json.encodeToString(deq))
        }

        return restored
    }
}

