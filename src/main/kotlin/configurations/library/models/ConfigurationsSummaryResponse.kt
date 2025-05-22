package configurations.library.models

import app.entities.ForkStatus
import configurations.common.models.AuthorInfo
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ForkInfo(
    @Contextual val sourceConfigurationPublicId: UUID,
    val author: AuthorInfo? = null,
)

@Serializable
data class ConfigurationSummaryResponse(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String? = null,
    val miniPreviewImageUrl: String? = null,
    val miniPreviewPanelUid: String? = null,
    val useMiniPreview: Boolean = true,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val isPublic: Boolean,
    val forkStatus: ForkStatus,
    val forkInfo: ForkInfo? = null
)
