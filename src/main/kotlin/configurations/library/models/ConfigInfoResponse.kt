package configurations.library.models

import app.entities.ForkStatus


data class ConfigInfoResponse (
    val name: String? = null,
    val description: String? = null,
    val previewImageUrl: String? = null,
    val miniPreviewImageUrl: String? = null,
    val forkStatus: ForkStatus? = null,
)
