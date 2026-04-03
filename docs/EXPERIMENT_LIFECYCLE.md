# Experiment Lifecycle: Kokoromi

## Overview

Every experiment follows a defined lifecycle: it is created, run day-by-day, and eventually resolved into a final decision. This document defines the state machine, valid transitions, edge cases, and the business logic that governs each transition.

---

## State Machine

```
                        ┌─────────────┐
                        │   CREATED   │  ← (transient: not stored, just validated)
                        └──────┬──────┘
                               │ save
                               ▼
                        ┌─────────────┐
                        │   ACTIVE    │ ←──────────────────┐
                        └──────┬──────┘                    │
                               │                           │
              ┌────────────────┼─────────────┐             │
              │                │             │             │
              ▼                ▼             ▼             │
        ┌──────────┐    ┌───────────┐  ┌─────────┐        │
        │  PAUSED  │    │ COMPLETED │  │ARCHIVED │        │
        └──────────┘    └─────┬─────┘  └─────────┘        │
              │               │                            │
              └───────────────┤ decision: KEEP / ADJUST    │
                              │──────────────────────────►─┘
                              │
                              │ decision: ARCHIVE
                              ▼
                        ┌─────────────┐
                        │  ARCHIVED   │
                        └─────────────┘

                              │ decision: PIVOT
                              ▼
                  ┌───────────────────────┐
                  │  new ACTIVE experiment │
                  └───────────────────────┘
```

---

## States

### ACTIVE

An experiment is active when:
- It has been saved with a start date ≤ today
- Its end date has not yet passed
- Status is explicitly `ACTIVE`

**Behavior while ACTIVE**:
- Appears on Home Screen
- User can log a daily check-in for each day from `startDate` to today
- User can write a weekly reflection at any time
- User can manually pause or archive it

**Slot rule**: At most 2 experiments can be ACTIVE at the same time. The create flow enforces this—if 2 are already active, the button is disabled with a clear explanation.

---

### PAUSED

An experiment is paused when the user explicitly chooses to pause it.

**When to use**: The user wants to stop tracking temporarily without losing their data or making a final decision. Travel, illness, life interruption.

**Behavior while PAUSED**:
- Removed from Home Screen
- No check-ins expected or prompted
- Data (past logs, reflections) fully preserved
- Slot freed up: another experiment can become active

**Transition back to ACTIVE**: User resumes from the Archive/Paused screen. End date is NOT extended automatically—that's a future enhancement. User can choose to edit the end date if they resume.

---

### COMPLETED

An experiment is completed when its `endDate` has passed and the user has not yet made a final decision.

**When this triggers**: On app open, the system checks all ACTIVE experiments. Any whose `endDate < today` are transitioned to `COMPLETED` automatically.

**Behavior while COMPLETED**:
- Triggers the Completion Screen on next app open (or from Home Screen banner)
- No new check-ins can be logged (date has passed)
- User must make a decision to move forward

**Important**: COMPLETED is a transitional state, not a resting state. The app gently prompts the user to resolve it, but does not force them immediately.

---

### ARCHIVED

An experiment is archived after the user makes a final decision at the Completion Screen, choosing "Archive" or "Pivot". Also reachable directly from ACTIVE if the user chooses to archive early.

**Behavior while ARCHIVED**:
- Visible in the Archive Screen with full history
- All daily logs, reflections, and completion data readable
- No further check-ins possible
- Cannot be un-archived (by design—decisions are final, but data is preserved)

---

## Transitions

### ACTIVE → PAUSED

**Trigger**: User taps "Pause experiment" in the experiment detail or context menu.

**Guard**: None. Any active experiment can be paused.

**Side effects**:
- `status` updated to `PAUSED`
- `updated_at` timestamp updated
- Experiment removed from Home Screen immediately
- No check-in entry created for the pause day

**UI**: Confirm dialog: "Pause this experiment? You can resume it later." [Pause] [Cancel]

---

### PAUSED → ACTIVE

**Trigger**: User taps "Resume" on the paused experiment in the Archive/Paused list.

**Guard**: Active experiment slot must be available (< 2 active experiments).

**Side effects**:
- `status` updated to `ACTIVE`
- `updated_at` timestamp updated
- Experiment reappears on Home Screen

**Edge case**: If 2 experiments are already active, the Resume button is disabled with explanation: "Finish or pause an active experiment first."

---

### ACTIVE → COMPLETED (automatic)

**Trigger**: App open, background check finds `endDate < today` for an ACTIVE experiment.

**Guard**: None—this is always valid.

**Side effects**:
- `status` updated to `COMPLETED`
- Completion Screen shown (or banner on Home Screen if user dismisses)
- Completion stats computed (completion rate, mood averages)

**Implementation**:
```kotlin
// In HomeViewModel or a dedicated lifecycle checker
suspend fun checkForCompletedExperiments() {
    val activeExperiments = experimentRepository.getActiveExperiments().first()
    val today = LocalDate.now()
    activeExperiments
        .filter { it.endDate < today }
        .forEach { experiment ->
            experimentRepository.updateExperimentStatus(
                id = experiment.id,
                status = ExperimentStatus.COMPLETED
            )
        }
}
```

---

### ACTIVE → ARCHIVED (early exit)

**Trigger**: User taps "Pause experiment" early — before the end date — from the experiment detail context menu. This is an intentional early stop, not a mid-experiment pause.

**Guard**: None.

**Side effects**:
- `status` updated to `ARCHIVED`
- A `Completion` record is created with:
  - `completionDate` = today
  - `completionRate` = computed from logs so far
  - `decision` = `PAUSE`
  - `learnings` = optional user input

**UI**: Brief dialog: "End this experiment early? Your data is saved." [End experiment] [Cancel]

---

### COMPLETED → ARCHIVED (Pause)

**Trigger**: User makes a final decision at the Completion Screen: "Pause".

**Meaning**: The experiment isn't serving the user right now. Set it aside. Data is preserved. Could return to it in the future.

**Side effects**:
- `status` updated to `ARCHIVED`
- `Completion` record created with `decision = PAUSE` and user's learnings

**Note**: This is a completed experiment that has been consciously set aside — distinct from mid-experiment PAUSED status, which means temporarily stopped with intent to resume.

---

### COMPLETED → ACTIVE (Persist)

**Trigger**: User makes a final decision at the Completion Screen: "Persist".

**Meaning**: The experiment is working and bringing value. Keep going as is with a new round.

**Behavior**: Start a new experiment identical to the current one, with a new start/end date.

**Side effects**:
- Old experiment: `status` → `ARCHIVED`, `Completion` record created with `decision = PERSIST`
- New experiment: `status` = `ACTIVE` (clean slate, no link needed)

**Guard**: Slot must be available for the new ACTIVE experiment.

---

### COMPLETED → ACTIVE (Pivot)

**Trigger**: User makes final decision: "Pivot".

**Meaning**: The experiment holds potential but something feels off. Adjust the approach.

**Two sub-flows**:
- **Adjust this pact**: Opens Create Experiment screen pre-filled with current data. User modifies action, duration, or hypothesis.
- **Try a new pact**: Opens Create Experiment screen blank. User creates a different experiment.

Both are stored as `decision = PIVOT`.

**Side effects**:
- Old experiment: `status` → `ARCHIVED`, `Completion` record created with `decision = PIVOT`
- New experiment: `status` = `ACTIVE`, optionally linked via `Completion.next_experiment_id`
- Pivot chain visible in experiment detail (future enhancement)

---

## Check-In Logic

### Can the user check in today?

```kotlin
fun canCheckInToday(experiment: Experiment, today: LocalDate): Boolean {
    return experiment.status == ExperimentStatus.ACTIVE
        && today >= experiment.startDate
        && today <= experiment.endDate
}
```

### Can the user back-fill a past date?

**Decision**: Yes, within the experiment window. Users can log yesterday if they forgot.

```kotlin
fun canLogForDate(experiment: Experiment, date: LocalDate): Boolean {
    return experiment.status == ExperimentStatus.ACTIVE
        && date >= experiment.startDate
        && date <= minOf(experiment.endDate, LocalDate.now())
}
```

**Rationale**: Life is messy. Forgot to log Sunday? Log it Monday. We show the data honestly either way.

### What if they log twice for the same day?

The database enforces `UNIQUE(experiment_id, date)`. The repository uses `UPSERT` — the latest entry wins. The UI shows "Edit today's log" if one already exists.

---

## Completion Stats Computation

Computed at the moment a COMPLETED experiment is resolved (Completion Screen):

```kotlin
data class CompletionStats(
    val totalDays: Int,           // endDate - startDate + 1
    val daysLogged: Int,          // rows in daily_logs for this experiment
    val daysCompleted: Int,       // rows where completed = true
    val completionRate: Float,    // daysCompleted / totalDays (not / daysLogged)
    val avgMoodBefore: Float?,    // null if no mood data
    val avgMoodAfter: Float?,
    val moodDelta: Float?         // avgMoodAfter - avgMoodBefore (if both present)
)
```

**Note**: `completionRate` divides by `totalDays`, not `daysLogged`. A day with no log entry is treated as not completed. This is honest.

---

## Edge Cases

### Experiment created with start date in the future

Allowed. The experiment stays ACTIVE but check-ins are not available until `startDate`. The Home Screen shows it as "starting on [date]".

### End date passes while app is offline

When the app next opens, it runs the lifecycle check and transitions COMPLETED. No data is lost—all logs still exist.

### User changes device or reinstalls app

All data is local. On reinstall, data is gone unless the user exported a backup. This is a known limitation of the offline-first design. Future: optional cloud backup.

### Two experiments complete on the same day

Both will show on the Completion Screen (or as banners). User resolves them one at a time. No special handling needed.

### Experiment paused on its last day

The `endDate` passes while paused. When the user resumes, the experiment would immediately be past its end date. On resume:
- Transition to ACTIVE is allowed
- Lifecycle check immediately transitions it to COMPLETED
- User is taken to Completion Screen

**This is acceptable behavior.** We don't auto-extend end dates on resume.

---

## Business Rules Summary

| Rule | Value |
|------|-------|
| Max active experiments | 2 |
| Minimum experiment duration | 1 day |
| Maximum experiment duration | 180 days |
| Default experiment duration | 14 days |
| Can log for past dates? | Yes, within experiment window |
| Can log for future dates? | No |
| Can un-archive? | No |
| Does pause extend end date? | No (v1) |
| Completion rate denominator | Total days in window |
| End-of-experiment decisions | Persist, Pivot, Pause (Steering Sheet) |

---

## Use Cases Involved

| Action | Use Case |
|--------|---------|
| Create experiment | `CreateExperimentUseCase` |
| Log check-in | `LogDailyCheckInUseCase` |
| Pause experiment (mid-run) | `UpdateExperimentStatusUseCase` |
| Resume experiment | `UpdateExperimentStatusUseCase` |
| Auto-complete expired | `CheckExperimentLifecycleUseCase` |
| Resolve completed (Persist/Pivot/Pause) | `CompleteExperimentUseCase` |
| End early (Pause decision) | `CompleteExperimentUseCase` (with PAUSE decision) |

---

**Version**: 1.0
**Last Updated**: March 2026
**Status**: Ready for implementation
