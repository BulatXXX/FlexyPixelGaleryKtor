package configurations.util


import configurations.common.models.FrameData
import configurations.common.models.PanelData
import kotlinx.serialization.json.Json
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PreviewGenerator(
    private val outputDir: File,
    private val baseUrl: String
) {
    private val pixelSize = 10

    /**
     * Генерирует превью на основе списка панелей и одного кадра.
     * @return URL сохранённого превью.
     */
    fun generate(panels: List<PanelData>, frame: FrameData, configurationId: UUID): String {
        // 1. Распарсить JSON с цветами: Map<panelUid, List<List<hexColor>>>
        val colorsMap: Map<String, List<List<String>>> =
            Json.decodeFromString(frame.panelPixelColors)

        // 2. Найти размер одной панели (предполагаем квадратную матрицу)
        val referenceMatrix = colorsMap.values.firstOrNull()
            ?: throw IllegalArgumentException("frame.panelPixelColors пустой")
        val panelHeight = referenceMatrix.size
        val panelWidth = referenceMatrix.first().size

        // 3. Вычислить границы всего изображения
        val xs = panels.map { it.x }
        val ys = panels.map { it.y }
        val minX = xs.minOrNull() ?: 0
        val minY = ys.minOrNull() ?: 0
        val maxX = panels.maxOf { it.x } + panelWidth
        val maxY = panels.maxOf { it.y } + panelHeight

        val imgWidth = (maxX - minX) * pixelSize
        val imgHeight = (maxY - minY) * pixelSize

        // 4. Подготовить пустое изображение
        val image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB)

        val g = image.createGraphics().apply {
            // (опционально) сглаживание, если нужно
            // setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        }

        // 5. Нарисовать каждый пиксель панели
        panels.forEach { panel ->
            val matrix = colorsMap[panel.uid]
                ?: return@forEach // пропускаем, если нет данных по uid
            matrix.forEachIndexed { row, cols ->
                cols.forEachIndexed { col, hex ->
                    val x = (panel.x - minX + col) * pixelSize
                    val y = (panel.y - minY + row) * pixelSize
                    g.color = Color.decode(hex)
                    g.fillRect(x, y, pixelSize, pixelSize)
                }
            }
        }

        // 6. Сохранить файл
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val fileName = "preview-${configurationId}.png"
        val outFile = File(outputDir, fileName)
        ImageIO.write(image, "png", outFile)

        // 7. Вернуть URL для доступа
        return "$baseUrl/$fileName"
    }
}
