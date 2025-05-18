package mobile.models.results

import java.util.*

sealed interface PostResult {
    data class Success(val publicId: UUID): PostResult
    data object Failure: PostResult
}