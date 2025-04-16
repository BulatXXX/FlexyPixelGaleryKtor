package app.entities

import com.flexypixelgalleryapi.app.entities.LEDPanelsConfiguration
import com.flexypixelgalleryapi.app.entities.User
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Rating : Table("ratings") {
    val userId = integer("user_id").references(User.id, onDelete = ReferenceOption.CASCADE)
    val configurationId = integer("configuration_id").references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.CASCADE)
    val rating = integer("rating")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(userId, configurationId)
}
