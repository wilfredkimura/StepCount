package com.example.stepcount.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stepcount.core.components.MetricCard
import com.example.stepcount.core.components.PrimaryButton
import com.example.stepcount.core.components.SecondaryButton
import com.example.stepcount.core.components.StepBottomNavBar
import com.example.stepcount.core.theme.MetricCaloriesColor
import com.example.stepcount.core.theme.MetricDistanceColor
import com.example.stepcount.core.theme.MetricTimeColor
import java.util.Locale

/**
 * User Profile screen displaying account information, daily goal customization, and lifetime achievements.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onBottomNavNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            StepBottomNavBar(
                currentRoute = "profile",
                onNavigate = onBottomNavNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Settings action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Avatar & Name
            val displayName = state.userProfile?.name ?: "User"
            val displayEmail = state.userProfile?.email ?: "local@user"
            val initial = displayName.take(1).uppercase(Locale.US)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayEmail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Step Goal Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Step Goal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = { viewModel.toggleEditGoal(!state.isEditingGoal) }) {
                            Text(if (state.isEditingGoal) "Cancel" else "Edit")
                        }
                    }

                    if (!state.isEditingGoal) {
                        Text(
                            text = "%,d steps / day".format(state.currentGoal),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.tempGoalInput,
                            onValueChange = { viewModel.onGoalInputChange(it) },
                            label = { Text("Target Steps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Goal Presets
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(5000, 8000, 10000, 12000).forEach { preset ->
                                SuggestionChip(
                                    onClick = { viewModel.selectPresetGoal(preset) },
                                    label = { Text("${preset / 1000}k") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "Save Target",
                            onClick = { viewModel.saveGoal() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lifetime Walking Statistics Header
            Text(
                text = "Lifetime Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3 Metric Cards for Lifetime Totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "Total Steps",
                    value = "%,d".format(state.totalLifetimeSteps),
                    icon = Icons.Rounded.DirectionsWalk,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Distance",
                    value = String.format(Locale.US, "%.1f km", state.totalDistanceKm),
                    icon = Icons.Rounded.Straighten,
                    iconTint = MetricDistanceColor,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Calories",
                    value = "%,d kcal".format(state.totalCalories),
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = MetricCaloriesColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
