package mobile.models.results

sealed interface DeleteResult {
    data object Success : DeleteResult
    data object NotFound : DeleteResult
    data object Forbidden : DeleteResult
    data object DeleteFailed : DeleteResult
}