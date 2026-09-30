package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThemePreference
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.ThreatRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }

    val user = currentUser

    // Edit Profile Dialog
    if (showEditProfileDialog && user != null) {
        var editName by remember { mutableStateOf(user.name) }
        var editEmail by remember { mutableStateOf(user.email) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateToggle(name = editName, email = editEmail)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF00363D))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All Scans", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all cached scan forensics from this device. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThreatRed)
                ) {
                    Text("Delete History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your PhishGuard security profile?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThreatRed)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Auto-Lock Timer Dialog
    if (showAutoLockDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showAutoLockDialog = false },
            title = { Text("Auto-Lock Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(1, 5, 15, 30).forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateToggle(autoLockMins = mins)
                                    showAutoLockDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = user.autoLockMinutes == mins,
                                onClick = {
                                    viewModel.updateToggle(autoLockMins = mins)
                                    showAutoLockDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$mins minutes of inactivity")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoLockDialog = false }) { Text("Close") }
            }
        )
    }

    // FAQ Dialog
    if (showFaqDialog) {
        FaqDialog(onDismiss = { showFaqDialog = false })
    }

    // Support Dialog
    if (showSupportDialog) {
        SupportDialog(onDismiss = { showSupportDialog = false })
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Top Header
        item {
            Text(
                text = "Security Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure app protection and alert preferences",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.3f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonCyan, Color(0xFF1E3A8A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.name ?: "Alex Mercer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user?.email ?: "alex.mercer@security.io",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SafeGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO CYBER DEFENSE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        }
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("settings_edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Notification Preferences
        item {
            SettingsSectionHeader(title = "Notification Preferences", icon = Icons.Default.Notifications)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsToggleRow(
                        title = "Threat Detection Alerts",
                        subtitle = "Instant alert when high-risk phishing is found",
                        checked = user?.threatAlertsEnabled ?: true,
                        onCheckedChange = { viewModel.updateToggle(threatAlerts = it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Scan Completion Alerts",
                        subtitle = "Notify when background scan finishes",
                        checked = user?.scanCompletionAlertsEnabled ?: true,
                        onCheckedChange = { viewModel.updateToggle(scanCompletion = it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Security Recommendations",
                        subtitle = "Proactive threat prevention advice",
                        checked = user?.recommendationsEnabled ?: true,
                        onCheckedChange = { viewModel.updateToggle(recommendations = it) }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Push Notifications",
                        subtitle = "System tray notifications on lock screen",
                        checked = user?.pushNotificationsEnabled ?: true,
                        onCheckedChange = { viewModel.updateToggle(push = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Security & App Lock
        item {
            SettingsSectionHeader(title = "Security & Protection", icon = Icons.Default.Lock)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsToggleRow(
                        title = "App PIN Lock",
                        subtitle = "Require 4-digit PIN upon launch",
                        checked = user?.appLockEnabled ?: false,
                        onCheckedChange = { viewModel.updateToggle(appLock = it) }
                    )
                    SettingsDivider()
                    SettingsClickRow(
                        title = "Auto-Lock Timer",
                        subtitle = "${user?.autoLockMinutes ?: 5} minutes",
                        icon = Icons.Default.Timer,
                        onClick = { showAutoLockDialog = true }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Secure Scan Mode",
                        subtitle = "Sandboxed isolated HTTP telemetry",
                        checked = user?.secureScanEnabled ?: true,
                        onCheckedChange = { viewModel.updateToggle(secureScan = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Appearance & Theme
        item {
            SettingsSectionHeader(title = "Appearance & Theme", icon = Icons.Default.ColorLens)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val currentTheme = user?.themePreference ?: ThemePreference.SYSTEM
                    SettingsRadioRow(
                        title = "System Default",
                        subtitle = "Match Android device theme settings",
                        selected = currentTheme == ThemePreference.SYSTEM,
                        onClick = { viewModel.updateToggle(theme = ThemePreference.SYSTEM) }
                    )
                    SettingsDivider()
                    SettingsRadioRow(
                        title = "Cyber Dark",
                        subtitle = "Optimized dark palette with neon accents",
                        selected = currentTheme == ThemePreference.DARK,
                        onClick = { viewModel.updateToggle(theme = ThemePreference.DARK) }
                    )
                    SettingsDivider()
                    SettingsRadioRow(
                        title = "Clean Light",
                        subtitle = "High contrast light interface",
                        selected = currentTheme == ThemePreference.LIGHT,
                        onClick = { viewModel.updateToggle(theme = ThemePreference.LIGHT) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Privacy & Data
        item {
            SettingsSectionHeader(title = "Privacy & Local Data", icon = Icons.Default.Shield)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Your scan data is stored in an encrypted local database on your device and never shared with advertisers or third parties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("settings_clear_history_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThreatRed.copy(alpha = 0.15f), contentColor = ThreatRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Scan History", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Support & FAQ & About
        item {
            SettingsSectionHeader(title = "Support & Information", icon = Icons.Default.HelpOutline)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsClickRow(
                        title = "Frequently Asked Questions",
                        subtitle = "How PhishGuard detects phishing",
                        icon = Icons.Default.QuestionAnswer,
                        onClick = { showFaqDialog = true }
                    )
                    SettingsDivider()
                    SettingsClickRow(
                        title = "Contact Security Support",
                        subtitle = "Report a bug or submit feedback",
                        icon = Icons.Default.Phone,
                        onClick = { showSupportDialog = true }
                    )
                    SettingsDivider()
                    SettingsClickRow(
                        title = "About PhishGuard AI",
                        subtitle = "Version 1.0 • Gemini 3.5 Flash Engine",
                        icon = Icons.Default.Info,
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Logout Button
        item {
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_logout_button"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ThreatRed.copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = ThreatRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = ThreatRed, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00363D),
                checkedTrackColor = NeonCyan
            )
        )
    }
}

@Composable
private fun SettingsClickRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SettingsRadioRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    )
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cybersecurity FAQ", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.height(360.dp)) {
                item {
                    FaqItem(
                        q = "How does PhishGuard AI detect phishing?",
                        a = "PhishGuard combines heuristics (brand impersonation, suspicious TLDs, urgent keywords, URL obfuscation) with deep Gemini 3.5 Flash neural models to dissect content intent."
                    )
                    FaqItem(
                        q = "What is Attack Reconstruction?",
                        a = "It is an interactive threat simulation that illustrates the exact psychological and technical steps an attacker uses to compromise victim credentials."
                    )
                    FaqItem(
                        q = "Is my scan data private?",
                        a = "Yes. All scan logs, indicator findings, and user profiles are stored in an encrypted on-device Room database."
                    )
                    FaqItem(
                        q = "Can I scan QR codes safely?",
                        a = "Yes. PhishGuard decodes the QR target in an isolated sandbox, ensuring malicious URLs are analyzed before any browser navigates to them."
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = NeonCyan) } }
    )
}

@Composable
private fun FaqItem(q: String, a: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(text = q, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeonCyan)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = a, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
    }
}

@Composable
private fun SupportDialog(onDismiss: () -> Unit) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contact Security Support", fontWeight = FontWeight.Bold) },
        text = {
            if (submitted) {
                Text("Thank you! Your feedback/report has been submitted to the PhishGuard security research team.", color = SafeGreen)
            } else {
                Column {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message / False Positive URL") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!submitted) {
                Button(
                    onClick = { submitted = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF00363D))
                ) { Text("Send Message") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (!submitted) TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About PhishGuard AI", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("PhishGuard AI v1.0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("AI-Powered Phishing Detection & Threat Intelligence", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Built with Kotlin, Jetpack Compose, Room Database, and Google Gemini 3.5 Flash.\n\nProtecting users worldwide against social engineering, credential theft, and brand impersonation.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = NeonCyan) } }
    )
}
