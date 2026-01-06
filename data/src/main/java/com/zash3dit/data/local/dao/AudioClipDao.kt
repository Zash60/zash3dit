package com.zash3dit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zash3dit.data.local.entity.AudioClipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioClipDao {
    @Query("SELECT * FROM audio_clips WHERE projectId = :projectId ORDER BY position")
    fun getClipsForProject(projectId: Long): Flow<List<AudioClipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: AudioClipEntity): Long

    @Update
    suspend fun updateClip(clip: AudioClipEntity)

    @Delete
    suspend fun deleteClip(clip: AudioClipEntity)

    @Query("DELETE FROM audio_clips WHERE projectId = :projectId")
    suspend fun deleteClipsForProject(projectId: Long)
}