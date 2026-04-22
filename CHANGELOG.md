# Changelog

All notable changes to Kokoromi are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Milestone 7] - 2026-04-22 — Archive, Field Notes, Settings & Export

Archive screen for completed/paused experiments, freeform field notes, full settings screen, JSON export and import, and opt-in daily check-in reminders.

### Added
- `ArchiveScreen` — lists COMPLETED, PAUSED, and ARCHIVED experiments with hypothesis, status, date range, and completion rate; Resume button for PAUSED experiments (guarded by active slot count)
- `ArchiveViewModel` — combines `GetNonActiveExperimentsUseCase` with per-experiment completion stats
- `GetNonActiveExperimentsUseCase` — flow of all non-ACTIVE experiments for the archive
- `FieldNotesScreen` — scrollable list of freeform field notes sorted by `createdAt`; tap to edit, swipe to delete
- `AddEditNoteScreen` — create and edit field notes with `createdAt` (user-editable observation time) and character limit (`FIELD_NOTE_BODY_MAX_CHARS = 5000`)
- `FieldNotesViewModel`, `AddEditNoteViewModel`
- `SaveFieldNoteUseCase`, `DeleteFieldNoteUseCase`, `GetFieldNotesUseCase`, `GetFieldNoteUseCase`
- `SettingsScreen` — theme selector (System/Light/Dark), reflection day picker, opt-in check-in reminder (switch + time picker), export, import, and delete-all-data
- `SettingsViewModel` — manages preferences, export, import, and clear-all flows via events
- `ExportDataUseCase` — collects all user data across five repositories and serialises to JSON
- `ImportDataUseCase` — parses the v1 export format and persists records; skips ID collisions for experiments and completions; upserts logs, reflections, and field notes
- `JsonExporter` (`data/export/`) — pure serialiser mirroring the v1 JSON schema
- `JsonImporter` (`data/imports/`) — resilient parser; malformed individual records are skipped rather than aborting the import
- `ClearAllDataUseCase` — wipes all five tables via `DatabaseCleaner`
- `UpdateExperimentStatusUseCase` — `pause()` / `archive()` / `endEarly()` / `resume()` with status guards; `resume()` enforces `MAX_ACTIVE_EXPERIMENTS`
- `CheckInReminderWorker` — `@HiltWorker`; suppresses notification if all active experiments are already logged today
- `ReminderScheduler` — schedules/cancels a `PeriodicWorkRequest` for the user-chosen time
- Notification channel and `POST_NOTIFICATIONS` permission (runtime request on Android 13+)
- `KokoromiBottomNav` — four-tab bottom navigation (Home, Notes, Archive, Settings)
- `GetReflectionPromptStateUseCaseTest` — 9 unit tests: non-reflection day early return, prompt shown/suppressed, week boundary calculation (Monday–Sunday), result field population
- `GetActiveExperimentsWithLogsUseCaseTest` — 9 unit tests: empty list, non-ACTIVE filtering, `todayLog` presence/absence, multi-experiment combination
- `UpdateExperimentStatusUseCaseTest` — 18 unit tests covering all four operations and their guards (not found, wrong status, slot cap)

### Changed
- `HomeScreen` — excludes PAUSED experiments from the active card list
- `KokoromiNavigation` — added routes for Archive, Field Notes, Settings; bottom nav wired
- `PreferencesRepository` / `DefaultPreferencesRepository` — added `reminderEnabled`, `reminderHour`, `reminderMinute` fields and setters
- `UserPreferences` — added reminder fields with defaults (`enabled = false`, `hour = 20`, `minute = 0`)
- `KokoromiApp` — creates notification channel on startup; provides `HiltWorkerFactory` via `Configuration.Provider`
- `KokoromiDatabase` — added `FieldNoteDao` binding

### Fixed
- PAUSED experiments incorrectly set to ARCHIVED status on pause — now correctly set to PAUSED
- PAUSED experiments incorrectly appearing on the Home Screen — now excluded from active list

### Accessibility
- `LogItem` and `ReflectionItem` in `ExperimentDetailScreen` — added `contentDescription` to `semantics(mergeDescendants = true)` blocks; timeline entries now announced to screen readers
- `ArchiveItem` — `contentDescription` includes hypothesis, status, date range, and completion rate
- "Edit Log" button in `ExperimentCard` — added `contentDescription`

---

## [Milestone 6] - 2026-04-07 — Reflections

Weekly Plus-Minus-Next reflection prompts, surfaced on the home screen on the configured day and accessible manually from the check-in screen at any time.

### Added
- `SaveReflectionUseCase` — validates and persists reflections; requires at least one field; enforces per-field character limit (`REFLECTION_MAX_CHARS = 2000`); trims whitespace and nullifies blank fields
- `GetReflectionPromptStateUseCase` — determines whether a weekly reflection prompt should be shown for a given experiment on the configured day
- `ReflectionScreen` — Plus/Minus/Next form with a prompt header; supports loading an existing reflection for the current week (edit flow)
- `ReflectionViewModel` — loads existing reflection on init; manages form state; calls `SaveReflectionUseCase` on submit
- `ReflectionPromptCard` — secondary-container banner on `HomeScreen` surfaced on the configured reflection day; taps navigate to `ReflectionScreen`
- `SaveReflectionUseCaseTest` — 14 unit tests covering happy path (each field in isolation, all three, trimming, blank→null, boundary length), all validation failure cases, and repository failure
- `FakePreferencesRepository` — instrumented-test double for `PreferencesRepository`
- `FakeReflectionRepository` — instrumented-test double for `ReflectionRepository`

### Changed
- `HomeViewModel` — now combines active experiments, completed experiments, and user preferences to compute `reflectionPrompts`; accepts `GetReflectionPromptStateUseCase` and `PreferencesRepository`
- `HomeUiState.Success` — added `reflectionPrompts: List<ReflectionPromptState>`
- `HomeScreen` — accepts `onNavigateToReflection` callback; renders `ReflectionPromptCard` per prompt above experiment cards
- `CheckInScreen` — accepts `onReflect` callback; shows `+ Add a quick reflection for today` text link below LOG ACTIVITY, always visible
- `KokoromiNavigation` — added `Screen.Reflection` route; wired reflection navigation from both `HomeScreen` and `CheckInScreen`
- `HomeScreenTest` — updated for new `HomeViewModel` constructor (`GetReflectionPromptStateUseCase`, `FakePreferencesRepository`) and `onNavigateToReflection` parameter
- `CheckInScreenTest` — updated for new `onReflect` parameter

### Fixed
- `CompletionButtons` in `CheckInScreen` — YES and SKIP buttons previously swapped their filled/outlined roles by branching the entire composable tree; fixed by giving each button its own independent style conditional, keeping positions stable across recomposition

### Accessibility
- `PlusMinusNextForm` fields — each input has a clear label for screen readers

### Deferred to M7
- Past reflections viewable in experiment detail and archive
- `PreferencesRepository` wired to reflection day setting in `SettingsScreen`

---

## [Milestone 5] - 2026-04-06 — Experiment Lifecycle & Completion

Experiment auto-transition on app open, completion decision screen (Persist/Pivot/Pause), and all supporting use cases.

### Added
- `CheckExperimentLifecycleUseCase` — runs on app open; auto-transitions ACTIVE → COMPLETED for any experiment whose `endDate` is in the past
- `ComputeCompletionStatsUseCase` — computes `totalDays`, `daysLogged`, `daysCompleted`, `completionRate`, and `avgMoodAfter` from an experiment's logs
- `CompleteExperimentUseCase` — three decision paths: `persist()` (archive + start new identical round), `pivot()` (archive + navigate to create), `pause()` (archive, no follow-up)
- `UpdateExperimentStatusUseCase` — `pause()` / `resume()` mid-experiment transitions with slot guard on resume
- `CompletionStats` domain model — stats data class produced by `ComputeCompletionStatsUseCase`
- `ExperimentWithLogs` domain model — groups `Experiment` with its full log list and today's log
- `GetActiveExperimentsWithLogsUseCase` — reactive flow combining experiments with per-experiment logs via `flatMapLatest` + `combine`
- `CompletionScreen` — end-of-experiment decision screen: summary card (dates, completion rate, hypothesis), learnings field, steering sheet (external + internal signal inputs), and PERSIST/PIVOT/PAUSE decision buttons
- `CompletionViewModel` — loads experiment + stats on init; manages form state; dispatches to `CompleteExperimentUseCase`; navigates on decision
- `CheckExperimentLifecycleUseCaseTest` — 6 unit tests covering expired, active, and mixed experiment lists
- `ComputeCompletionStatsUseCaseTest` — 9 unit tests covering all stats fields, null mood handling, and empty log set
- `CompleteExperimentUseCaseTest` — 14 unit tests covering all three decision paths, slot cap guard, not-found guard, learnings trimming, and side-effect verification

### Changed
- `HomeViewModel` — runs lifecycle check in `init`; `uiState` now combines active + completed experiments via `combine`
- `HomeUiState.Success` — added `completedExperiments: List<Experiment>` for banner display
- `HomeScreen` — shows `CompletionBanner` for each completed experiment; empty state now requires both lists to be empty
- `ExperimentDao` — added `getCompletedExperiments()` query
- `ExperimentRepository` / `DefaultExperimentRepository` — added `getCompletedExperiments(): Flow<List<Experiment>>`
- `FakeExperimentRepository` — implemented `getCompletedExperiments()`
- `KokoromiNavigation` — added `Screen.Completion` route; wired banner tap and post-decision navigation (persist/pause → home, pivot → create)
- `HomeScreenTest` — updated for new `HomeViewModel` constructor and `onNavigateToCompletion` parameter

### Accessibility
- `CompletionScreen` decision buttons — each has a `contentDescription` describing the consequence (e.g. "Persist: start a new round of this experiment")
- `SummaryCard` — `semantics(mergeDescendants = true)` so TalkBack reads it as a single unit

### Deferred to M7
- Pivot pre-fill: `CreateExperimentScreen` currently opens blank after PIVOT; pre-filling with the old experiment's data is deferred
- `CompletionButtons` layout swap in `CheckInScreen`
- Pause/early-exit mid-experiment dialogs (require `ExperimentDetail` as entry point)

---

## [Milestone 4] - 2026-04-06 — Daily Check-In Loop

Core daily interaction: logging check-ins, viewing streak history, and editing past entries.

### Added
- `LogDailyCheckInUseCase` — validates and upserts daily check-ins; enforces date bounds, mood range, and active-experiment guard
- `CheckInScreen` — full check-in UI with completion buttons, 1–5 mood rating, and notes input; supports both new log and edit (UPSERT) flows
- `CheckInViewModel` — manages form state and async status as separate flows; handles back-fill via optional `date` nav argument
- `CheckInDisplayState` — stable display data (experiment action, editing state, date) held separately from async `CheckInUiState`
- `StreakDisplay` component — row of colored squares on `ExperimentCard` showing one dot per day: full-opacity primary for today (unlogged), 60% primary for completed, `outlineVariant` for skipped/no log, 40% alpha for future days
- `GetActiveExperimentsWithLogsUseCase` — reactively combines experiments flow with each experiment's logs flow using `flatMapLatest` + `combine`
- `ExperimentWithLogs` domain model — groups `Experiment`, its full log list, and today's log for convenient card rendering
- `FakeDailyLogRepository` — instrumented-test double with `seedLog` helper
- `CheckInScreenTest` — 13 instrumented Compose UI tests covering title, prompts, YES/SKIP buttons, mood star states, tap-to-clear, and successful submit
- `LogDailyCheckInUseCaseTest` — unit tests covering happy path, date validation, mood bounds, and back-fill

### Changed
- `ExperimentCard` — accepts `ExperimentWithLogs` instead of `Experiment`; shows `StreakDisplay`; displays "Edit Log" button (full-width, outlined) when today is already logged; both YES and SKIP are equally styled (outlined) before logging — no nudge toward YES
- `HomeViewModel` — switched from `GetActiveExperimentsUseCase` to `GetActiveExperimentsWithLogsUseCase`
- `HomeUiState` — added `Error` variant; flow now emits it via `.catch {}` instead of silently hanging on DB failure
- `HomeScreen` — handles `HomeUiState.Error` with a user-facing message
- `HomeScreenTest` — updated setUp to use `GetActiveExperimentsWithLogsUseCase` and `FakeDailyLogRepository`
- `CheckInUiState` — `Ready` and `Error` variants no longer carry display fields; `Error` carries only a message string; `onErrorDismissed` simplified to one line

### Fixed
- Silent loading hang when `getActiveExperiments()` flow threw — now surfaces as `HomeUiState.Error`
- YES/SKIP buttons on `ExperimentCard` previously misled the user about today's log status; now corrected via `todayLog` from `ExperimentWithLogs`
- Three redundant `when` blocks in `CheckInScreen` replaced by a single `displayState` flow

### Accessibility
- `StreakDisplay` — `semantics { contentDescription }` on the `FlowRow` announces completion count as text ("X completed out of Y days so far"); individual dots are decorative
- Mood stars — each `IconButton` has a `contentDescription` describing filled/empty state; the currently selected star appends ", tap to clear"

### Other
- `.kotlin/` added to `.gitignore` (Kotlin compiler session files)

---

## [Milestone 3] - 2026-04-04 — Create & View Experiments

First usable feature: a user can create an experiment and see it on the home screen.

### Added
- `CreateExperimentUseCase` — validates input, enforces the 2-experiment slot cap, builds and persists the experiment
- `ExperimentValidator` — pure Kotlin validation (no Android imports) for hypothesis, action, why, and duration fields
- `GetActiveExperimentsUseCase` — returns a live `Flow` of active experiments
- `HomeScreen` — shows up to 2 active `ExperimentCard` components, empty state when none exist, hides the create button when both slots are full
- `HomeViewModel` — wired to `GetActiveExperimentsUseCase`, exposes `StateFlow<HomeUiState>`
- `ExperimentCard` component — displays experiment action, day progress, and YES/SKIP buttons
- `CreateExperimentScreen` — form with hypothesis, action, duration picker (presets + custom), optional why field, and a pact preview
- `CreateExperimentViewModel` — manages form state and submission, exposes `StateFlow<CreateExperimentUiState>`
- `CreateExperimentUseCaseTest` — 20 unit tests covering happy path, slot cap, all validation boundaries, whitespace trimming, and repository failure
- `HomeScreenTest` — 8 instrumented Compose UI tests covering empty state, card rendering, slot cap behaviour, and button callbacks
- `CreateExperimentScreenTest` — 9 instrumented Compose UI tests covering form fields, duration options, back navigation, pact preview, and successful submission

### Changed
- Default experiment duration set to 7 days
- Light and dark Material themes refined
- `CreateExperimentScreen` placeholder text and `ExperimentCard` display text polished
- Home screen title corrected; card accessibility description punctuation fixed

### Fixed
- `minimumInteractiveComponentSize` import corrected in `ExperimentCard`
- `DailyLogDao` and `ReflectionDao` upsert changed to `REPLACE` conflict strategy
- Keyboard padding added to `CreateExperimentScreen` via `imePadding()`

### Accessibility
- `ExperimentCard` — `semantics { contentDescription }` describing action and day progress
- YES/SKIP buttons — `Modifier.minimumInteractiveComponentSize()` enforcing 48dp touch targets
- Empty state emoji (`🔬`) — suppressed from screen readers (decorative)
- "Create experiment" and "New experiment" buttons — explicit `contentDescription` removing the `+` prefix that TalkBack would read as "plus"

### Dependencies
- Added `org.mockito.kotlin:mockito-kotlin:5.4.0` (unit test mocking)
- Updated `androidx.test.espresso:espresso-core` to `3.7.0` (Android 16 / API 36 compatibility)

---

## [Milestone 2] - 2026-04-03 — Data Layer

Full persistence layer. No UI — just the data foundation.

### Added
- Room entities: `ExperimentEntity`, `DailyLogEntity`, `ReflectionEntity`, `CompletionEntity`, `FieldNoteEntity`
- DAOs: `ExperimentDao`, `DailyLogDao`, `ReflectionDao`, `CompletionDao`, `FieldNoteDao` — all queries matching `DATA_MODEL.md`
- `KokoromiDatabase` — Room database with schema export and type converters (`LocalDate`, `Instant` ↔ ISO 8601 strings)
- Domain models: `Experiment`, `DailyLog`, `Reflection`, `Completion`, `FieldNote` (plain Kotlin data classes, no Room annotations)
- Repository interfaces and `Default*` implementations for all five entities, with full entity ↔ domain model mapping
- `PreferencesRepository` — DataStore-backed storage for reflection day and theme preferences
- Hilt modules: `DatabaseModule` (database + DAOs), `RepositoryModule` (binds all implementations), `PreferencesModule` (DataStore)
- `DatabaseTest` — instrumented tests covering DAO queries, type converters, and constraint behaviour against an in-memory database
- `ExperimentRepositoryTest` — instrumented tests covering the full repository layer end-to-end

### Fixed
- `DailyLogDao` and `ReflectionDao` upsert corrected to use `REPLACE` conflict strategy

---

## [Milestone 1] - 2026-04-03 — Project Scaffold

Empty Android project skeleton before any feature work.

### Added
- Android project: Kotlin, Gradle Kotlin DSL, minSdk 26, targetSdk 35, compileSdk 36
- Hilt setup: `KokoromiApp`, `DatabaseModule`, `RepositoryModule`
- Jetpack Compose + Navigation: `MainActivity`, `KokoromiNavigation`
- Material 3 theme: `Color.kt`, `Type.kt`, `Theme.kt`, `Shapes.kt`
- `Constants.kt`: `MAX_ACTIVE_EXPERIMENTS = 2`, character limits, duration defaults
- CI: `.github/workflows/android-build.yml` — build and lint on every PR
- `strings.xml`, `.gitignore`, `LICENSE` (GPL-3.0)
- Design documentation: `ARCHITECTURE.md`, `DATA_MODEL.md`, `EXPERIMENT_LIFECYCLE.md`, `REFLECTION_SYSTEM.md`, `UI_DESIGN.md`, `PHILOSOPHY.md`, and supporting docs
