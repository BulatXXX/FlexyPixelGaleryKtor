import app.module
import com.typesafe.config.ConfigFactory
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.test.*

@Serializable
data class RegisterRequest(
    val email: String,
    val login: String,
    val displayName: String,
    val password: String,
    val phone: String? = null
)

object Users : Table("users") {
    val id    = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val login = varchar("login", 100)
    val hash  = varchar("password_hash", 255)
    override val primaryKey = PrimaryKey(id)
}

class UserRegistrationTest {
    @BeforeTest
    fun setupDb() {
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        transaction {
            SchemaUtils.create(Users)
        }

    }

    @Test
    fun `POST register should create user in DB`() = testApplication {
        environment {
            config = HoconApplicationConfig(
                ConfigFactory.parseResources("application-test.conf").resolve()
            )
        }
        application {
            module()  // ваш Ktor-модуль с routing & DI
        }

        val client = createClient {
            install(ContentNegotiation) {
                json() // подключаем kotlinx.serialization
            }
        }

        val req = RegisterRequest("flex@example.com", "flex", "flex","secret")
        val response: HttpResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(req))
        }
        assertEquals(HttpStatusCode.Created, response.status)

        transaction {
            val row = Users.selectAll().where { Users.email eq "flex@example.com" }.singleOrNull()
            assertNotNull(row, "User must be present in DB after registration")
            assertEquals("flex", row[Users.login])
            rollback()
        }
    }
    @AfterTest
    fun teardownDb() {

        transaction {
            Users.deleteWhere { Op.TRUE }
        }
    }
}
