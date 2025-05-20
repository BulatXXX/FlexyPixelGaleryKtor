package mobile

import configurations.common.models.FrameData
import configurations.common.models.PanelData
import configurations.util.SvgPreviewGenerator
import mobile.models.ConfigData
import mobile.models.MobileConfiguration
import mobile.models.results.DeleteResult
import mobile.models.results.GetResult
import mobile.models.results.PostResult
import mobile.models.results.UpdateResult
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class MobileConfigurationsService(
    private val mobileConfigurationsRepository: MobileConfigurationsRepository,
    private val previewGenerator: SvgPreviewGenerator
) {
    fun getAll(ownerId: Int): List<ConfigData> {
        return mobileConfigurationsRepository.getAll(ownerId)
    }

    fun getConfiguration(configId: UUID, ownerId: Int): GetResult = transaction {
        when {
            mobileConfigurationsRepository.isOwner(configId, ownerId) -> {
                var result = mobileConfigurationsRepository.getConfigurations(publicId = configId)
                    ?: return@transaction GetResult.NotFound
                result = result.parseToMobile()
                GetResult.Success(result)
            }

            mobileConfigurationsRepository.exists(configId) -> {
                GetResult.Forbidden
            }

            else -> GetResult.NotFound
        }
    }

    fun createConfiguration(ownerId: Int, mobileConfiguration: MobileConfiguration): PostResult {

        val configId = UUID.randomUUID()
        val parsedConfig = mobileConfiguration.parseFromMobile()

        val panels = parsedConfig.panelsInfo.map {
            PanelData(
                x = it.x,
                y = it.y,
                direction = it.direction,
                uid = it.uid
            )
        }
        val frame = FrameData(
            index = 0,
            panelPixelColors = parsedConfig.frameInfo[0].panelPixelColors
        )

        val previewUrl = previewGenerator.generate(
            panels = panels,
            frame = frame,
            configurationId = configId,
        )
        val miniPreviewUrl = previewGenerator.generate(
            panels = panels,
            frame = frame,
            configurationId = configId,
            miniPanelUID = mobileConfiguration.miniPreviewPanelUid
        )


        return if (mobileConfigurationsRepository.createConfiguration(
                ownerId = ownerId,
                configId = configId,
                mobileConfiguration = parsedConfig,
                previewUrl = previewUrl,
                miniPreviewUrl = miniPreviewUrl
            )
        ) PostResult.Success(configId)
        else PostResult.Failure
    }

    fun updateConfiguration(ownerId: Int, mobileConfiguration: MobileConfiguration, configId: UUID): UpdateResult = transaction {
        if (mobileConfigurationsRepository.isOwner(configId, ownerId)) {
            val parsedConfig = mobileConfiguration.parseFromMobile(configId)
            val panels = parsedConfig.panelsInfo.map {
                PanelData(
                    x = it.x,
                    y = it.y,
                    direction = it.direction,
                    uid = it.uid
                )
            }
            val frame = FrameData(
                index = 0,
                panelPixelColors = parsedConfig.frameInfo[0].panelPixelColors
            )

            val previewUrl = previewGenerator.generate(
                panels = panels,
                frame = frame,
                configurationId = configId,
            )
            val miniPreviewUrl = previewGenerator.generate(
                panels = panels,
                frame = frame,
                configurationId = configId,
                miniPanelUID = mobileConfiguration.miniPreviewPanelUid
            )
            return@transaction if (mobileConfigurationsRepository.updateConfiguration(
                    mobileConfiguration = parsedConfig,
                    previewUrl = previewUrl,
                    miniPreviewUrl = miniPreviewUrl
                )
            ) UpdateResult.Success
            else UpdateResult.UpdateError
        } else {
            if (mobileConfigurationsRepository.exists(configId)) {
                return@transaction UpdateResult.Forbidden
            }
            return@transaction UpdateResult.NotFound
        }


    }

    private fun deletePreviewFile(url: String) {
        val fileName = url.substringAfterLast("/")
        val fullFile = File(previewGenerator.outputDir, fileName)
        val miniFile = File(previewGenerator.outputDir, fileName) // если у вас разные папки, корректируйте путь
        if (fullFile.exists()) fullFile.delete()
        if (miniFile.exists()) miniFile.delete()
    }

    fun deleteConfiguration(ownerId: Int, configId: UUID): DeleteResult = transaction {
        when {
            mobileConfigurationsRepository.isOwner(configId, ownerId) -> {
                mobileConfigurationsRepository.getPreviewUrls(configId).forEach {
                    it?.let { url -> deletePreviewFile(url) }
                }
                if (mobileConfigurationsRepository.deleteConfiguration(configId)) DeleteResult.Success
                else DeleteResult.DeleteFailed

            }
            mobileConfigurationsRepository.exists(configId) -> DeleteResult.Forbidden
            else -> DeleteResult.NotFound
        }
    }


}

