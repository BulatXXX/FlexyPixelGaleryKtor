package users

import app.entities.User.email
import users.models.get_request.GetResult
import users.models.get_request.UserResponse
import users.models.update_request.UpdateRequest
import users.models.update_request.UpdateResult
import users.repositories.UserRepository
import java.util.*

class UserService(private val userRepository: UserRepository) {

    fun getByPublicId(id: UUID): GetResult {
        val userResponse = userRepository.findByPublicId(id) ?: return GetResult.NotFound
        return GetResult.SuccessPublic(userResponse)
    }

    fun getUserById(userId: Int): GetResult {
        val userResponse = userRepository.findById(userId) ?: return GetResult.NotFound
        return GetResult.SuccessFull(userResponse)
    }

    fun updateUserInfo(userId: Int, request: UpdateRequest) = when {
        userRepository.updateUser(userId,request) -> UpdateResult.Success
        userRepository.findById(userId) != null -> UpdateResult.DatabaseError
        else -> UpdateResult.NotFound
    }

}