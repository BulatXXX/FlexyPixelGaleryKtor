package configurations.library

import configurations.common.models.FrameData
import configurations.common.models.PanelData
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

class SvgPreviewGenerator(
    val outputDir: File,
    private val baseUrl: String
) {
    /**
     * Генерирует SVG-превью на основе списка панелей и одного кадра.
     * @return URL сохранённого SVG-превью.
     */
    fun generate(
        panels: List<PanelData>,
        frame: FrameData,
        configurationId: UUID,
        miniPanelUID: String? = null
    ): String {
        val colorsMap: Map<String, List<List<String>>> =
            Json.decodeFromString(frame.panelPixelColors)

        val targetPanels = if (miniPanelUID != null) {
            panels.filter { it.uid == miniPanelUID }
        } else {
            panels
        }

        if (targetPanels.isEmpty()) throw IllegalArgumentException("No panels to render")

        val referenceMatrix = colorsMap[targetPanels.first().uid]
            ?: throw IllegalArgumentException("No pixel data for panel ${targetPanels.first().uid}")

        val panelHeight = referenceMatrix.size
        val panelWidth = referenceMatrix.first().size

        val minX = targetPanels.minOfOrNull { it.x } ?: 0
        val minY = targetPanels.minOfOrNull { it.y } ?: 0
        val maxX = targetPanels.maxOf { it.x } + panelWidth
        val maxY = targetPanels.maxOf { it.y } + panelHeight

        val imgWidth = maxX - minX
        val imgHeight = maxY - minY

        val svg = StringBuilder().apply {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine(
                """<svg xmlns="http://www.w3.org/2000/svg" """ +
                        """width="$imgWidth" height="$imgHeight" """ +
                        """viewBox="0 0 $imgWidth $imgHeight" """ +
                        """shape-rendering="crispEdges" """ +
                        """style="image-rendering: pixelated;">"""
            )
            targetPanels.forEach { panel ->
                val matrix = colorsMap[panel.uid] ?: return@forEach
                matrix.forEachIndexed { row, cols ->
                    cols.forEachIndexed { col, hex ->
                        val x = panel.x - minX + col
                        val y = panel.y - minY + row
                        appendLine("""  <rect x="$x" y="$y" width="1" height="1" fill="$hex" />""")
                    }
                }
            }
            appendLine("</svg>")
        }

        if (!outputDir.exists()) outputDir.mkdirs()
        val fileSuffix = if (miniPanelUID != null) "-mini" else ""
        val fileName = "preview-$configurationId$fileSuffix.svg"
        val outFile = File(outputDir, fileName)
        outFile.writeText(svg.toString())

        return "$baseUrl/$fileName"
    }

}
