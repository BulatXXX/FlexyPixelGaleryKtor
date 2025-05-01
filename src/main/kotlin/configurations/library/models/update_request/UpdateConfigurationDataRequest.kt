package configurations.library.models.update_request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationDataRequest(
    val name: String? = null,
    val description: String? = null,
    val previewUrl: String? = null,
    val miniPreviewUrl: String? = null,
)
