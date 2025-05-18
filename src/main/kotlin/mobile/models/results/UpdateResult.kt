package mobile.models.results

sealed interface UpdateResult {
    data object Success : UpdateResult
    data object NotFound : UpdateResult
    data object Forbidden : UpdateResult
    data object NoConfigId : UpdateResult
    data object UpdateError : UpdateResult
}
