# Project Structure: Kokoromi

## Directory Layout

```
kokoromi/
│
├── app/                                    # Main app module
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/kokoromi/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── KokoromiApp.kt          # Application class, Hilt setup
│   │   │   │   │
│   │   │   │   ├── ui/                            # Jetpack Compose screens
│   │   │   │   │   ├── KokoromiNavigation.kt (NavController setup)
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── ExperimentCard.kt
│   │   │   │   │   │       ├── CheckInButton.kt
│   │   │   │   │   │       └── StreakDisplay.kt
│   │   │   │   │   │
│   │   │   │   │   ├── create/
│   │   │   │   │   │   ├── CreateExperimentScreen.kt
│   │   │   │   │   │   ├── CreateExperimentViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── HypothesisInput.kt
│   │   │   │   │   │       ├── ActionInput.kt
│   │   │   │   │   │       ├── DurationPicker.kt
│   │   │   │   │   │       └── WhyInput.kt
│   │   │   │   │   │
│   │   │   │   │   ├── checkin/
│   │   │   │   │   │   ├── CheckInScreen.kt
│   │   │   │   │   │   ├── CheckInViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── CompletionButtons.kt
│   │   │   │   │   │       ├── MoodRating.kt
│   │   │   │   │   │       └── NotesInput.kt
│   │   │   │   │   │
│   │   │   │   │   ├── reflection/
│   │   │   │   │   │   ├── ReflectionScreen.kt
│   │   │   │   │   │   ├── ReflectionViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── PlusMinus NextForm.kt
│   │   │   │   │   │       └── ReflectionPrompt.kt
│   │   │   │   │   │
│   │   │   │   │   ├── completion/
│   │   │   │   │   │   ├── ExperimentCompletionScreen.kt
│   │   │   │   │   │   ├── CompletionViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── CompletionSummary.kt
│   │   │   │   │   │       ├── DecisionButtons.kt
│   │   │   │   │   │       └── FinalReflection.kt
│   │   │   │   │   │
│   │   │   │   │   ├── fieldnotes/
│   │   │   │   │   │   ├── FieldNotesScreen.kt
│   │   │   │   │   │   ├── FieldNotesViewModel.kt
│   │   │   │   │   │   ├── AddEditNoteScreen.kt
│   │   │   │   │   │   ├── AddEditNoteViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       └── NoteCard.kt
│   │   │   │   │   │
│   │   │   │   │   ├── archive/
│   │   │   │   │   │   ├── ArchiveScreen.kt
│   │   │   │   │   │   ├── ArchiveViewModel.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── ExperimentList.kt
│   │   │   │   │   │       └── ExperimentDetail.kt
│   │   │   │   │   │
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Type.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Shapes.kt
│   │   │   │   │
│   │   │   │   ├── data/                          # Data layer (Room, repositories)
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── KokoromiDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── ExperimentDao.kt
│   │   │   │   │   │   │   ├── DailyLogDao.kt
│   │   │   │   │   │   │   ├── ReflectionDao.kt
│   │   │   │   │   │   │   ├── CompletionDao.kt
│   │   │   │   │   │   │   └── FieldNoteDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── ExperimentEntity.kt
│   │   │   │   │   │       ├── DailyLogEntity.kt
│   │   │   │   │   │       ├── ReflectionEntity.kt
│   │   │   │   │   │       ├── CompletionEntity.kt
│   │   │   │   │   │       └── FieldNoteEntity.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── ExperimentRepository.kt (interface)
│   │   │   │   │   │   ├── DefaultExperimentRepository.kt (impl)
│   │   │   │   │   │   ├── DailyLogRepository.kt
│   │   │   │   │   │   ├── ReflectionRepository.kt
│   │   │   │   │   │   ├── FieldNoteRepository.kt
│   │   │   │   │   │   └── PreferencesRepository.kt (user settings)
│   │   │   │   │   │
│   │   │   │   │   └── model/
│   │   │   │   │       ├── Experiment.kt (data class)
│   │   │   │   │       ├── DailyLog.kt
│   │   │   │   │       ├── Reflection.kt
│   │   │   │   │       ├── Completion.kt
│   │   │   │   │       ├── FieldNote.kt
│   │   │   │   │       └── UserPreferences.kt
│   │   │   │   │
│   │   │   │   ├── domain/                        # Business logic layer
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── CreateExperimentUseCase.kt
│   │   │   │   │   │   ├── LogDailyCheckInUseCase.kt
│   │   │   │   │   │   ├── GetActiveExperimentsUseCase.kt
│   │   │   │   │   │   ├── CompleteExperimentUseCase.kt
│   │   │   │   │   │   ├── SaveReflectionUseCase.kt
│   │   │   │   │   │   ├── GetArchiveUseCase.kt
│   │   │   │   │   │   ├── SaveFieldNoteUseCase.kt
│   │   │   │   │   │   ├── GetFieldNotesUseCase.kt
│   │   │   │   │   │   ├── DeleteFieldNoteUseCase.kt
│   │   │   │   │   │   └── ExportDataUseCase.kt
│   │   │   │   │   │
│   │   │   │   │   └── model/
│   │   │   │   │       ├── ExperimentStatus.kt (enum)
│   │   │   │   │       ├── Frequency.kt (enum)
│   │   │   │   │       ├── DecisionType.kt (enum: persist, pivot, pause)
│   │   │   │   │       └── PlainDailyLog.kt (non-entity version)
│   │   │   │   │
│   │   │   │   ├── di/                           # Dependency injection (Hilt)
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   ├── RepositoryModule.kt
│   │   │   │   │   ├── UseCaseModule.kt
│   │   │   │   │   └── PreferencesModule.kt
│   │   │   │   │
│   │   │   │   └── util/
│   │   │   │       ├── DateUtils.kt
│   │   │   │       ├── JsonExporter.kt             # Data export
│   │   │   │       ├── Constants.kt
│   │   │   │       └── Extensions.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   │
│   │   │   │   ├── drawable/                      # Icons, simple graphics
│   │   │   │   │   └── ic_*.xml
│   │   │   │   │
│   │   │   │   └── layout/                         # Used only for system layouts if needed
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/
│   │   │   └── kotlin/com/kokoromi/
│   │   │       ├── domain/
│   │   │       │   └── usecase/                    # Unit tests for use cases
│   │   │       │       ├── CreateExperimentUseCaseTest.kt
│   │   │       │       ├── LogDailyCheckInUseCaseTest.kt
│   │   │       │       └── ...
│   │   │       │
│   │   │       └── data/
│   │   │           └── repository/                 # Unit tests for repositories
│   │   │               └── ExperimentRepositoryTest.kt
│   │   │
│   │   └── androidTest/
│   │       └── kotlin/com/kokoromi/
│   │           ├── ui/
│   │           │   ├── HomeScreenTest.kt
│   │           │   ├── CreateExperimentScreenTest.kt
│   │           │   └── CheckInScreenTest.kt
│   │           │
│   │           └── db/
│   │               └── DatabaseTest.kt
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── build.gradle.kts                        # Root build file
├── settings.gradle.kts                     # Multi-module setup
├── gradle.properties                       # Gradle config
│
├── .github/
│   └── workflows/
│       ├── android-build.yml               # CI: build on every PR
│       ├── android-test.yml                # CI: run tests
│       └── release.yml                     # CD: release tags to Play Store
│
├── docs/
│   ├── ARCHITECTURE.md
│   ├── BUDGET_PHONE_OPTIMIZATION.md
│   ├── DATA_MODEL.md
│   ├── DESIGN_CONSTRAINTS.md
│   ├── EXPERIMENT_LIFECYCLE.md
│   ├── LEGAL_CONSIDERATIONS.md
│   ├── MANIFEST.md
│   ├── OPEN_SOURCE_GUIDE.md
│   ├── PERFORMANCE_RESPONSIVENESS_SECURITY.md
│   ├── PHILOSOPHY.md
│   ├── PROJECT_STRUCTURE.md (this file)
│   ├── README.md
│   ├── REFLECTION_SYSTEM.md
│   └── UI_DESIGN.md
│
├── assets/                                 # Design reference files (not part of build)
│   ├── material-theme/                     # Light theme export from Material Theme Builder
│   └── material-theme-dark/                # Dark theme export from Material Theme Builder
│                                           # Themes generated with https://material-foundation.github.io/material-theme-builder/
│
├── CLAUDE.md                               # Guidance for Claude Code
├── DESIGN_DOCUMENTATION_SUMMARY.md        # Overview of all design docs
├── LICENSE                                 # GPL-3.0
├── MILESTONES.org                          # Development milestones and effort estimates
└── .gitignore
```

---

## Module Organization

### `app/src/main/kotlin/com/kokoromi/`

**Purpose**: Core app code, organized by feature

**Key principles**:
- **By feature, not by layer**: `home/`, `create/`, `checkin/` are features. Within each feature: screens, ViewModels, components.
- **Separation of concerns**: UI layer (`ui/`), business logic (`domain/`), data (`data/`)
- **Dependency direction**: UI → Domain → Data (never reverse)

### `ui/`

**Purpose**: Jetpack Compose screens and components

**Organization**:
- Each major feature gets a folder: `home/`, `create/`, `checkin/`, etc.
- Each folder has:
  - `*Screen.kt` — main Composable, handles navigation
  - `*ViewModel.kt` — state management, business logic calls
  - `components/` — smaller reusable Composables (cards, buttons, forms)

**No XML layouts**: Everything is Compose. No legacy XML files.

### `data/`

**Purpose**: Local persistence, data abstraction

**Organization**:
- `db/` — Room database setup (entities, DAOs, database class)
- `repository/` — Repository pattern (abstract data source behind interface)
- `model/` — Data classes (Kotlin data classes, not entities)
- `dao/` — Data Access Objects (SQL queries)
- `entity/` — Room entities (database table definitions)

**Why separate model/ and entity/?**
- `entity/` = database schema (tied to Room, has annotations)
- `model/` = business logic objects (clean, reusable, testable)
- Repositories translate between them

### `domain/`

**Purpose**: Business logic, use cases, domain models

**Organization**:
- `usecase/` — Use cases (CreateExperiment, LogCheckIn, etc.)
- `model/` — Enums and domain-specific types (ExperimentStatus, Frequency, DecisionType)

**No dependencies on UI or data layers**: Domain is pure business logic, testable in isolation.

### `di/`

**Purpose**: Dependency injection (Hilt)

**Organization**:
- `DatabaseModule.kt` — Room database provider
- `RepositoryModule.kt` — Repository implementations
- `UseCaseModule.kt` — Use case instantiation
- `PreferencesModule.kt` — SharedPreferences for user settings

**Single source of truth**: All dependencies created here, injected into ViewModels and other classes.

---

## Key Files & Responsibilities

| File | Purpose |
|------|---------|
| `KokoromiApp.kt` | Application class, Hilt initialization, global app setup |
| `MainActivity.kt` | Single Activity (modern Android), hosts NavHost, handles deep links |
| `KokoromiNavigation.kt` | Navigation graph setup (Jetpack Navigation) |
| `KokoromiDatabase.kt` | Room database class, migration management |
| `ExperimentDao.kt` | SQL queries for experiments (create, read, update, filter) |
| `FieldNoteDao.kt` | SQL queries for field notes (CRUD, ordered by created_at DESC) |
| `ExperimentRepository.kt` | Data abstraction (interface), implemented by DefaultExperimentRepository |
| `FieldNoteRepository.kt` | Data abstraction for field notes |
| `CreateExperimentUseCase.kt` | Business logic for creating an experiment (validation, defaults, etc.) |
| `SaveFieldNoteUseCase.kt` | Validates and persists a field note |
| `HomeViewModel.kt` | UI state for home screen (active experiments, loading, error states) |
| `FieldNotesViewModel.kt` | UI state for field notes list screen |
| `HomeScreen.kt` | Compose UI for home screen |
| `FieldNotesScreen.kt` | Compose UI for field notes list |
| `AddEditNoteScreen.kt` | Compose UI for creating and editing field notes |
| `JsonExporter.kt` | Exports all user data as JSON for backup (includes field notes) |

---

## Build Configuration

### `app/build.gradle.kts`

**Dependencies** (sample):
```kotlin
dependencies {
    // Jetpack
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")  // uses KSP, not kapt
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### `build.gradle.kts` (root)

**Key settings**:
- Kotlin version: 2.0.21
- Android Gradle Plugin: 8.13.2
- Compile SDK: 36
- Min SDK: 26 (Android 8.0, released 2017)
- Target SDK: 35

---

## Development Commands

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing key)
./gradlew assembleRelease

# Build all tests
./gradlew build
```

### Test

```bash
# Run unit tests (domain, data)
./gradlew test

# Run instrumented tests (UI tests, need emulator/device)
./gradlew connectedAndroidTest

# Run all tests
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

### Run

```bash
# Run on emulator or connected device
./gradlew installDebug
adb shell am start -n com.kokoromi/.MainActivity

# Or use Android Studio's Run button
```

### Lint & Code Quality

```bash
# Run lint checks
./gradlew lint

# Format code (ktlint)
./gradlew ktlintFormat

# Run detekt (static analysis)
./gradlew detekt
```

---

## Dependency Directions (Architecture)

```
UI Layer (Compose)
    ↓ depends on
Domain Layer (Use Cases, Enums)
    ↓ depends on
Data Layer (Repository, Room)
    ↓ depends on
Android Framework (Activity, Context, Database)
```

**Rule**: Never go upwards. Data never calls UI. Domain never imports Android classes.

---

## Key Classes Overview

### `Experiment` (Domain Model)

```kotlin
data class Experiment(
    val id: UUID,
    val hypothesis: String,
    val action: String,
    val why: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val frequency: Frequency,    // DAILY, CUSTOM, etc.
    val status: ExperimentStatus, // ACTIVE, COMPLETED, ARCHIVED, PAUSED
    val createdAt: LocalDateTime
)
```

### `DailyLog` (Domain Model)

```kotlin
data class DailyLog(
    val id: UUID,
    val experimentId: UUID,
    val date: LocalDate,
    val completed: Boolean,
    val moodBefore: Int?,  // 1-5
    val moodAfter: Int?,
    val notes: String?,
    val loggedAt: LocalDateTime
)
```

### `ExperimentStatus` (Enum)

```kotlin
enum class ExperimentStatus {
    ACTIVE,       // Currently running
    COMPLETED,    // Duration ended, awaiting decision
    ARCHIVED,     // User decided they're done
    PAUSED,       // Paused for now, can resume
}
```

---

## Testing Structure

### Unit Tests (test/)

Test business logic:
```kotlin
// tests in test/kotlin/...UseCase.kt
class CreateExperimentUseCaseTest {
    @Test
    fun createsExperimentWithValidInputs() { ... }
    
    @Test
    fun rejectsInvalidHypothesis() { ... }
}
```

### Instrumented Tests (androidTest/)

Test UI and database:
```kotlin
// tests in androidTest/kotlin/...ScreenTest.kt
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun displaysActiveExperiments() { ... }
}
```

---

## Configuration Management

### User Preferences (DataStore)

```kotlin
// Via PreferencesRepository (injected)
userPreferencesRepository.setReflectionDay(DayOfWeek.SUNDAY)
userPreferencesRepository.getReflectionDay() // → Flow<DayOfWeek>
```

Stored keys:
- `reflection_day` — day to show reflection prompts
- `reflection_time` — time to show reflection prompts (future)
- `use_system_theme` — respect system dark mode

### App Constants

```kotlin
// Constants.kt
object Constants {
    const val MAX_ACTIVE_EXPERIMENTS = 2
    const val HYPOTHESIS_MAX_CHARS = 500
    const val ACTION_MAX_CHARS = 500
    const val DEFAULT_EXPERIMENT_DURATION_DAYS = 14
}
```

---

## Data Backup & Export

### JSON Export (Offline)

```kotlin
// JsonExporter.kt
class JsonExporter(
    val experimentRepository: ExperimentRepository,
    val dailyLogRepository: DailyLogRepository,
    val reflectionRepository: ReflectionRepository
) {
    suspend fun exportAllData(): String {
        // Returns JSON containing all experiments, logs, reflections
    }
    
    suspend fun exportSingleExperiment(experimentId: UUID): String {
        // Returns JSON for one experiment and its logs
    }
}
```

**Format**:
```json
{
  "version": "1.0",
  "exportDate": "2026-03-30T15:45:00Z",
  "experiments": [ ... ],
  "dailyLogs": [ ... ],
  "reflections": [ ... ]
}
```

---

## CI/CD Pipeline

### GitHub Actions

**Build on every PR** (`.github/workflows/android-build.yml`):
- Compile debug APK
- Run unit tests
- Check lint

**Test on every PR** (`.github/workflows/android-test.yml`):
- Run instrumented tests on emulator
- Generate coverage report

**Release on tag** (`.github/workflows/release.yml`):
- Build release APK
- Sign with release key
- Create GitHub Release with APK
- (Future: Auto-upload to Google Play Store)

---

## Adding New Features

When you add a new feature, follow this structure:

1. **Define domain model** in `domain/model/`
2. **Create use case** in `domain/usecase/`
3. **Add Room entities** in `data/db/entity/`
4. **Add DAO queries** in `data/db/dao/`
5. **Create repository** in `data/repository/`
6. **Bind in Hilt** in `di/`
7. **Create ViewModel** in `ui/yourfeature/`
8. **Create Compose screens** in `ui/yourfeature/`
9. **Add tests** in `test/` and `androidTest/`
10. **Update navigation** in `KokoromiNavigation.kt`

---

## Version Management

**Semantic Versioning**: MAJOR.MINOR.PATCH

- **MAJOR**: Breaking changes (data migration required)
- **MINOR**: New features (backwards compatible)
- **PATCH**: Bug fixes

`build.gradle.kts`:
```kotlin
android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.kokoromi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1  // Increment on every release
        versionName = "1.0.0"  // Match semantic versioning
    }
}
```

---

**Version**: 1.0  
**Last Updated**: March 2026  
**Maintainer**: [Your team]
