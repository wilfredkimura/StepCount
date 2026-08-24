package com.example.stepcount.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcount.core.components.ConfirmationDialog
import com.example.stepcount.core.components.DangerButton
import com.example.stepcount.core.components.PrimaryButton
import com.example.stepcount.core.components.StepTopAppBar
import com.example.stepcount.core.theme.AppThemeMode

/**
 * Settings screen providing preferences, sync trigger, account sign-out, and account deletion.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Display user-friendly Toast notifications for manual sync results/errors
    LaunchedEffect(Unit) {
        viewModel.toastMessageEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogoutSuccess()
        }
    }

    val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.toggleNotifications(isGranted)
    }


    Scaffold(
        topBar = {
            StepTopAppBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Preferences Section
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme Mode Selector (3-way Segmented Control)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SettingsBrightness,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Appearance Theme",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Choose light, dark, or follow system default",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val modes = listOf(
                                Triple(AppThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
                                Triple(AppThemeMode.DARK, "Dark", Icons.Rounded.DarkMode),
                                Triple(AppThemeMode.SYSTEM, "System", Icons.Rounded.SettingsBrightness)
                            )
                            modes.forEach { (mode, title, icon) ->
                                val isSelected = state.themeMode == mode
                                Surface(
                                    onClick = { viewModel.setThemeMode(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    // Distance Units Selector (2-way Segmented Control)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Distance Units",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Select preferred unit for distance calculations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val units = listOf(
                                Triple(true, "Kilometers (km)", Icons.Rounded.Straighten),
                                Triple(false, "Miles (mi)", Icons.Rounded.NearMe)
                            )
                            units.forEach { (isKm, title, icon) ->
                                val isSelected = state.isKilometers == isKm
                                Surface(
                                    onClick = { viewModel.setDistanceUnit(isKm) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                    // 24/7 Background Tracking Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "24/7 Background Tracking",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Record steps when app is closed, even if opened once a week",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.isPersistentTrackingEnabled,
                            onCheckedChange = { viewModel.togglePersistentTracking(context, it) }
                        )
                    }

                    if (state.isPersistentTrackingEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        // Ignore if settings intent fails
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryChargingFull,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disable Battery Restrictions")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goal & Milestone Notifications Section
            Text(
                text = "Goal Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Main Notification Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Goal & Milestone Alerts",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Notify when reaching milestone & 100% goal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.isNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !com.example.stepcount.sensor.hasNotificationPermission(context)) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleNotifications(enabled)
                                }
                            }
                        )
                    }

                    if (state.isNotificationsEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Milestone Percentage Slider
                        Text(
                            text = "Custom Milestone Alert: ${state.milestonePercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = state.milestonePercentage.toFloat(),
                            onValueChange = { viewModel.setMilestonePercentage(it.toInt()) },
                            valueRange = 25f..90f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Test Notification Trigger
                        OutlinedButton(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && !com.example.stepcount.sensor.hasNotificationPermission(context)) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.sendTestNotification()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Milestone Notification")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cloud Synchronization Section
            Text(
                text = "Cloud Backup & Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Automatic Cloud Sync Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Automatic Cloud Sync",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Automatically back up daily steps and update leaderboard rankings in background",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.isAutoCloudSyncEnabled,
                            onCheckedChange = { viewModel.toggleAutoCloudSync(context, it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "Manually upload pending step records to the server database right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = if (state.isSyncingNow) "Syncing..." else "Sync Now",
                        onClick = { viewModel.syncNow() },
                        enabled = !state.isSyncingNow,
                        leadingIcon = Icons.Rounded.Sync
                    )
                    state.syncSuccessMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account & Danger Actions
            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.promptSignOut(true) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.promptClearHistory(true) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Rounded.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Step History")
            }

            Spacer(modifier = Modifier.height(12.dp))

            DangerButton(
                text = "Delete Account & Data",
                onClick = { viewModel.promptDeleteAccount(true) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Version Footer
            Text(
                text = "StepCount Version 1.4.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Sign Out Dialog
    if (state.showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out of your account?",
            confirmText = "Sign Out",
            onConfirm = { viewModel.confirmSignOut() },
            onDismiss = { viewModel.promptSignOut(false) }
        )
    }

    // Clear History Dialog
    if (state.showClearHistoryDialog) {
        ConfirmationDialog(
            title = "Clear History",
            message = "This will erase all recorded daily steps from this device. Are you sure?",
            confirmText = "Clear History",
            onConfirm = { viewModel.confirmClearHistory() },
            onDismiss = { viewModel.promptClearHistory(false) },
            isDanger = true
        )
    }

    // Delete Account Dialog
    if (state.showDeleteAccountDialog) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Permanently delete your profile, ranking, and history records? This cannot be undone.",
            confirmText = "Delete Account",
            onConfirm = { viewModel.confirmDeleteAccount() },
            onDismiss = { viewModel.promptDeleteAccount(false) },
            isDanger = true
        )
    }
}
