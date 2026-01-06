package com.zash3dit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zash3dit.data.local.entity.VideoClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoClipDao {
    @Query("SELECT * FROM video_clips WHERE projectId = :projectId ORDER BY position")
    fun getClipsForProject(projectId: Long): Flow<List<VideoClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: VideoClipEntity): Long

    @Update
    suspend fun updateClip(clip: VideoClipEntity)

    @Delete
    suspend fun deleteClip(clip: VideoClipEntity)

    @Query("DELETE FROM video_clips WHERE projectId = :projectId")
    suspend fun deleteClipsForProject(projectId: Long)
}