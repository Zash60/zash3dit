package com.zash3dit.domain.repository

import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.TextOverlay
import kotlinx.coroutines.flow.Flow

interface EditProjectRepository {
    fun getAllProjects(): Flow<List<EditProject>>
    suspend fun getProjectById(id: Long): EditProject?
    suspend fun insertProject(project: EditProject): Long
    suspend fun updateProject(project: EditProject)
    suspend fun deleteProject(id: Long)

    // Text overlay methods
    suspend fun addTextOverlay(overlay: TextOverlay): Long
    suspend fun updateTextOverlay(overlay: TextOverlay)
    suspend fun deleteTextOverlay(overlayId: Long)
    fun getTextOverlaysForProject(projectId: Long): Flow<List<TextOverlay>>
}