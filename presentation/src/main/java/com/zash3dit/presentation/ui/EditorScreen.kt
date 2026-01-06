package com.zash3dit.presentation.ui

import android.Manifest
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.zash3dit.domain.model.AudioClip
import com.zash3dit.domain.model.TextOverlay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zash3dit.domain.model.TransitionType
import com.zash3dit.domain.model.VideoFilter
import com.zash3dit.presentation.viewmodel.EditorViewModel

fun getVideoDuration(context: android.content.Context, uri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durationStr?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

fun getAudioDuration(context: android.content.Context, uri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durationStr?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: Long?,
    viewModel: EditorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val selectedClipIds by viewModel.selectedClipIds.collectAsState()
    val selectedOverlayIds by viewModel.selectedOverlayIds.collectAsState()
    val selectedAudioIds by viewModel.selectedAudioIds.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingMessage by viewModel.processingMessage.collectAsState()

    // Effect state
    var selectedFilter by remember { mutableStateOf(VideoFilter.NONE) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var playbackSpeed by remember { mutableStateOf(1f) }

    // Text overlay state
    var overlayText by remember { mutableStateOf("") }
    var overlayStartTime by remember { mutableStateOf(0L) }
    var overlayDuration by remember { mutableStateOf(5000L) } // 5 seconds default
    var overlayX by remember { mutableStateOf(0.1f) } // 10% from left
    var overlayY by remember { mutableStateOf(0.1f) } // 10% from top
    var overlayFontSize by remember { mutableStateOf(24) }
    var overlayColor by remember { mutableStateOf("#FFFFFF") } // White

    // Audio state
    var audioVolume by remember { mutableStateOf(1.0f) }
    var audioStartTime by remember { mutableStateOf(0L) }
    var audioDuration by remember { mutableStateOf(0L) }

    val context = LocalContext.current

    // Permission launchers
    val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_VIDEO else Manifest.permission.READ_EXTERNAL_STORAGE
    val cameraPermission = Manifest.permission.CAMERA

    val readPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // Launch gallery picker
            galleryLauncher.launch(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // Launch camera
            cameraLauncher.launch(null)
        }
    }

    // Activity result launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val duration = getVideoDuration(context, it)
            viewModel.importVideo(it.toString(), duration)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakeVideo()) { uri ->
        uri?.let {
            val duration = getVideoDuration(context, it)
            viewModel.importVideo(it.toString(), duration)
        }
    }

    // Audio gallery launcher
    val audioGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val duration = getAudioDuration(context, it)
            viewModel.importAudio(it.toString(), duration)
        }
    }

    LaunchedEffect(projectId) {
        if (projectId != null) {
            viewModel.loadProject(projectId)
        }
    }

    // Update effect state when selected clip changes
    LaunchedEffect(selectedClipIds, project) {
        val selectedClipId = selectedClipIds.firstOrNull()
        val selectedClip = project?.videoClips?.find { it.id == selectedClipId }
        if (selectedClip != null) {
            selectedFilter = selectedClip.filter
            brightness = selectedClip.brightness
            contrast = selectedClip.contrast
            saturation = selectedClip.saturation
            playbackSpeed = selectedClip.playbackSpeed
        }
    }

    fun importFromGallery() {
        if (ContextCompat.checkSelfPermission(context, readPermission) == PermissionChecker.PERMISSION_GRANTED) {
            galleryLauncher.launch(ActivityResultContracts.PickVisualMedia.VideoOnly)
        } else {
            readPermissionLauncher.launch(readPermission)
        }
    }

    fun importFromCamera() {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PermissionChecker.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(cameraPermission)
        }
    }

    fun importAudioFromGallery() {
        if (ContextCompat.checkSelfPermission(context, readPermission) == PermissionChecker.PERMISSION_GRANTED) {
            audioGalleryLauncher.launch(ActivityResultContracts.PickVisualMedia.AudioOnly)
        } else {
            readPermissionLauncher.launch(readPermission)
        }
    }

    if (projectId == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editor") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Text("<")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a project from Home to start editing")
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(project?.name ?: "Editor") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Text("<")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.saveProject() }) {
                            Text("Save")
                        }
                    }
                )
            }
        ) { padding ->
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(padding))
            } else {
                Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Tools Panel
                ToolsPanel(
                    modifier = Modifier.width(200.dp),
                    selectedClipIds = selectedClipIds,
                    selectedOverlayIds = selectedOverlayIds,
                    selectedAudioIds = selectedAudioIds,
                    selectedFilter = selectedFilter,
                    brightness = brightness,
                    contrast = contrast,
                    saturation = saturation,
                    playbackSpeed = playbackSpeed,
                    overlayText = overlayText,
                    overlayStartTime = overlayStartTime,
                    overlayDuration = overlayDuration,
                    overlayX = overlayX,
                    overlayY = overlayY,
                    overlayFontSize = overlayFontSize,
                    overlayColor = overlayColor,
                    audioVolume = audioVolume,
                    audioStartTime = audioStartTime,
                    audioDuration = audioDuration,
                    onImportFromGallery = { importFromGallery() },
                    onImportFromCamera = { importFromCamera() },
                    onImportAudioFromGallery = { importAudioFromGallery() },
                    onFilterChange = { selectedFilter = it },
                    onBrightnessChange = { brightness = it },
                    onContrastChange = { contrast = it },
                    onSaturationChange = { saturation = it },
                    onPlaybackSpeedChange = { playbackSpeed = it },
                    onApplyEffects = {
                        selectedClipIds.firstOrNull()?.let { clipId ->
                            viewModel.applyVideoEffects(clipId, selectedFilter, brightness, contrast, saturation, playbackSpeed)
                        }
                    },
                    onOverlayTextChange = { overlayText = it },
                    onOverlayStartTimeChange = { overlayStartTime = it },
                    onOverlayDurationChange = { overlayDuration = it },
                    onOverlayXChange = { overlayX = it },
                    onOverlayYChange = { overlayY = it },
                    onOverlayFontSizeChange = { overlayFontSize = it },
                    onOverlayColorChange = { overlayColor = it },
                    onAddTextOverlay = {
                        viewModel.addTextOverlay(overlayText, overlayStartTime, overlayDuration, overlayX, overlayY, overlayFontSize, overlayColor)
                        // Reset form
                        overlayText = ""
                        overlayStartTime = 0L
                        overlayDuration = 5000L
                        overlayX = 0.1f
                        overlayY = 0.1f
                        overlayFontSize = 24
                        overlayColor = "#FFFFFF"
                    },
                    onApplyTextOverlays = {
                        selectedClipIds.firstOrNull()?.let { clipId ->
                            viewModel.applyTextOverlaysToClip(clipId)
                        }
                    },
                    onAudioVolumeChange = { audioVolume = it },
                    onAudioStartTimeChange = { audioStartTime = it },
                    onAudioDurationChange = { audioDuration = it },
                    onUpdateAudioClip = {
                        selectedAudioIds.firstOrNull()?.let { audioId ->
                            viewModel.updateAudioClip(audioId, audioStartTime, audioDuration, audioVolume)
                        }
                    },
                    onApplyAudioMixing = {
                        selectedClipIds.firstOrNull()?.let { clipId ->
                            viewModel.applyAudioMixingToClip(clipId)
                        }
                    }
                )

                // Main Editor Area
                Column(modifier = Modifier.weight(1f)) {
                    // Playback Area
                    PlaybackArea(
                        project = project,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onPlayPause = { playing ->
                            if (playing) viewModel.play() else viewModel.pause()
                        },
                        onSeek = { position -> viewModel.seekTo(position) },
                        onPositionUpdate = { position -> viewModel.updatePosition(position) },
                        onDurationUpdate = { dur -> viewModel.updateDuration(dur) },
                        modifier = Modifier.weight(1f)
                    )

                    // Timeline
                    Timeline(
                        project = project,
                        selectedClipIds = selectedClipIds,
                        selectedOverlayIds = selectedOverlayIds,
                        selectedAudioIds = selectedAudioIds,
                        isProcessing = isProcessing,
                        processingMessage = processingMessage,
                        onClipSelected = { clipId -> viewModel.selectClip(clipId) },
                        onOverlaySelected = { overlayId -> viewModel.selectOverlay(overlayId) },
                        onAudioSelected = { audioId -> viewModel.selectAudio(audioId) },
                        onTrimVideo = { clipId, start, end -> viewModel.trimVideo(clipId, start, end) },
                        onSplitVideo = { clipId, time -> viewModel.splitVideo(clipId, time) },
                        onMergeVideos = { clipIds -> viewModel.mergeVideos(clipIds) },
                        onDeleteTextOverlay = { overlayId -> viewModel.deleteTextOverlay(overlayId) },
                        onDeleteAudioClip = { audioId -> viewModel.deleteAudioClip(audioId) },
                        modifier = Modifier.height(150.dp)
                    )
                }
            }
        }
        
        @Composable
        fun VideoClipItem(
            clip: VideoClip,
            isSelected: Boolean,
            onClick: () -> Unit,
            onTrim: (Long, Long) -> Unit,
            onSplit: (Long) -> Unit
        ) {
            val clipWidth = (clip.duration / 1000f * 50).dp // 50 pixels per second, adjust as needed
        
            Box(
                modifier = Modifier
                    .width(clipWidth)
                    .height(80.dp)
                    .background(if (isSelected) Color.Yellow else Color.Blue)
                    .clickable { onClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Clip ${clip.position + 1}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${clip.duration / 1000}s",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (clip.filter != VideoFilter.NONE) {
                        Text(
                            clip.filter.name.replace("_", " ").lowercase().capitalize(),
                            color = Color.Yellow,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
        
                    if (isSelected) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Trim handles (simplified)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.Red)
                                    .clickable { /* TODO: Implement trim start */ }
                            )
                            Button(
                                onClick = { onSplit(clip.duration / 2) }, // Split in middle for demo
                                modifier = Modifier.height(20.dp),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Split", style = MaterialTheme.typography.bodySmall)
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.Red)
                                    .clickable { /* TODO: Implement trim end */ }
                            )
                        }
                    }
                }
            }
            
            @Composable
            fun AudioClipItem(
                audioClip: AudioClip,
                isSelected: Boolean,
                onClick: () -> Unit,
                onDelete: () -> Unit
            ) {
                val audioWidth = (audioClip.duration / 1000f * 50).dp // 50 pixels per second

                Box(
                    modifier = Modifier
                        .width(audioWidth)
                        .height(60.dp)
                        .background(if (isSelected) Color.Green else Color.Yellow)
                        .clickable { onClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Audio ${audioClip.position + 1}",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${audioClip.duration / 1000}s",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Vol: ${audioClip.volume.format(1)}",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (isSelected) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Text("×", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            
                @Composable
                fun TransitionItem(
                    clip: VideoClip,
                    onTransitionChange: (TransitionType, Long) -> Unit
                ) {
                    var showDialog by remember { mutableStateOf(false) }
                    var selectedType by remember { mutableStateOf(clip.transitionType) }
                    var duration by remember { mutableStateOf(clip.transitionDuration) }
            
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(Color.Gray)
                            .clickable { showDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (clip.transitionType) {
                                TransitionType.NONE -> "—"
                                TransitionType.FADE_IN_OUT -> "F"
                                TransitionType.SLIDE -> "S"
                                TransitionType.DISSOLVE -> "D"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
            
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Transition Settings") },
                            text = {
                                Column {
                                    Text("Type", style = MaterialTheme.typography.bodyMedium)
                                    TransitionType.values().forEach { type ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedType == type,
                                                onClick = { selectedType = type }
                                            )
                                            Text(type.name.replace("_", " ").lowercase().capitalize(), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = (duration / 1000f).toString(),
                                        onValueChange = { duration = (it.toFloatOrNull() ?: 1f).toLong() * 1000 },
                                        label = { Text("Duration (s)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    onTransitionChange(selectedType, duration)
                                    showDialog = false
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            @Composable
            fun TextOverlayItem(
                overlay: TextOverlay,
                isSelected: Boolean,
                onClick: () -> Unit,
                onDelete: () -> Unit
            ) {
                val overlayWidth = (overlay.duration / 1000f * 50).dp // 50 pixels per second

                Box(
                    modifier = Modifier
                        .width(overlayWidth)
                        .height(60.dp)
                        .background(if (isSelected) Color.Cyan else Color.Magenta)
                        .clickable { onClick() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            overlay.text.take(10) + if (overlay.text.length > 10) "..." else "",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${overlay.duration / 1000}s",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (isSelected) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Text("×", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolsPanel(
    modifier: Modifier = Modifier,
    selectedClipIds: Set<Long>,
    selectedOverlayIds: Set<Long>,
    selectedAudioIds: Set<Long>,
    selectedFilter: VideoFilter,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    playbackSpeed: Float,
    overlayText: String,
    overlayStartTime: Long,
    overlayDuration: Long,
    overlayX: Float,
    overlayY: Float,
    overlayFontSize: Int,
    overlayColor: String,
    audioVolume: Float,
    audioStartTime: Long,
    audioDuration: Long,
    onImportFromGallery: () -> Unit,
    onImportFromCamera: () -> Unit,
    onImportAudioFromGallery: () -> Unit,
    onFilterChange: (VideoFilter) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onApplyEffects: () -> Unit,
    onOverlayTextChange: (String) -> Unit,
    onOverlayStartTimeChange: (Long) -> Unit,
    onOverlayDurationChange: (Long) -> Unit,
    onOverlayXChange: (Float) -> Unit,
    onOverlayYChange: (Float) -> Unit,
    onOverlayFontSizeChange: (Int) -> Unit,
    onOverlayColorChange: (String) -> Unit,
    onAddTextOverlay: () -> Unit,
    onApplyTextOverlays: () -> Unit,
    onAudioVolumeChange: (Float) -> Unit,
    onAudioStartTimeChange: (Long) -> Unit,
    onAudioDurationChange: (Long) -> Unit,
    onUpdateAudioClip: () -> Unit,
    onApplyAudioMixing: () -> Unit
) {
    Column(modifier = modifier.background(Color.LightGray).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Video", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onImportFromGallery) { Text("Import from Gallery") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onImportFromCamera) { Text("Record Video") }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Effects", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedClipIds.isNotEmpty()) {
            // Filter selection
            Text("Filter", style = MaterialTheme.typography.bodyMedium)
            VideoFilter.values().forEach { filter ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) }
                    )
                    Text(filter.name.replace("_", " ").lowercase().capitalize(), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brightness slider
            Text("Brightness: ${brightness.format(1)}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = -1f..1f,
                steps = 20
            )

            // Contrast slider
            Text("Contrast: ${contrast.format(1)}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = contrast,
                onValueChange = onContrastChange,
                valueRange = 0f..2f,
                steps = 20
            )

            // Saturation slider
            Text("Saturation: ${saturation.format(1)}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = saturation,
                onValueChange = onSaturationChange,
                valueRange = 0f..2f,
                steps = 20
            )

            // Playback speed slider
            Text("Speed: ${playbackSpeed.format(1)}x", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = playbackSpeed,
                onValueChange = onPlaybackSpeedChange,
                valueRange = 0.5f..2f,
                steps = 15
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onApplyEffects) { Text("Apply Effects") }
        } else {
            Text("Select a clip to apply effects", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Audio", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onImportAudioFromGallery) { Text("Import Audio") }

        if (selectedAudioIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Volume slider
            Text("Volume: ${audioVolume.format(1)}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = audioVolume,
                onValueChange = onAudioVolumeChange,
                valueRange = 0f..2f,
                steps = 20
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Start time input
            OutlinedTextField(
                value = (audioStartTime / 1000f).toString(),
                onValueChange = { onAudioStartTimeChange((it.toFloatOrNull() ?: 0f) * 1000).toLong() },
                label = { Text("Start Time (s)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration input
            OutlinedTextField(
                value = (audioDuration / 1000f).toString(),
                onValueChange = { onAudioDurationChange((it.toFloatOrNull() ?: 0f) * 1000).toLong() },
                label = { Text("Duration (s)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onUpdateAudioClip) { Text("Update Audio") }
        } else {
            Text("Select an audio clip to adjust", style = MaterialTheme.typography.bodySmall)
        }

        if (selectedClipIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onApplyAudioMixing) {
                Text("Apply Audio Mixing")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Text Overlays", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        // Text input
        OutlinedTextField(
            value = overlayText,
            onValueChange = onOverlayTextChange,
            label = { Text("Text") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Start time input
        OutlinedTextField(
            value = (overlayStartTime / 1000f).toString(),
            onValueChange = { onOverlayStartTimeChange((it.toFloatOrNull() ?: 0f) * 1000).toLong() },
            label = { Text("Start Time (s)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Duration input
        OutlinedTextField(
            value = (overlayDuration / 1000f).toString(),
            onValueChange = { onOverlayDurationChange((it.toFloatOrNull() ?: 5f) * 1000).toLong() },
            label = { Text("Duration (s)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Position controls
        Text("Position", style = MaterialTheme.typography.bodyMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = overlayX.toString(),
                onValueChange = { onOverlayXChange(it.toFloatOrNull() ?: 0.1f) },
                label = { Text("X") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = overlayY.toString(),
                onValueChange = { onOverlayYChange(it.toFloatOrNull() ?: 0.1f) },
                label = { Text("Y") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Font size
        OutlinedTextField(
            value = overlayFontSize.toString(),
            onValueChange = { onOverlayFontSizeChange(it.toIntOrNull() ?: 24) },
            label = { Text("Font Size") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color picker (simplified as text input for now)
        OutlinedTextField(
            value = overlayColor,
            onValueChange = onOverlayColorChange,
            label = { Text("Color (hex)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onAddTextOverlay, enabled = overlayText.isNotEmpty()) {
            Text("Add Text Overlay")
        }

        if (selectedClipIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onApplyTextOverlays) {
                Text("Apply Text Overlays")
            }
        }
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun PlaybackArea(
    project: EditProject?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: (Boolean) -> Unit,
    onSeek: (Long) -> Unit,
    onPositionUpdate: (Long) -> Unit,
    onDurationUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val videoUri = remember(project) {
        project?.videoClips?.firstOrNull()?.let { clip ->
            Uri.parse(clip.filePath)
        }
    }

    if (videoUri != null) {
        VideoPlayer(
            videoUri = videoUri,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onPositionUpdate = onPositionUpdate,
            onDurationUpdate = onDurationUpdate,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No video clips available", color = Color.White)
        }
    }
}

@Composable
fun Timeline(
    project: EditProject?,
    selectedClipIds: Set<Long>,
    selectedOverlayIds: Set<Long>,
    selectedAudioIds: Set<Long>,
    isProcessing: Boolean,
    processingMessage: String,
    onClipSelected: (Long) -> Unit,
    onOverlaySelected: (Long) -> Unit,
    onAudioSelected: (Long) -> Unit,
    onTrimVideo: (Long, Long, Long) -> Unit,
    onSplitVideo: (Long, Long) -> Unit,
    onMergeVideos: (List<Long>) -> Unit,
    onDeleteTextOverlay: (Long) -> Unit,
    onDeleteAudioClip: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Color.Gray).padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Timeline", style = MaterialTheme.typography.headlineSmall)

            if (isProcessing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(processingMessage, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (selectedClipIds.size > 1) {
                Button(onClick = { onMergeVideos(selectedClipIds.toList()) }) {
                    Text("Merge")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Video clips timeline
        Text("Video Clips", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sortedClips = project?.videoClips?.sortedBy { it.position } ?: emptyList()
            sortedClips.forEachIndexed { index, clip ->
                VideoClipItem(
                    clip = clip,
                    isSelected = selectedClipIds.contains(clip.id),
                    onClick = { onClipSelected(clip.id) },
                    onTrim = { start, end -> onTrimVideo(clip.id, start, end) },
                    onSplit = { time -> onSplitVideo(clip.id, time) }
                )
                // Add transition UI between clips (except after the last one)
                if (index < sortedClips.size - 1) {
                    TransitionItem(
                        clip = clip,
                        onTransitionChange = { type, duration ->
                            viewModel.updateVideoClipTransition(clip.id, type, duration)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Audio clips timeline
        Text("Audio Clips", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            project?.audioClips?.sortedBy { it.startTime }?.forEach { audioClip ->
                AudioClipItem(
                    audioClip = audioClip,
                    isSelected = selectedAudioIds.contains(audioClip.id),
                    onClick = { onAudioSelected(audioClip.id) },
                    onDelete = { onDeleteAudioClip(audioClip.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text overlays timeline
        Text("Text Overlays", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            project?.textOverlays?.sortedBy { it.startTime }?.forEach { overlay ->
                TextOverlayItem(
                    overlay = overlay,
                    isSelected = selectedOverlayIds.contains(overlay.id),
                    onClick = { onOverlaySelected(overlay.id) },
                    onDelete = { onDeleteTextOverlay(overlay.id) }
                )
            }
        }
    }
}