# Project Structure

## Directory Layout

```
kokoromi/
├── app/src/main/kotlin/com/kokoromi/
│   ├── MainActivity.kt
│   ├── KokoromiApp.kt               # Application class, Hilt init
│   │
│   ├── ui/                          # Compose screens, organized by feature
│   │   ├── KokoromiNavigation.kt
│   │   ├── home/                    # HomeScreen, HomeViewModel, ExperimentCard, StreakDisplay
│   │   ├── create/                  # CreateExperimentScreen, CreateExperimentViewModel
│   │   ├── checkin/                 # CheckInScreen, CheckInViewModel
│   │   ├── reflection/              # ReflectionScreen, ReflectionViewModel
│   │   ├── completion/              # ExperimentCompletionScreen, CompletionViewModel
│   │   ├── notes/                   # FieldNotesScreen, AddEditNoteScreen + ViewModels
│   │   ├── archive/                 # ArchiveScreen, ArchiveViewModel
│   │   └── theme/                   # Color.kt, Type.kt, Theme.kt, Shapes.kt
│   │
│   ├── data/
│   │   ├── db/
│   │   │   ├── KokoromiDatabase.kt
│   │   │   ├── dao/                 # ExperimentDao, DailyLogDao, ReflectionDao, CompletionDao, FieldNoteDao
│   │   │   └── entity/              # Room entities (one per table)
│   │   ├── export/                  # JsonExporter
│   │   ├── imports/                 # JsonImporter, ImportPayload
│   │   ├── repository/              # Interfaces + Default* implementations
│   │   └── model/                   # UserPreferences, ThemePreference (DataStore models)
│   │
│   ├── domain/
│   │   ├── usecase/                 # One use case per business operation
│   │   └── model/                   # Domain models + enums (Experiment, DailyLog, ExperimentStatus, …)
│   │
│   ├── di/                          # Hilt modules: DatabaseModule, RepositoryModule, PreferencesModule
│   ├── notification/                # CheckInReminderWorker, ReminderScheduler
│   └── util/                        # Constants.kt
│
├── app/src/test/                    # JVM unit tests (use cases, validators)
├── app/src/androidTest/             # Instrumented tests (Compose UI, Room DB)
│
├── .github/workflows/
│   ├── android-build.yml            # Build + lint on every PR
│   └── release.yml                  # Build, sign, publish APK on git tag
│
├── docs/                            # All design docs
├── assets/                          # Material Theme Builder exports (not part of build)
├── CLAUDE.md
└── CHANGELOG.md
```

---

## Dependency Direction

```
UI (Compose screens + ViewModels)
    ↓
Domain (use cases, enums) — no Android imports
    ↓
Data (repositories, Room, DataStore)
    ↓
Android Framework
```

Never reverse. Data never calls UI. Domain never imports Android classes.

---

## Key Files

| File | Purpose |
|------|---------|
| `KokoromiNavigation.kt` | Full nav graph; all routes defined here |
| `KokoromiDatabase.kt` | Room DB class; migration management |
| `Constants.kt` | `MAX_ACTIVE_EXPERIMENTS=2`, char limits, duration defaults |
| `data/export/JsonExporter.kt` | Serialises all user data to JSON |
| `data/imports/JsonImporter.kt` | Parses JSON export back into domain models |
| `PreferencesRepository.kt` | DataStore: reflection day, theme, reminder settings |

**Key constants**

| Constant | Value |
|----------|-------|
| MAX_ACTIVE_EXPERIMENTS | 2 |
| HYPOTHESIS_MAX_CHARS | 500 |
| ACTION_MAX_CHARS | 500 |
| REFLECTION_MAX_CHARS | 2000 |
| FIELD_NOTE_MAX_CHARS | 5000 |
| DEFAULT_DURATION_DAYS | 7 |

---

## Build & SDK

| Setting | Value |
|---------|-------|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.13.2 |
| Compile SDK | 36 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| DI annotation processor | KSP (not kapt) |

---

## Development Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease        # requires signing key

# Test
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # instrumented (needs device/emulator)

# Quality
./gradlew lint
./gradlew ktlintFormat

# Install on connected device
./gradlew installDebug
```

---

## CI/CD

| Workflow | Trigger | Does |
|----------|---------|------|
| `android-build.yml` | Every PR | Compile debug APK, lint, unit tests |
| `release.yml` | Git tag push | Build release APK, sign, create GitHub Release |

---

## Adding a Feature

1. Domain model in `domain/model/`
2. Use case in `domain/usecase/` + unit test
3. Room entity in `data/db/entity/`
4. DAO in `data/db/dao/`
5. Repository interface + `Default*` impl in `data/repository/`
6. Bind in `di/RepositoryModule.kt`
7. ViewModel in `ui/yourfeature/`
8. Compose screen(s) in `ui/yourfeature/`
9. Wire route in `KokoromiNavigation.kt`
10. Instrumented UI test in `androidTest/`
