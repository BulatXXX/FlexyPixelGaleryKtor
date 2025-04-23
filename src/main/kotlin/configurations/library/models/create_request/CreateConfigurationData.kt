package configurations.library.models.create_request

import configurations.common.FrameData
import configurations.common.PanelData
import kotlinx.serialization.Serializable

@Serializable
data class CreateConfigurationData(
    val name: String,
    val description: String,
    val panels: List<PanelData> = emptyList(),
    val frames: List<FrameData> = emptyList()
)
