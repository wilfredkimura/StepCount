package com.example.stepcount.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stepcount.core.components.ConfirmationDialog
import com.example.stepcount.core.components.DangerButton
import com.example.stepcount.core.components.PrimaryButton
import com.example.stepcount.core.components.StepTopAppBar

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
                    // Dark Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.DarkMode, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Dark Theme", style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = state.isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Distance Unit Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.Straighten, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Units (Metric km)", style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = state.isKilometers,
                            onCheckedChange = { viewModel.toggleUnits(it) }
                        )
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

                        Text(
                            text = "Custom Milestone Alert Point",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Choose the progress point (e.g. 50% or 65%) to get a motivational alert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Preset Milestone Percentage Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50, 65, 75, 80).forEach { preset ->
                                val selected = state.milestonePercentage == preset
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setMilestonePercentage(preset) },
                                    label = { Text("$preset%") },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Continuous Slider for Custom Percentage Selection
                        Text(
                            text = "Milestone Threshold: ${state.milestonePercentage}% of goal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Slider(
                            value = state.milestonePercentage.toFloat(),
                            onValueChange = { viewModel.setMilestonePercentage(it.toInt()) },
                            valueRange = 25f..90f,
                            steps = 12
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Test Notification Trigger Button
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

            // Data & Synchronization Section
            Text(
                text = "Data Synchronization",
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
                    Text(
                        text = "Sync pending step records to the server database.",
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
                Icon(imageVector = Icons.Rounded.Logout, contentDescription = null)
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
                text = "StepCount Version 1.0.0",
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
