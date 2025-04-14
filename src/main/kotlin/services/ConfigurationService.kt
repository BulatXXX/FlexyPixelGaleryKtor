package com.flexypixelgalleryapi.services

import com.flexypixelgalleryapi.models.configuration.*
import com.flexypixelgalleryapi.repositories.ConfigurationRepository
import java.util.UUID

class ConfigurationService(private val configurationRepository: ConfigurationRepository) {

    fun createConfiguration(
        ownerId: Int,
        name: String,
        description: String,
    ): UUID {
        val configurationPublicId = UUID.randomUUID()
        configurationRepository.createConfiguration(ownerId, configurationPublicId, name, description)
        return configurationPublicId
    }

    fun updateFrame(publicId: UUID, frameIndex: Int, newFrameJson: String,requesterId: Int): Boolean {
        return configurationRepository.updateFrameByPublicId(publicId, frameIndex, newFrameJson,requesterId)
    }

    fun createFullConfiguration(
        ownerId: Int,
        requestData: CreateConfigurationData
    ): UUID {
        val newPublicId = UUID.randomUUID()
        configurationRepository.createFullConfiguration(
            ownerId, newPublicId, requestData
        )
        return newPublicId
    }

    fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>,
        requesterId: Int
    ): Boolean {
        return configurationRepository.updatePanelsAndFrames(publicId, panels, frames,requesterId)
    }

    fun updateConfiguration(
        publicId: UUID,
        name: String?,
        description: String?,
        requesterId: Int
    ): Boolean {
        return configurationRepository.updateConfiguration(publicId, name, description,requesterId)
    }

    fun deleteConfiguration(publicId: UUID,requesterId: Int,): Boolean {
        return configurationRepository.deleteConfiguration(publicId,requesterId)
    }

    fun getFullConfiguration(publicId: UUID, requesterId: Int): ConfigurationForEditorFullData? {
        val config = configurationRepository.getFullConfiguration(publicId,requesterId) ?: return null
        return config
    }
    fun getConfigurationSummary(ownerId: Int): List<ConfigurationSummaryResponse> {
        val summary = configurationRepository.getConfigurationsByOwner(ownerId)
        return summary
    }
}
