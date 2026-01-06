package com.zash3dit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_clips",
    foreignKeys = [
        ForeignKey(
            entity = EditProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("projectId"),
        Index("projectId", "position")
    ]
)
data class AudioClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val filePath: String,
    val startTime: Long,
    val duration: Long,
    val position: Int,
    val volume: Float = 1.0f
)