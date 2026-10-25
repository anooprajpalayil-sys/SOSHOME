package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.hardware.SafetyToolsState
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite

@Composable
fun SafetyToolsScreen(
    prefs: UserPreferencesManager,
    safetyToolsState: SafetyToolsState,
    onToggleSiren: () -> Unit,
    onToggleStrobe: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onScheduleFakeCall: (Int, String) -> Unit,
    onTriggerTestDrill: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fakeCaller by remember { mutableStateOf(prefs.fakeCallerName) }
    var selectedDelay by remember { mutableIntStateOf(prefs.fakeCallDelaySeconds) }
    var shakeEnabled by remember { mutableStateOf(prefs.shakeToSosEnabled) }
    var shakeThreshold by remember { mutableStateOf(prefs.shakeThreshold) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Safety Toolkit",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Fake Call Escape Tool
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Fake Incoming Call Simulator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Triggers a realistic incoming phone call to exit awkward situations", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fakeCaller,
                    onValueChange = { fakeCaller = it },
                    label = { Text("Caller Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Trigger Delay:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 20, 30).forEach { sec ->
                        FilterChip(
                            selected = selectedDelay == sec,
                            onClick = { selectedDelay = sec },
                            label = { Text("${sec}s") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        prefs.fakeCallerName = fakeCaller
                        prefs.fakeCallDelaySeconds = selectedDelay
                        onScheduleFakeCall(selectedDelay, fakeCaller)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Schedule Fake Call in ${selectedDelay}s")
                }
            }
        }

        // Hardware Deterrents: Siren & Flashlight
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Emergency Deterrents & Beacons", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onToggleSiren,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (safetyToolsState.isSirenPlaying) CrimsonRed else Navy800
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (safetyToolsState.isSirenPlaying) "Stop Siren" else "Loud Siren")
                    }

                    Button(
                        onClick = onToggleStrobe,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (safetyToolsState.isStrobeActive) AmberWarning else Navy800
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (safetyToolsState.isStrobeActive) "Stop Strobe" else "Strobe SOS")
                    }
                }
            }
        }

        // Shake-to-SOS Configuration
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Navy700)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Shake-to-Alert (Accelerometer)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Double vigorous shake triggers SOS", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Switch(
                        checked = shakeEnabled,
                        onCheckedChange = {
                            shakeEnabled = it
                            prefs.shakeToSosEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = CrimsonRed
                        )
                    )
                }

                if (shakeEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sensitivity Threshold: ${shakeThreshold.toInt()} m/s²",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Slider(
                        value = shakeThreshold,
                        onValueChange = {
                            shakeThreshold = it
                            prefs.shakeThreshold = it
                        },
                        valueRange = 11f..22f,
                        colors = SliderDefaults.colors(
                            thumbColor = Navy800,
                            activeTrackColor = Navy800
                        )
                    )
                }
            }
        }

        // Safe Emergency Drill / Test Mode Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GuardianGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Safe Emergency Drill (Test Mode)",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Runs the entire SOS workflow: 3-second cancellation countdown, audio recorder visualizer, and GPS coordinates verification — without dispatching real SMS alerts to emergency numbers.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTriggerTestDrill,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start Safety Drill Test", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Privacy & System Permissions Hub
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPermissions() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Navy700)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Privacy, Permissions & Disclosures", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Microphone, Location, Foreground service policy", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Text("View", color = Navy700, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
