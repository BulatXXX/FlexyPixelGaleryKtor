package users.repositories

import users.models.get_request.PublicUserResponse
import users.models.get_request.UserResponse
import users.models.update_request.UpdateRequest
import java.util.UUID

interface UserRepository {
    fun exist(email: String, login: String): Boolean
    fun findByPublicId(publicId: UUID): PublicUserResponse?
    fun findById(userId: Int): UserResponse?
    fun updateUser(userId: Int, request: UpdateRequest): Boolean

    fun getAvatarUrl(userId: Int): String?
    fun updateAvatarUrl(userId: Int, avatarUrl: String): Boolean

    fun getUserId(email: String): Int?

    fun getAvatarUrl(loginOrEmail: String): String?
}


