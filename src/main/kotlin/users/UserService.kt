package users

import app.entities.User.email
import users.models.get_request.GetResult
import users.models.get_request.UserResponse
import users.models.update_request.UpdateRequest
import users.models.update_request.UpdateResult
import users.repositories.UserRepository
import java.io.File
import java.io.IOException
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
        userRepository.updateUser(userId,request) -> UpdateResult.Success
        userRepository.findById(userId) != null -> UpdateResult.DatabaseError
        else -> UpdateResult.NotFound
    }

//    fun replaceAvatar(userId: Int, originalName: String, bytes: ByteArray): Result<String> {
//        // 1) Проверка расширения
//        val ext = originalName.substringAfterLast('.').lowercase()
//        if (ext !in listOf("jpg","jpeg","png","gif")) {
//            return Result.failure(IllegalArgumentException("Invalid file type"))
//        }
//        // 2) Удаляем старый аватар из FS (если есть)
//        userRepository.getAvatarUrl(userId)
//            ?.substringAfterLast("/uploads/avatars/")
//            ?.let { oldFile ->
//                File(uploadDir, oldFile).takeIf { it.exists() }?.delete()
//            }
//
//        // 3) Сохраняем новый файл
//        uploadDir.mkdirs()
//        val newFileName = "$userId.$ext"
//        val target = File(uploadDir, newFileName)
//        try {
//            target.writeBytes(bytes)
//        } catch(e: IOException) {
//            return Result.failure(e)
//        }
//
//        // 4) Обновляем БД
//        val publicUrl = "$uploadBaseUrl/$newFileName"
//        val updated = userRepository.updateAvatarUrl(userId, publicUrl)
//        if (!updated) {
//            return Result.failure(IllegalStateException("Failed to update database"))
//        }
//        return Result.success(publicUrl)
//    }
fun replaceAvatar(userId: Int, originalName: String, bytes: ByteArray): Result<String> {
    val ext = originalName.substringAfterLast('.').lowercase()
    if (ext !in listOf("jpg","jpeg","png","gif")) {
        return Result.failure(IllegalArgumentException("Invalid file type"))
    }

    // если gif — прогоняем через ImageMagick, чтобы установить loop=1
    val processedBytes = if (ext == "gif") {
        try {
            // Конверт из stdin в stdout, ставим -loop 1 (один проигрыш), -coalesce для правильной сборки
            val pb = ProcessBuilder("convert", "gif:-", "-coalesce", "-loop", "1", "gif:-")
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()
            pb.outputStream.use { it.write(bytes) }
            val out = pb.inputStream.readBytes()
            pb.waitFor()
            out
        } catch(e: Exception) {
            return Result.failure(IOException("Failed to process GIF loop: ${e.message}", e))
        }
    } else {
        bytes
    }

    // далее — удаляем старый, сохраняем новый, обновляем БД точно так же, но с processedBytes
    userRepository.getAvatarUrl(userId)
        ?.substringAfterLast("/uploads/avatars/")
        ?.let { old ->
            File(uploadDir, old).takeIf { it.exists() }?.delete()
        }

    uploadDir.mkdirs()
    val newFileName = "$userId.$ext"
    val target = File(uploadDir, newFileName)
    try {
        target.writeBytes(processedBytes)
    } catch(e: IOException) {
        return Result.failure(e)
    }

    val publicUrl = "$uploadBaseUrl/$newFileName"
    if (!userRepository.updateAvatarUrl(userId, publicUrl)) {
        return Result.failure(IllegalStateException("Failed to update database"))
    }
    return Result.success(publicUrl)
}


}