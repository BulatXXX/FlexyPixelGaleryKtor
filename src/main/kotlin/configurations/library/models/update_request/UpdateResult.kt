package configurations.library.models.update_request


sealed interface UpdateResult {
    data class Success(val message: String) : UpdateResult
    data class ValidationError(val message: String) : UpdateResult
    data object NotFound : UpdateResult
    data object Forbidden : UpdateResult
    data object DatabaseError : UpdateResult
}