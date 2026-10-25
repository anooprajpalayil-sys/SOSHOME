package com.example.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceState(
    val isListening: Boolean = false,
    val lastRecognizedText: String = "",
    val matchConfidence: Float = 0f,
    val isPhraseMatched: Boolean = false,
    val soundLevelRms: Float = 0f,
    val errorMessage: String? = null
)

class VoiceActivationManager(
    private val context: Context,
    private val onTriggerActivated: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var targetPhrase: String = "save me god"
    private var languageCode: String = "en-US"
    private var isContinuousListening: Boolean = false
    private var isTestingMode: Boolean = false

    fun updateConfig(phrase: String, language: String) {
        this.targetPhrase = normalizePhrase(phrase)
        this.languageCode = language
    }

    fun startListening(continuous: Boolean = false, testOnly: Boolean = false) {
        if (_voiceState.value.isListening) return

        this.isContinuousListening = continuous
        this.isTestingMode = testOnly

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                        _voiceState.value = _voiceState.value.copy(
                            errorMessage = "Speech recognition is not available on this device"
                        )
                        return@post
                    }
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                _voiceState.value = _voiceState.value.copy(
                    isListening = true,
                    errorMessage = null,
                    isPhraseMatched = false
                )
            } catch (e: Exception) {
                Log.e("VoiceActivation", "Failed to start speech recognizer: ${e.message}")
                _voiceState.value = _voiceState.value.copy(
                    isListening = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun stopListening() {
        isContinuousListening = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            } finally {
                speechRecognizer = null
            }
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                soundLevelRms = 0f
            )
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = _voiceState.value.copy(isListening = true, errorMessage = null)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                _voiceState.value = _voiceState.value.copy(soundLevelRms = rmsdB.coerceAtLeast(0f))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = _voiceState.value.copy(soundLevelRms = 0f)
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match detected"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    else -> "Speech recognizer paused"
                }

                _voiceState.value = _voiceState.value.copy(
                    isListening = false,
                    soundLevelRms = 0f,
                    errorMessage = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) null else errorMsg
                )

                // If continuous guardian mode is active, restart after brief delay
                if (isContinuousListening && !isTestingMode) {
                    mainHandler.postDelayed({
                        if (isContinuousListening) {
                            startListening(continuous = true, testOnly = false)
                        }
                    }, 1200L)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                checkMatches(matches)

                if (isContinuousListening && !isTestingMode && !_voiceState.value.isPhraseMatched) {
                    mainHandler.postDelayed({
                        if (isContinuousListening) {
                            startListening(continuous = true, testOnly = false)
                        }
                    }, 500L)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                checkMatches(matches)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun checkMatches(matches: ArrayList<String>?) {
        if (matches.isNullOrEmpty()) return

        for (spoken in matches) {
            val normalizedSpoken = normalizePhrase(spoken)
            val similarity = computeMatchScore(normalizedSpoken, targetPhrase)

            _voiceState.value = _voiceState.value.copy(
                lastRecognizedText = spoken,
                matchConfidence = similarity
            )

            // Trigger threshold: 0.70 or direct substring match
            val isMatched = normalizedSpoken.contains(targetPhrase) ||
                    targetPhrase.contains(normalizedSpoken) ||
                    similarity >= 0.72f

            if (isMatched) {
                _voiceState.value = _voiceState.value.copy(
                    isPhraseMatched = true,
                    matchConfidence = 1.0f
                )
                if (!isTestingMode) {
                    onTriggerActivated(spoken)
                }
                break
            }
        }
    }

    private fun normalizePhrase(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), "") // Keep unicode letters (supports Malayalam scripts)
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Levenshtein similarity metric (0.0 to 1.0)
     */
    private fun computeMatchScore(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f
        val maxLen = maxOf(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)
        return (maxLen - distance).toFloat() / maxLen
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}
