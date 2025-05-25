package users.repositories

import app.entities.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import users.models.get_request.UserResponse
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import users.models.get_request.PublicUserResponse
import users.models.update_request.UpdateRequest
import java.time.LocalDateTime
import java.util.*

class UserRepositoryImpl : UserRepository {


    override fun exist(email: String, login: String): Boolean {
        return transaction {
            User.selectAll().where {
                (User.email eq email) or (User.login eq login)
            }.count() > 0
        }
    }


    override fun findByPublicId(publicId: UUID): PublicUserResponse? = transaction {
        User.selectAll().where { User.publicId eq publicId }
            .singleOrNull()?.let {
                toPublicUserResponse(it)
            }
    }

    private fun toPublicUserResponse(row: ResultRow) =
        PublicUserResponse(
            publicId = row[User.publicId],
            username = row[User.username],
            displayName = row[User.displayName],
            avatarUrl = row[User.avatarUrl],
            bio = row[User.bio],
            createdAt = row[User.createdAt],
        )


    override fun findById(userId: Int): UserResponse? = transaction {
        User.selectAll().where {
            User.id eq userId
        }.singleOrNull()?.let {
            toUserResponse(it)
        }
    }

    override fun updateUser(userId: Int, request: UpdateRequest): Boolean = transaction {
        User.update({ User.id eq userId }) { user ->
            request.email?.also { user[email] = it }
            request.username?.also { user[username] = it }
            request.displayName?.also { user[displayName] = it }
            request.phone?.also { user[phone] = it }
            request.avatarUrl?.also { user[avatarUrl] = it }
            request.bio?.also { user[bio] = it }
            user[updatedAt] = LocalDateTime.now()
        } > 0
    }

    private fun toUserResponse(row: ResultRow) =
        UserResponse(
            publicId = row[User.publicId],
            email = row[User.email],
            username = row[User.username],
            displayName = row[User.displayName],
            phone = row[User.phone],
            avatarUrl = row[User.avatarUrl],
            bio = row[User.bio],
            isVerified = row[User.isVerified],
            role = row[User.role],
            mobileRole = row[User.mobileRole],
            createdAt = row[User.createdAt]
        )

    override fun getAvatarUrl(userId: Int): String? = transaction {
        User.selectAll().where { User.id eq userId }
            .singleOrNull()
            ?.get(User.avatarUrl)
    }

    override fun getAvatarUrl(loginOrEmail: String): String? = transaction {
        val user = User.selectAll().where{
            (User.login eq loginOrEmail) or (User.email eq loginOrEmail)
        }.singleOrNull() ?: return@transaction null
        return@transaction user[User.avatarUrl]
    }

    override fun updateAvatarUrl(userId: Int, avatarUrl: String): Boolean = transaction {
        User.update({ User.id eq userId }) {
            it[User.avatarUrl] = avatarUrl
            it[User.updatedAt] = LocalDateTime.now()
        } > 0
    }

    override fun getUserId(email: String): Int? = transaction {
        val row = User.selectAll().where {
            User.email eq email
        }.singleOrNull() ?: return@transaction null
        row[User.id]
    }
}