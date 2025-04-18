package com.flexypixelgalleryapi.entities


import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

enum class ForkStatus { ORIGINAL, ADDED, MODIFIED }

object LEDPanelsConfiguration : Table(name="configurations") {
    val id = integer("id").autoIncrement()
    val publicId = uuid("public_id").uniqueIndex()
    val ownerId = integer("owner_id").references(User.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val description = text("description")
    val isPublic = bool("is_public").default(false)
    val previewImageUrl = varchar("preview_image_url", 512).nullable()

    val sourceConfigurationId =
        integer("source_configuration_id")
            .references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.SET_NULL).nullable()

    val forkStatus = enumerationByName("fork_status", 10, ForkStatus::class).default(ForkStatus.ORIGINAL)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}
