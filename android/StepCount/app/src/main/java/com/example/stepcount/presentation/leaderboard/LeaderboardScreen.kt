package com.example.stepcount.presentation.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stepcount.core.components.StatusBadge
import com.example.stepcount.core.components.StepBottomNavBar
import com.example.stepcount.core.theme.PodiumBronze
import com.example.stepcount.core.theme.PodiumGold
import com.example.stepcount.core.theme.PodiumSilver
import com.example.stepcount.domain.model.LeaderboardEntry

/**
 * Screen displaying rankings and top 3 podium standings.
 */
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onBottomNavNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            StepBottomNavBar(
                currentRoute = "leaderboard",
                onNavigate = onBottomNavNavigate
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See how you rank against fellow walkers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Rankings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Tab Row
            TabRow(
                selectedTabIndex = when (state.activePeriod) {
                    "today" -> 0
                    "week" -> 1
                    else -> 2
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Tab(
                    selected = state.activePeriod == "today",
                    onClick = { viewModel.switchPeriod("today") },
                    text = { Text("Today", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = state.activePeriod == "week",
                    onClick = { viewModel.switchPeriod("week") },
                    text = { Text("This Week", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = state.activePeriod == "all_time",
                    onClick = { viewModel.switchPeriod("all_time") },
                    text = { Text("All Time", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top 3 Podium Row (if available)
            if (state.topThree.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place (Silver)
                    if (state.topThree.size > 1) {
                        PodiumCard(
                            entry = state.topThree[1],
                            medalColor = PodiumSilver,
                            height = 130.dp,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // 1st Place (Gold)
                    PodiumCard(
                        entry = state.topThree[0],
                        medalColor = PodiumGold,
                        height = 155.dp,
                        modifier = Modifier.weight(1.1f)
                    )

                    // 3rd Place (Bronze)
                    if (state.topThree.size > 2) {
                        PodiumCard(
                            entry = state.topThree[2],
                            medalColor = PodiumBronze,
                            height = 115.dp,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Rankings List for 4th and beyond
            if (state.entries.isEmpty() && !state.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Leaderboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No rankings available yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.restOfLeaderboard, key = { it.userId }) { entry ->
                        LeaderboardListRow(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(
    entry: LeaderboardEntry,
    medalColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.height(height)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(medalColor)
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "%,d".format(entry.steps),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LeaderboardListRow(entry: LeaderboardEntry) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (entry.isCurrentUser) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "%,d steps".format(entry.steps),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
