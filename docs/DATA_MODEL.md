# Data Model: Kokoromi

## Core Entities

The database consists of four main entities: `Experiment`, `DailyLog`, `Reflection`, and `Completion`. All data is stored locally in SQLite (via Room).

---

## Entity: Experiment

Represents a single tiny experiment a user is running.

### Database Schema

```sql
CREATE TABLE experiments (
    id TEXT PRIMARY KEY,
    hypothesis TEXT NOT NULL,
    action TEXT NOT NULL,
    why TEXT,
    start_date TEXT NOT NULL,      -- ISO 8601 date (YYYY-MM-DD)
    end_date TEXT NOT NULL,         -- Calculated from start + duration
    frequency TEXT NOT NULL,        -- DAILY, CUSTOM (enum)
    frequency_custom TEXT,          -- JSON for custom frequencies (future)
    status TEXT NOT NULL,           -- ACTIVE, COMPLETED, ARCHIVED, PAUSED
    created_at TEXT NOT NULL,       -- ISO 8601 datetime
    updated_at TEXT NOT NULL
);
```

### Kotlin Entity

```kotlin
@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "hypothesis")
    val hypothesis: String,
    
    @ColumnInfo(name = "action")
    val action: String,
    
    @ColumnInfo(name = "why")
    val why: String?,
    
    @ColumnInfo(name = "start_date")
    val startDate: String,  // LocalDate.toString() → "2026-03-30"
    
    @ColumnInfo(name = "end_date")
    val endDate: String,    // Calculated on insert
    
    @ColumnInfo(name = "frequency")
    val frequency: String,  // "DAILY" or "CUSTOM"
    
    @ColumnInfo(name = "frequency_custom")
    val frequencyCustom: String?,  // Reserved for future (custom Mon/Wed/Fri, etc.)
    
    @ColumnInfo(name = "status")
    val status: String,  // "ACTIVE", "COMPLETED", "ARCHIVED", "PAUSED"
    
    @ColumnInfo(name = "created_at")
    val createdAt: String,  // Instant.now().toString()
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String   // Updated on status change
)
```

### Domain Model (Clean Separation)

```kotlin
data class Experiment(
    val id: String,
    val hypothesis: String,
    val action: String,
    val why: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val frequency: Frequency,  // Enum
    val status: ExperimentStatus,  // Enum
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class Frequency {
    DAILY,
    CUSTOM  // Future: JSON-based custom schedules
}

enum class ExperimentStatus {
    ACTIVE,      // Currently running, visible on home screen
    COMPLETED,   // Duration ended, awaiting user decision
    ARCHIVED,    // User finished with it (learned enough or moved on)
    PAUSED       // User paused it temporarily (can resume)
}
```

### Repository Methods

```kotlin
interface ExperimentRepository {
    // Create
    suspend fun createExperiment(experiment: Experiment): String  // Returns ID
    
    // Read
    suspend fun getExperiment(id: String): Experiment?
    
    // Get all active experiments (displayed on home screen)
    fun getActiveExperiments(): Flow<List<Experiment>>
    
    // Get all (for archive)
    fun getAllExperiments(): Flow<List<Experiment>>
    
    // Get archived experiments
    fun getArchivedExperiments(): Flow<List<Experiment>>
    
    // Update status
    suspend fun updateExperimentStatus(id: String, status: ExperimentStatus)
    
    // Update after completion
    suspend fun completeExperiment(
        id: String,
        decision: DecisionType,  // persist, pivot, pause (from Steering Sheet)
        nextExperimentId: String?  // If decision is "pivot" and a new experiment was created
    )
}
```

### Validation Rules

- **Hypothesis**: 1-500 characters, required
- **Action**: 1-500 characters, required
- **Why**: 0-500 characters, optional
- **Start date**: Must be today or later
- **End date**: Must be >= start date (min 1 day)
- **Duration**: 1-180 days (max 6 months)

---

## Entity: DailyLog

Represents a single day's check-in for an experiment.

### Database Schema

```sql
CREATE TABLE daily_logs (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL,
    date TEXT NOT NULL,             -- ISO 8601 date (YYYY-MM-DD)
    completed BOOLEAN NOT NULL,      -- Did user do the action today?
    mood_before INTEGER,             -- 1-5 rating (nullable)
    mood_after INTEGER,              -- 1-5 rating (nullable)
    notes TEXT,                      -- Free-text notes
    logged_at TEXT NOT NULL,         -- ISO 8601 datetime (when user logged it)
    
    FOREIGN KEY(experiment_id) REFERENCES experiments(id)
    UNIQUE(experiment_id, date)      -- One log per experiment per day
);

CREATE INDEX idx_daily_logs_experiment_date ON daily_logs(experiment_id, date);
```

### Kotlin Entity

```kotlin
@Entity(
    tableName = "daily_logs",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["experiment_id", "date"], unique = true),
        Index(value = ["experiment_id"])
    ]
)
data class DailyLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "experiment_id")
    val experimentId: String,
    
    @ColumnInfo(name = "date")
    val date: String,  // LocalDate.toString()
    
    @ColumnInfo(name = "completed")
    val completed: Boolean,
    
    @ColumnInfo(name = "mood_before")
    val moodBefore: Int?,  // 1-5
    
    @ColumnInfo(name = "mood_after")
    val moodAfter: Int?,  // 1-5
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "logged_at")
    val loggedAt: String  // Instant.now().toString()
)
```

### Domain Model

```kotlin
data class DailyLog(
    val id: String,
    val experimentId: String,
    val date: LocalDate,
    val completed: Boolean,
    val moodBefore: Int?,  // 1-5 or null
    val moodAfter: Int?,   // 1-5 or null
    val notes: String?,
    val loggedAt: Instant
)
```

### Repository Methods

```kotlin
interface DailyLogRepository {
    // Create/update
    suspend fun upsertDailyLog(log: DailyLog)
    
    // Get logs for a specific experiment
    fun getLogsForExperiment(experimentId: String): Flow<List<DailyLog>>
    
    // Get logs in date range (for displaying a week or month)
    suspend fun getLogsInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyLog>
    
    // Get log for specific date (check if already logged)
    suspend fun getLogForDate(experimentId: String, date: LocalDate): DailyLog?
    
    // Compute completion stats
    suspend fun getCompletionRate(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float  // 0.0 to 1.0
}
```

### Queries

```kotlin
@Dao
interface DailyLogDao {
    @Upsert
    suspend fun upsertLog(log: DailyLogEntity)
    
    @Query("SELECT * FROM daily_logs WHERE experiment_id = :experimentId ORDER BY date DESC")
    fun getLogsForExperiment(experimentId: String): Flow<List<DailyLogEntity>>
    
    @Query("""
        SELECT * FROM daily_logs 
        WHERE experiment_id = :experimentId 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    suspend fun getLogsInRange(
        experimentId: String,
        startDate: String,
        endDate: String
    ): List<DailyLogEntity>
    
    @Query("""
        SELECT * FROM daily_logs 
        WHERE experiment_id = :experimentId AND date = :date
    """)
    suspend fun getLogForDate(experimentId: String, date: String): DailyLogEntity?
    
    @Query("""
        SELECT COUNT(*) FROM daily_logs
        WHERE experiment_id = :experimentId 
        AND date BETWEEN :startDate AND :endDate
        AND completed = 1
    """)
    suspend fun countCompleted(
        experimentId: String,
        startDate: String,
        endDate: String
    ): Int
}
```

---

## Entity: Reflection

Represents a single reflection (Plus-Minus-Next).

### Database Schema

```sql
CREATE TABLE reflections (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL,
    reflection_date TEXT NOT NULL,  -- ISO 8601 date (when user reflected)
    plus TEXT,                      -- "What went well?"
    minus TEXT,                     -- "What was hard?"
    next TEXT,                      -- "What will you adjust?"
    created_at TEXT NOT NULL,
    
    FOREIGN KEY(experiment_id) REFERENCES experiments(id)
);

CREATE INDEX idx_reflections_experiment_date ON reflections(experiment_id, reflection_date);
```

### Kotlin Entity

```kotlin
@Entity(
    tableName = "reflections",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["experiment_id", "reflection_date"], unique = true)
    ]
)
data class ReflectionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "experiment_id")
    val experimentId: String,
    
    @ColumnInfo(name = "reflection_date")
    val reflectionDate: String,  // LocalDate.toString()
    
    @ColumnInfo(name = "plus")
    val plus: String?,
    
    @ColumnInfo(name = "minus")
    val minus: String?,
    
    @ColumnInfo(name = "next")
    val next: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
```

### Domain Model

```kotlin
data class Reflection(
    val id: String,
    val experimentId: String,
    val reflectionDate: LocalDate,
    val plus: String?,  // What went well
    val minus: String?, // What was hard
    val next: String?,  // What to adjust
    val createdAt: Instant
)
```

### Repository Methods

```kotlin
interface ReflectionRepository {
    suspend fun saveReflection(reflection: Reflection)
    
    // Get all reflections for an experiment (chronological)
    fun getReflectionsForExperiment(experimentId: String): Flow<List<Reflection>>
    
    // Get latest reflection (for experiment completion screen)
    suspend fun getLatestReflection(experimentId: String): Reflection?

    // Check if a reflection exists within a date range (used for weekly prompt logic)
    suspend fun getReflectionInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Reflection?
}
```

---

## Entity: Completion

Represents the final decision and learnings when an experiment ends.

### Database Schema

```sql
CREATE TABLE completions (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL UNIQUE,
    completion_date TEXT NOT NULL,     -- ISO 8601 date (when experiment ended)
    completion_rate REAL NOT NULL,     -- 0.0 to 1.0 (13/14 = 0.9286)
    decision TEXT NOT NULL,            -- persist, pivot, pause (Steering Sheet decisions)
    learnings TEXT,                    -- User's final reflection/summary
    next_experiment_id TEXT,           -- If decision is "pivot"
    created_at TEXT NOT NULL,
    
    FOREIGN KEY(experiment_id) REFERENCES experiments(id),
    FOREIGN KEY(next_experiment_id) REFERENCES experiments(id)
);
```

### Kotlin Entity

```kotlin
@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experiment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CompletionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "experiment_id")
    val experimentId: String,
    
    @ColumnInfo(name = "completion_date")
    val completionDate: String,
    
    @ColumnInfo(name = "completion_rate")
    val completionRate: Float,  // 0.0-1.0
    
    @ColumnInfo(name = "decision")
    val decision: String,  // "persist", "pivot", "pause"
    
    @ColumnInfo(name = "learnings")
    val learnings: String?,
    
    @ColumnInfo(name = "next_experiment_id")
    val nextExperimentId: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String
)
```

### Domain Model

```kotlin
data class Completion(
    val id: String,
    val experimentId: String,
    val completionDate: LocalDate,
    val completionRate: Float,  // 0.0-1.0
    val decision: DecisionType,
    val learnings: String?,
    val nextExperimentId: String?,  // For "pivot" decision
    val createdAt: Instant
)

enum class DecisionType {
    PERSIST, // Keep going as is — experiment is working, continue another round
    PIVOT,   // Adjust approach or try something new — holds potential but something feels off
    PAUSE    // Set aside for now — experiment isn't serving you; data saved for future reference
}
```

---

## Entity: FieldNote

Represents a single free-form observation — a timestamped note the user writes about anything they notice in their life. The raw material for identifying patterns and forming hypotheses. Not tied to any experiment.

### Database Schema

```sql
CREATE TABLE field_notes (
    id TEXT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL,   -- ISO 8601 datetime; user-editable (the "when" of the observation)
    updated_at TEXT NOT NULL    -- ISO 8601 datetime; system-managed, updated on every edit
);

CREATE INDEX idx_field_notes_created_at ON field_notes(created_at DESC);
```

### Kotlin Entity

```kotlin
@Entity(
    tableName = "field_notes",
    indices = [Index(value = ["created_at"])]
)
data class FieldNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,  // User-editable — the observation timestamp, not the insert time

    @ColumnInfo(name = "updated_at")
    val updatedAt: String   // Set to Instant.now() on every save
)
```

### Domain Model

```kotlin
data class FieldNote(
    val id: String,
    val content: String,
    val createdAt: Instant,  // User-editable; shown as the note's timestamp in the UI
    val updatedAt: Instant
)
```

### Repository Methods

```kotlin
interface FieldNoteRepository {
    // Ordered by createdAt DESC (most recent first)
    fun getAllNotes(): Flow<List<FieldNote>>

    suspend fun getNote(id: String): FieldNote?

    suspend fun saveNote(note: FieldNote)

    suspend fun updateNote(note: FieldNote)

    suspend fun deleteNote(id: String)
}
```

### DAO

```kotlin
@Dao
interface FieldNoteDao {
    @Query("SELECT * FROM field_notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<FieldNoteEntity>>

    @Query("SELECT * FROM field_notes WHERE id = :id")
    suspend fun getNote(id: String): FieldNoteEntity?

    @Upsert
    suspend fun upsertNote(note: FieldNoteEntity)

    @Query("DELETE FROM field_notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}
```

### Validation Rules

- **Content**: 1–5000 characters, required (no blank notes)
- **Created at**: Any valid datetime; defaults to now but user can change it (e.g., logging an observation from earlier in the day)
- No maximum number of notes

### Notes on the `created_at` Field

Unlike other entities where `created_at` is the insert timestamp and immutable, `FieldNote.createdAt` is the *observation* timestamp — the moment the user noticed something, which may differ from when they open the app to write it down. Making it editable lets users backfill notes accurately ("I noticed this at lunch but I'm writing it now at 9 PM").

---

## Relationships & Cascading

### Experiment Deletion

When an experiment is deleted (or marked as deleted):
- All DailyLogs for that experiment are deleted (CASCADE)
- All Reflections for that experiment are deleted (CASCADE)
- All Completions for that experiment are deleted (CASCADE)

Field notes are independent — they are never deleted by experiment deletion.

### Foreign Keys

```
Experiment (parent)
    ├── DailyLog.experiment_id → Experiment.id
    ├── Reflection.experiment_id → Experiment.id
    └── Completion.experiment_id → Experiment.id
    
Completion.next_experiment_id → Experiment.id (nullable, for pivots)
```

---

## Query Patterns

### Get Home Screen Data

```kotlin
// Active experiments with today's log status
@Query("""
    SELECT 
        e.*,
        (SELECT COUNT(*) FROM daily_logs 
         WHERE experiment_id = e.id AND completed = 1) as days_completed,
        (SELECT COUNT(*) FROM daily_logs 
         WHERE experiment_id = e.id) as total_days,
        (SELECT completed FROM daily_logs 
         WHERE experiment_id = e.id AND date = :today) as today_logged
    FROM experiments e
    WHERE e.status = 'ACTIVE'
    AND date('now') BETWEEN e.start_date AND e.end_date
    ORDER BY e.created_at DESC
""")
suspend fun getActiveExperimentsWithProgress(
    today: String
): List<ExperimentWithProgress>
```

### Completion Statistics

```kotlin
// For end-of-experiment screen
@Query("""
    SELECT
        COUNT(*) as total_days,
        SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) as days_completed,
        AVG(mood_before) as avg_mood_before,
        AVG(mood_after) as avg_mood_after
    FROM daily_logs
    WHERE experiment_id = :experimentId
""")
suspend fun getExperimentStats(experimentId: String): ExperimentStats
```

---

## Data Validation Layer

```kotlin
// In domain layer
object ExperimentValidator {
    fun validateHypothesis(hypothesis: String): Result<Unit> {
        return when {
            hypothesis.isBlank() -> Result.failure(Exception("Hypothesis required"))
            hypothesis.length > 500 -> Result.failure(Exception("Max 500 chars"))
            else -> Result.success(Unit)
        }
    }
    
    fun validateDuration(startDate: LocalDate, endDate: LocalDate): Result<Unit> {
        return when {
            startDate > endDate -> Result.failure(Exception("Start must be before end"))
            Days.daysBetween(startDate, endDate).days < 1 -> 
                Result.failure(Exception("Min 1 day"))
            Days.daysBetween(startDate, endDate).days > 180 -> 
                Result.failure(Exception("Max 180 days"))
            else -> Result.success(Unit)
        }
    }
}
```

---

## Database Migrations

### Version 1 (Initial)

All tables created as described above: `experiments`, `daily_logs`, `reflections`, `completions`, `field_notes`.

### Future Migrations

When schema changes:
1. Create new migration file: `Migration_1_2.kt`
2. Update database class:
```kotlin
@Database(
    entities = [ExperimentEntity::class, DailyLogEntity::class, ...],
    version = 2  // Increment
)
abstract class KokoromiDatabase : RoomDatabase {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ADD COLUMN, ALTER TABLE, etc.
            }
        }
    }
}
```

---

## Backup & Export

### JSON Export Format

```json
{
  "version": "1.0",
  "exportDate": "2026-03-30T15:45:30Z",
  "experiments": [
    {
      "id": "uuid",
      "hypothesis": "Walking helps me think",
      "action": "Take 15-min morning walk",
      "why": "See if it clears my head",
      "startDate": "2026-03-16",
      "endDate": "2026-03-30",
      "frequency": "DAILY",
      "status": "COMPLETED",
      "createdAt": "2026-03-16T10:00:00Z"
    }
  ],
  "dailyLogs": [
    {
      "id": "uuid",
      "experimentId": "uuid",
      "date": "2026-03-16",
      "completed": true,
      "moodBefore": 3,
      "moodAfter": 4,
      "notes": "Felt energized",
      "loggedAt": "2026-03-16T08:30:00Z"
    }
  ],
  "reflections": [
    {
      "id": "uuid",
      "experimentId": "uuid",
      "reflectionDate": "2026-03-23",
      "plus": "Felt good on walking days",
      "minus": "Skipped one day due to rain",
      "next": "Bring umbrella on rainy days",
      "createdAt": "2026-03-23T20:00:00Z"
    }
  ],
  "completions": [
    {
      "id": "uuid",
      "experimentId": "uuid",
      "completionDate": "2026-03-30",
      "completionRate": 0.9286,
      "decision": "persist",
      "learnings": "Walking definitely helps me focus. Will continue.",
      "nextExperimentId": null,
      "createdAt": "2026-03-30T15:45:00Z"
    }
  ],
  "fieldNotes": [
    {
      "id": "uuid",
      "content": "Noticed I have way more energy on days when I skip the morning news feed.",
      "createdAt": "2026-03-31T09:14:00Z",
      "updatedAt": "2026-03-31T09:14:00Z"
    }
  ]
}
```

---

## Testing the Data Layer

```kotlin
class ExperimentRepositoryTest {
    private lateinit var database: KokoromiDatabase
    private lateinit var repository: ExperimentRepository
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            context,
            KokoromiDatabase::class.java
        ).build()
        repository = DefaultExperimentRepository(
            database.experimentDao(),
            database.dailyLogDao(),
            // ... other DAOs
        )
    }
    
    @Test
    fun createsExperimentAndFetchesIt() = runTest {
        val exp = Experiment(
            id = UUID.randomUUID().toString(),
            hypothesis = "Test",
            action = "Action",
            why = null,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(14),
            frequency = Frequency.DAILY,
            status = ExperimentStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        repository.createExperiment(exp)
        val fetched = repository.getExperiment(exp.id)
        
        assertEquals(exp.hypothesis, fetched?.hypothesis)
    }
}
```

---

**Version**: 1.0  
**Last Updated**: March 2026
