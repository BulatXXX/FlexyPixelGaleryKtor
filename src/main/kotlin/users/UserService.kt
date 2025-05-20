package users
import com.madgag.gif.fmsware.AnimatedGifEncoder
import com.madgag.gif.fmsware.GifDecoder
import app.entities.User.email
import users.models.get_request.GetResult
import users.models.get_request.UserResponse
import users.models.update_request.UpdateRequest
import users.models.update_request.UpdateResult
import users.repositories.UserRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

class UserService(private val userRepository: UserRepository,
                  private val uploadBaseUrl: String,           // например, "http://localhost:8080/uploads/avatars"
                  private val uploadDir: File
) {

    fun getByPublicId(id: UUID): GetResult {
        val userResponse = userRepository.findByPublicId(id) ?: return GetResult.NotFound
        return GetResult.SuccessPublic(userResponse)
    }

    fun getUserById(userId: Int): GetResult {
        val userResponse = userRepository.findById(userId) ?: return GetResult.NotFound
        return GetResult.SuccessFull(userResponse)
    }

    fun updateUserInfo(userId: Int, request: UpdateRequest) = when {
        userRepository.updateUser(userId, request) -> UpdateResult.Success
        userRepository.findById(userId) != null -> UpdateResult.DatabaseError
        else -> UpdateResult.NotFound
    }

    fun replaceAvatar(userId: Int, originalName: String, bytes: ByteArray): Result<String> {
        // 1) Проверка расширения
        val ext = originalName.substringAfterLast('.').lowercase()
        if (ext !in listOf("jpg","jpeg","png","gif")) {
            return Result.failure(IllegalArgumentException("Invalid file type"))
        }
        // 2) Удаляем старый аватар из FS (если есть)
        userRepository.getAvatarUrl(userId)
            ?.substringAfterLast("/uploads/avatars/")
            ?.let { oldFile ->
                File(uploadDir, oldFile).takeIf { it.exists() }?.delete()
            }

        // 3) Сохраняем новый файл
        uploadDir.mkdirs()
        val newFileName = "$userId${LocalDateTime.now()}.$ext"
        val target = File(uploadDir, newFileName)
        try {
            target.writeBytes(bytes)
        } catch(e: IOException) {
            return Result.failure(e)
        }

        // 4) Обновляем БД
        val publicUrl = "$uploadBaseUrl/$newFileName"
        val updated = userRepository.updateAvatarUrl(userId, publicUrl)
        if (!updated) {
            return Result.failure(IllegalStateException("Failed to update database"))
        }
        return Result.success(publicUrl)
    }
}