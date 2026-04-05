# Changelog

All notable changes to Kokoromi are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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
