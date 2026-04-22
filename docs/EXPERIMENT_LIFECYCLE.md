# Experiment Lifecycle

## State Machine

```
                     ┌─────────────┐
                     │   CREATED   │  ← transient: validated, not stored
                     └──────┬──────┘
                            │ save
                            ▼
                     ┌─────────────┐
                     │   ACTIVE    │ ◄──────────────────┐
                     └──────┬──────┘                    │
                            │                           │
           ┌────────────────┼──────────────┐            │
           │                │              │            │
           ▼                ▼              ▼            │
     ┌──────────┐    ┌───────────┐   ┌──────────┐      │
     │  PAUSED  │    │ COMPLETED │   │ ARCHIVED │      │
     └────┬─────┘    └─────┬─────┘   └──────────┘      │
          │                │                            │
          └────────────────┤ Persist / Pivot ───────────┘
                           │
                           │ Pause (decision)
                           ▼
                     ┌──────────┐
                     │ ARCHIVED │
                     └──────────┘
```

---

## States

**ACTIVE** — on Home Screen; check-ins available; max 2 at once.

**PAUSED** — mid-experiment stop with intent to resume. Removed from Home Screen; slot freed; all data preserved. End date is not extended on resume (v1).

**COMPLETED** — transitional state: `endDate` has passed, no decision made yet. Triggers Completion Screen banner. No new check-ins possible. Auto-set on app open by `CheckExperimentLifecycleUseCase`.

**ARCHIVED** — terminal state. Visible in Archive with full history. No further check-ins. Cannot be un-archived (by design — decisions are final, data is preserved).

---

## Transitions

### ACTIVE → PAUSED
User taps "Pause" in experiment detail. No guard. Sets `status = PAUSED`, frees the slot.

**UI**: confirm dialog — "Pause this experiment? You can resume it later."

### PAUSED → ACTIVE (Resume)
User taps "Resume" from Archive/Paused list.

**Guard**: fewer than 2 experiments currently ACTIVE. If full, button is disabled: "Finish or pause an active experiment first."

### ACTIVE → COMPLETED (automatic)
On app open, `CheckExperimentLifecycleUseCase` transitions any ACTIVE experiment whose `endDate < today` to COMPLETED.

### ACTIVE → ARCHIVED (early exit)
User ends experiment before `endDate`. Creates a `Completion` record with `decision = PAUSE`, `completionDate = today`, and computed stats so far.

**UI**: "End this experiment early? Your data is saved."

### COMPLETED → ARCHIVED (Pause decision)
User's final decision is "Pause" — experiment isn't serving them right now. Sets `status = ARCHIVED`, creates `Completion` with `decision = PAUSE`.

**Note**: This is a completed experiment consciously set aside — distinct from mid-experiment PAUSED status (temporary stop, intent to resume).

### COMPLETED → ACTIVE (Persist)
Experiment is working; keep going. Archives old experiment with `decision = PERSIST`, creates a new identical experiment as ACTIVE.

**Guard**: slot must be available for the new experiment.

### COMPLETED → ACTIVE (Pivot)
Something feels off; adjust the approach. Two sub-flows:
- **Adjust this pact**: Create Experiment screen pre-filled with current data
- **Try a new pact**: Create Experiment screen blank

Both archive old experiment with `decision = PIVOT`. New experiment optionally linked via `Completion.next_experiment_id`.

---

## Check-In Rules

- Check-ins allowed only when `status == ACTIVE` and `startDate ≤ date ≤ endDate`
- Back-filling past dates is allowed within the experiment window (life is messy; log Sunday on Monday)
- Future dates: not allowed
- Duplicate on the same day: `UPSERT` — latest entry wins; UI shows "Edit today's log"

---

## Completion Stats

Computed at Completion Screen from all `DailyLog` rows for the experiment:

| Field | Computation |
|-------|-------------|
| totalDays | `endDate - startDate + 1` |
| daysLogged | count of log rows |
| daysCompleted | count where `completed = true` |
| completionRate | `daysCompleted / totalDays` (not / daysLogged) |
| avgMoodAfter | average of non-null `mood_after` values |
| moodDelta | `avgMoodAfter - avgMoodBefore` (if both present) |

**completionRate uses `totalDays` as denominator** — a day with no log is treated as not completed. This is honest.

---

## Edge Cases

**Start date in the future**: allowed. Check-ins unavailable until `startDate`; card shows "starting on [date]".

**End date passes while app offline**: lifecycle check runs on next open; transitions to COMPLETED. No data lost.

**Paused experiment whose end date passes while paused**: on resume, lifecycle check fires immediately → COMPLETED → Completion Screen. End date is not auto-extended.

**Two experiments complete on the same day**: both show as banners on Home Screen; user resolves one at a time. No special handling.

**Reinstall / new device**: all data is local. Data lost without a prior export. Known limitation of offline-first.

---

## Business Rules

| Rule | Value |
|------|-------|
| Max active experiments | 2 |
| Min duration | 1 day |
| Max duration | 180 days |
| Default duration | 7 days |
| Back-fill past dates | Yes, within experiment window |
| Log future dates | No |
| Un-archive | No |
| Pause extends end date | No (v1) |
| Completion rate denominator | Total days in window |
| End-of-experiment decisions | Persist · Pivot · Pause |

---

## Use Cases

| Action | Use Case |
|--------|---------|
| Create | `CreateExperimentUseCase` |
| Log check-in | `LogDailyCheckInUseCase` |
| Pause / Resume (mid-run) | `UpdateExperimentStatusUseCase` |
| Auto-complete expired | `CheckExperimentLifecycleUseCase` |
| Resolve at completion | `CompleteExperimentUseCase` |
