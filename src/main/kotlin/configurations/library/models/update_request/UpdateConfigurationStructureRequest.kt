package configurations.library.models.update_request

import configurations.common.FrameData
import configurations.common.PanelData
import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationStructureRequest(
    val panels: List<PanelData>,
    val frames: List<FrameData>
)

