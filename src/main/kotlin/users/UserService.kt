package users

import com.flexypixelgalleryapi.repositories.UserRepository
import java.util.*

class UserService(private val userRepository: UserRepository) {
    fun getByPublicId(id: UUID): UserResponse? {
        val user = userRepository.findByPublicId(id) ?: return null
        return user
    }

    fun getUserIdByPublicId(publicId: UUID): Int? {
        return userRepository.getUserIdByPublicId(publicId = publicId)
    }
}