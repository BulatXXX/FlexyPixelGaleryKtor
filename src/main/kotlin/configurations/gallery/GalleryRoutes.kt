package configurations.gallery

import app.requireParam
import app.requireUserId
import configurations.gallery.models.publish_request.PublishRequest
import configurations.gallery.models.publish_request.PublishResult
import configurations.gallery.models.search_request.*
import configurations.gallery.models.subscribe_request.SubscribeResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*


suspend fun ApplicationCall.respondPublish(result: PublishResult) = when (result) {
    is PublishResult.Success -> respond(HttpStatusCode.OK, result.publishResponse)
    is PublishResult.AlreadyPublished -> respond(
        HttpStatusCode.Conflict,
        mapOf("error" to "Configuration is already published")
    )

    is PublishResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "Configuration is not found"))
    is PublishResult.Forbidden -> respond(
        HttpStatusCode.Forbidden,
        mapOf("error" to "User is not the owner of configuration")
    )

    is PublishResult.DatabaseError -> respond(
        HttpStatusCode.InternalServerError,
        mapOf("error" to "Internal Server Error")
    )
}

suspend fun ApplicationCall.respondSubscribe(result: SubscribeResult) = when (result) {
    is SubscribeResult.Success -> respond(HttpStatusCode.OK, mapOf("publicId" to result.publicId))
    is SubscribeResult.IsNotPublic -> respond(
        HttpStatusCode.Forbidden,
        mapOf("error" to "Configuration is not published in gallery")
    )

    is SubscribeResult.NotFound -> respond(HttpStatusCode.NotFound, mapOf("error" to "Configuration is not found"))
    is SubscribeResult.DatabaseError -> respond(HttpStatusCode.InternalServerError, mapOf("error" to "Database Error"))
}

fun Route.galleryRoutes() {
    val galleryService by inject<GalleryService>()
    authenticate("auth-jwt") {
        route("/gallery") {
            post("/{publicId}/publish") {
                val request = call.receive<PublishRequest>()
                val requesterId = call.requireUserId() ?: return@post
                val configId = call.requireParam("publicId") { UUID.fromString(it) } ?: return@post

                val result = galleryService.publishConfiguration(request, requesterId, configId)
                call.respondPublish(result)
            }
            post("/{publicId}/subscribe") {
                val requesterId = call.requireUserId() ?: return@post
                val configId = call.requireParam("publicId") { UUID.fromString(it) } ?: return@post

                val result = galleryService.subscribeConfiguration(requesterId, configId)
                call.respondSubscribe(result)
            }

            post("/search") {
                val filters = SearchFiltersMapper.parse(call)
                val result = galleryService.searchGallery(filters)
                println(mapOf("size" to result.size))// Передаём в сервис
                call.respond(result)
            }
        }
    }
}


object SearchFiltersMapper {

    suspend fun parse(call: ApplicationCall): SearchFilters {
        val body = call.receive<JsonObject>()

        val searchQuery = body["searchQuery"]?.jsonPrimitive?.contentOrNull.also {
            println("searchQuery = $it")
        }

        val tagFilterIds = body["tagFilterIds"]?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.intOrNull }
            ?.also { println("tagFilterIds = $it") } ?: emptyList()

        val tagMatchMode = body["tagMatchMode"]?.jsonPrimitive?.contentOrNull
            ?.let { TagMatchMode.valueOf(it) }
            ?.also { println("tagMatchMode = $it") }
            ?: TagMatchMode.ANY

        val publishedAtRange = body["publishedAtRange"]
            ?.takeIf { it is JsonObject }
            ?.jsonObject
            ?.let { range ->
                val from = range["from"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.let { OffsetDateTime.parse(it).toLocalDateTime() }

                val to = range["to"]
                    ?.jsonPrimitive
                    ?.contentOrNull
                    ?.let { OffsetDateTime.parse(it).toLocalDateTime() }

                RangeFilter(from = from, to = to)
            }

        val ratingRange = body["ratingRange"]
            ?.takeIf { it is JsonObject }
            ?.jsonObject
            ?.let { range ->
                RangeFilter(
                    from = range["from"]?.jsonPrimitive?.doubleOrNull,
                    to = range["to"]?.jsonPrimitive?.doubleOrNull
                ).also { println("ratingRange = $it") }
            }

        val addedCountRange = body["addedCountRange"]
            ?.takeIf { it is JsonObject }
            ?.jsonObject
            ?.let { range ->
                RangeFilter(
                    from = range["from"]?.jsonPrimitive?.intOrNull,
                    to = range["to"]?.jsonPrimitive?.intOrNull
                ).also { println("addedCountRange = $it") }
            }

        val sortBy = body["sortBy"]
            ?.takeIf { it is JsonObject }
            ?.jsonObject
            ?.let { sort ->
                val type = sort["type"]?.jsonPrimitive?.contentOrNull
                val order = sort["order"]?.jsonPrimitive?.contentOrNull
                    ?.let { SortDirection.valueOf(it) }
                    ?: SortDirection.DESC

                when (type?.uppercase()) {
                    "PUBLISHEDAT" -> SortBy.PublishedAt(order)
                    "AVERAGERATING" -> SortBy.AverageRating(order)
                    "ADDEDCOUNT" -> SortBy.AddedCount(order)
                    else -> SortBy.PublishedAt(order)
                }.also { println("sortBy = $it") }
            } ?: SortBy.PublishedAt(SortDirection.DESC)

        val offset = body["offset"]?.jsonPrimitive?.longOrNull
            ?.also { println("offset = $it") } ?: 0L

        val size = body["size"]?.jsonPrimitive?.intOrNull
            ?.also { println("size = $it") } ?: 20

        println("✅ Successfully parsed SearchFilters")

        return SearchFilters(
            searchQuery = searchQuery,
            tagFilterIds = tagFilterIds,
            tagMatchMode = tagMatchMode,
            publishedAtRange = publishedAtRange,
            ratingRange = ratingRange,
            addedCountRange = addedCountRange,
            sortBy = sortBy,
            offset = offset,
            size = size
        )
    }
}
