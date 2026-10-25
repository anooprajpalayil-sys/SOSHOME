package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun FakeCallScreen(
    callerName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCallAnswered by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isCallAnswered) {
        if (isCallAnswered) {
            while (true) {
                delay(1000L)
                callSeconds++
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section: Caller info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callerName,
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isCallAnswered) "In Call: %02d:%02d".format(callSeconds / 60, callSeconds % 60) else "Incoming Call...",
                    color = if (isCallAnswered) GuardianGreen else Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom Section: Actions
            if (!isCallAnswered) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = PureWhite, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Decline", color = PureWhite, fontSize = 13.sp)
                    }

                    // Accept
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { isCallAnswered = true },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(GuardianGreen)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Accept", tint = PureWhite, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Accept", color = PureWhite, fontSize = 13.sp)
                    }
                }
            } else {
                // End Call
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(CrimsonRed)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = PureWhite, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("End Call", color = PureWhite, fontSize = 13.sp)
                }
            }
        }
    }
}
