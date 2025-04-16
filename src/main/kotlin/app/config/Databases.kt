package com.flexypixelgalleryapi.app.config

import app.entities.ConfigurationTags
import app.entities.Rating
import app.entities.Tags
import com.flexypixelgalleryapi.app.entities.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureDatabases() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/flexydb",
        driver = "org.postgresql.Driver",
        user = "flexyuser",
        password = "flexapipass2025"
    )
    transaction {
        //SchemaUtils.drop(User, LEDPanelsConfiguration, LEDPanelsConfigurationMetadata, Tags, ConfigurationTags, Panel, Frame)

        SchemaUtils.create(
            User,
            LEDPanelsConfiguration,
            LEDPanelsConfigurationMetadata,
            Tags,
            ConfigurationTags,
            Panel,
            Frame,
            Rating
        )
    }
}
