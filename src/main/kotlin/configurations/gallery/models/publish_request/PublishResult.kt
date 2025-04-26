package configurations.gallery.models.publish_request

sealed interface PublishResult {
    data class Success(val publishResponse: PublishResponse) : PublishResult
    data object AlreadyPublished : PublishResult
    data object NotFound : PublishResult
    data object Forbidden : PublishResult
    data object DatabaseError : PublishResult
}