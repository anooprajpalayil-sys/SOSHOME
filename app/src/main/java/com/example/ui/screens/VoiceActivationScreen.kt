package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.voice.VoiceActivationManager
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceActivationScreen(
    voiceManager: VoiceActivationManager,
    isGuardianActive: Boolean,
    currentPhrase: String,
    currentLanguage: String,
    onSavePhrase: (String, String) -> Unit,
    onToggleGuardian: () -> Unit,
    onStartTest: () -> Unit,
    onStopTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by voiceManager.voiceState.collectAsState()

    var phraseInput by remember(currentPhrase) { mutableStateOf(currentPhrase) }
    var selectedLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }

    val presetPhrases = listOf(
        "Save me God",
        "Help me now",
        "Save me please",
        "എന്നെ രക്ഷിക്കൂ", // Malayalam: Enne rakshikku
        "രക്ഷിക്കണേ ദൈവമേ" // Malayalam: Rakshikkane Daivame
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Guardian Mode Master Toggle Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isGuardianActive) CrimsonRed else Color.Gray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Guardian Protection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isGuardianActive) "Actively listening for your secret phrase" else "Disabled — Tap toggle to activate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Switch(
                    checked = isGuardianActive,
                    onCheckedChange = { onToggleGuardian() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = CrimsonRed
                    )
                )
            }
        }

        // Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configure Secret Emergency Phrase",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Choose words that sound natural to you when in danger or distress.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phraseInput,
                    onValueChange = { phraseInput = it },
                    label = { Text("Secret Phrase") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_phrase_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quick Presets:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetPhrases.forEach { preset ->
                        FilterChip(
                            selected = phraseInput == preset,
                            onClick = {
                                phraseInput = preset
                                if (preset.contains("രക്ഷിക്കൂ") || preset.contains("ദൈവമേ")) {
                                    selectedLanguage = "ml-IN"
                                }
                            },
                            label = { Text(preset, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Detection Language:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedLanguage == "en-US",
                        onClick = { selectedLanguage = "en-US" },
                        label = { Text("English (US / Global)") }
                    )
                    FilterChip(
                        selected = selectedLanguage == "ml-IN",
                        onClick = { selectedLanguage = "ml-IN" },
                        label = { Text("Malayalam (മലയാളം)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSavePhrase(phraseInput, selectedLanguage)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_phrase_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Secret Phrase & Language")
                }
            }
        }

        // --- Live Voice Recognition Practice Test Module ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Test & Train Voice Recognition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Speak into your microphone to verify detection confidence before relying on it in an emergency.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (voiceState.isListening) CrimsonRed else Navy800)
                        .clickable {
                            if (voiceState.isListening) onStopTest() else onStartTest()
                        }
                        .testTag("test_voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (voiceState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Test Mic",
                        tint = PureWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (voiceState.isListening) "LISTENING... Speak: \"$phraseInput\"" else "Tap to Speak Phrase",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (voiceState.isListening) CrimsonRed else MaterialTheme.colorScheme.onSurface
                )

                if (voiceState.lastRecognizedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy800)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Recognized Speech:",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "\"${voiceState.lastRecognizedText}\"",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Confidence: ${(voiceState.matchConfidence * 100).toInt()}%",
                                    color = if (voiceState.isPhraseMatched) GuardianGreen else Color.LightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(
                                    text = if (voiceState.isPhraseMatched) "MATCH CONFIRMED" else "LOW MATCH",
                                    isDanger = !voiceState.isPhraseMatched
                                )
                            }
                        }
                    }
                }

                if (voiceState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = voiceState.errorMessage ?: "",
                        color = CrimsonRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Privacy & OS Architecture Disclosures
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Navy700,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Android Privacy & Battery Transparency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• On-Device Detection: Keyword matching processes speech locally to respect your personal conversations.\n" +
                            "• Foreground Service Notice: Android requires a persistent notification banner when the microphone is monitored in the background.\n" +
                            "• Battery Optimization: Android may pause listening in extreme Battery Saver mode.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
