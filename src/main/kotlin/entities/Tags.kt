package com.flexypixelgalleryapi.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Tags : Table("tags") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex() // Уникальное имя тега
    override val primaryKey = PrimaryKey(id)
}

object ConfigurationTags : Table("Configuration_tags") {
    val configurationId = integer("configuration_id").references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.CASCADE)
    val tagId = integer("tag_id").references(Tags.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(configurationId, tagId)
}
