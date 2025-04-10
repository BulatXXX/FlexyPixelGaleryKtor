package com.flexypixelgalleryapi.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Frame : Table("frames") {
    val id = integer("id").autoIncrement()
    val configurationId = integer("configuration_id")
        .references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.CASCADE)

    val index = integer("index")
    val panelPixelColors = text("panel_pixel_colors")

    override val primaryKey = PrimaryKey(id)
}
