package configurations.gallery.models.search_request

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SortOrder
import java.time.LocalDateTime

@Serializable
data class RangeFilter<T : Comparable<T>>(
    val from: T? = null,
    val to: T? = null
)

@Serializable
enum class TagMatchMode {
    ANY,
    ALL
}

@Serializable
enum class SortDirection {
    ASC,
    DESC
}


@Serializable
sealed class SortBy {
    abstract val order: SortDirection

    @Serializable
    @SerialName("PublishedAt")
    data class PublishedAt(override val order: SortDirection) : SortBy()

    @Serializable
    @SerialName("AverageRating")
    data class AverageRating(override val order: SortDirection) : SortBy()

    @Serializable
    @SerialName("AddedCount")
    data class AddedCount(override val order: SortDirection) : SortBy()

    fun toExposedSortOrder(): SortOrder {
        return when (order) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }
    }
}

@Serializable
data class SearchFilters(
    val searchQuery: String? = null,
    val tagFilterIds: List<Int> = emptyList(),
    val tagMatchMode: TagMatchMode = TagMatchMode.ANY,
    val publishedAtRange: RangeFilter<@Contextual LocalDateTime>? = null,
    val ratingRange: RangeFilter<Double>? = null,
    val addedCountRange: RangeFilter<Int>? = null,
    val sortBy: SortBy = SortBy.PublishedAt(SortDirection.DESC),
    val offset: Long = 0,
    val size: Int = 20
)
