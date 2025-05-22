package auth.repositories

import auth.models.login_request.LoginCredentials
import auth.models.register_request.RegisterRequest
import app.entities.MobileRole
import app.entities.User
import app.entities.UserRole
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class AuthRepositoryImpl : AuthRepository {

    override fun exists(email:String,login:String): Boolean = transaction {
        User.selectAll().where {
            (User.email eq email) or (User.login eq login)
        }.count() > 0
    }

    override fun registerUser(
        publicId: UUID,
        request: RegisterRequest,
        hashedPassword: String,
        role: UserRole
    ): Boolean = transaction {
        User.insert {
            it[User.publicId] = publicId
            it[User.email] = request.email
            it[User.login] = request.login
            it[User.username] = generateUsername(request.displayName)
            it[User.displayName] = request.displayName
            it[User.passwordHash] = hashedPassword
            it[User.phone] = request.phone
            it[User.role] = role
            it[User.mobileRole] = MobileRole.USER
            it[User.isVerified] = false
            it[User.createdAt] = LocalDateTime.now()
            it[User.updatedAt] = LocalDateTime.now()
        }.insertedCount > 0
    }

    override fun findByLoginOrEmail(loginOrEmail: String): LoginCredentials? =
        transaction {
            User.selectAll().where {
                (User.email eq loginOrEmail) or (User.login eq loginOrEmail)
            }.map {
                LoginCredentials(
                    id = it[User.id],
                    publicId = it[User.publicId],
                    passwordHash = it[User.passwordHash],
                    role = it[User.role],
                )
            }.singleOrNull()
        }

    private fun generateUsername(displayName: String): String {
        val base = displayName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(20)

        var username = base
        var suffix = 1

        transaction {
            while (!User.selectAll().where { User.username eq username }.empty()) {
                username = "$base$suffix"
                suffix++
            }
        }
        return username
    }

}