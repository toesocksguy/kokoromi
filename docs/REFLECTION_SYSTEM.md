# Reflection System: Kokoromi

## Overview

Reflection is how experiments become learning. The app supports two types of reflection: **weekly reflections** (periodic, prompted) and **completion reflections** (final, at experiment end). Both use the Plus-Minus-Next framework. Both are optional.

---

## The Plus-Minus-Next Framework

Borrowed from Anne-Laure Le Cunff's Tiny Experiments methodology:

| Field | Question | Purpose |
|-------|----------|---------|
| **Plus (+)** | What went well? What did you enjoy? | Anchor positive observations |
| **Minus (−)** | What didn't work? What felt off? | Surface friction without judgment |
| **Next (→)** | What small tweak can you make based on what you've learned? | Close the learning loop |

**Why this framework**:
- Simple enough to complete in 2 minutes
- Covers past (plus, minus) and future (next) — a complete loop
- Non-judgmental: "felt off" ≠ "failed"
- Scales to any experiment type (physical, mental, creative, social)
- Flexible cadence: can be used daily, weekly, or monthly depending on the format of the pact
- Well-established in the Ness Labs / Tiny Experiments community

---

## Three Types of Reflection

### 1. Weekly Reflection (prompted)

**When**: Periodically throughout the experiment. Default trigger: Sunday evening (user's configured reflection day). Configurable in Settings.

**Scope**: Reflects on the past week of an experiment (not the whole experiment).

**Frequency**: The weekly prompt is the default automatic trigger. The book notes Plus Minus Next can be used daily, weekly, or monthly depending on the pact's format. V1 defaults to weekly prompts for all experiments; future versions could make the prompt cadence configurable per-experiment.

**Data stored**: `Reflection` entity with `reflection_date`, `plus`, `minus`, `next`.

**Character limits**: 1000 characters per field (generous — reflections can be detailed).

---

### 2. Daily Reflection (optional, user-initiated)

**When**: After completing a daily check-in, the user can optionally add a quick reflection for that day.

**Scope**: Reflects on just that day's experience — a lighter, more immediate version of Plus Minus Next.

**Frequency**: User-initiated only. Never prompted automatically. No pressure.

**Entry point**: A subtle "Add reflection" link appears on the Check-In Screen after the user logs an activity. Also accessible from the Experiment Detail Screen by tapping any daily log entry.

**Data stored**: Same `Reflection` entity, with `reflection_date` = today. One per day per experiment (the unique constraint on `experiment_id` + `reflection_date` naturally enforces this).

**Relationship to weekly**: Daily and weekly reflections coexist. A daily reflection for a day within a weekly reflection's week does not replace the weekly prompt — they're independent entries. The weekly reflection screen shows how many daily reflections were logged that week as context.

---

### 3. Completion Reflection

**When**: At the Completion Screen, when the user is making their final decision.

**Scope**: Reflects on the whole experiment.

**This is a different flow**: The Completion Screen asks "What did you learn overall?" as a single free-text `learnings` field, not a full Plus-Minus-Next. The Plus-Minus-Next is for ongoing reflection; the completion field is for synthesis.

**Data stored**: `Completion.learnings` field.

---

## Weekly Reflection Flow

### Entry Points

1. **Organic**: User opens the app on a Sunday (or their configured reflection day) and sees a reflection prompt on the Home Screen.
2. **Manual**: User taps "Reflect" on an experiment card at any time.
3. **Archive screen**: User taps "Add reflection" on an active experiment from the Archive/Detail view.

### Screen Flow

```
Home Screen
    │
    │ [Reflect] tapped on experiment card
    ▼
Reflection Screen
    ├── Shows experiment name and current week date range
    ├── Plus field (multiline text input)
    ├── Minus field (multiline text input)
    ├── Next field (multiline text input)
    └── [Save] / [Skip]
```

### Behavior

**Saving**: All three fields are optional. A reflection can be saved with only Plus filled in, or all three, or just Next. An empty reflection (all blank) is not saved.

**Skipping**: User taps "Skip" or navigates back. No reflection is created. No penalty. No reminder.

**Editing**: A saved reflection for the current week can be edited until the week ends. After that, it becomes read-only (in v1). This prevents retroactive rewriting of the record but allows same-day corrections.

**One per week per experiment**: The system allows one reflection per 7-day window (Monday–Sunday) per experiment. If a reflection already exists for this week, the screen opens in edit mode.

---

## Reflection Prompt Text

Prompts are designed to be curious and open-ended, not evaluative.

### Default Prompts

```
Plus (+):  "What went well? What did you enjoy?"
Minus (−): "What didn't work? What felt off?"
Next (→):  "What small tweak can you make based on what you've learned?"
```

### Tone Guidelines

- No "Did you succeed?" framing
- No "How many days did you complete?" (that's for the check-in, not reflection)
- No "Rate your performance"
- Curiosity-first: "What surprised you?" rather than "What did you do right?"

---

## When to Show the Reflection Prompt

### Rule

Show a reflection prompt card on the Home Screen when:
1. Today is the user's configured reflection day (default: Sunday)
2. An active experiment exists
3. No reflection has been saved for this experiment in the current week

### Implementation

```kotlin
data class ReflectionPromptState(
    val shouldShowPrompt: Boolean,
    val experimentId: String,
    val experimentName: String
)

suspend fun getReflectionPromptState(
    experiment: Experiment,
    today: LocalDate,
    reflectionDay: DayOfWeek,
    reflectionRepository: ReflectionRepository
): ReflectionPromptState {
    val isReflectionDay = today.dayOfWeek == reflectionDay
    if (!isReflectionDay) return ReflectionPromptState(false, ...)

    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    val existingReflection = reflectionRepository.getReflectionInRange(
        experimentId = experiment.id,
        startDate = weekStart,
        endDate = weekEnd
    )

    return ReflectionPromptState(
        shouldShowPrompt = existingReflection == null,
        experimentId = experiment.id,
        experimentName = experiment.hypothesis
    )
}
```

---

## Reflection Data Model

### Reflection Entity (from DATA_MODEL.md)

```kotlin
data class Reflection(
    val id: String,
    val experimentId: String,
    val reflectionDate: LocalDate,  // The date the user reflected
    val plus: String?,
    val minus: String?,
    val next: String?,
    val createdAt: Instant
)
```

### Additional Repository Method (extends DATA_MODEL.md)

```kotlin
interface ReflectionRepository {
    // From DATA_MODEL.md
    suspend fun saveReflection(reflection: Reflection)
    fun getReflectionsForExperiment(experimentId: String): Flow<List<Reflection>>
    suspend fun getLatestReflection(experimentId: String): Reflection?

    // Additional: check if reflection exists for current week
    suspend fun getReflectionInRange(
        experimentId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Reflection?
}
```

---

## Displaying Past Reflections

Past reflections are visible in:
1. **Experiment Detail Screen** — full timeline of reflections for one experiment
2. **Archive Screen** — when viewing a completed experiment, all reflections shown

### Display Format

```
Week of Mar 16 – Mar 22
+ Morning walks helped me think through design problems.
− Skipped Wednesday — meeting ran long and I lost the habit.
→ Set a 7 AM alarm as an anchor for the walk.
```

Reflections are shown in reverse chronological order (most recent first).

---

## Reflection Settings

### Configurable: Reflection Day

User can change which day of the week triggers the reflection prompt.

Default: Sunday
Options: Any day of the week
Stored in: `UserPreferences.reflectionDay` (DataStore)

### Not Configurable in v1

- Reflection time (prompt shows all day on the reflection day, not at a specific time — no notifications)
- Reflection frequency (always weekly — could be monthly in future)
- Custom prompt text (always Plus-Minus-Next in v1)

---

## Reflection and Completion

When an experiment completes, the most recent weekly reflection is shown as context on the Completion Screen ("Here's what you noted recently..."). This helps the user write their final learnings without starting from a blank slate.

```kotlin
// CompletionViewModel
val latestReflection: StateFlow<Reflection?> = reflectionRepository
    .getLatestReflection(experimentId)
    .stateIn(viewModelScope, SharingStarted.Lazily, null)
```

---

## Edge Cases

### User opens app mid-week (not on reflection day)

No prompt shown. User can still manually add a reflection via the experiment card menu.

### User has no active experiments

No reflection prompt shown. Nothing to reflect on.

### Experiment ends before Sunday

The weekly reflection prompt won't trigger after the experiment completes. Any reflections written mid-experiment are preserved and visible in Archive.

### User writes a reflection for a completed experiment

Not possible via the main flow. A completed experiment's Reflection Screen is read-only. Only the `Completion.learnings` field accepts new input at the Completion Screen.

### All fields are blank at save

The Save button is disabled when all three fields are empty. An empty reflection is not saved (no point storing an empty record).

---

## Future Enhancements (Out of Scope for v1)

- **Monthly reflection**: Longer synthesis across multiple experiments
- **Custom prompts**: User writes their own reflection questions
- **Reflection reminders**: Optional notification at a specific time (user opt-in)
- **Cross-experiment themes**: Surface patterns across multiple experiments ("You often mention sleep as a minus")
- **Export reflections**: JSON export already includes reflections; future: Markdown or plain text export

---

**Version**: 1.0
**Last Updated**: March 2026
**Status**: Ready for implementation
