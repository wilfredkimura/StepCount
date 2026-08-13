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
