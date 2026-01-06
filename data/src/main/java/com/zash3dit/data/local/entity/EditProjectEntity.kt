package com.zash3dit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "edit_projects",
    indices = [Index("modifiedAt")]
)
data class EditProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val modifiedAt: Long,
    val resolution: String,
    val frameRate: Int
)