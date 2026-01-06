package com.zash3dit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zash3dit.data.local.entity.TextOverlayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TextOverlayDao {
    @Query("SELECT * FROM text_overlays WHERE projectId = :projectId ORDER BY position")
    fun getOverlaysForProject(projectId: Long): Flow<List<TextOverlayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOverlay(overlay: TextOverlayEntity): Long

    @Update
    suspend fun updateOverlay(overlay: TextOverlayEntity)

    @Delete
    suspend fun deleteOverlay(overlay: TextOverlayEntity)

    @Query("DELETE FROM text_overlays WHERE projectId = :projectId")
    suspend fun deleteOverlaysForProject(projectId: Long)

    @Query("DELETE FROM text_overlays WHERE id = :overlayId")
    suspend fun deleteOverlayById(overlayId: Long)
}