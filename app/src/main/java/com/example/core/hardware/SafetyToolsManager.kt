package com.example.core.hardware

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import kotlin.math.PI
import kotlin.math.sin

data class SafetyToolsState(
    val isSirenPlaying: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isStrobeActive: Boolean = false
)

class SafetyToolsManager(private val context: Context) {

    private val _state = MutableStateFlow(SafetyToolsState())
    val state: StateFlow<SafetyToolsState> = _state.asStateFlow()

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    private var sirenJob: Job? = null
    private var strobeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }?.let { cameraId = it }
        } catch (e: Exception) {
            Log.e("SafetyTools", "Failed to get camera flash ID: ${e.message}")
        }
    }

    // --- Loud Siren Synthesizer ---
    fun toggleSiren() {
        if (_state.value.isSirenPlaying) {
            stopSiren()
        } else {
            startSiren()
        }
    }

    fun startSiren() {
        if (_state.value.isSirenPlaying) return
        _state.value = _state.value.copy(isSirenPlaying = true)

        sirenJob?.cancel()
        sirenJob = scope.launch {
            val sampleRate = 44100
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                null
            }

            if (audioTrack == null) {
                _state.value = _state.value.copy(isSirenPlaying = false)
                return@launch
            }

            audioTrack.play()

            val buffer = ShortArray(1024)
            var phase = 0.0
            var timeStep = 0.0

            try {
                while (isActive && _state.value.isSirenPlaying) {
                    // Modulate frequency between 600 Hz and 1300 Hz over ~1.2s cycles
                    timeStep += 0.015
                    val freq = 850.0 + 450.0 * sin(timeStep)
                    val angularFreq = 2.0 * PI * freq / sampleRate

                    for (i in buffer.indices) {
                        buffer[i] = (sin(phase) * 32000).toInt().toShort()
                        phase += angularFreq
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun stopSiren() {
        sirenJob?.cancel()
        sirenJob = null
        _state.value = _state.value.copy(isSirenPlaying = false)
    }

    // --- Flashlight Strobe ---
    fun toggleFlashlight() {
        val newState = !_state.value.isFlashlightOn
        setTorchMode(newState)
        _state.value = _state.value.copy(isFlashlightOn = newState, isStrobeActive = false)
    }

    fun toggleStrobe() {
        if (_state.value.isStrobeActive) {
            stopStrobe()
        } else {
            startStrobe()
        }
    }

    fun startStrobe() {
        stopStrobe()
        _state.value = _state.value.copy(isStrobeActive = true, isFlashlightOn = true)

        strobeJob = scope.launch {
            var on = false
            try {
                while (isActive && _state.value.isStrobeActive) {
                    on = !on
                    setTorchMode(on)
                    delay(120L)
                }
            } finally {
                setTorchMode(false)
            }
        }
    }

    fun stopStrobe() {
        strobeJob?.cancel()
        strobeJob = null
        setTorchMode(false)
        _state.value = _state.value.copy(isStrobeActive = false, isFlashlightOn = false)
    }

    private fun setTorchMode(enabled: Boolean) {
        val cid = cameraId ?: return
        try {
            cameraManager?.setTorchMode(cid, enabled)
        } catch (e: CameraAccessException) {
            Log.e("SafetyTools", "Torch error: ${e.message}")
        } catch (e: Exception) {
            // Handled
        }
    }

    // --- Haptics (Vibrator) ---
    fun triggerSosVibration() {
        vibrator?.let { v ->
            val pattern = longArrayOf(0, 150, 100, 150, 100, 150, 250, 400, 100, 400, 100, 400, 250, 150, 100, 150)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }

    fun cancelVibration() {
        vibrator?.cancel()
    }

    fun release() {
        stopSiren()
        stopStrobe()
        cancelVibration()
    }
}
