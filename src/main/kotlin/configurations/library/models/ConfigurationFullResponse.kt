package configurations.library.models

import app.entities.ForkStatus
import configurations.common.models.FrameData
import configurations.common.models.PanelData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ConfigurationFullResponse(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String?,
    val miniPreviewImageUrl: String?,
    val miniPreviewPanelUid: String?,
    val useMiniPreview: Boolean,
    val forkStatus: ForkStatus,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val panels: List<PanelData>,
    val frames: List<FrameData>
)
