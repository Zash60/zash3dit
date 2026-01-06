package com.zash3dit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zash3dit.data.local.entity.EditProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditProjectDao {
    @Query("SELECT * FROM edit_projects")
    fun getAllProjects(): Flow<List<EditProjectEntity>>

    @Query("SELECT * FROM edit_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): EditProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: EditProjectEntity): Long

    @Update
    suspend fun updateProject(project: EditProjectEntity)

    @Delete
    suspend fun deleteProject(project: EditProjectEntity)
}