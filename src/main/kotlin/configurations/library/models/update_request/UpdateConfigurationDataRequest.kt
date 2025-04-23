package configurations.library.models.update_request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConfigurationDataRequest(
    val name: String = "MyConfiguration",
    val description: String = "Description",
)
