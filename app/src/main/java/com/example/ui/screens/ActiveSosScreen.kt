package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audio.RecordingStatus
import com.example.core.hardware.SafetyToolsState
import com.example.core.location.LocationData
import com.example.data.model.EmergencyEvent
import com.example.data.model.TrustedContact
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CrimsonRedBright
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite

@Composable
fun ActiveSosScreen(
    event: EmergencyEvent?,
    locationData: LocationData,
    recordingStatus: RecordingStatus,
    contactsAlertedCount: Int,
    contacts: List<TrustedContact>,
    safetyToolsState: SafetyToolsState,
    isTestMode: Boolean,
    responderAcknowledgments: Map<String, String> = emptyMap(),
    deliveryStatusSummary: Map<String, String> = emptyMap(),
    onResolveEmergency: (String) -> Unit,
    onToggleSiren: () -> Unit,
    onToggleStrobe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResolveDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val primaryContact = contacts.firstOrNull { it.isPrimary } ?: contacts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Flashing Emergency Header Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(beaconPulse)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isTestMode) AmberWarning else CrimsonRed)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isTestMode) "TEST EMERGENCY DRILL" else "EMERGENCY ACTIVE",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Triggered via ${event?.triggerMethod ?: "Manual SOS"}",
                            color = PureWhite.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PureWhite.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // --- Live Audio Evidence Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(CrimsonRedBright)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audio Evidence Recording",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "${recordingStatus.elapsedSeconds}s / ${recordingStatus.fileSizeFormatted}",
                        color = CrimsonRedBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AudioWaveformVisualizer(
                    isRecording = recordingStatus.isRecording,
                    amplitude = recordingStatus.currentAmplitude,
                    activeColor = CrimsonRedBright
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Encrypted in local vault: audio recorded continuously for legal evidence.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // --- GPS Live Tracking & Map Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GuardianGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live GPS Coordinates",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    StatusBadge(
                        text = "±${locationData.accuracy.toInt()}m ACCURACY",
                        isDanger = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = locationData.address,
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Lat: %.5f  |  Lng: %.5f".format(locationData.latitude, locationData.longitude),
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val mapUri = Uri.parse("https://maps.google.com/?q=${locationData.latitude},${locationData.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Live Coordinates in Google Maps", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Emergency Contacts Alert Status Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Emergency Contacts Alerted",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$contactsAlertedCount out of ${contacts.size} contacts received SMS with your live GPS location map link.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                if (primaryContact != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy900)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Primary: ${primaryContact.name}",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = primaryContact.phone,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${primaryContact.phone}"))
                                context.startActivity(callIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }

                // Verified Delivery Status list
                if (deliveryStatusSummary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Verified Delivery Status:", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        deliveryStatusSummary.forEach { (recipient, status) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = recipient, color = Color.LightGray, fontSize = 11.sp)
                                StatusBadge(text = status, isDanger = status == "PENDING")
                            }
                        }
                    }
                }
            }
        }

        // --- Live Responder Acknowledgments Banner ---
        if (responderAcknowledgments.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GuardianGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GuardianGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Guardian Responses (${responderAcknowledgments.size})",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    responderAcknowledgments.forEach { (name, msg) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy900)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✓ $name: “$msg”",
                                color = PureWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Hardware Alarm Tools (Siren & Strobe) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onToggleSiren,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (safetyToolsState.isSirenPlaying) AmberWarning else Navy800
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (safetyToolsState.isSirenPlaying) "Silence Siren" else "Loud Siren")
            }

            Button(
                onClick = onToggleStrobe,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (safetyToolsState.isStrobeActive) AmberWarning else Navy800
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (safetyToolsState.isStrobeActive) "Stop Strobe" else "Strobe Flash")
            }
        }

        // --- Safe Resolution Button ---
        Button(
            onClick = { showResolveDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("resolve_sos_button"),
            colors = ButtonDefaults.buttonColors(containerColor = GuardianGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PureWhite, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I AM SAFE NOW - RESOLVE EMERGENCY",
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confirmation dialog before resolving
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = {
                Text("Resolve Emergency & Stop Alerts?")
            },
            text = {
                Text("This will stop audio evidence recording, halt live GPS location sharing, silence all alarms, and save the incident securely to your Evidence Vault.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResolveDialog = false
                        onResolveEmergency("Safely resolved by user")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianGreen)
                ) {
                    Text("Confirm - I Am Safe", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Keep Active")
                }
            }
        )
    }
}
