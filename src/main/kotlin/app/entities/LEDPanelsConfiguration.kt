package app.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

enum class ForkStatus { ORIGINAL, ADDED, MODIFIED }

fun toForkStatus(value: String): ForkStatus = when (value.uppercase(Locale.getDefault())) {
    "ORIGINAL" -> ForkStatus.ORIGINAL
    "ADDED" -> ForkStatus.ADDED
    "MODIFIED" -> ForkStatus.MODIFIED
    else -> ForkStatus.ADDED
}


object LEDPanelsConfiguration : Table(name = "configurations") {
    val id = integer("id").autoIncrement()
    val publicId = uuid("public_id").uniqueIndex()
    val ownerId = integer("owner_id").references(User.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = text("description")
    val isPublic = bool("is_public").default(false)
    
    val previewImageUrl = varchar("preview_image_url", 512).nullable() // Полное
    val miniPreviewImageUrl = varchar("mini_preview_image_url", 512).nullable() // Мини

    val miniPreviewPanelUid = varchar("mini_preview_panel_uid", 64).nullable() // UID панели для мини
    val useMiniPreview = bool("use_mini_preview").default(true) // Использовать мини в карточке


    val sourceConfigurationId =
        integer("source_configuration_id")
            .references(id, onDelete = ReferenceOption.SET_NULL).nullable()

    val forkStatus = enumerationByName("fork_status", 10, ForkStatus::class).default(ForkStatus.ORIGINAL)
    val interFrameDelay = integer("inter_frame_delay").default(1000)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val isDeleted = bool("is_deleted").default(false)
    val isBanned = bool("is_banned").default(false)

    override val primaryKey = PrimaryKey(id)
}
