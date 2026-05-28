# Progresso do Planejamento — Lumis

## Fase 1 — Setup do Projeto
**Status:** [x] Concluída

- [x] Criar projeto no Android Studio (Kotlin + Compose) — arquivos criados manualmente, abrir no AS para sync
- [x] Configurar `build.gradle.kts` com dependências (Health Connect, SceneView, Room, Vico, WorkManager, Hilt, KSP)
- [x] Estrutura de pacotes (`alarm/`, `health/`, `ui/`, `character/`, `data/`)
- [x] `MainActivity.kt` com NavHost (home, data, alarm)
- [x] AndroidManifest com permissões
- [x] Primeira leitura de dados do Health Connect funcionando (`HealthConnectManager.kt`)

## Fase 2 — Lógica do Alarme
**Status:** [x] Concluída

- [x] `InactivityDetector.kt` — UsageStatsManager, detecção de 15 min sem atividade
- [x] `AlarmCalculator.kt` — cálculo de ciclos (latência + numCiclos × duração)
- [x] `AlarmScheduler.kt` — `AlarmManager.setAlarmClock()` (resistente ao Doze)
- [x] `AlarmReceiver.kt` — notificação em loop + `DismissAlarmReceiver.kt`
- [x] `BootReceiver.kt` — reagenda WorkManager após reinício
- [x] WorkManager: `SleepDetectionWorker.kt` — verificação periódica a cada 5 min
- [x] `AlarmScreen.kt` — sliders, preview dinâmico, toggle ativo/inativo

## Fase 3 — Health Connect e Score
**Status:** [x] Concluída

- [x] `HealthConnectManager.kt` — leitura de sessões (última noite + 7 dias)
- [x] `SleepScoreCalculator.kt` — fórmula: deep×0.4 + rem×0.3 + light×0.2 + duração×0.1
- [x] Room: entidades `SleepNight` e `AlarmConfigEntity`, cache local
- [x] `SleepRepository.kt` — unifica Health Connect + Room
- [x] `DataScreen.kt` — gráfico de barras de score semanal + fases da noite

## Fase 4 — Personagem Lumis
**Status:** [ ] Não iniciada

- [ ] Modelo 3D no Blender (corpo perolado/translúcido, olhos grandes, 3 tufos)
- [ ] Exportar para GLB: animações `idle_[estado]` × 5 estados
- [ ] `LumisController.kt` — já criado! `getStateFromScore()`, `getAnimationName()`, `getGlowColor()`
- [ ] `HomeScreen.kt` — substituir placeholder emoji por SceneView com modelo real
- [ ] Transições suaves entre estados

## Fase 5 — Polimento
**Status:** [ ] Não iniciada

- [ ] Onboarding: fluxo de permissões (UsageStats, Health Connect, notificações, bateria)
- [ ] Tratamento de erros (Health Connect unavailable, sem dados, permissão negada)
- [ ] Fallback Poco: instruções para desativar otimização de bateria
- [ ] Ícone do app + splash screen
- [ ] Testes manuais com Galaxy Fit 3 real
- [ ] Ajustes de UI/UX finais

---

## Status atual

App instalado e rodando no Poco. Próximo passo: conceder permissão do Health Connect e sincronizar Galaxy Fit 3 com Samsung Health para ter dados de sono disponíveis.

---

## Notas de Sessões

### 2026-05-28 (sessão 1)
- Organização inicial do repositório: `architecture.md` e `healthconnect.md` movidos para `references/`
- `SKILL.md` movido para `.claude/skills/lumis/SKILL.md` — agora é auto-ativado como skill
- Pasta `memory/` criada localmente no projeto para rastrear progresso entre sessões
- Skill `/salvar` atualizada para registrar notas de sessão automaticamente ao fazer commit

### 2026-05-28 (sessão 3 — correções de build e Health Connect)
- Atualizado AGP `8.2.2` → `8.9.1`, Gradle `8.4` → `8.11.1`, `compileSdk/targetSdk` 34 → 36 (exigência do `connect-client:1.1.0`)
- App instalado com sucesso no Poco e rodando
- Adicionado fluxo de solicitação de permissão do Health Connect na `HomeScreen` via `rememberLauncherForActivityResult`
- Adicionado novo estado `HealthConnectNeedsUpdate` + card com botão "Atualizar Health Connect" que abre Play Store
- Corrigido `health_permissions.xml`: formato errado (`<array>`) e referência errada (`@array`) → agora usa formato `<permissions>` em `res/xml/` com referência `@xml/health_permissions`
- Corrigidas constantes de fase do sono (`STAGE_TYPE_DEEP/REM/LIGHT` sem prefixo `SLEEPING_`) e null-safety no `AlarmScheduler`

### 2026-05-28 (sessão 2 — desenvolvimento)
- Criados **35 arquivos** cobrindo Fases 1, 2 e 3 completas:
  - Gradle: `settings.gradle.kts`, `build.gradle.kts` (root + app), `gradle.properties`, `gradle-wrapper.properties`
  - Core: `LumisApplication.kt`, `MainActivity.kt`, `Theme.kt` (tema dark noturno)
  - UI: `HomeScreen.kt`, `HomeViewModel.kt`, `DataScreen.kt`, `DataViewModel.kt`, `AlarmScreen.kt`, `AlarmViewModel.kt`
  - Alarme: `AlarmConfig.kt`, `AlarmCalculator.kt`, `AlarmScheduler.kt`, `AlarmReceiver.kt`, `DismissAlarmReceiver.kt`, `BootReceiver.kt`, `InactivityDetector.kt`, `SleepDetectionWorker.kt`
  - Health: `HealthConnectManager.kt`, `SleepScoreCalculator.kt`, `SleepDataState.kt`
  - Personagem: `LumisController.kt` (5 estados + cores + textos)
  - Room: `SleepNight.kt`, `AlarmConfigEntity.kt`, `SleepNightDao.kt`, `AlarmConfigDao.kt`, `AppDatabase.kt`, `SleepRepository.kt`, `DatabaseModule.kt`
  - Res: `strings.xml`, `colors.xml`, `themes.xml`, `ic_lumis.xml`, `health_permissions.xml`
- **HomeScreen** tem placeholder emoji no lugar do modelo 3D (será substituído na Fase 4)
- Stack: AGP 8.2.2, Kotlin 1.9.22, Compose BOM 2024.02.00, Hilt 2.50, KSP 1.9.22-1.0.17
