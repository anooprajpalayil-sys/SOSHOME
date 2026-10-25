package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.core.audio.PlayerStatus
import com.example.data.model.EmergencyEvent
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvidenceVaultScreen(
    events: List<EmergencyEvent>,
    playerStatus: PlayerStatus,
    onPlayAudio: (String) -> Unit,
    onPauseAudio: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onDeleteEvent: (EmergencyEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Encrypted Evidence Vault",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tamper-evident logs of emergency activations, GPS coordinates, and audio recordings.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        if (events.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Navy700,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Vault is Currently Empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When an SOS or Voice trigger activates, audio recordings and GPS telemetry will be preserved here.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(events) { event ->
                val isPlayingThis = playerStatus.currentFilePlaying == event.audioFilePath && playerStatus.isPlaying
                val hasAudio = !event.audioFilePath.isNullOrBlank() && File(event.audioFilePath).exists()

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(event.timestamp)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            StatusBadge(
                                text = event.status,
                                isDanger = event.status == "ACTIVE"
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Trigger: ${event.triggerMethod}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CrimsonRed
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GuardianGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.address,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (event.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Note: ${event.notes}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Audio Player Container
                        if (hasAudio && event.audioFilePath != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Navy800)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    if (isPlayingThis) {
                                                        onPauseAudio()
                                                    } else {
                                                        onPlayAudio(event.audioFilePath)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(CrimsonRed)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Play/Pause",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Incident Audio Recording",
                                                    color = PureWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${event.audioDurationSeconds}s • MPEG-4 AAC",
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        // Share Audio Button
                                        IconButton(
                                            onClick = {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "SafeVoice Incident Log:\nTime: ${dateFormat.format(Date(event.timestamp))}\nLocation: https://maps.google.com/?q=${event.latitude},${event.longitude}\nTrigger: ${event.triggerMethod}"
                                                    )
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Incident Report"))
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = PureWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    if (playerStatus.currentFilePlaying == event.audioFilePath) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val progress = if (playerStatus.totalDurationMs > 0) {
                                            playerStatus.currentPositionMs.toFloat() / playerStatus.totalDurationMs.toFloat()
                                        } else 0f

                                        Slider(
                                            value = progress,
                                            onValueChange = { frac ->
                                                val newPos = (frac * playerStatus.totalDurationMs).toInt()
                                                onSeekAudio(newPos)
                                            },
                                            colors = SliderDefaults.colors(
                                                thumbColor = CrimsonRed,
                                                activeTrackColor = CrimsonRed,
                                                inactiveTrackColor = Color.DarkGray
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${playerStatus.currentPositionMs / 1000}s",
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "${playerStatus.totalDurationMs / 1000}s",
                                                color = Color.LightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Map Coordinates",
                                color = Navy700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    val mapUri = Uri.parse("https://maps.google.com/?q=${event.latitude},${event.longitude}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                                }
                            )

                            IconButton(
                                onClick = { onDeleteEvent(event) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
