package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.emergency.EmergencyDispatcher
import com.example.data.model.TrustedContact
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.PureWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactsScreen(
    contacts: List<TrustedContact>,
    dispatcher: EmergencyDispatcher,
    userName: String,
    onAddContact: (TrustedContact) -> Unit,
    onUpdateContact: (TrustedContact) -> Unit,
    onDeleteContact: (TrustedContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<TrustedContact?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trusted Emergency Contacts",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "When an emergency triggers, SafeVoice instantly dispatches alerts with your live GPS location map to these individuals.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(contacts) { contact ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (contact.isPrimary) CrimsonRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (contact.isPrimary) CrimsonRed else Navy800),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1),
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = contact.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (contact.isPrimary) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Primary Contact",
                                            tint = CrimsonRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${contact.relationship} • ${contact.phone}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            IconButton(onClick = { contactToEdit = contact }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Navy700, modifier = Modifier.size(20.dp))
                            }

                            IconButton(onClick = { onDeleteContact(contact) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Channels badge strip & Action shortcuts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (contact.sendSms) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Navy800.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("SMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Navy800)
                                    }
                                }
                                if (contact.sendWhatsApp) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF25D366).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF075E54))
                                    }
                                }
                                if (contact.directCall) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CrimsonRed.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Direct Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        val intent = dispatcher.createCallIntent(contact.phone)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Navy800)
                                }

                                IconButton(
                                    onClick = {
                                        val msg = dispatcher.formatEmergencyMessage(
                                            userName = userName,
                                            latitude = 12.9716,
                                            longitude = 77.5946,
                                            address = "SafeVoice Test Drill",
                                            isTest = true
                                        )
                                        val intent = dispatcher.createWhatsAppIntent(contact.phone, msg)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "WhatsApp Test", tint = Color(0xFF075E54))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CrimsonRed,
            contentColor = PureWhite,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }
    }

    // Add Contact Dialog
    if (showAddDialog) {
        ContactEditDialog(
            title = "Add Trusted Emergency Contact",
            initialContact = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newContact ->
                onAddContact(newContact)
                showAddDialog = false
            }
        )
    }

    // Edit Contact Dialog
    contactToEdit?.let { contact ->
        ContactEditDialog(
            title = "Edit Emergency Contact",
            initialContact = contact,
            onDismiss = { contactToEdit = null },
            onConfirm = { updated ->
                onUpdateContact(updated)
                contactToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactEditDialog(
    title: String,
    initialContact: TrustedContact?,
    onDismiss: () -> Unit,
    onConfirm: (TrustedContact) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "Mother") }
    var isPrimary by remember { mutableStateOf(initialContact?.isPrimary ?: false) }
    var sendSms by remember { mutableStateOf(initialContact?.sendSms ?: true) }
    var sendWhatsApp by remember { mutableStateOf(initialContact?.sendWhatsApp ?: true) }
    var directCall by remember { mutableStateOf(initialContact?.directCall ?: false) }

    val relationships = listOf("Mother", "Father", "Spouse", "Sister", "Brother", "Trusted Friend", "Police / 112", "Women Helpline / 1091")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (with Country Code)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_phone_input")
                )

                Text("Relationship:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationships.forEach { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = { relationship = rel },
                            label = { Text(rel, fontSize = 11.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                    Text("Set as Primary Emergency Contact", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sendSms, onCheckedChange = { sendSms = it })
                    Text("Send Auto-SMS with GPS Map Link", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = sendWhatsApp, onCheckedChange = { sendWhatsApp = it })
                    Text("Generate WhatsApp Alert", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = directCall, onCheckedChange = { directCall = it })
                    Text("Instant Call Shortcut on SOS", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val contact = initialContact?.copy(
                            name = name,
                            phone = phone,
                            relationship = relationship,
                            isPrimary = isPrimary,
                            sendSms = sendSms,
                            sendWhatsApp = sendWhatsApp,
                            directCall = directCall
                        ) ?: TrustedContact(
                            name = name,
                            phone = phone,
                            relationship = relationship,
                            isPrimary = isPrimary,
                            sendSms = sendSms,
                            sendWhatsApp = sendWhatsApp,
                            directCall = directCall
                        )
                        onConfirm(contact)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                modifier = Modifier.testTag("confirm_contact_button")
            ) {
                Text("Save Contact", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
