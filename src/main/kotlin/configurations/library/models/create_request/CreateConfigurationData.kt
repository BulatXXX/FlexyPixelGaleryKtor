package configurations.library.models.create_request

import configurations.common.models.FrameData
import configurations.common.models.PanelData
import kotlinx.serialization.Serializable

@Serializable
data class CreateConfigurationData(
    val name: String,
    val description: String,
    val panels: List<PanelData> = emptyList(),
    val frames: List<FrameData> = emptyList()
)
