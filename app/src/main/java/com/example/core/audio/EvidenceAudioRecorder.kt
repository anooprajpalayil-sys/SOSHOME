package com.example.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
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

data class RecordingStatus(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentAmplitude: Int = 0,
    val filePath: String? = null,
    val fileSizeFormatted: String = "0 KB"
)

class EvidenceAudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    private val _status = MutableStateFlow(RecordingStatus())
    val status: StateFlow<RecordingStatus> = _status.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun startRecording(eventId: String): String? {
        if (_status.value.isRecording) return _status.value.filePath

        val evidenceDir = File(context.filesDir, "evidence").apply {
            if (!exists()) mkdirs()
        }

        val file = File(evidenceDir, "evidence_${eventId}_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _status.value = RecordingStatus(
                isRecording = true,
                elapsedSeconds = 0,
                currentAmplitude = 0,
                filePath = file.absolutePath
            )

            startTimerAndVisualizer()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording: ${e.message}")
            stopRecording()
            null
        }
    }

    private fun startTimerAndVisualizer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var seconds = 0
            while (isActive && _status.value.isRecording) {
                delay(200L)
                val amp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }

                if (seconds % 5 == 0) { // every 1s
                    val sizeKb = (currentOutputFile?.length() ?: 0) / 1024
                    _status.value = _status.value.copy(
                        elapsedSeconds = seconds / 5,
                        currentAmplitude = amp,
                        fileSizeFormatted = "$sizeKb KB"
                    )
                } else {
                    _status.value = _status.value.copy(
                        currentAmplitude = amp
                    )
                }
                seconds++
            }
        }
    }

    fun stopRecording(): String? {
        timerJob?.cancel()
        timerJob = null

        val path = currentOutputFile?.absolutePath

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recorder: ${e.message}")
        } finally {
            mediaRecorder = null
        }

        val finalSizeKb = (currentOutputFile?.length() ?: 0) / 1024
        _status.value = _status.value.copy(
            isRecording = false,
            currentAmplitude = 0,
            fileSizeFormatted = "$finalSizeKb KB"
        )
        return path
    }
}
