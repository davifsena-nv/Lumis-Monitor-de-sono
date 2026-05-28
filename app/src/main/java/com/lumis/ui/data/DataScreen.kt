package com.lumis.ui.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(
    navController: NavController,
    viewModel: DataViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados de sono") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.errorMessage?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(modifier = Modifier.padding(16.dp), text = error)
                }
                return@Column
            }

            // Score da semana
            Text("Semana de sono", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            WeekScoreChart(scores = uiState.weekScores)

            // Resumo da última noite
            Text("Última noite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LastNightSummary(
                totalHours = uiState.totalHoursLastNight,
                deepPercent = uiState.deepPercent,
                remPercent = uiState.remPercent,
                lightPercent = uiState.lightPercent,
            )

            // Fases da última noite (timeline simplificada)
            if (uiState.lastNightPhases.isNotEmpty()) {
                Text("Fases do sono", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SleepPhasesTimeline(phases = uiState.lastNightPhases)
            }
        }
    }
}

@Composable
private fun WeekScoreChart(scores: List<DayScore>) {
    if (scores.isEmpty()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(modifier = Modifier.padding(16.dp), text = "Sem dados da semana ainda.")
        }
        return
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                scores.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${day.score}",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor(day.score),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((day.score * 0.8f).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(scoreColor(day.score))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = day.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LastNightSummary(
    totalHours: Float,
    deepPercent: Int,
    remPercent: Int,
    lightPercent: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(label = "Total", value = "${String.format("%.1f", totalHours)}h", modifier = Modifier.weight(1f))
        StatCard(label = "Profundo", value = "$deepPercent%", color = Color(0xFF7EC8E3), modifier = Modifier.weight(1f))
        StatCard(label = "REM", value = "$remPercent%", color = Color(0xFF9B6DFF), modifier = Modifier.weight(1f))
        StatCard(label = "Leve", value = "$lightPercent%", color = Color(0xFFB0C4DE), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SleepPhasesTimeline(phases: List<SleepPhaseSegment>) {
    val totalMinutes = phases.maxOfOrNull { it.endMinute } ?: return

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                phases.forEach { phase ->
                    val startFraction = phase.startMinute / totalMinutes
                    val widthFraction = (phase.endMinute - phase.startMinute) / totalMinutes
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(widthFraction)
                            .offset(x = (startFraction * 100).toInt().dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(phaseColor(phase.phase))
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PhaseLegend("Profundo", Color(0xFF7EC8E3))
                PhaseLegend("REM", Color(0xFF9B6DFF))
                PhaseLegend("Leve", Color(0xFFB0C4DE))
                PhaseLegend("Acordado", Color(0xFFFF8C00))
            }
        }
    }
}

@Composable
private fun PhaseLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun phaseColor(phase: String): Color = when (phase) {
    "deep" -> Color(0xFF7EC8E3)
    "rem" -> Color(0xFF9B6DFF)
    "light" -> Color(0xFFB0C4DE)
    "awake" -> Color(0xFFFF8C00)
    else -> Color(0xFF708090)
}

private fun scoreColor(score: Int): Color = when {
    score > 85 -> Color(0xFFFFD700)
    score > 70 -> Color(0xFF9B6DFF)
    score > 50 -> Color(0xFF7EC8E3)
    score > 30 -> Color(0xFFB0C4DE)
    else -> Color(0xFF8B0000)
}
