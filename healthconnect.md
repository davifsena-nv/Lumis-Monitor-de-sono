# Health Connect API — Referência para o Lumis

## Setup de Permissões

### AndroidManifest.xml
```xml
<!-- Dentro de <activity> da MainActivity -->
<intent-filter>
    <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"/>
</intent-filter>
```

### Solicitar permissões ao usuário
```kotlin
// Em MainActivity ou HomeViewModel
val permissions = setOf(
    HealthPermission.getReadPermission(SleepSessionRecord::class)
)

val requestPermissions = registerForActivityResult(
    PermissionController.createRequestPermissionResultContract()
) { granted ->
    if (HealthPermission.getReadPermission(SleepSessionRecord::class) in granted) {
        // Permissão concedida — carregar dados
        viewModel.loadSleepData()
    } else {
        // Mostrar explicação para o usuário
    }
}

// Chamar quando necessário:
requestPermissions.launch(permissions)
```

---

## Leitura de Sessões de Sono

### Última noite
```kotlin
suspend fun readLastNight(): SleepSessionRecord? {
    val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)
    val request = ReadRecordsRequest(
        recordType = SleepSessionRecord::class,
        timeRangeFilter = TimeRangeFilter.between(yesterday, Instant.now()),
        ascendingOrder = false,
        pageSize = 1
    )
    return client.readRecords(request).records.firstOrNull()
}
```

### Últimos 7 dias (para score semanal)
```kotlin
suspend fun readWeek(): List<SleepSessionRecord> {
    val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
    val request = ReadRecordsRequest(
        recordType = SleepSessionRecord::class,
        timeRangeFilter = TimeRangeFilter.between(weekAgo, Instant.now())
    )
    return client.readRecords(request).records
}
```

---

## Estrutura de uma Sessão de Sono

```kotlin
SleepSessionRecord(
    startTime = Instant,          // início do sono
    endTime = Instant,            // fim do sono
    startZoneOffset = ZoneOffset, // fuso horário
    endZoneOffset = ZoneOffset,
    stages = listOf(
        SleepSessionRecord.Stage(
            startTime = Instant,
            endTime = Instant,
            stage = Int  // constante abaixo
        )
    ),
    title = String?,     // nome da sessão (opcional)
    notes = String?      // notas (opcional)
)
```

### Constantes de Fase
```kotlin
SleepSessionRecord.STAGE_TYPE_UNKNOWN        // 0 — desconhecido
SleepSessionRecord.STAGE_TYPE_AWAKE          // 1 — acordado
SleepSessionRecord.STAGE_TYPE_SLEEPING       // 2 — dormindo (genérico)
SleepSessionRecord.STAGE_TYPE_OUT_OF_BED     // 3 — fora da cama
SleepSessionRecord.STAGE_TYPE_SLEEPING_LIGHT // 4 — sono leve ← usar
SleepSessionRecord.STAGE_TYPE_SLEEPING_DEEP  // 5 — sono profundo ← usar
SleepSessionRecord.STAGE_TYPE_SLEEPING_REM   // 6 — REM ← usar
```

---

## Parsing de Fases para o DataScreen

```kotlin
data class SleepPhaseData(
    val startMinute: Float,   // minutos desde início da sessão
    val endMinute: Float,
    val phase: String         // "deep", "rem", "light", "awake"
)

fun parseSleepPhases(session: SleepSessionRecord): List<SleepPhaseData> {
    val sessionStart = session.startTime

    return session.stages.map { stage ->
        val startMin = Duration.between(sessionStart, stage.startTime).toMinutes().toFloat()
        val endMin = Duration.between(sessionStart, stage.endTime).toMinutes().toFloat()
        val phaseName = when (stage.stage) {
            SleepSessionRecord.STAGE_TYPE_SLEEPING_DEEP  -> "deep"
            SleepSessionRecord.STAGE_TYPE_SLEEPING_REM   -> "rem"
            SleepSessionRecord.STAGE_TYPE_SLEEPING_LIGHT -> "light"
            SleepSessionRecord.STAGE_TYPE_AWAKE          -> "awake"
            else -> "unknown"
        }
        SleepPhaseData(startMin, endMin, phaseName)
    }
}
```

---

## Checklist de Integração

- [ ] Health Connect instalado no dispositivo (nativo no Android 14+)
- [ ] Samsung Health configurado para sincronizar com Health Connect
  - Samsung Health → Configurações → Conectar apps → Health Connect → Ativar
- [ ] Galaxy Fit 3 sincronizado com Samsung Health antes de tentar ler dados
- [ ] Permissão `READ_SLEEP_SESSION` concedida pelo usuário
- [ ] Testar com pelo menos uma noite de dados antes de implementar score

---

## Fallback: sem dados disponíveis

```kotlin
sealed class SleepDataState {
    object Loading : SleepDataState()
    object NoPermission : SleepDataState()
    object NoData : SleepDataState()
    object HealthConnectUnavailable : SleepDataState()
    data class Success(
        val sessions: List<SleepSessionRecord>,
        val weeklyScore: Int,
        val lumisState: LumisState
    ) : SleepDataState()
}
```

Quando `NoData`: mostrar Lumis no estado `REGULAR` (neutro) com mensagem
*"Use o Galaxy Fit 3 esta noite para ver seus dados amanhã"*.
