package configurations.gallery.repositories

import app.entities.*
import configurations.common.models.AuthorInfo
import configurations.gallery.models.search_request.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class SearchRepositoryImpl : SearchRepository {
    override fun searchGallery(filters: SearchFilters): List<GalleryCardResponse> = transaction {

        var query = LEDPanelsConfiguration
            .leftJoin(
                LEDPanelsConfigurationMetadata,
                { LEDPanelsConfiguration.id },
                { LEDPanelsConfigurationMetadata.configurationId })
            .leftJoin(ConfigurationTags, { LEDPanelsConfiguration.id }, { ConfigurationTags.configurationId })
            .leftJoin(Tags, { ConfigurationTags.tagId }, { Tags.id })
            .selectAll().where { (LEDPanelsConfiguration.isPublic eq true) and (LEDPanelsConfiguration.isBanned eq false)}
            .withDistinct()

        if (!filters.searchQuery.isNullOrBlank()) {
            val words = filters.searchQuery
                .trim()
                .split("\\s+".toRegex())
                .filter { it.length >= 2 }

            words.forEach { word ->
                val pattern = "%${word.lowercase()}%"
                query = query.andWhere {
                    (LEDPanelsConfiguration.name.lowerCase() like pattern) or
                            (LEDPanelsConfiguration.description.lowerCase() like pattern) or
                            (Tags.name.lowerCase() like pattern)
                }
            }
        }

        if (filters.tagFilterIds.isNotEmpty()) {
            when (filters.tagMatchMode) {
                TagMatchMode.ANY -> {
                    query = query.andWhere {
                        exists(
                            ConfigurationTags
                                .selectAll()
                                .where {
                                    (ConfigurationTags.configurationId eq LEDPanelsConfiguration.id) and
                                            (ConfigurationTags.tagId inList filters.tagFilterIds)
                                }
                        )
                    }
                }

                TagMatchMode.ALL -> {
                    throw UnsupportedOperationException("TagMatchMode.ALL not supported yet")
                }
            }
        }

        query = query.applyRangeFilter(LEDPanelsConfigurationMetadata.publishedAt, filters.publishedAtRange)
        query = query.applyRangeFilter(LEDPanelsConfigurationMetadata.averageRating, filters.ratingRange)
        query = query.applyRangeFilter(LEDPanelsConfigurationMetadata.addedCount, filters.addedCountRange)


        query = when (val sort = filters.sortBy) {
            is SortBy.PublishedAt -> query.orderBy(
                LEDPanelsConfigurationMetadata.publishedAt to sort.toExposedSortOrder()
            )

            is SortBy.AverageRating -> query.orderBy(
                LEDPanelsConfigurationMetadata.averageRating to sort.toExposedSortOrder()
            )

            is SortBy.AddedCount -> query.orderBy(
                LEDPanelsConfigurationMetadata.addedCount to sort.toExposedSortOrder()
            )

            else -> query // на всякий случай, но все случаи покрыты
        }

//        query = query.offset(filters.offset).limit(filters.size)


        val ownerIds = query.map { it[LEDPanelsConfiguration.ownerId] }.toSet()

        val owners = User
            .selectAll().where { User.id inList ownerIds }
            .associateBy { it[User.id] }

        val configurationIds = query.map { it[LEDPanelsConfiguration.id] }.toSet()

        val tagsByConfiguration = ConfigurationTags
            .innerJoin(Tags, { ConfigurationTags.tagId }, { Tags.id })
            .selectAll().where { ConfigurationTags.configurationId inList configurationIds }
            .groupBy(
                keySelector = { it[ConfigurationTags.configurationId] },
                valueTransform = { it[Tags.name] }
            )

        query.distinctBy { it[LEDPanelsConfiguration.id] }.map { row ->
            val configId = row[LEDPanelsConfiguration.id]
            val ownerId = row[LEDPanelsConfiguration.ownerId]
            val ownerRow = owners[ownerId]

            GalleryCardResponse(
                publicId = row[LEDPanelsConfiguration.publicId],
                name = row[LEDPanelsConfiguration.name],
                description = row[LEDPanelsConfiguration.description],
                previewImageUrl = row[LEDPanelsConfiguration.previewImageUrl],
                author = ownerRow?.let {
                    AuthorInfo(
                        publicId = it[User.publicId],
                        username = it[User.username],
                        displayName = it[User.displayName],
                        avatarUrl = it[User.avatarUrl]
                    )
                } ?: AuthorInfo(
                    publicId = UUID.fromString("49baa434-73ae-45f2-b34b-74c51ce40dda"),
                    username = "Undefined",
                    displayName = "Undefined",
                    avatarUrl = "noURL"
                ),
                tags = tagsByConfiguration[configId] ?: emptyList(),
                createdAt = row[LEDPanelsConfiguration.createdAt],
                publishedAt = row[LEDPanelsConfigurationMetadata.publishedAt] ?: row[LEDPanelsConfiguration.createdAt],
                averageRating = row[LEDPanelsConfigurationMetadata.averageRating] ?: 0.0,
                addedCount = row[LEDPanelsConfigurationMetadata.addedCount]
            )
        } .drop(filters.offset.toInt())
            .take(filters.size)

    }

    private fun <T : Comparable<T>> Query.applyRangeFilter(
        column: Column<out T?>,
        range: RangeFilter<T>?
    ): Query {
        var result = this
        range?.from?.let { from ->
            // Exposed позволяет сравнивать nullable-колонки с non-null значением
            result = result.andWhere { column greaterEq from }
        }
        range?.to?.let { to ->
            result = result.andWhere { column lessEq to }
        }
        return result
    }


}