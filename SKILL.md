---
name: lumis
description: >
  Contexto completo do projeto Lumis — app Android de alarme inteligente de sono
  com personagem 3D fofo. Use esta skill sempre que o usuário mencionar Lumis,
  alarme de ciclo de sono, personagem de sono, Health Connect, Galaxy Fit 3,
  cálculo de ciclos, UsageStatsManager, SceneView, estado do Lumis, score semanal,
  ou qualquer feature, bug, tela ou decisão técnica deste projeto Android em Kotlin.
---

# Lumis — App Android de Alarme Inteligente de Sono

App Android nativo (Kotlin + Jetpack Compose) que detecta automaticamente quando
o usuário foi dormir, calcula o melhor horário para acordar baseado em ciclos de
sono configuráveis, e representa a qualidade do sono da semana através de um
personagem 3D fofo chamado Lumis.

**Contexto de origem:** usuário tem Galaxy Fit 3 + celular Poco (não-Samsung).
O Smart Alarm nativo do Samsung Health não funciona em celulares não-Samsung,
portanto o Lumis preenche exatamente esse gap.

---

## Estrutura do Projeto

```
lumis/
├── app/src/main/
│   ├── java/com/lumis/
│   │   ├── MainActivity.kt              ← Entry point, navegação entre telas
│   │   ├── alarm/
│   │   │   ├── AlarmCalculator.kt       ← Lógica de cálculo de ciclos
│   │   │   ├── AlarmScheduler.kt        ← AlarmManager + notificações
│   │   │   └── InactivityDetector.kt    ← UsageStatsManager
│   │   ├── health/
│   │   │   ├── HealthConnectManager.kt  ← Leitura de dados de sono
│   │   │   └── SleepScoreCalculator.kt  ← Score semanal 0–100
│   │   ├── ui/
│   │   │   ├── home/HomeScreen.kt       ← Tela do Lumis (personagem 3D)
│   │   │   ├── data/DataScreen.kt       ← Gráficos e histórico
│   │   │   └── alarm/AlarmScreen.kt     ← Configurações do alarme
│   │   ├── character/
│   │   │   └── LumisController.kt       ← Estado e animação do personagem
│   │   └── data/
│   │       ├── AppDatabase.kt           ← Room database
│   │       └── SleepRepository.kt       ← Repositório unificado
│   ├── assets/
│   │   └── lumis/
│   │       ├── lumis_base.glb           ← Modelo 3D (Blender → glTF)
│   │       └── animations/              ← idle, happy, sad, etc.
│   └── AndroidManifest.xml
└── SKILL.md
```

Para rodar: abrir no Android Studio → conectar celular via USB → Run

---

## Stack Tecnológica

| Componente | Tecnologia | Versão |
|---|---|---|
| Linguagem | Kotlin | 1.9+ |
| UI | Jetpack Compose | BOM 2024 |
| Personagem 3D | SceneView | 2.2.1 |
| Dados de sono | Health Connect API | 1.1.0 |
| Background | WorkManager + AlarmManager | 2.9.0 |
| Inatividade | UsageStatsManager | Android SDK |
| Banco local | Room | 2.6.1 |
| Gráficos | Vico | 1.13.0 |

---

## Permissões (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.health.READ_SLEEP_SESSION"/>
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

> ⚠️ `PACKAGE_USAGE_STATS` exige ativação manual pelo usuário em
> Configurações → Apps → Acesso especial → Uso de dados de apps

---

## Fluxo Principal

```
Galaxy Fit 3 coleta dados durante o sono
        ↓
Samsung Health processa e classifica fases
        ↓
Health Connect sincroniza os dados
        ↓
HealthConnectManager.kt lê via Health Connect API
        ↓
SleepScoreCalculator calcula score dos últimos 7 dias (0–100)
        ↓
LumisController define estado do personagem → SceneView renderiza
        ↓
AlarmCalculator determina próximo horário ideal
        ↓
AlarmScheduler agenda via AlarmManager
        ↓
Notificação de alta prioridade → Galaxy Fit 3 vibra via espelhamento
```

---

## Lógica do Alarme

**Cálculo:**
```
horário_dormir = última_interação_tela + latência_sono
horário_alarme = horário_dormir + (num_ciclos × duração_ciclo)
```

**Parâmetros configuráveis pelo usuário:**
- `numCycles`: 1–6 ciclos (padrão: 4)
- `cycleDuration`: 60–120 min (padrão: 90 min)
- `sleepLatency`: 5–30 min (padrão: 15 min)
- `maxWakeTime`: horário limite máximo para acordar (ex: 08:00)

**Detecção de inatividade (InactivityDetector.kt):**
- `UsageStatsManager` monitora última interação com a tela
- Threshold: 15 minutos sem atividade → assume início do sono
- WorkManager agenda verificações periódicas a cada 5 min

**Estratégia de alarme:**
- `AlarmManager.setAlarmClock()` — garante disparo mesmo com app em background
- Notificação em loop (a cada 30s) até usuário dispensar
- Som no celular + vibração na pulseira simultâneos

---

## Personagem Lumis

Criatura fofa translúcida feita de matéria de sonho. Corpo perolado e
translúcido como bolha de sabão cruzada com nuvem. Olhos grandes e brilhantes,
três tufos de nuvem na cabeça, bracinhos pequenos.

**Estados baseados no score semanal:**

| Score | Estado | Descrição visual |
|---|---|---|
| > 85 | `EXCELLENT` | Flutuando, brilho dourado, estrelinhas orbitando |
| 70–85 | `GOOD` | Relaxado, brilho azul suave, sorriso leve |
| 50–70 | `REGULAR` | Cabisbaixo, olheiras leves, brilho opaco |
| 30–50 | `BAD` | Pesado, arrastando, brilho quase apagado |
| < 30 | `TERRIBLE` | Escuro, olhos vermelhos, mal se sustenta |

**Animações (arquivo .glb):**
- `idle_[estado]` — animação de repouso para cada estado (respiração, piscar)
- `transition_[de]_[para]` — transição suave entre estados
- Todas exportadas do Blender em formato glTF/GLB

**LumisController.kt:**
```kotlin
enum class LumisState { EXCELLENT, GOOD, REGULAR, BAD, TERRIBLE }

fun getStateFromScore(score: Int): LumisState
fun playAnimation(state: LumisState, sceneView: SceneView)
fun transitionTo(newState: LumisState)
```

---

## Telas

### HomeScreen (Tela Principal)
- Personagem Lumis 3D centralizado com SceneView
- Card inferior: próximo alarme calculado + tempo restante
- Card inferior: score semanal + resumo da última noite
- FAB para navegar para AlarmScreen

### DataScreen (Dados & Gráficos)
- Gráfico de linha do tempo: fases da última noite (Vico)
- Gráfico de barras: score dos últimos 7 dias (Vico)
- Cards: horas dormidas, ciclos completos, score médio
- Histórico navegável por data (← →)

### AlarmScreen (Configurações)
- Toggle: alarme ativo/inativo
- Slider: número de ciclos (1–6)
- Slider: duração do ciclo (60–120 min)
- Slider: latência de sono (5–30 min)
- TimePicker: horário limite máximo
- Toggle: som + vibração
- Preview dinâmico: *"Se dormir às 23h → acorda às 06h15"*

---

## Health Connect — Dados Lidos

```kotlin
// Sessão de sono
SleepSessionRecord(
    startTime, endTime,
    stages: List<SleepSessionRecord.Stage>
    // Stage.STAGE_TYPE_SLEEPING_LIGHT
    // Stage.STAGE_TYPE_SLEEPING_DEEP
    // Stage.STAGE_TYPE_SLEEPING_REM
    // Stage.STAGE_TYPE_AWAKE
)
```

**SleepScoreCalculator — fórmula do score:**
```
score = (pct_deep × 0.4) + (pct_rem × 0.3) + (pct_light × 0.2) + (consistency_bonus × 0.1)
```
Score de 0–100. Calculado sobre os últimos 7 dias de sessões.

---

## Room Database

```kotlin
@Entity data class SleepNight(
    @PrimaryKey val date: LocalDate,
    val startTime: Instant,
    val endTime: Instant,
    val score: Int,
    val deepMinutes: Int,
    val remMinutes: Int,
    val lightMinutes: Int,
    val cycles: Int
)

@Entity data class AlarmConfig(
    @PrimaryKey val id: Int = 1,
    val numCycles: Int = 4,
    val cycleDuration: Int = 90,
    val sleepLatency: Int = 15,
    val maxWakeTime: LocalTime? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isActive: Boolean = true
)
```

---

## Decisões de Design Importantes

- **Sem Smart Alarm Samsung** — funciona em qualquer Android, não depende de One UI
- **UsageStats para detectar sono** — não exige sensor físico no celular
- **Health Connect como fonte de dados** — API oficial Google, funciona com Samsung Health em qualquer Android
- **Vibração via espelhamento de notificação** — sem API direta para pulseira; Galaxy Fit 3 espelha notificações automaticamente
- **Notificações em loop** — alarme continua até usuário dispensar ativamente
- **AlarmManager.setAlarmClock()** — único método confiável para alarme com app em background no Android
- **Room para cache local** — evita requisições repetidas ao Health Connect

---

## Problemas Conhecidos e Mitigações

| Problema | Mitigação |
|---|---|
| Poco mata background service | Instruir usuário: Bateria → Sem restrições para o Lumis |
| Health Connect sem dados sincronizados | Fallback: mostrar "sem dados" + sugerir abrir Samsung Health |
| Delay de vibração na pulseira | Aceitável (1–2s); combinado com som no celular |
| UsageStats não disponível (permissão negada) | Fallback: usuário define horário manualmente |
| Modelo 3D pesado | Otimizar no Blender: remover geometria interna, usar LOD |

---

## Fases de Desenvolvimento

- **Fase 1 — Setup:** projeto, Health Connect, leitura de dados, navegação entre telas
- **Fase 2 — Alarme:** InactivityDetector, AlarmCalculator, AlarmScheduler, notificações
- **Fase 3 — Dados:** SleepScoreCalculator, Room, DataScreen com Vico
- **Fase 4 — Lumis:** modelo Blender, SceneView, LumisController, estados animados
- **Fase 5 — Polimento:** onboarding, erros, testes, ícone

Leia `references/architecture.md` para detalhes de cada fase com código de referência.
Leia `references/healthconnect.md` para exemplos completos da API de sono.
