package com.zash3dit.presentation.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun VideoPlayer(
    videoUri: Uri?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: (Boolean) -> Unit,
    onSeek: (Long) -> Unit,
    onPositionUpdate: (Long) -> Unit,
    onDurationUpdate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    // Initialize ExoPlayer
    LaunchedEffect(Unit) {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            exoPlayer?.duration?.let { dur ->
                                if (dur > 0) onDurationUpdate(dur)
                            }
                        }
                    }
                }
            })
        }
    }

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    exoPlayer?.playWhenReady = isPlaying
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer?.playWhenReady = false
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer?.release()
                    exoPlayer = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Update media item when videoUri changes
    LaunchedEffect(videoUri) {
        videoUri?.let { uri ->
            exoPlayer?.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer?.prepare()
        }
    }

    // Handle play/pause state changes
    LaunchedEffect(isPlaying) {
        exoPlayer?.playWhenReady = isPlaying
    }

    // Handle seek
    LaunchedEffect(currentPosition) {
        if (exoPlayer?.currentPosition != currentPosition) {
            exoPlayer?.seekTo(currentPosition)
        }
    }

    // Update position periodically
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                exoPlayer?.let { player ->
                    onPositionUpdate(player.currentPosition)
                }
                delay(100) // Update every 100ms
            }
        }
    }

    Column(modifier = modifier) {
        // Video display area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // We'll use custom controls
                        playerView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Custom controls
        VideoControls(
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            onPlayPause = { onPlayPause(!isPlaying) },
            onSeek = onSeek
        )
    }
}

@Composable
private fun VideoControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Play/Pause button
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play"
            )
        }

        // Seek bar
        var sliderPosition by remember { mutableStateOf(0f) }

        LaunchedEffect(currentPosition, duration) {
            if (duration > 0) {
                sliderPosition = currentPosition.toFloat() / duration.toFloat()
            }
        }

        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                sliderPosition = value
            },
            onValueChangeFinished = {
                val newPosition = (sliderPosition * duration).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.weight(1f)
        )

        // Time display
        Text(
            text = formatTime(currentPosition) + " / " + formatTime(duration),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}