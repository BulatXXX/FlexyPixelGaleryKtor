package configurations.gallery.models.subscribe_request

import java.util.*

sealed interface SubscribeResult {
    data class Success(val publicId: UUID) : SubscribeResult
    data object NotFound: SubscribeResult
    data object IsNotPublic : SubscribeResult
    data object DatabaseError : SubscribeResult
}