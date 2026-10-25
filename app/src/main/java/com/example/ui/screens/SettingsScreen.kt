package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite

@Composable
fun SettingsScreen(
    prefs: UserPreferencesManager,
    onSaveProfile: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var userName by remember { mutableStateOf(prefs.userName) }
    var userPhone by remember { mutableStateOf(prefs.userPhone) }
    var countdownSecs by remember { mutableIntStateOf(prefs.cancellationCountdownSeconds) }
    var autoRecord by remember { mutableStateOf(prefs.autoRecordEvidence) }
    var sirenOnSos by remember { mutableStateOf(prefs.loudSirenOnSos) }
    var strobeOnSos by remember { mutableStateOf(prefs.strobeFlashlightOnSos) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "User Profile & Security Settings",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Guardian Identity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Your Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = { userPhone = it },
                    label = { Text("Your Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        prefs.userName = userName
                        prefs.userPhone = userPhone
                        onSaveProfile(userName, userPhone)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Update Profile")
                }
            }
        }

        // Emergency Workflow Behavior
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Emergency Workflow Behavior", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Cancellation Window Countdown:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Time given to cancel an accidental trigger before alerts send.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 10).forEach { s ->
                        FilterChip(
                            selected = countdownSecs == s,
                            onClick = {
                                countdownSecs = s
                                prefs.cancellationCountdownSeconds = s
                            },
                            label = { Text("${s} Seconds") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Record Audio Evidence", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Saves tamper-proof audio in vault during SOS", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = autoRecord,
                        onCheckedChange = {
                            autoRecord = it
                            prefs.autoRecordEvidence = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sound Loud Siren on SOS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Immediately disorients attacker and attracts bystanders", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = sirenOnSos,
                        onCheckedChange = {
                            sirenOnSos = it
                            prefs.loudSirenOnSos = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Flashlight SOS Strobe on Trigger", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Night emergency beacon signaling", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = strobeOnSos,
                        onCheckedChange = {
                            strobeOnSos = it
                            prefs.strobeFlashlightOnSos = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = CrimsonRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
