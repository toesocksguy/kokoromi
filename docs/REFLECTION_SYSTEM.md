# Reflection System

Reflection is how experiments become learning. Two types: **weekly** (prompted) and **daily** (optional, user-initiated). Both use Plus-Minus-Next. Both are optional.

---

## Plus-Minus-Next Framework

| Field | Question | Purpose |
|-------|----------|---------|
| **Plus (+)** | What went well? What did you enjoy? | Anchor positive observations |
| **Minus (−)** | What didn't work? What felt off? | Surface friction without judgment |
| **Next (→)** | What small tweak can you make? | Close the learning loop |

Simple enough for 2 minutes. Non-judgmental. Covers past (plus, minus) and future (next).

---

## Three Reflection Types

### 1. Weekly (prompted)

Triggered on the user's configured reflection day (default: Sunday). One per experiment per week (Monday–Sunday window). Scope: the past week, not the whole experiment.

**Show prompt when**: today is the reflection day AND an active experiment exists AND no reflection saved for this experiment this week.

**Saving**: any one field is sufficient; all-blank is rejected. **Editing**: same-week reflections editable until week ends, then read-only.

### 2. Daily (optional, user-initiated)

Triggered only by the user — never prompted automatically. Entry point: subtle link on Check-In Screen after logging activity. Same `Reflection` entity, `reflection_date` = today. One per day per experiment (unique constraint). Does not replace the weekly prompt — they're independent.

### 3. Completion (at experiment end)

Different flow: the Completion Screen asks for overall `learnings` as a single free-text field on the `Completion` entity — not a Plus-Minus-Next form. Plus-Minus-Next is for ongoing reflection; `learnings` is for synthesis.

The most recent weekly reflection is shown as context on the Completion Screen so the user doesn't start from blank.

---

## Screen Flow (Weekly)

```
Home Screen
    │ [Reflect] tapped (or prompt card tapped)
    ▼
Reflection Screen
    ├── Experiment name + week date range
    ├── Plus field (multiline, optional)
    ├── Minus field (multiline, optional)
    ├── Next field (multiline, optional)
    └── [Save] / [Skip]
```

Skip: no record created, no penalty, no reminder.

---

## Prompt Text

```
Plus (+):  "What went well? What did you enjoy?"
Minus (−): "What didn't work? What felt off?"
Next (→):  "What small tweak can you make based on what you've learned?"
```

**Tone**: curiosity-first. No "did you succeed?" framing. No performance rating. "What surprised you?" not "What did you do right?"

---

## Display Format

Past reflections shown in Experiment Detail and Archive, reverse chronological:

```
Week of Mar 16 – Mar 22
+ Morning walks helped me think through design problems.
− Skipped Wednesday — meeting ran long and I lost the habit.
→ Set a 7 AM alarm as an anchor for the walk.
```

---

## Settings

- **Reflection day**: any day of week, default Sunday. Stored in `UserPreferences.reflectionDay`.
- Reflection time: not configurable in v1 — prompt shows all day, no notification.
- Frequency: always weekly in v1.
- Prompt text: always Plus-Minus-Next in v1.

---

## Edge Cases

**Not the reflection day**: no prompt shown; user can still add manually.

**No active experiments**: no prompt shown.

**Experiment ends before Sunday**: weekly prompt won't trigger post-completion; existing reflections preserved in Archive.

**All fields blank at save**: Save button disabled; empty reflections not stored.

**Reflection on a completed experiment**: not possible via main flow — Completion Screen is read-only for reflections; only `learnings` field accepts new input.
