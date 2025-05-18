package mobile.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ConfigData(
    @Contextual val publicId: UUID,
    val name: String,
    @Contextual val createdAt: LocalDateTime,
    val miniPreviewPanelUid: String? = null,
    val miniPreviewImageUrl: String? = null,
    )
