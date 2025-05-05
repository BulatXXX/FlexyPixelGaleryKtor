package configurations.newlibrary

import configurations.library.models.create_request.CreateConfigurationData
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

interface LibraryConfigurationRepository {
    fun createConfiguration(ownerId: Int, publicId: UUID?, request: CreateConfigurationData): Boolean

}