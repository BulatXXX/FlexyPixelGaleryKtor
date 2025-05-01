package app.config

import app.entities.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureDatabases() {
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/flexydb"
    val dbUser = System.getenv("DATABASE_USER") ?: "flexyuser"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "flexapipass2025"

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
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
