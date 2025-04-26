package configurations.library.models.update_request

import configurations.common.models.FrameData
import configurations.common.models.PanelData
import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationStructureRequest(
    val panels: List<PanelData>,
    val frames: List<FrameData>
)

