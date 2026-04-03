# Architecture: Kokoromi

## Overview

Kokoromi uses a layered MVVM architecture with unidirectional data flow. The design prioritizes testability, clear separation of concerns, and simplicity—no over-engineering for a single-module app.

---

## Layer Diagram

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                          │
│   Jetpack Compose screens + ViewModels               │
│   Observes StateFlow, emits events via functions     │
└───────────────────────┬─────────────────────────────┘
                        │ calls use cases
                        ▼
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                        │
│   Use cases — orchestrate business logic             │
│   Domain models — pure Kotlin, no Android imports    │
│   Validation — pure functions, returns Result<T>     │
└───────────────────────┬─────────────────────────────┘
                        │ reads/writes via interfaces
                        ▼
┌─────────────────────────────────────────────────────┐
│                   Data Layer                         │
│   Repository implementations                         │
│   Room DAOs + entities                               │
│   DataStore for preferences                          │
└─────────────────────────────────────────────────────┘
```

**Dependency rule**: Each layer only imports from the layer directly below it. The data layer never imports from domain or UI. The domain layer never imports from UI or Android framework.

---

## UI Layer

### ViewModels

Each screen has one ViewModel. ViewModels:
- Are the single source of truth for screen state
- Expose state as `StateFlow<UiState>` (never raw mutable state)
- Accept user actions as regular functions (not events or sealed classes — keep it simple)
- Call domain use cases, never repositories directly
- Do not contain business logic (that lives in use cases)

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveExperimentsUseCase: GetActiveExperimentsUseCase,
    private val checkExperimentLifecycleUseCase: CheckExperimentLifecycleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            checkExperimentLifecycleUseCase()  // auto-complete expired experiments
            loadExperiments()
        }
    }

    private fun loadExperiments() {
        viewModelScope.launch {
            getActiveExperimentsUseCase()
                .collect { experiments ->
                    _uiState.value = HomeUiState.Success(experiments)
                }
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val experiments: List<ExperimentWithProgress>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

### Composables

- Composables are stateless: they receive state and callbacks as parameters
- `*Screen.kt` — top-level Composable, connected to ViewModel via `collectAsStateWithLifecycle()`
- `components/` — smaller, reusable Composables with no ViewModel dependency

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCreateExperiment: () -> Unit,
    onCheckIn: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is HomeUiState.Loading -> LoadingSpinner()
        is HomeUiState.Success -> HomeContent(
            experiments = (uiState as HomeUiState.Success).experiments,
            onCheckIn = onCheckIn,
            onCreateExperiment = onCreateExperiment
        )
        is HomeUiState.Error -> ErrorMessage((uiState as HomeUiState.Error).message)
    }
}
```

### Navigation

Single-Activity app. All navigation handled by Jetpack Navigation Compose.

```kotlin
@Composable
fun KokoromiNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onCreateExperiment = { navController.navigate("create") },
                onCheckIn = { id -> navController.navigate("checkin/$id") }
            )
        }
        composable("create") {
            CreateExperimentScreen(
                onSaved = { navController.popBackStack() }
            )
        }
        composable("checkin/{experimentId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("experimentId") ?: return@composable
            CheckInScreen(
                experimentId = id,
                onDone = { navController.popBackStack() }
            )
        }
        // ... other routes
    }
}
```

---

## Domain Layer

### Use Cases

Use cases are the heart of the domain layer. Each use case does one thing and returns a `Result<T>` for error handling.

```kotlin
class CreateExperimentUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository
) {
    suspend operator fun invoke(
        hypothesis: String,
        action: String,
        why: String?,
        startDate: LocalDate,
        durationDays: Int
    ): Result<String> {
        // Validate inputs
        ExperimentValidator.validate(hypothesis, action, startDate, durationDays)
            .onFailure { return Result.failure(it) }

        // Check slot availability
        val activeCount = experimentRepository.getActiveExperimentCount()
        if (activeCount >= Constants.MAX_ACTIVE_EXPERIMENTS) {
            return Result.failure(Exception("Max active experiments reached"))
        }

        // Create and persist
        val experiment = Experiment(
            id = UUID.randomUUID().toString(),
            hypothesis = hypothesis,
            action = action,
            why = why,
            startDate = startDate,
            endDate = startDate.plusDays(durationDays.toLong()),
            frequency = Frequency.DAILY,
            status = ExperimentStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val id = experimentRepository.createExperiment(experiment)
        return Result.success(id)
    }
}
```

### Validation

All validation lives in the domain layer as pure functions. No Android framework imports.

```kotlin
object ExperimentValidator {
    fun validate(
        hypothesis: String,
        action: String,
        startDate: LocalDate,
        durationDays: Int
    ): Result<Unit> {
        return when {
            hypothesis.isBlank() -> Result.failure(Exception("Hypothesis is required"))
            hypothesis.length > 500 -> Result.failure(Exception("Hypothesis too long (max 500)"))
            action.isBlank() -> Result.failure(Exception("Action is required"))
            action.length > 500 -> Result.failure(Exception("Action too long (max 500)"))
            startDate < LocalDate.now() -> Result.failure(Exception("Start date must be today or later"))
            durationDays < 1 -> Result.failure(Exception("Duration must be at least 1 day"))
            durationDays > 180 -> Result.failure(Exception("Duration max 180 days"))
            else -> Result.success(Unit)
        }
    }
}
```

### Domain Models

Domain models are pure Kotlin data classes. No Room annotations, no Android imports.

```kotlin
// domain/model/
data class Experiment(...)
data class DailyLog(...)
data class Reflection(...)
data class Completion(...)

enum class ExperimentStatus { ACTIVE, COMPLETED, ARCHIVED, PAUSED }
enum class Frequency { DAILY, CUSTOM }
enum class DecisionType { KEEP, ADJUST, ARCHIVE, PIVOT, PAUSE }
```

---

## Data Layer

### Repositories

Repositories are the boundary between domain and data. They:
- Accept and return domain models (not entities)
- Translate between domain models and Room entities internally
- Are defined as interfaces in the domain layer, implemented in the data layer

```kotlin
// Interface lives in domain or data — either is fine; we put it in data/repository/
interface ExperimentRepository {
    suspend fun createExperiment(experiment: Experiment): String
    suspend fun getExperiment(id: String): Experiment?
    fun getActiveExperiments(): Flow<List<Experiment>>
    suspend fun getActiveExperimentCount(): Int
    suspend fun updateExperimentStatus(id: String, status: ExperimentStatus)
    // ...
}

class DefaultExperimentRepository @Inject constructor(
    private val experimentDao: ExperimentDao
) : ExperimentRepository {
    override fun getActiveExperiments(): Flow<List<Experiment>> {
        return experimentDao.getActiveExperiments()
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    // ...
}
```

### Room Database

Single `KokoromiDatabase` class. All DAOs accessed from it.

```kotlin
@Database(
    entities = [
        ExperimentEntity::class,
        DailyLogEntity::class,
        ReflectionEntity::class,
        CompletionEntity::class
    ],
    version = 1,
    exportSchema = true  // Schema exported to /schemas/ for migration testing
)
@TypeConverters(Converters::class)
abstract class KokoromiDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun completionDao(): CompletionDao
}
```

### Type Converters

Room stores primitives. Type converters bridge the gap for `LocalDate`, `Instant`, etc.

```kotlin
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromInstant(instant: Instant?): String? = instant?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }
}
```

### User Preferences

User settings (reflection day, theme) stored in DataStore (not SharedPreferences — DataStore is the modern replacement and is coroutine-friendly).

```kotlin
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val REFLECTION_DAY = intPreferencesKey("reflection_day")

    val reflectionDay: Flow<DayOfWeek> = dataStore.data.map { prefs ->
        val day = prefs[REFLECTION_DAY] ?: DayOfWeek.SUNDAY.value
        DayOfWeek.of(day)
    }

    suspend fun setReflectionDay(day: DayOfWeek) {
        dataStore.edit { prefs -> prefs[REFLECTION_DAY] = day.value }
    }
}
```

---

## Dependency Injection (Hilt)

Hilt is the DI framework. All dependencies are provided through Hilt modules.

### Module Structure

```kotlin
// DatabaseModule.kt — provides Room database and DAOs
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KokoromiDatabase {
        return Room.databaseBuilder(
            context,
            KokoromiDatabase::class.java,
            "tiny_experiments.db"
        ).build()
    }

    @Provides
    fun provideExperimentDao(db: KokoromiDatabase): ExperimentDao = db.experimentDao()
    // ... other DAOs
}

// RepositoryModule.kt — binds interfaces to implementations
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindExperimentRepository(
        impl: DefaultExperimentRepository
    ): ExperimentRepository
    // ... other repositories
}
```

### Why Hilt (not manual DI or Koin)?

- **Hilt**: Android-native, compile-time verified, integrates with ViewModel injection
- **Koin**: Runtime DI, simpler setup but no compile-time safety
- **Manual DI**: Works for small apps, but Hilt scales better as the project grows

For this app, Hilt is the right call.

---

## Concurrency

All database work happens on `Dispatchers.IO`. All use cases are `suspend` functions called from ViewModel's `viewModelScope`.

```kotlin
// In use cases — called from coroutines, no explicit dispatcher needed
// Room DAOs already dispatch to IO thread when using suspend functions

// In ViewModels — use viewModelScope
viewModelScope.launch {
    val result = createExperimentUseCase(hypothesis, action, ...)
    result
        .onSuccess { id -> navigateToHome() }
        .onFailure { e -> showError(e.message) }
}
```

**Rule**: Never block the main thread. All suspend functions in DAOs and repositories run on the caller's coroutine context. Room handles the dispatcher internally for `Flow` queries.

---

## Error Handling

**Pattern**: Use cases return `Result<T>`. ViewModels handle success/failure and update UI state.

No global error handler. No crashing on expected errors (validation failures, empty states). Unexpected errors (database corruption, out of memory) are not caught — they surface as crashes to be fixed.

```kotlin
// ViewModel pattern
viewModelScope.launch {
    _uiState.value = CreateExperimentUiState.Loading
    createExperimentUseCase(...)
        .onSuccess { _uiState.value = CreateExperimentUiState.Success }
        .onFailure { error ->
            _uiState.value = CreateExperimentUiState.Error(
                message = error.message ?: "Something went wrong"
            )
        }
}
```

---

## Testing Strategy

### Unit Tests (JVM, no Android runtime)

Test: use cases, validators, repository logic (with in-memory Room)

```kotlin
// Use case test
class CreateExperimentUseCaseTest {
    private val fakeRepository = FakeExperimentRepository()
    private val useCase = CreateExperimentUseCase(fakeRepository)

    @Test
    fun `rejects blank hypothesis`() = runTest {
        val result = useCase("", "some action", null, LocalDate.now(), 14)
        assertTrue(result.isFailure)
    }

    @Test
    fun `creates experiment with valid inputs`() = runTest {
        val result = useCase("Does walking help?", "Walk 15 mins", null, LocalDate.now(), 14)
        assertTrue(result.isSuccess)
    }
}
```

### Integration Tests (Android, in-memory Room)

Test: repositories against real Room database (in-memory)

```kotlin
@RunWith(AndroidJUnit4::class)
class ExperimentRepositoryTest {
    private lateinit var db: KokoromiDatabase
    private lateinit var repository: DefaultExperimentRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KokoromiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = DefaultExperimentRepository(db.experimentDao())
    }

    @After
    fun teardown() { db.close() }
}
```

### UI Tests (Compose Testing)

Test: individual screens in isolation with fake ViewModels or fake data

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows empty state when no experiments`() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState.Success(emptyList()),
                onCreateExperiment = {},
                onCheckIn = {}
            )
        }
        composeTestRule.onNodeWithText("No active experiments").assertIsDisplayed()
    }
}
```

### What We Don't Test

- Room SQL query correctness via mocks (we use in-memory DB instead)
- Android framework internals
- Third-party library behavior

---

## Architecture Decisions

### Why MVVM and not MVI?

MVI (Model-View-Intent) is a good pattern but adds ceremony (sealed event classes, reducers) that isn't needed for an app of this size. MVVM with `StateFlow` and simple state sealed classes gives the same unidirectional data flow with less boilerplate.

If the app grows significantly in complexity (e.g., complex undo/redo, heavy async coordination), migrating to MVI is reasonable.

### Why Use Cases?

For a small app, use cases might seem like over-engineering. We include them because:
1. They're the only place that holds business logic (slot limit, validation, lifecycle transitions)
2. They're trivially testable without Android or Room
3. They make the ViewModel thin and focused on UI state

### Why Not Multi-Module?

Multi-module setup (`:feature:home`, `:core:data`, etc.) is valuable for large teams and apps. For a single-maintainer open source project starting from scratch, one module is the right call. Refactor to multi-module if the team grows or build times become a problem.

### Why Room over SQLite directly?

Room provides:
- Compile-time SQL verification
- Coroutine and Flow support built-in
- Type converters for clean domain model mapping
- Migration framework

The tradeoff (annotation processing, slight overhead) is worth it.

---

## Future Toolkit Tools: Architectural Notes

The Settings screen surfaces five additional tools from the Tiny Experiments toolkit as informational content (no interactive features in v1). If any are ever implemented as full features in a future version, two have architectural implications worth noting:

### Triple Check (Tool 5)
Would add structured head/heart/hand fields to the Skip flow. **Do not** add these as columns on `DailyLog` — that would require a schema migration and couples unrelated concerns. Instead, model it as a separate entity linked to `DailyLog` via foreign key. This is probably out of scope for v2 as well, but the pattern matters if it ever ships.

### Two-Step Reset (Tool 9)
Could be surfaced when a user pauses an experiment mid-run. Integration would mean linking a Reset record to the PAUSED state transition, touching `UpdateExperimentStatusUseCase` and the schema. Keep the pause flow's use case interface simple and extensible rather than tightly coupled to the current two-field signature. Also probably out of scope for v2.

---

**Version**: 1.0
**Last Updated**: March 2026
**Status**: Ready for implementation
