package com.zash3dit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.zash3dit.domain.model.AppError
import com.zash3dit.domain.model.EditProject
import com.zash3dit.domain.model.ErrorState
import com.zash3dit.domain.model.TextOverlay
import com.zash3dit.domain.model.VideoClip
import com.zash3dit.domain.model.VideoFilter
import com.zash3dit.domain.usecase.GetProjectByIdUseCase
import com.zash3dit.domain.usecase.ImportVideoUseCase
import com.zash3dit.domain.usecase.UpdateProjectUseCase
import com.zash3dit.domain.usecase.TrimVideoUseCase
import com.zash3dit.domain.usecase.SplitVideoUseCase
import com.zash3dit.domain.usecase.MergeVideosUseCase
import com.zash3dit.domain.usecase.ApplyVideoEffectsUseCase
import com.zash3dit.domain.usecase.AddTextOverlayUseCase
import com.zash3dit.domain.usecase.ApplyTextOverlayUseCase
import com.zash3dit.domain.usecase.ImportAudioUseCase
import com.zash3dit.domain.usecase.ApplyAudioMixingUseCase
import com.zash3dit.domain.usecase.ApplyTransitionUseCase
import com.zash3dit.domain.model.TransitionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.Job
import java.io.File

class EditorViewModel(
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val importVideoUseCase: ImportVideoUseCase,
    private val trimVideoUseCase: TrimVideoUseCase,
    private val splitVideoUseCase: SplitVideoUseCase,
    private val mergeVideosUseCase: MergeVideosUseCase,
    private val applyVideoEffectsUseCase: ApplyVideoEffectsUseCase,
    private val addTextOverlayUseCase: AddTextOverlayUseCase,
    private val applyTextOverlayUseCase: ApplyTextOverlayUseCase,
    private val importAudioUseCase: ImportAudioUseCase,
    private val applyAudioMixingUseCase: ApplyAudioMixingUseCase,
    private val applyTransitionUseCase: ApplyTransitionUseCase
) : ViewModel() {

    // Use SupervisorJob to prevent child coroutine failures from cancelling parent
    private val supervisorJob = SupervisorJob()
    private val ffmpegSemaphore = Semaphore(1) // Limit to 1 concurrent FFmpeg operation

    private val _project = MutableStateFlow<EditProject?>(null)
    val project: StateFlow<EditProject?> = _project

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    // Video editing state
    private val _selectedClipIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedClipIds: StateFlow<Set<Long>> = _selectedClipIds

    // Text overlay state
    private val _selectedOverlayIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedOverlayIds: StateFlow<Set<Long>> = _selectedOverlayIds

    // Audio state
    private val _selectedAudioIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedAudioIds: StateFlow<Set<Long>> = _selectedAudioIds

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val _processingMessage = MutableStateFlow("")
    val processingMessage: StateFlow<String> = _processingMessage

    fun loadProject(projectId: Long) {
        viewModelScope.launch(supervisorJob) {
            try {
                _isLoading.value = true
                val loadedProject = getProjectByIdUseCase(projectId)
                _project.value = loadedProject
            } catch (e: Exception) {
                // Handle error - could emit error state
                _project.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProject() {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isLoading.value = true
                updateProjectUseCase(currentProject.copy(modifiedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importVideo(filePath: String, duration: Long = 0L) {
        if (!validateFilePath(filePath)) return
        if (duration < 0) return

        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isLoading.value = true
                importVideoUseCase(currentProject.id, filePath, duration)
                // Reload project to get updated state
                val updatedProject = getProjectByIdUseCase(currentProject.id)
                _project.value = updatedProject
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Placeholder for updating project state
    fun updateProject(updatedProject: EditProject) {
        _project.value = updatedProject
    }

    // Playback control methods
    fun play() {
        _isPlaying.value = true
    }

    fun pause() {
        _isPlaying.value = false
    }

    fun seekTo(position: Long) {
        _currentPosition.value = position
    }

    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }

    fun updateDuration(duration: Long) {
        _duration.value = duration
    }

    // Video editing methods
    fun selectClip(clipId: Long) {
        _selectedClipIds.value = setOf(clipId)
    }

    fun toggleClipSelection(clipId: Long) {
        val current = _selectedClipIds.value
        _selectedClipIds.value = if (current.contains(clipId)) {
            current - clipId
        } else {
            current + clipId
        }
    }

    fun clearSelection() {
        _selectedClipIds.value = emptySet()
    }

    // Text overlay methods
    fun selectOverlay(overlayId: Long) {
        _selectedOverlayIds.value = setOf(overlayId)
    }

    fun toggleOverlaySelection(overlayId: Long) {
        val current = _selectedOverlayIds.value
        _selectedOverlayIds.value = if (current.contains(overlayId)) {
            current - overlayId
        } else {
            current + overlayId
        }
    }

    fun clearOverlaySelection() {
        _selectedOverlayIds.value = emptySet()
    }

    // Audio methods
    fun selectAudio(audioId: Long) {
        _selectedAudioIds.value = setOf(audioId)
    }

    fun toggleAudioSelection(audioId: Long) {
        val current = _selectedAudioIds.value
        _selectedAudioIds.value = if (current.contains(audioId)) {
            current - audioId
        } else {
            current + audioId
        }
    }

    fun clearAudioSelection() {
        _selectedAudioIds.value = emptySet()
    }

    fun importAudio(filePath: String, duration: Long = 0L) {
        if (!validateFilePath(filePath)) return
        if (duration < 0) return

        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isLoading.value = true
                importAudioUseCase(currentProject.id, filePath, duration)
                // Reload project to get updated state
                val updatedProject = getProjectByIdUseCase(currentProject.id)
                _project.value = updatedProject
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAudioClip(audioId: Long, startTime: Long? = null, duration: Long? = null, volume: Float? = null) {
        val currentProject = _project.value ?: return
        val existingAudio = currentProject.audioClips.find { it.id == audioId } ?: return

        viewModelScope.launch {
            val updatedAudio = existingAudio.copy(
                startTime = startTime ?: existingAudio.startTime,
                duration = duration ?: existingAudio.duration,
                volume = volume ?: existingAudio.volume
            )
            val updatedAudios = currentProject.audioClips.map { audio ->
                if (audio.id == audioId) updatedAudio else audio
            }
            val updatedProject = currentProject.copy(audioClips = updatedAudios)
            updateProjectUseCase(updatedProject)
            _project.value = updatedProject
        }
    }

    fun deleteAudioClip(audioId: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch {
            val updatedAudios = currentProject.audioClips.filter { it.id != audioId }
            val updatedProject = currentProject.copy(audioClips = updatedAudios)
            updateProjectUseCase(updatedProject)
            _project.value = updatedProject
            // Clear selection if deleted audio was selected
            if (_selectedAudioIds.value.contains(audioId)) {
                _selectedAudioIds.value = _selectedAudioIds.value - audioId
            }
        }
    }

    fun applyAudioMixingToClip(clipId: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Mixing audio..."

                val outputPath = createTempFile("audio_mixed", ".mp4").absolutePath
                val command = applyAudioMixingUseCase(currentProject.id, clipId, outputPath)

                executeFFmpegCommand(command).fold(
                    onSuccess = {
                        // Update project with processed clip
                        updateProjectWithProcessedClip(currentProject, clipId, outputPath, currentProject.videoClips.find { it.id == clipId }!!.duration)
                    },
                    onFailure = {
                        throw it
                    }
                )
            } catch (e: Exception) {
                _processingMessage.value = "Audio mixing failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun addTextOverlay(text: String, startTime: Long, duration: Long, x: Float, y: Float, fontSize: Int, color: String) {
        // Input validation
        if (!validateTextInput(text)) return
        if (!validateTimeInputs(startTime, duration)) return
        if (!validatePositionInputs(x, y)) return
        if (!validateFontSize(fontSize)) return
        if (!validateColorInput(color)) return

        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                val overlay = TextOverlay(
                    projectId = currentProject.id,
                    text = text,
                    startTime = startTime,
                    duration = duration,
                    position = currentProject.textOverlays.size, // Add at the end
                    x = x,
                    y = y,
                    fontSize = fontSize,
                    color = color
                )
                addTextOverlayUseCase(overlay)
                // Reload project to get updated state
                val updatedProject = getProjectByIdUseCase(currentProject.id)
                _project.value = updatedProject
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTextOverlay(overlayId: Long, text: String? = null, startTime: Long? = null, duration: Long? = null, x: Float? = null, y: Float? = null, fontSize: Int? = null, color: String? = null) {
        val currentProject = _project.value ?: return
        val existingOverlay = currentProject.textOverlays.find { it.id == overlayId } ?: return

        viewModelScope.launch {
            val updatedOverlay = existingOverlay.copy(
                text = text ?: existingOverlay.text,
                startTime = startTime ?: existingOverlay.startTime,
                duration = duration ?: existingOverlay.duration,
                x = x ?: existingOverlay.x,
                y = y ?: existingOverlay.y,
                fontSize = fontSize ?: existingOverlay.fontSize,
                color = color ?: existingOverlay.color
            )
            // Note: We need to add updateTextOverlay to repository, but for now we'll update the project
            val updatedOverlays = currentProject.textOverlays.map { overlay ->
                if (overlay.id == overlayId) updatedOverlay else overlay
            }
            val updatedProject = currentProject.copy(textOverlays = updatedOverlays)
            updateProjectUseCase(updatedProject)
            _project.value = updatedProject
        }
    }

    fun deleteTextOverlay(overlayId: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch {
            // Remove from project state
            val updatedOverlays = currentProject.textOverlays.filter { it.id != overlayId }
            val updatedProject = currentProject.copy(textOverlays = updatedOverlays)
            updateProjectUseCase(updatedProject)
            _project.value = updatedProject
            // Clear selection if deleted overlay was selected
            if (_selectedOverlayIds.value.contains(overlayId)) {
                _selectedOverlayIds.value = _selectedOverlayIds.value - overlayId
            }
        }
    }

    fun applyTextOverlaysToClip(clipId: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Applying text overlays..."

                val outputPath = createTempFile("text_overlay", ".mp4").absolutePath
                val command = applyTextOverlayUseCase(currentProject.id, clipId, outputPath)

                executeFFmpegCommand(command).fold(
                    onSuccess = {
                        // Update project with processed clip
                        updateProjectWithProcessedClip(currentProject, clipId, outputPath, currentProject.videoClips.find { it.id == clipId }!!.duration)
                    },
                    onFailure = {
                        throw it
                    }
                )
            } catch (e: Exception) {
                _processingMessage.value = "Apply text overlays failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun trimVideo(clipId: Long, startTime: Long, endTime: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Trimming video..."

                val outputPath = createTempFile("trimmed", ".mp4").absolutePath
                val command = trimVideoUseCase(currentProject.id, clipId, startTime, endTime, outputPath)

                executeFFmpegCommand(command).fold(
                    onSuccess = {
                        // Update project with trimmed clip
                        updateProjectWithTrimmedClip(currentProject, clipId, outputPath, endTime - startTime)
                    },
                    onFailure = {
                        throw it
                    }
                )
            } catch (e: Exception) {
                // Handle error
                _processingMessage.value = "Trim failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun splitVideo(clipId: Long, splitTime: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Splitting video..."

                val outputPath1 = createTempFile("split1", ".mp4").absolutePath
                val outputPath2 = createTempFile("split2", ".mp4").absolutePath
                val (command1, command2) = splitVideoUseCase(currentProject.id, clipId, splitTime, outputPath1, outputPath2)

                // Execute both commands sequentially with semaphore
                val result1 = executeFFmpegCommand(command1)
                val result2 = executeFFmpegCommand(command2)

                if (result1.isSuccess && result2.isSuccess) {
                    // Update project with split clips
                    updateProjectWithSplitClips(currentProject, clipId, outputPath1, outputPath2, splitTime)
                } else {
                    throw Exception("FFmpeg failed: ${result1.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                _processingMessage.value = "Split failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun mergeVideos(clipIds: List<Long>) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Merging videos..."

                val outputPath = createTempFile("merged", ".mp4").absolutePath
                val command = mergeVideosUseCase(currentProject.id, clipIds, outputPath)

                executeFFmpegCommand(command).fold(
                    onSuccess = {
                        // Update project with merged clip
                        updateProjectWithMergedClip(currentProject, clipIds, outputPath)
                    },
                    onFailure = {
                        throw it
                    }
                )
            } catch (e: Exception) {
                _processingMessage.value = "Merge failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun applyVideoEffects(clipId: Long, filter: VideoFilter, brightness: Float, contrast: Float, saturation: Float, playbackSpeed: Float) {
        val currentProject = _project.value ?: return
        viewModelScope.launch(supervisorJob) {
            try {
                _isProcessing.value = true
                _processingMessage.value = "Applying effects..."

                // First update the clip with new effect values
                val updatedClips = currentProject.videoClips.map { clip ->
                    if (clip.id == clipId) {
                        clip.copy(
                            filter = filter,
                            brightness = brightness,
                            contrast = contrast,
                            saturation = saturation,
                            playbackSpeed = playbackSpeed
                        )
                    } else {
                        clip
                    }
                }
                val tempProject = currentProject.copy(videoClips = updatedClips)
                updateProjectUseCase(tempProject)

                // Apply effects
                val outputPath = createTempFile("effects", ".mp4").absolutePath
                val command = applyVideoEffectsUseCase(currentProject.id, clipId, outputPath)

                executeFFmpegCommand(command).fold(
                    onSuccess = {
                        // Update project with processed clip
                        val newDuration = (currentProject.videoClips.find { it.id == clipId }!!.duration / playbackSpeed).toLong()
                        updateProjectWithProcessedClip(currentProject, clipId, outputPath, newDuration)
                    },
                    onFailure = {
                        throw it
                    }
                )
            } catch (e: Exception) {
                _processingMessage.value = "Apply effects failed: ${e.message}"
            } finally {
                _isProcessing.value = false
                _processingMessage.value = ""
            }
        }
    }

    fun updateVideoClipTransition(clipId: Long, transitionType: TransitionType, transitionDuration: Long) {
        val currentProject = _project.value ?: return
        viewModelScope.launch {
            val updatedClips = currentProject.videoClips.map { clip ->
                if (clip.id == clipId) {
                    clip.copy(transitionType = transitionType, transitionDuration = transitionDuration)
                } else {
                    clip
                }
            }
            val updatedProject = currentProject.copy(videoClips = updatedClips, modifiedAt = System.currentTimeMillis())
            updateProjectUseCase(updatedProject)
            _project.value = updatedProject
        }
    }

    private fun createTempFile(prefix: String, suffix: String): File {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "zash3dit_temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return File.createTempFile(prefix, suffix, tempDir)
    }

    private suspend fun updateProjectWithTrimmedClip(project: EditProject, clipId: Long, newPath: String, newDuration: Long) {
        val updatedClips = project.videoClips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(filePath = newPath, duration = newDuration)
            } else {
                clip
            }
        }
        val updatedProject = project.copy(videoClips = updatedClips, modifiedAt = System.currentTimeMillis())
        updateProjectUseCase(updatedProject)
        _project.value = updatedProject
    }

    private suspend fun updateProjectWithSplitClips(project: EditProject, originalClipId: Long, path1: String, path2: String, splitTime: Long) {
        val originalClip = project.videoClips.find { it.id == originalClipId } ?: return
        val clip1 = originalClip.copy(
            id = System.currentTimeMillis(), // Generate new ID
            filePath = path1,
            duration = splitTime,
            position = originalClip.position
        )
        val clip2 = originalClip.copy(
            id = System.currentTimeMillis() + 1, // Generate new ID
            filePath = path2,
            duration = originalClip.duration - splitTime,
            position = originalClip.position + 1,
            startTime = originalClip.startTime + splitTime
        )

        val updatedClips = project.videoClips
            .filter { it.id != originalClipId }
            .map { clip ->
                if (clip.position > originalClip.position) {
                    clip.copy(position = clip.position + 1)
                } else {
                    clip
                }
            } + listOf(clip1, clip2)

        val updatedProject = project.copy(videoClips = updatedClips, modifiedAt = System.currentTimeMillis())
        updateProjectUseCase(updatedProject)
        _project.value = updatedProject
    }

    private suspend fun updateProjectWithMergedClip(project: EditProject, clipIds: List<Long>, mergedPath: String) {
        val clipsToMerge = project.videoClips.filter { clipIds.contains(it.id) }
        val totalDuration = clipsToMerge.sumOf { it.duration }
        val mergedClip = VideoClip(
            id = System.currentTimeMillis(),
            projectId = project.id,
            filePath = mergedPath,
            startTime = clipsToMerge.first().startTime,
            duration = totalDuration,
            position = clipsToMerge.minOf { it.position }
        )

        val remainingClips = project.videoClips.filterNot { clipIds.contains(it.id) }
        val updatedClips = remainingClips.map { clip ->
            if (clip.position > mergedClip.position) {
                clip.copy(position = clip.position - clipsToMerge.size + 1)
            } else {
                clip
            }
        } + mergedClip

        val updatedProject = project.copy(videoClips = updatedClips, modifiedAt = System.currentTimeMillis())
        updateProjectUseCase(updatedProject)
        _project.value = updatedProject
    }

    private suspend fun updateProjectWithProcessedClip(project: EditProject, clipId: Long, newPath: String, newDuration: Long) {
        val updatedClips = project.videoClips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(filePath = newPath, duration = newDuration)
            } else {
                clip
            }
        }
        val updatedProject = project.copy(videoClips = updatedClips, modifiedAt = System.currentTimeMillis())
        updateProjectUseCase(updatedProject)
        _project.value = updatedProject
    }

    // Input validation functions
    private fun validateTextInput(text: String): Boolean {
        return text.isNotBlank() && text.length <= 500 && !text.contains(Regex("[<>\"'&]"))
    }

    private fun validateFilePath(filePath: String): Boolean {
        return filePath.isNotBlank() &&
               filePath.length <= 4096 &&
               !filePath.contains("..") &&
               File(filePath).exists() &&
               (filePath.endsWith(".mp4") || filePath.endsWith(".avi") || filePath.endsWith(".mov") ||
                filePath.endsWith(".mkv") || filePath.endsWith(".mp3") || filePath.endsWith(".wav"))
    }

    private fun validateTimeInputs(startTime: Long, duration: Long): Boolean {
        return startTime >= 0 && duration > 0 && duration <= 3600000 // Max 1 hour
    }

    private fun validatePositionInputs(x: Float, y: Float): Boolean {
        return x in 0.0..1.0 && y in 0.0..1.0
    }

    private fun validateFontSize(fontSize: Int): Boolean {
        return fontSize in 8..72
    }

    private fun validateColorInput(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancel()
        cleanupTempFiles()
    }

    private fun cleanupTempFiles() {
        try {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "zash3dit_temp")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.startsWith("zash3dit_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    private suspend fun executeFFmpegCommand(command: String): Result<Unit> {
        return ffmpegSemaphore.withPermit {
            try {
                val session = FFmpegKit.execute(command)
                if (ReturnCode.isSuccess(session.returnCode)) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("FFmpeg failed: ${session.failStackTrace}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}