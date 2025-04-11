package com.flexypixelgalleryapi.services

import com.flexypixelgalleryapi.models.ConfigurationFullData
import com.flexypixelgalleryapi.models.configuration.FrameData
import com.flexypixelgalleryapi.models.configuration.PanelData
import com.flexypixelgalleryapi.repositories.ConfigurationRepository
import java.util.UUID

class ConfigurationService(private val configurationRepository: ConfigurationRepository) {

    fun createConfiguration(
        ownerId: Int,
        name: String,
        description: String,
        previewImageUrl: String? = null
    ): UUID {
        val newPublicId = UUID.randomUUID()
        configurationRepository.createConfiguration(ownerId, newPublicId, name, description, previewImageUrl)
        return newPublicId
    }

    fun updateFrame(publicId: UUID, frameIndex: Int, newFrameJson: String): Boolean {
        return configurationRepository.updateFrameByPublicId(publicId, frameIndex, newFrameJson)
    }

    fun createFullConfiguration(
        ownerId: Int,
        name: String,
        description: String,
        previewImageUrl: String? = null,
        panels: List<PanelData>,
        frames: List<FrameData>
    ): UUID {
        val newPublicId = UUID.randomUUID()
        configurationRepository.createFullConfiguration(
            ownerId, newPublicId, name, description, previewImageUrl, panels, frames
        )
        return newPublicId
    }

    fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>
    ): Boolean {
        return configurationRepository.updatePanelsAndFrames(publicId, panels, frames)
    }

    fun updateConfiguration(
        publicId: UUID,
        name: String?,
        description: String?,
        previewImageUrl: String?
    ): Boolean {
        return configurationRepository.updateConfiguration(publicId, name, description, previewImageUrl)
    }

    fun deleteConfiguration(publicId: UUID): Boolean {
        return configurationRepository.deleteConfiguration(publicId)
    }

    fun getFullConfiguration(publicId: UUID): ConfigurationFullData? {
        return configurationRepository.getFullConfiguration(publicId)
    }
}
