package com.flexypixelgalleryapi.repositories

import com.flexypixelgalleryapi.models.configuration.*
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




