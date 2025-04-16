package com.flexypixelgalleryapi.configurations.userLibrary.repositories

import com.flexypixelgalleryapi.configurations.userLibrary.models.ConfigurationForEditorFullData
import com.flexypixelgalleryapi.configurations.userLibrary.models.ConfigurationSummaryResponse
import com.flexypixelgalleryapi.configurations.userLibrary.models.CreateConfigurationData
import com.flexypixelgalleryapi.configurations.userLibrary.models.FrameData
import com.flexypixelgalleryapi.configurations.userLibrary.models.PanelData
import java.util.*



interface ConfigurationRepository{
    fun createConfiguration(
        ownerId: Int,
        publicId: UUID,
        name: String,
        description: String,
    )

    fun createFullConfiguration(
        ownerId: Int,
        publicId: UUID,
        requestData: CreateConfigurationData
    )

    fun updateConfiguration(publicId: UUID, name: String?, description: String?,requesterId: Int): Boolean

    fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>,
        requesterId: Int
    ): Boolean

    fun deleteConfiguration(publicId: UUID,requesterId: Int): Boolean

    fun getFullConfiguration(publicId: UUID,requesterId: Int): ConfigurationForEditorFullData?

    fun updateFrameByPublicId(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String,
        requesterId: Int
    ): Boolean

    fun getConfigurationsByOwner(ownerId: Int): List<ConfigurationSummaryResponse>



}




