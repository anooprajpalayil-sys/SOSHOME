package com.example.core.audio

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlayerStatus(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0,
    val currentFilePlaying: String? = null
)

class EvidenceAudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _status = MutableStateFlow(PlayerStatus())
    val status: StateFlow<PlayerStatus> = _status.asStateFlow()

    fun playAudio(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        if (_status.value.currentFilePlaying == filePath && mediaPlayer != null) {
            if (_status.value.isPlaying) {
                pause()
            } else {
                resume()
            }
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    stop()
                }
            }
            mediaPlayer = player

            _status.value = PlayerStatus(
                isPlaying = true,
                currentPositionMs = 0,
                totalDurationMs = player.duration,
                currentFilePlaying = filePath
            )

            startProgressTracker()
        } catch (e: Exception) {
            stop()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _status.value.isPlaying) {
                delay(300L)
                val pos = mediaPlayer?.currentPosition ?: 0
                val dur = mediaPlayer?.duration ?: 0
                _status.value = _status.value.copy(
                    currentPositionMs = pos,
                    totalDurationMs = dur
                )
            }
        }
    }

    fun pause() {
        mediaPlayer?.pause()
        progressJob?.cancel()
        _status.value = _status.value.copy(isPlaying = false)
    }

    fun resume() {
        mediaPlayer?.start()
        _status.value = _status.value.copy(isPlaying = true)
        startProgressTracker()
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _status.value = _status.value.copy(currentPositionMs = positionMs)
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaPlayer = null
        }
        _status.value = PlayerStatus()
    }
}
