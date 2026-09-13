package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream

data class AudioPlaybackState(
    val currentFileId: Long? = null,
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = player.currentPosition.toLong(),
                        durationMs = player.duration.toLong().coerceAtLeast(0L),
                        isPlaying = true
                    )
                    handler.postDelayed(this, 100)
                }
            }
        }
    }

    /**
     * Plays or toggles audio for the specified file.
     * Uses streaming file descriptor to avoid loading the file into memory.
     */
    fun togglePlay(file: com.example.model.FileItem) {
        togglePlay(file.id, file.filePath)
    }

    fun togglePlay(fileId: Long, filePath: String) {
        val currentState = _playbackState.value

        if (currentState.currentFileId == fileId && mediaPlayer != null) {
            if (mediaPlayer?.isPlaying == true) {
                pause()
            } else {
                resume()
            }
            return
        }

        // Switching to a new file or starting fresh
        stop()

        val file = File(filePath)
        if (!file.exists()) return

        try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // Streaming directly from file descriptor without loading entire file into memory
            FileInputStream(file).use { fis ->
                player.setDataSource(fis.fd)
            }

            player.setOnPreparedListener { mp ->
                mp.start()
                _playbackState.value = AudioPlaybackState(
                    currentFileId = fileId,
                    filePath = filePath,
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = mp.duration.toLong()
                )
                handler.post(progressRunnable)
            }

            player.setOnCompletionListener {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0L
                )
                handler.removeCallbacks(progressRunnable)
            }

            player.setOnErrorListener { _, _, _ ->
                stop()
                true
            }

            player.prepareAsync()
            mediaPlayer = player
        } catch (_: Exception) {
            stop()
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                handler.removeCallbacks(progressRunnable)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                handler.post(progressRunnable)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = positionMs
            )
        }
    }

    fun stop() {
        handler.removeCallbacks(progressRunnable)
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
            _playbackState.value = AudioPlaybackState()
        }
    }

    fun release() {
        stop()
    }
}
