package configurations.library.models.delete_request

sealed interface DeleteResult {
    data class Success(val message: String) : DeleteResult
    data object NotFound : DeleteResult
    data object Forbidden : DeleteResult
    data object DatabaseError : DeleteResult
}
