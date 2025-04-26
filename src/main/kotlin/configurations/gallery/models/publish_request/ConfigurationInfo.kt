package configurations.gallery.models.publish_request

import app.entities.ForkStatus

data class ConfigurationInfo(
    val isPublic: Boolean,
    val sourceConfigurationId: Int?,
    val forkStatus: ForkStatus,
)
