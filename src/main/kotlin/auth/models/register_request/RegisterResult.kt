package auth.models.register_request

import java.util.UUID

sealed interface RegisterResult {
    data class Success(val publicId: UUID) : RegisterResult
    data class ValidationError(val error: String) : RegisterResult
    data object Conflict: RegisterResult
    data object DatabaseError: RegisterResult
}