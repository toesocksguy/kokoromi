# Data Model

Five tables, all local SQLite via Room. All dates stored as ISO 8601 strings; `LocalDate`/`Instant` ↔ string conversion handled by Room TypeConverters.

---

## Experiments

```sql
CREATE TABLE experiments (
    id TEXT PRIMARY KEY,
    hypothesis TEXT NOT NULL,       -- "I think X will happen if I do Y"
    action TEXT NOT NULL,            -- "I will [action] for [duration]"
    why TEXT,                        -- Optional motivation
    start_date TEXT NOT NULL,        -- YYYY-MM-DD
    end_date TEXT NOT NULL,          -- Calculated from start + duration
    frequency TEXT NOT NULL,         -- DAILY | CUSTOM
    frequency_custom TEXT,           -- Reserved for future custom schedules
    status TEXT NOT NULL,            -- ACTIVE | COMPLETED | ARCHIVED | PAUSED
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

**Domain model**
```kotlin
data class Experiment(
    val id: String,
    val hypothesis: String,
    val action: String,
    val why: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val frequency: Frequency,        // DAILY, CUSTOM
    val status: ExperimentStatus,    // ACTIVE, COMPLETED, ARCHIVED, PAUSED
    val createdAt: Instant,
    val updatedAt: Instant
)
```

**Validation**

| Field | Rule |
|-------|------|
| hypothesis | 1–500 chars, required |
| action | 1–500 chars, required |
| why | 0–500 chars, optional |
| start_date | today or later |
| duration | 1–180 days |

---

## DailyLogs

```sql
CREATE TABLE daily_logs (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    date TEXT NOT NULL,             -- YYYY-MM-DD
    completed BOOLEAN NOT NULL,
    mood_before INTEGER,            -- 1–5, nullable
    mood_after INTEGER,             -- 1–5, nullable
    notes TEXT,
    logged_at TEXT NOT NULL,
    UNIQUE(experiment_id, date)     -- one log per experiment per day
);

CREATE INDEX idx_daily_logs_experiment_date ON daily_logs(experiment_id, date);
```

**Domain model**
```kotlin
data class DailyLog(
    val id: String,
    val experimentId: String,
    val date: LocalDate,
    val completed: Boolean,
    val moodBefore: Int?,   // 1–5
    val moodAfter: Int?,    // 1–5
    val notes: String?,
    val loggedAt: Instant
)
```

---

## Reflections

```sql
CREATE TABLE reflections (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL REFERENCES experiments(id) ON DELETE CASCADE,
    reflection_date TEXT NOT NULL,  -- YYYY-MM-DD
    plus TEXT,                      -- What went well
    minus TEXT,                     -- What was hard
    next TEXT,                      -- What to adjust
    created_at TEXT NOT NULL,
    UNIQUE(experiment_id, reflection_date)
);

CREATE INDEX idx_reflections_experiment_date ON reflections(experiment_id, reflection_date);
```

**Domain model**
```kotlin
data class Reflection(
    val id: String,
    val experimentId: String,
    val reflectionDate: LocalDate,
    val plus: String?,
    val minus: String?,
    val next: String?,
    val createdAt: Instant
)
```

**Validation**: at least one of plus/minus/next required; each field max 2000 chars.

---

## Completions

```sql
CREATE TABLE completions (
    id TEXT PRIMARY KEY,
    experiment_id TEXT NOT NULL UNIQUE REFERENCES experiments(id) ON DELETE CASCADE,
    completion_date TEXT NOT NULL,
    completion_rate REAL NOT NULL,      -- 0.0–1.0
    decision TEXT NOT NULL,             -- persist | pivot | pause
    learnings TEXT,
    next_experiment_id TEXT REFERENCES experiments(id),  -- populated on pivot
    created_at TEXT NOT NULL
);
```

**Domain model**
```kotlin
data class Completion(
    val id: String,
    val experimentId: String,
    val completionDate: LocalDate,
    val completionRate: Float,
    val decision: DecisionType,     // PERSIST, PIVOT, PAUSE
    val learnings: String?,
    val nextExperimentId: String?,  // set on PIVOT
    val createdAt: Instant
)
```

---

## FieldNotes

Free-form observations not tied to any experiment. Raw material for pattern recognition and hypothesis formation.

```sql
CREATE TABLE field_notes (
    id TEXT PRIMARY KEY,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL,   -- user-editable: the observation time, not insert time
    updated_at TEXT NOT NULL    -- system-managed
);

CREATE INDEX idx_field_notes_created_at ON field_notes(created_at DESC);
```

**Domain model**
```kotlin
data class FieldNote(
    val id: String,
    val content: String,
    val createdAt: Instant,  // editable — lets users backfill past observations
    val updatedAt: Instant
)
```

**Validation**: content 1–5000 chars; no max count.

---

## Relationships

```
Experiment (parent)
  ├── DailyLog.experiment_id  → CASCADE DELETE
  ├── Reflection.experiment_id → CASCADE DELETE
  └── Completion.experiment_id → CASCADE DELETE

Completion.next_experiment_id → Experiment.id (nullable, pivot only)

FieldNote — independent, no foreign keys
```

---

## Export Format (JSON)

Logs, reflections, and completion are nested inside each experiment object. Field notes are a top-level array.

```json
{
  "version": 1,
  "exportedAt": "2026-03-30T15:45:30Z",
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
      "createdAt": "2026-03-16T10:00:00Z",
      "updatedAt": "2026-03-30T15:45:00Z",
      "logs": [
        {
          "id": "uuid",
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
          "reflectionDate": "2026-03-23",
          "plus": "Felt good on walking days",
          "minus": "Skipped one day due to rain",
          "next": "Bring umbrella on rainy days",
          "createdAt": "2026-03-23T20:00:00Z"
        }
      ],
      "completion": {
        "completionDate": "2026-03-30",
        "completionRate": 0.9286,
        "decision": "PERSIST",
        "learnings": "Walking definitely helps me focus."
      }
    }
  ],
  "fieldNotes": [
    {
      "id": "uuid",
      "content": "More energy on days I skip the morning news feed.",
      "createdAt": "2026-03-31T09:14:00Z",
      "updatedAt": "2026-03-31T09:14:00Z"
    }
  ]
}
```

**Notes:**
- `completion` is omitted when absent (experiment not yet completed)
- `why`, `moodBefore`, `moodAfter`, `notes`, `plus`, `minus`, `next`, `learnings` are `null` when not set
- `decision` values: `PERSIST`, `PIVOT`, `PAUSE`
- `nextExperimentId` is not included in the export (PIVOT links are not preserved on import)
