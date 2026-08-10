package com.example.stepcount.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepcount.core.components.MetricCard
import com.example.stepcount.core.components.StatusBadge
import com.example.stepcount.core.components.StepBottomNavBar
import com.example.stepcount.core.theme.*
import com.example.stepcount.sensor.ActivityRecognitionPermissionEffect
import com.example.stepcount.sensor.StepSensorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main dashboard screen displaying real-time step progress, fitness metrics, and motivation.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSettings: () -> Unit,
    onBottomNavNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize sensor listener
    val sensorManager = remember {
        StepSensorManager(
            context = context,
            onStepCountUpdated = { steps ->
                viewModel.onStepCountUpdatedFromSensor(steps)
            }
        )
    }

    ActivityRecognitionPermissionEffect {
        sensorManager.startListening(state.liveSteps)
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorManager.stopListening()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = state.progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "StepProgressAnimation"
    )

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date())
    }

    Scaffold(
        bottomBar = {
            StepBottomNavBar(
                currentRoute = "dashboard",
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
            // Top Bar Row: Date & Settings Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.isGuestMode) {
                        StatusBadge(
                            text = "Guest",
                            icon = Icons.Rounded.PersonOutline,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goal Achieved Celebration Banner
            if (state.liveSteps >= state.dailyGoal && state.dailyGoal > 0) {
                Surface(
                    color = GoalMetGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoalMetGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = GoalMetGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily Goal Reached! Great job!",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoalMetGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Circular Step Progress Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .padding(8.dp)
            ) {
                // Background Track Ring
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )
                // Animated Active Progress Ring
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (state.liveSteps >= state.dailyGoal) GoalMetGreen else MaterialTheme.colorScheme.primary,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )

                // Inside Step Numbers
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%,d".format(state.liveSteps),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Goal: %,d (${state.goalPercentage}%)".format(state.dailyGoal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3 Fitness Metric Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val calories = (state.liveSteps * 0.04).toInt()
                val distanceKm = (state.liveSteps * 0.762) / 1000.0
                val activeMin = (state.liveSteps / 100).toInt()

                MetricCard(
                    label = "Calories",
                    value = "$calories kcal",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = MetricCaloriesColor,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Distance",
                    value = String.format(Locale.US, "%.1f km", distanceKm),
                    icon = Icons.Rounded.Straighten,
                    iconTint = MetricDistanceColor,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Active Time",
                    value = "$activeMin min",
                    icon = Icons.Rounded.Timer,
                    iconTint = MetricTimeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Motivational Quote Card
            state.motivationalQuote?.let { quote ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.FormatQuote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Daily Motivation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.fetchMotivationalQuote() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "New Quote",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${quote.quote}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "— ${quote.author}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
