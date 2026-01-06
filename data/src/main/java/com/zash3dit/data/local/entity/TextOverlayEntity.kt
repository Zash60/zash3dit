package com.zash3dit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "text_overlays",
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
data class TextOverlayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val text: String,
    val startTime: Long,
    val duration: Long,
    val position: Int,
    val x: Float,
    val y: Float,
    val fontSize: Int,
    val color: String
)