package com.flexypixelgalleryapi.app.entities

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object Panel: Table("panels") {
    val configurationId = integer("configuration_id")
        .references(LEDPanelsConfiguration.id, onDelete = ReferenceOption.CASCADE)

    val uid = varchar("uid", 50)

    val x = integer("x")
    val y = integer("y")
    val direction = varchar("direction", 20)
    val palette = text("palette").nullable()

    override val primaryKey = PrimaryKey(configurationId, uid)
}