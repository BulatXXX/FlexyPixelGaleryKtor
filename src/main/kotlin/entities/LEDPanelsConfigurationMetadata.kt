package com.flexypixelgalleryapi.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object LEDPanelsConfigurationMetadata : Table("configurations_metadata"){
    val configurationId = integer("configuration_id")
        .references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.CASCADE)
        .uniqueIndex()
    val addedCount = integer("views_count").default(0)
    val averageRating = double("average_rating").nullable()
    val publishedAt = datetime("published_at").nullable().default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(configurationId)
}