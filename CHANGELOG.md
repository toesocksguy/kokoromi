# Changelog

All notable changes to Kokoromi are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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
