package com.flexypixelgalleryapi.configurations.userLibrary.models

import com.flexypixelgalleryapi.app.entities.ForkStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*


@Serializable
data class AuthorInfo(
    @Contextual val publicId: UUID,
    val username: String,
    val avatarUrl: String? = null,
    val displayName: String
)

@Serializable
data class ForkInfo(
    @Contextual val sourceConfigurationPublicId: UUID,
    val author: AuthorInfo
)

@Serializable
data class ConfigurationSummaryResponse(
    @Contextual val publicId: UUID,
    val name: String,
    val description: String,
    val previewImageUrl: String? = null,
    @Contextual val createdAt: LocalDateTime,
    @Contextual val updatedAt: LocalDateTime,
    val forkStatus: ForkStatus,
    val forkInfo: ForkInfo? = null
)
