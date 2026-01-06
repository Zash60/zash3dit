package com.zash3dit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "video_clips",
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
data class VideoClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val filePath: String,
    val startTime: Long,
    val duration: Long,
    val position: Int,
    val trimStart: Long = 0,
    val trimEnd: Long = 0,
    val filter: String = "NONE",
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val playbackSpeed: Float = 1f,
    val transitionType: String = "NONE",
    val transitionDuration: Long = 1000L
)