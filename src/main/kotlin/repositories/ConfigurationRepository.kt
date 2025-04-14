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

    fun updateConfiguration(publicId: UUID, name: String?, description: String?,): Boolean

    fun updatePanelsAndFrames(
        publicId: UUID,
        panels: List<PanelData>,
        frames: List<FrameData>
    ): Boolean

    fun deleteConfiguration(publicId: UUID): Boolean

    fun getFullConfiguration(publicId: UUID): ConfigurationFullData?

    fun updateFrameByPublicId(
        publicId: UUID,
        frameIndex: Int,
        newFrameJson: String
    ): Boolean
}



