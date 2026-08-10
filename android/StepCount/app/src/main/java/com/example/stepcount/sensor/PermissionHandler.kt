package com.example.stepcount.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Checks if the physical activity recognition permission has been granted by the user.
 */
fun hasActivityRecognitionPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Permissions below Android 10 (API 29) are granted at install time
    }
}

/**
 * Composable dialog providing a clear explanation to the user before prompting for sensor permissions.
 */
@Composable
fun ActivityRecognitionPermissionEffect(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (hasActivityRecognitionPermission(context)) {
            onPermissionGranted()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showRationaleDialog = true
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Motion Tracking Permission",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "StepCount needs physical activity recognition access to count your walking steps throughout the day.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationaleDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    }
                ) {
                    Text("Allow Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }
}
