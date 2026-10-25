package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite

@Composable
fun PermissionsPrivacyScreen(
    modifier: Modifier = Modifier
) {
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.all { it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = Navy700, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Privacy & Permission Architecture", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Zero-covert tracking policy & Android platform compliance", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Grant Permissions Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Android System Permissions",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "To guarantee that voice phrase detection and live emergency GPS alerts function reliably under stress, SafeVoice requires critical hardware access.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val perms = mutableListOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.CALL_PHONE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GuardianGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant / Verify Device Permissions", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Permission Items
        PermissionItem(
            icon = Icons.Default.Mic,
            title = "Microphone (RECORD_AUDIO)",
            purpose = "Detects your secret trigger phrase ('Save me God' / 'എന്നെ രക്ഷിക്കൂ') and automatically records legal evidence audio during emergencies.",
            privacyNote = "Local processing only. Ordinary conversations are never uploaded or retained."
        )

        PermissionItem(
            icon = Icons.Default.LocationOn,
            title = "GPS Location (ACCESS_FINE_LOCATION)",
            purpose = "Fetches pinpoint GPS coordinates and reverse geocoded street addresses to send Google Maps links to emergency contacts.",
            privacyNote = "Location is only captured when an emergency is initiated or when sharing with your Safety Circle."
        )

        PermissionItem(
            icon = Icons.Default.Phone,
            title = "Phone & SMS (SEND_SMS / CALL_PHONE)",
            purpose = "Enables automatic dispatch of emergency alert SMS and instantaneous one-touch calling of primary contacts.",
            privacyNote = "Only contacts explicitly configured in your Trusted Contacts list receive alerts."
        )

        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "Foreground Services & Notifications",
            purpose = "Maintains a transparent notification banner while Guardian protection is armed, preventing the Android OS from terminating background safety listeners.",
            privacyNote = "Complies strictly with Android 14+ foreground service guidelines."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    purpose: String,
    privacyNote: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Navy800),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = purpose, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Privacy: $privacyNote", fontSize = 11.sp, color = GuardianGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
}
