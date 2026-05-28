# Lumis — Arquitetura Detalhada e Código de Referência

## Fase 1 — Setup do Projeto

### build.gradle (app)
```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Health Connect
    implementation("androidx.health.connect:connect-client:1.1.0")

    // SceneView (personagem 3D)
    implementation("io.github.sceneview:sceneview:2.2.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Vico (gráficos)
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.0")
}
```

### MainActivity.kt — Navegação
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LumisTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "home") {
                    composable("home") { HomeScreen(navController) }
                    composable("data") { DataScreen(navController) }
                    composable("alarm") { AlarmScreen(navController) }
                }
            }
        }
    }
}
```

---

## Fase 2 — Lógica do Alarme

### AlarmCalculator.kt
```kotlin
data class AlarmConfig(
    val numCycles: Int = 4,
    val cycleDuration: Int = 90,   // minutos
    val sleepLatency: Int = 15,    // minutos
    val maxWakeTime: LocalTime? = null
)

object AlarmCalculator {
    fun calculate(sleepDetectedAt: Instant, config: AlarmConfig): Instant {
        val totalMinutes = config.sleepLatency + (config.numCycles * config.cycleDuration)
        val wakeTime = sleepDetectedAt.plusSeconds(totalMinutes * 60L)

        // Respeitar horário limite se configurado
        config.maxWakeTime?.let { max ->
            val maxInstant = LocalDateTime.now()
                .with(max)
                .toInstant(ZoneOffset.systemDefault().rules.getOffset(Instant.now()))
            if (wakeTime.isAfter(maxInstant)) return maxInstant
        }

        return wakeTime
    }

    fun previewWakeTime(sleepAt: LocalTime, config: AlarmConfig): LocalTime {
        val totalMinutes = config.sleepLatency + (config.numCycles * config.cycleDuration)
        return sleepAt.plusMinutes(totalMinutes.toLong())
    }
}
```

### InactivityDetector.kt
```kotlin
class InactivityDetector(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getLastInteractionTime(): Instant? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000) // últimas 24h

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime, endTime
        )

        val lastUsed = stats
            .filter { it.lastTimeUsed > 0 }
            .maxByOrNull { it.lastTimeUsed }
            ?.lastTimeUsed

        return lastUsed?.let { Instant.ofEpochMilli(it) }
    }

    fun isInactive(thresholdMinutes: Int = 15): Boolean {
        val lastInteraction = getLastInteractionTime() ?: return false
        val minutesSince = Duration.between(lastInteraction, Instant.now()).toMinutes()
        return minutesSince >= thresholdMinutes
    }

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
```

### AlarmScheduler.kt
```kotlin
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleAlarm(wakeTime: Instant) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock garante disparo mesmo com Doze mode ativo
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            wakeTime.toEpochMilli(),
            pendingIntent
        )
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, ALARM_REQUEST_CODE, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    companion object {
        const val ALARM_REQUEST_CODE = 1001
    }
}
```

### AlarmReceiver.kt — Notificação em loop
```kotlin
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de alta prioridade
        val channel = NotificationChannel(
            "lumis_alarm", "Alarme Lumis",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        }
        notificationManager.createNotificationChannel(channel)

        val dismissIntent = Intent(context, DismissAlarmReceiver::class.java)
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, 0, dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "lumis_alarm")
            .setSmallIcon(R.drawable.ic_lumis)
            .setContentTitle("⏰ Hora de acordar!")
            .setContentText("Você completou seus ciclos de sono")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(dismissPendingIntent, true)
            .addAction(0, "Dispensar", dismissPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        notificationManager.notify(ALARM_NOTIFICATION_ID, notification)

        // Reagendar em 30s se não dispensado
        AlarmScheduler(context).scheduleRepeatingNotification()
    }

    companion object {
        const val ALARM_NOTIFICATION_ID = 42
    }
}
```

---

## Fase 3 — Health Connect e Score

### HealthConnectManager.kt
```kotlin
class HealthConnectManager(private val context: Context) {

    private val client = HealthConnectClient.getOrCreate(context)

    suspend fun readSleepSessions(days: Int = 7): List<SleepSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minus(days.toLong(), ChronoUnit.DAYS),
                Instant.now()
            )
        )
        return client.readRecords(request).records
    }

    suspend fun hasPermissions(): Boolean {
        val granted = client.permissionController.getGrantedPermissions()
        return HealthPermission.getReadPermission(SleepSessionRecord::class) in granted
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
}
```

### SleepScoreCalculator.kt
```kotlin
object SleepScoreCalculator {

    fun calculateWeeklyScore(sessions: List<SleepSessionRecord>): Int {
        if (sessions.isEmpty()) return 0

        val dailyScores = sessions.map { session ->
            calculateNightScore(session)
        }

        return dailyScores.average().toInt()
    }

    fun calculateNightScore(session: SleepSessionRecord): Int {
        val stages = session.stages
        if (stages.isEmpty()) return 50 // sem dados de fase = score médio

        val totalMinutes = stages.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }

        if (totalMinutes == 0L) return 0

        val deepMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_SLEEPING_DEEP }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val remMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_SLEEPING_REM }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val lightMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_SLEEPING_LIGHT }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val deepPct = (deepMinutes.toFloat() / totalMinutes) * 100
        val remPct = (remMinutes.toFloat() / totalMinutes) * 100
        val lightPct = (lightMinutes.toFloat() / totalMinutes) * 100

        // Pesos: deep 40%, REM 30%, light 20%, duração 10%
        val durationBonus = when {
            totalMinutes >= 480 -> 10 // 8h+
            totalMinutes >= 420 -> 7  // 7h+
            totalMinutes >= 360 -> 4  // 6h+
            else -> 0
        }

        return ((deepPct * 0.4) + (remPct * 0.3) + (lightPct * 0.2) + durationBonus)
            .toInt()
            .coerceIn(0, 100)
    }
}
```

---

## Fase 4 — Personagem Lumis

### LumisController.kt
```kotlin
enum class LumisState {
    EXCELLENT,  // score > 85
    GOOD,       // score 70–85
    REGULAR,    // score 50–70
    BAD,        // score 30–50
    TERRIBLE    // score < 30
}

object LumisController {

    fun getStateFromScore(score: Int): LumisState = when {
        score > 85 -> LumisState.EXCELLENT
        score > 70 -> LumisState.GOOD
        score > 50 -> LumisState.REGULAR
        score > 30 -> LumisState.BAD
        else -> LumisState.TERRIBLE
    }

    fun getAnimationName(state: LumisState): String = when (state) {
        LumisState.EXCELLENT -> "idle_excellent"
        LumisState.GOOD      -> "idle_good"
        LumisState.REGULAR   -> "idle_regular"
        LumisState.BAD       -> "idle_bad"
        LumisState.TERRIBLE  -> "idle_terrible"
    }

    fun getGlowColor(state: LumisState): Color = when (state) {
        LumisState.EXCELLENT -> Color(0xFFFFD700) // dourado
        LumisState.GOOD      -> Color(0xFF87CEEB) // azul céu
        LumisState.REGULAR   -> Color(0xFFB0C4DE) // azul opaco
        LumisState.BAD       -> Color(0xFF708090) // cinza
        LumisState.TERRIBLE  -> Color(0xFF8B0000) // vermelho escuro
    }

    fun getStatusText(state: LumisState): String = when (state) {
        LumisState.EXCELLENT -> "Semana incrível de sono! ✨"
        LumisState.GOOD      -> "Dormindo bem essa semana 😊"
        LumisState.REGULAR   -> "Sono regular, pode melhorar 😐"
        LumisState.BAD       -> "Semana difícil de sono 😔"
        LumisState.TERRIBLE  -> "Precisamos melhorar seu sono 💤"
    }
}
```

### HomeScreen.kt — SceneView + Lumis
```kotlin
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // Personagem 3D
        AndroidView(
            factory = { context ->
                SceneView(context).apply {
                    // Carregar modelo Lumis
                    // Ver references/sceneview.md para setup completo
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Card: próximo alarme
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Próximo alarme", style = MaterialTheme.typography.labelMedium)
                Text(
                    uiState.nextAlarmTime ?: "Configure o alarme",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    LumisController.getStatusText(uiState.lumisState),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

---

## Problemas Comuns e Soluções

### Poco mata o AlarmManager
```kotlin
// Instruir usuário programaticamente ao onboarding:
fun openBatteryOptimizationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}
```

### Health Connect não disponível
```kotlin
when (HealthConnectClient.getSdkStatus(context)) {
    HealthConnectClient.SDK_UNAVAILABLE -> {
        // Android muito antigo — mostrar mensagem
    }
    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
        // Redirecionar para Play Store para instalar Health Connect
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
        }
        context.startActivity(intent)
    }
    HealthConnectClient.SDK_AVAILABLE -> {
        // Tudo certo, prosseguir
    }
}
```

### UsageStats sem permissão
```kotlin
// Redirecionar para configurações
fun requestUsageStatsPermission(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}
```
