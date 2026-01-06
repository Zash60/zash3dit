package com.zash3dit.data.repository

import com.zash3dit.data.local.AppDatabase
import com.zash3dit.data.mapper.toDomain
import com.zash3dit.data.mapper.toEntity
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.TextOverlay
import com.zash3dit.domain.repository.EditProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EditProjectRepositoryImpl(
    private val database: AppDatabase
) : EditProjectRepository {

    private val projectDao = database.editProjectDao()
    private val videoClipDao = database.videoClipDao()
    private val audioClipDao = database.audioClipDao()
    private val textOverlayDao = database.textOverlayDao()

    // Simple in-memory cache for frequently accessed projects
    private val projectCache = mutableMapOf<Long, Pair<EditProject, Long>>()
    private val cacheMutex = Mutex()
    private val cacheExpiryMs = 5 * 60 * 1000L // 5 minutes

    override fun getAllProjects(): Flow<List<EditProject>> {
        val projectsFlow = projectDao.getAllProjects()
        val videoClipsFlow = projectsFlow.map { projects ->
            projects.associate { project ->
                project.id to videoClipDao.getClipsForProject(project.id).first()
            }
        }
        val audioClipsFlow = projectsFlow.map { projects ->
            projects.associate { project ->
                project.id to audioClipDao.getClipsForProject(project.id).first()
            }
        }
        val textOverlaysFlow = projectsFlow.map { projects ->
            projects.associate { project ->
                project.id to textOverlayDao.getOverlaysForProject(project.id).first()
            }
        }

        return combine(projectsFlow, videoClipsFlow, audioClipsFlow, textOverlaysFlow) { projects, videoMap, audioMap, textMap ->
            projects.map { project ->
                project.toDomain(
                    videoClips = videoMap[project.id] ?: emptyList(),
                    audioClips = audioMap[project.id] ?: emptyList(),
                    textOverlays = textMap[project.id] ?: emptyList()
                )
            }
        }
    }

    override suspend fun getProjectById(id: Long): EditProject? {
        val currentTime = System.currentTimeMillis()

        // Check cache first
        cacheMutex.withLock {
            val cached = projectCache[id]
            if (cached != null && (currentTime - cached.second) < cacheExpiryMs) {
                return cached.first
            }
        }

        // Fetch from database
        val projectEntity = projectDao.getProjectById(id) ?: return null
        val videoClips = videoClipDao.getClipsForProject(id).first()
        val audioClips = audioClipDao.getClipsForProject(id).first()
        val textOverlays = textOverlayDao.getOverlaysForProject(id).first()
        val project = projectEntity.toDomain(videoClips, audioClips, textOverlays)

        // Cache the result
        cacheMutex.withLock {
            projectCache[id] = Pair(project, currentTime)
        }

        return project
    }

    override suspend fun insertProject(project: EditProject): Long {
        return database.runInTransaction {
            val projectId = projectDao.insertProject(project.toEntity())
            val updatedProject = project.copy(id = projectId)
            insertClipsForProject(updatedProject)
            projectId
        }
    }

    override suspend fun updateProject(project: EditProject) {
        database.runInTransaction {
            projectDao.updateProject(project.toEntity())
            // Delete existing clips and overlays
            videoClipDao.deleteClipsForProject(project.id)
            audioClipDao.deleteClipsForProject(project.id)
            textOverlayDao.deleteOverlaysForProject(project.id)
            // Insert new ones
            insertClipsForProject(project)
        }
        // Invalidate cache
        cacheMutex.withLock {
            projectCache.remove(project.id)
        }
    }

    override suspend fun deleteProject(id: Long) {
        projectDao.deleteProject(projectDao.getProjectById(id)!!)
        // Invalidate cache
        cacheMutex.withLock {
            projectCache.remove(id)
        }
    }

    private suspend fun insertClipsForProject(project: EditProject) {
        project.videoClips.forEach { clip ->
            videoClipDao.insertClip(clip.copy(projectId = project.id).toEntity())
        }
        project.audioClips.forEach { clip ->
            audioClipDao.insertClip(clip.copy(projectId = project.id).toEntity())
        }
        project.textOverlays.forEach { overlay ->
            textOverlayDao.insertOverlay(overlay.copy(projectId = project.id).toEntity())
        }
    }

    // Text overlay methods
    override suspend fun addTextOverlay(overlay: TextOverlay): Long {
        return textOverlayDao.insertOverlay(overlay.toEntity())
    }

    override suspend fun updateTextOverlay(overlay: TextOverlay) {
        textOverlayDao.updateOverlay(overlay.toEntity())
    }

    override suspend fun deleteTextOverlay(overlayId: Long) {
        textOverlayDao.deleteOverlayById(overlayId)
    }

    override fun getTextOverlaysForProject(projectId: Long): Flow<List<TextOverlay>> {
        return textOverlayDao.getOverlaysForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}