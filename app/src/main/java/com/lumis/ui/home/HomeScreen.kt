package com.lumis.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumis.character.LumisController
import com.lumis.character.LumisState
import com.lumis.health.SleepDataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lumis", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate("data") }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Dados")
                    }
                    IconButton(onClick = { navController.navigate("alarm") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Área do personagem Lumis (placeholder até Fase 4)
            LumisCharacterArea(
                state = uiState.lumisState,
                score = uiState.weeklyScore,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Cards informativos
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlarmCard(
                    nextAlarmTime = uiState.nextAlarmTime,
                    isActive = uiState.isAlarmActive,
                    onClick = { navController.navigate("alarm") }
                )

                when (val state = uiState.sleepDataState) {
                    is SleepDataState.Success -> SleepSummaryCard(
                        score = uiState.weeklyScore,
                        summary = uiState.lastNightSummary,
                        lumisState = uiState.lumisState,
                    )
                    is SleepDataState.NoData -> NoDataCard()
                    is SleepDataState.NoPermission -> PermissionCard()
                    is SleepDataState.HealthConnectUnavailable -> UnavailableCard()
                    is SleepDataState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LumisCharacterArea(
    state: LumisState,
    score: Int,
    modifier: Modifier = Modifier,
) {
    val glowColor = LumisController.getGlowColor(state)
    val statusText = LumisController.getStatusText(state)

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                    radius = 600f
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder do personagem 3D (Fase 4: substituir por SceneView)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.6f),
                                glowColor.copy(alpha = 0.1f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (state) {
                        LumisState.EXCELLENT -> "✨"
                        LumisState.GOOD -> "😊"
                        LumisState.REGULAR -> "😐"
                        LumisState.BAD -> "😔"
                        LumisState.TERRIBLE -> "😴"
                    },
                    fontSize = 72.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun AlarmCard(
    nextAlarmTime: String?,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Próximo alarme",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (isActive) nextAlarmTime ?: "Calculando..." else "Alarme desativado",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SleepSummaryCard(
    score: Int,
    summary: String,
    lumisState: LumisState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Última noite",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = summary.ifEmpty { "Sem dados ainda" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = LumisController.getGlowColor(lumisState),
                )
                Text(
                    text = "score",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NoDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Use o Galaxy Fit 3 esta noite para ver seus dados amanhã 💤",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PermissionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Permissão do Health Connect necessária para ler dados de sono.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun UnavailableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Health Connect não disponível neste dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
