package configurations.gallery.models

sealed interface BanResult {
    data object Success : BanResult
    data object Forbidden: BanResult
    data object Failure : BanResult
}