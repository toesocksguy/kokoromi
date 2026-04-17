# UI Design

## Design System

**Colors** — Material 3 tokens; all respect system dark mode.

| Role | Color |
|------|-------|
| Primary actions / Active status | Teal |
| Secondary actions / Paused status | Gray |
| Destructive / Warnings | Coral |
| Completed status | Teal (lighter) |

**Typography**

| Style | Size | Weight |
|-------|------|--------|
| Screen titles | 24sp | 600 |
| Section headers | 20sp | 600 |
| Card titles | 16sp | 600 |
| Body | 16sp | 400 |
| Small / Caption | 14–12sp | 400 |

**Spacing**: 4 / 8 / 16 / 24 / 32 dp

**Touch targets**: 48 × 48 dp minimum. **Icons**: Material Icons, 24dp standard.

---

## Navigation

```
Home
├── Tap experiment card → Check-In
├── Completion banner tap → Completion Screen
├── Reflection prompt card tap → Reflection Screen
└── Bottom nav: Home · Notes · Archive · Settings

Check-In → back to Home (or forward to Reflection)

Field Notes
└── Tap note / [+] → Add/Edit Note

Archive
└── Tap experiment → Experiment Detail

Create Experiment ← from Home (+ button) or post-Pivot

Completion Screen ← auto on experiment end
  ├── Persist → Home
  ├── Pivot → Create Experiment (pre-filled or blank)
  └── Pause → Home

Settings (standalone)
```

---

## Screens

### 1. Home

Active experiments + daily check-in.

- Up to 2 `ExperimentCard`s: action, "Day X of Y", streak dots, [✓ YES] [✗ SKIP]
- YES/SKIP styled identically before logging — no nudge toward YES
- [+ New Experiment] hidden (not disabled) when both slots full
- Completion banners for experiments awaiting decision
- Reflection prompt cards on configured reflection day
- Empty state: "No active experiments" + create CTA

```
┌─────────────────────────────────────┐
│  Kokoromi                           │
├─────────────────────────────────────┤
│                                     │
│  Morning Walk                       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │
│  Day 6 of 14                        │
│                                     │
│  [✓ YES]     [✗ SKIP]              │
│                                     │
│  ─────────────────────────────────  │
│                                     │
│  [+ New Experiment]                 │
│                                     │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings    │
└─────────────────────────────────────┘
```

---

### 2. Check-In

Log today's activity for an experiment.

- YES / SKIP completion buttons (pre-selected based on which was tapped from Home)
- Mood: 1–5 star rating (maps to `moodAfter`; `moodBefore` unused in v1)
- Notes: optional free text
- [LOG ACTIVITY] saves and reveals "Add a quick reflection for today" link (not shown before save)

```
┌─────────────────────────────────────┐
│  ← Morning Walk                     │
├─────────────────────────────────────┤
│                                     │
│  Did you do your walk today?       │
│                                     │
│  [✓ YES]     [✗ SKIP]              │
│                                     │
│  ───────────────────────────────── │
│                                     │
│  How did it feel?                   │
│  ☆☆☆☆☆                            │
│                                     │
│  Notes (optional)                   │
│  ┌─────────────────────────────────┐│
│  │ Felt energized today            ││
│  └─────────────────────────────────┘│
│                                     │
│            [LOG ACTIVITY]           │
│                                     │
│  + Add a quick reflection for today │  ← appears after save
└─────────────────────────────────────┘
```

---

### 3. Archive

Browse all non-active experiments.

- Tabs: Active · Completed · Paused (Archived folds into Completed or its own tab)
- Each row: title, status badge, completion rate, date range
- Tap → Experiment Detail

```
┌─────────────────────────────────────┐
│  Archive                            │
├─────────────────────────────────────┤
│                                     │
│  [Active] [Completed] [Paused]     │
│                                     │
│  Morning Walks                      │
│  Completed · 13/14 days             │
│  Mar 16 – Mar 30, 2026              │
│                                     │
│  Meditation                         │
│  Paused · Day 5 of 7               │
│                                     │
│  Cold Showers                       │
│  Archived · Mar 10 – Mar 12        │
│                                     │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings    │
└─────────────────────────────────────┘
```

---

### 4. Experiment Detail

Full record of a single experiment.

- Hypothesis + why
- Progress bar + streak visual (if still active)
- Daily logs timeline (most recent first): date, ✓/✗, mood, notes snippet
- Reflections (collapsed by default, tap to expand)
- Completion summary if ended: rate, decision, learnings
- [Pause] / [Resume] action buttons

```
┌─────────────────────────────────────┐
│  ← Morning Walks  [COMPLETED]       │
├─────────────────────────────────────┤
│                                     │
│  What I tested                      │
│  If I take a 15-min morning walk,  │
│  I'll think more clearly            │
│  Why: See if it helps with focus   │
│                                     │
│  Progress: 13 of 14 days (93%)     │
│  ✓✓✓✓✓✗✓✓✓✓✓✓✓✓                  │
│                                     │
│  Daily Logs                         │
│  ◼ March 30 · ✓ · 😊😊😊 · "Energized"  │
│  ◼ March 29 · ✓ · 😊😊😊😊 · "Great focus" │
│  ◼ March 28 · ✗ · (rain)           │
│                                     │
│  Reflections                        │
│  Week of Mar 23 ▶                  │
│                                     │
│  Completion: Persist                │
│  "Walking definitely helps focus"  │
│                                     │
└─────────────────────────────────────┘
```

---

### 5. Create Experiment

Guided form for a new experiment.

- Hypothesis input (required, 1–500 chars)
- Action input (required, 1–500 chars)
- Duration picker: [1w] [2w] [4w] [Custom] — shows calculated end date
- Pact preview updates live: `"I will [action] for [duration]."`
- Why input (optional, 500 chars)
- [START EXPERIMENT] disabled until required fields valid

```
┌─────────────────────────────────────┐
│  ← What are you curious about?     │
├─────────────────────────────────────┤
│                                     │
│  What do you want to test?         │
│  ┌─────────────────────────────────┐│
│  │ If I meditate 10 min daily,    ││
│  │ will I feel less stressed?     ││
│  │                      67 / 500  ││
│  └─────────────────────────────────┘│
│                                     │
│  What will you do?                  │
│  ┌─────────────────────────────────┐│
│  │ Sit quietly with a guided app  ││
│  │ for 10 min each morning 41/500 ││
│  └─────────────────────────────────┘│
│                                     │
│  How long?                          │
│  [1w] [2w] [4w] [Custom]           │
│  Ends: April 13, 2026              │
│                                     │
│  Your pact:                         │
│  "I will meditate for 10 min each  │
│   morning for 2 weeks."            │
│                                     │
│  Why? (optional)                    │
│  ┌─────────────────────────────────┐│
│  │ Been feeling overwhelmed lately ││
│  └─────────────────────────────────┘│
│                                     │
│        [START EXPERIMENT]           │
└─────────────────────────────────────┘
```

---

### 6. Reflection Screen

Weekly Plus-Minus-Next prompt.

- Shows which experiment + week range + completion count
- Three optional inputs: plus / minus / next (at least one required to save)
- [SKIP] dismisses without saving; [SAVE] validates and persists

```
┌─────────────────────────────────────┐
│  Time to Reflect                    │
│  Morning Walks · Week of Mar 23    │
│  Completed: 6 of 7 days            │
├─────────────────────────────────────┤
│                                     │
│  What went well?                    │
│  ┌─────────────────────────────────┐│
│  │ Felt good, much clearer thinking││
│  └─────────────────────────────────┘│
│                                     │
│  What didn't work?                  │
│  ┌─────────────────────────────────┐│
│  │ Skipped one day due to rain    ││
│  └─────────────────────────────────┘│
│                                     │
│  What's your next small tweak?      │
│  ┌─────────────────────────────────┐│
│  │ Bring umbrella on rainy days   ││
│  └─────────────────────────────────┘│
│                                     │
│  [SKIP]              [SAVE]         │
└─────────────────────────────────────┘
```

---

### 7. Completion Screen

End-of-experiment decision flow.

- Summary card: dates, completion rate, hypothesis
- Final Plus-Minus-Next reflection (optional)
- Steering Sheet: external signals + internal signals (both optional free text)
- Decision buttons with outcomes:
  - **[PERSIST]** → experiment ARCHIVED (decision=PERSIST), new identical round starts ACTIVE
  - **[PIVOT]** → experiment ARCHIVED (decision=PIVOT), navigate to Create (pre-filled or blank)
  - **[PAUSE]** → experiment ARCHIVED (decision=PAUSE), no new experiment; data in Archive

```
┌─────────────────────────────────────┐
│  Your 2-Week Experiment Ended!     │
├─────────────────────────────────────┤
│                                     │
│  Morning Walks                      │
│  Completed: 13 of 14 days (93%)    │
│  March 16 – March 30               │
│  Hypothesis: Walking helps me think │
│                                     │
│  Final Reflection                   │
│  What went well? [...]              │
│  What didn't work? [...]            │
│  What's your next small tweak? [...] │
│                                     │
│  What's telling you what to do next?│
│  External (facts, context):         │
│  ┌─────────────────────────────────┐│
│  │ Completion rate was high        ││
│  └─────────────────────────────────┘│
│  Internal (feelings, motivation):   │
│  ┌─────────────────────────────────┐│
│  │ Felt great, want to continue   ││
│  └─────────────────────────────────┘│
│                                     │
│  [PERSIST]   [PIVOT]   [PAUSE]     │
└─────────────────────────────────────┘
```

---

### 8. Settings

- **Preferences**: Theme (System/Light/Dark), Reflection day (day of week picker)
- **Data**: [Export all data as JSON], [Delete all data] (confirmation required)
- **More Tools**: Static list of other Tiny Experiments tools with links to nesslabs.com/book — no interactive features
- **About**: version, GPL-3.0, source code link, contributing link

```
┌─────────────────────────────────────┐
│  Settings                           │
├─────────────────────────────────────┤
│  Preferences                        │
│  Theme: ◉ System  ○ Light  ○ Dark  │
│  Reflection Day: ◉ Sunday  ○ Mon…  │
│  ─────────────────────────────────  │
│  Data Management                    │
│  [Export all data as JSON]          │
│  [Delete all data]                  │
│  ─────────────────────────────────  │
│  About                              │
│  Kokoromi v1.0.0                   │
│  License: GPL-3.0                   │
│  [View source code]                 │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings    │
└─────────────────────────────────────┘
```

---

### 9. Field Notes

Running log of free-form observations, independent of any experiment.

- List in reverse chronological order: timestamp + first ~150 chars of content
- FAB [+] or "New note" link to Add Note
- Empty state: "Nothing noted yet. Start by writing down something you've been noticing."

```
┌─────────────────────────────────────┐
│  Field Notes                        │
│  Observations about your life       │
├─────────────────────────────────────┤
│                                     │
│  Mar 31 · 9:14 AM                  │
│  Noticed I have way more energy on │
│  days when I skip the morning news │
│  feed. Something about starting... │
│                                     │
│  Mar 29 · 7:42 PM                  │
│  Felt drained after the team       │
│  meeting. Is it the length or...   │
│                                     │
│                                 [+] │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings    │
└─────────────────────────────────────┘
```

---

### 10. Add / Edit Note

- Timestamp row: defaults to now; tappable to open system date/time picker (lets users backfill)
- Large multi-line content input, autofocus on open (5000 char max, enforced silently)
- [SAVE NOTE] disabled when content empty
- [DELETE NOTE] shown in edit mode only, coral color, requires confirmation

```
┌─────────────────────────────────────┐
│  ← New Note                         │
├─────────────────────────────────────┤
│                                     │
│  March 31, 2026 · 9:14 AM  [edit] │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Noticed I have way more energy ││
│  │ on days when I skip the morning││
│  │ news feed...                   ││
│  │                                ││
│  └─────────────────────────────────┘│
│                                     │
│          [SAVE NOTE]                │
│          [DELETE NOTE]              │  ← edit mode only
│                                     │
└─────────────────────────────────────┘
```

---

## Text Display Conventions

Applied wherever user text is displayed (cards, previews, pact text). Not applied to input fields.

**Action text** (ExperimentCard, pact preview):
- Capitalize first character
- Strip trailing punctuation (`. , ! ? ; :`)
- Examples: `"do 10 pushups a day"` → `"Do 10 pushups a day"` · `"Go for a walk."` → `"Go for a walk"`

**Pact preview** (`"I will … for …"`):
- Action additionally lowercased at first character so it reads naturally mid-sentence
- Trailing punctuation stripped before insertion
- Result: `"I will do 10 pushups a day for 1 week."`

---

## Accessibility

- Color contrast: 4.5:1 minimum (WCAG AA)
- All interactive elements: explicit `contentDescription` (e.g. "Create experiment button", not "Button")
- Experiment cards announce: action + day progress + button states
- Streak dots: single `contentDescription` on the row ("X completed out of Y days") — individual dots are decorative
- Mood stars: each announces filled/empty state; selected star appends "tap to clear"
- Decorative emoji suppressed from screen readers
- All screens keyboard-navigable
- Animations respect `prefers-reduced-motion`
