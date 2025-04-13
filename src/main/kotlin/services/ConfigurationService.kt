package com.flexypixelgalleryapi.services

import com.flexypixelgalleryapi.models.ConfigurationFullData
import com.flexypixelgalleryapi.models.configuration.CreateConfigurationData
import com.flexypixelgalleryapi.models.configuration.FrameData
import com.flexypixelgalleryapi.models.configuration.PanelData
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

    fun updateFrame(publicId: UUID, frameIndex: Int, newFrameJson: String): Boolean {
        return configurationRepository.updateFrameByPublicId(publicId, frameIndex, newFrameJson)
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
        frames: List<FrameData>
    ): Boolean {
        return configurationRepository.updatePanelsAndFrames(publicId, panels, frames)
    }

    fun updateConfiguration(
        publicId: UUID,
        name: String?,
        description: String?,
    ): Boolean {
        return configurationRepository.updateConfiguration(publicId, name, description)
    }

    fun deleteConfiguration(publicId: UUID): Boolean {
        return configurationRepository.deleteConfiguration(publicId)
    }

    fun getFullConfiguration(publicId: UUID): ConfigurationFullData? {
        return configurationRepository.getFullConfiguration(publicId)
    }
}
