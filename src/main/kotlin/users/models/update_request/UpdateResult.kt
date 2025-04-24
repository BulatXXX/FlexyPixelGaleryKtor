package users.models.update_request

sealed interface UpdateResult {
    data object Success : UpdateResult
    data class ValidationError(val message: String) : UpdateResult
    data object NotFound : UpdateResult
    data object DatabaseError : UpdateResult
}