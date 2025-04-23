package configurations.library.models.create_request

import java.util.*

sealed interface CreateResult {
    data class Success(val publicId: UUID) : CreateResult
    data class ValidationError(val message: String) : CreateResult
    data object DatabaseError : CreateResult
}
