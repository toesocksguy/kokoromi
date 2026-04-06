# UI Design: Kokoromi

## Design System

### Colors

**Primary Actions**: Teal (emerald-like)  
**Secondary Actions**: Gray  
**Status: Active**: Teal  
**Status: Paused**: Gray  
**Status: Completed**: Teal (lighter)  
**Destructive/Warnings**: Coral  
**Text Primary**: Near-black (light mode) / Near-white (dark mode)  
**Text Secondary**: Medium gray (muted)  
**Backgrounds**: White (light) / Dark gray (dark)

**Dark mode support**: All colors respect system preferences via Material 3 theming.

### Typography

- **Heading 1** (screen titles): 24sp, weight 600
- **Heading 2** (section headers): 20sp, weight 600
- **Heading 3** (card titles): 16sp, weight 600
- **Body**: 16sp, weight 400
- **Small**: 14sp, weight 400
- **Caption**: 12sp, weight 400

### Spacing

- **Small (xs)**: 4dp
- **Small (s)**: 8dp
- **Medium (m)**: 16dp
- **Large (l)**: 24dp
- **X-Large (xl)**: 32dp

### Touch Targets

Minimum 48dp (48x48 dp for buttons, cards with interactive areas).

### Icons

- Use Material Icons (androidx.compose.material.icons)
- Size: 24dp standard, 48dp large buttons
- No custom illustrations in v1 (keep it simple)

---

## Screens Overview

### Navigation Structure

```
Home Screen (entry point)
├── Active Experiments display
├── Tap on experiment → Check-In Screen
├── Bottom navigation:
│   ├── Home (active experiments)
│   ├── Notes (field notes)
│   ├── Archive (past experiments)
│   └── Settings (user preferences)
│
Check-In Screen
├── Completion button
├── Optional: Mood rating
├── Optional: Notes input
├── After saving: link to Add Daily Reflection
├── Back to Home
│
Field Notes Screen
├── List of notes (date/time stamped, abbreviated)
├── Tap note → View/Edit Note Screen
├── [+ Add Note] button → Add Note Screen
│
Add / Edit Note Screen
├── Editable date/time
├── Full text input
├── Save button
├── Delete button (edit mode only)
│
Archive Screen
├── List of all experiments (completed, paused, archived)
├── Tap on experiment → Experiment Detail Screen
│
Experiment Detail Screen
├── Full experiment info
├── All daily logs (timeline)
├── Reflections (chronological)
├── (If completed) Completion summary
│
Create Experiment Screen
├── Hypothesis input
├── Action input
├── Duration selector
├── Pact preview ("I will [action] for [duration]")
├── Why input (optional)
├── Create button
│
Reflection Screen (weekly prompt)
├── Plus-Minus-Next form
├── Skip/Submit
│
Completion Screen (end of experiment)
├── Summary (completion rate, dates)
├── Hypothesis vs. reality
├── Final Plus-Minus-Next
├── Steering Sheet (external + internal signals)
├── Decision buttons (Persist, Pivot, Pause)
│
Settings Screen
├── User preferences (reflection day/time, theme)
├── Export data
├── About / License
```

---

## Screen Details

### 1. Home Screen

**Purpose**: Primary user interaction point. Shows active experiments and daily check-in buttons.

**Components**:
- **Header**: "Kokoromi" (with optional day count)
- **Experiment Cards** (up to 2):
  - Experiment name (title)
  - Progress: "Day X of Y"
  - Streak display with gaps (visual)
  - [✓ YES] [✗ SKIP] buttons
    - **Before logging**: both buttons should appear visually identical (same style, no implied default) — there's no "right answer" yet and we don't want to nudge the user toward YES
    - **After logging**: reflect the logged state (e.g. YES highlighted if completed, SKIP highlighted if skipped) — implement as part of `StreakDisplay` when today's log status is available on the card
  - Tap card to see details
- **Create Experiment Button**:
  - Shown below experiment cards when fewer than 2 experiments are active
  - Hidden (not disabled) when both slots are full — no button, no explanation until the user tries to create
  - Tapping it navigates to Create Experiment Screen
- **Empty State** (if no active experiments):
  - "No active experiments"
  - "Create one to get started" button (prominent)
  - Link to archive
- **Bottom Navigation**:
  - Home (active)
  - Archive
  - Settings

**Wireframe** (1 active experiment, slot available):
```
┌─────────────────────────────────────┐
│  Kokoromi                           │
├─────────────────────────────────────┤
│                                      │
│  Morning Walk                        │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │ (gap streak)
│  Day 6 of 14                         │
│                                      │
│  [✓ YES]     [✗ SKIP]               │
│                                      │
│  ──────────────────────────────────  │
│                                      │
│  [+ New Experiment]                  │ (hidden when 2 active)
│                                      │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │ (bottom nav)
└─────────────────────────────────────┘
```

**Wireframe** (2 active experiments, both slots full):
```
┌─────────────────────────────────────┐
│  Kokoromi                           │
├─────────────────────────────────────┤
│                                      │
│  Morning Walk                        │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│  Day 6 of 14                         │
│                                      │
│  [✓ YES]     [✗ SKIP]               │
│                                      │
│  ──────────────────────────────────  │
│                                      │
│  Meditation                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│  Day 3 of 7                          │
│                                      │
│  [✓ YES]     [✗ SKIP]               │
│                                      │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │
└─────────────────────────────────────┘
```

**State Variations**:
- **Loading**: Shimmer/skeleton for cards while data loads
- **Empty**: Show "Create Experiment" button prominently
- **Completed Today**: Checkmark icon next to completion button
- **Experiment Completed (awaiting decision)**: Banner at top: "Morning Walk ended today!" with link to completion screen

**Interactions**:
- Tap [✓ YES] → Opens Check-In Screen with completion pre-selected
- Tap [✗ SKIP] → Opens Check-In Screen with SKIP pre-selected (allows adding a note before saving)
- Tap card → Opens Experiment Detail Screen
- Long-press card → Context menu (pause, view details, archive)
- Tap Archive tab → Goes to Archive Screen
- Tap Settings tab → Goes to Settings Screen

---

### 2. Check-In Screen

**Purpose**: Detailed check-in for an experiment, allowing optional mood and notes.

**Flow**:
- User taps [✓ YES] on home screen
- Check-In Screen opens with experiment info
- User confirms completion or adds notes/mood
- Returns to Home Screen

**Components**:
- **Header**: Experiment name + "Did you do it today?"
- **Completion Buttons**: [✓ YES] [✗ SKIP]
  - YES: Pre-filled, tappable to submit
  - SKIP: Pre-filled, tappable to skip
- **Optional Sections** (expandable or always visible):
  - **Mood**: "How did it feel?" with 1-5 star/emoji rating (tap to select) — maps to `moodAfter` in the data model. `moodBefore` is left null; a pre-activity rating would require a second prompt and adds friction without clear payoff in v1.
  - **Notes**: Free text input, placeholder "Anything interesting?"
- **Save Button**: "Log Activity"
- **Optional Daily Reflection** (appears after saving, not before):
  - Subtle link: "Add a quick reflection for today"
  - Opens a lightweight Plus-Minus-Next form (same fields, same optional character)
  - Not shown as a prompt — only visible as a link the user chooses to tap
- **Back/Close**: Arrow back to Home

**Wireframe**:
```
┌─────────────────────────────────────┐
│  ← Morning Walk                      │
├─────────────────────────────────────┤
│                                      │
│  Did you do your walk today?        │
│                                      │
│  [✓ YES]     [✗ SKIP]               │
│                                      │
│  ───────────────────────────────────  │ (divider)
│                                      │
│  How did it feel?                    │
│  ☆☆☆☆☆  (tap to rate)              │
│                                      │
│  Notes (optional)                    │
│  ┌──────────────────────────────────┐│
│  │ Felt energized today             ││
│  └──────────────────────────────────┘│
│                                      │
│             [LOG ACTIVITY]           │
│                                      │
└─────────────────────────────────────┘
```

**State Variations**:
- **Just tapped YES**: YES button highlighted/selected by default
- **Just tapped SKIP**: SKIP button highlighted by default
- **Mood selected**: Show current rating as filled stars
- **Notes entered**: Show preview in condensed form

**Interactions**:
- Tap [✓ YES] → YES highlighted
- Tap [✗ SKIP] → SKIP highlighted
- Tap star → Set mood (1-5)
- Tap in notes → Keyboard opens, focus
- Tap [LOG ACTIVITY] → Validate (must have YES or SKIP), save to database, reveal "Add a quick reflection for today" link
- Tap "Add a quick reflection" → Opens daily reflection form (Plus-Minus-Next, same UI as weekly but lighter header)
- Back arrow → Return to Home (discard unsaved changes)

---

### 3. Archive Screen

**Purpose**: Browse all past experiments (completed, paused, archived) with ability to view details or re-run.

**Components**:
- **Header**: "Archive"
- **Tabs or Sections**:
  - "Active" (currently running, not yet ended)
  - "Completed" (awaiting decision)
  - "Archived" (finished/learned enough)
  - "Paused" (paused temporarily)
- **Experiment List**:
  - Experiment title
  - Status badge (COMPLETED, ARCHIVED, PAUSED)
  - Completion rate (if completed)
  - Date range (Mar 16 - Mar 30)
  - Tap to view details
- **Search** (future): Filter by name or status

**Wireframe**:
```
┌─────────────────────────────────────┐
│  Archive                             │
├─────────────────────────────────────┤
│                                      │
│  [Active] [Completed] [Paused]      │ (tabs)
│                                      │
│  Morning Walks                       │
│  Completed · 13/14 days              │
│  Mar 16 – Mar 30, 2026               │
│  [Tap to view]                       │
│                                      │
│  Meditation                          │
│  Paused · Day 5 of 7                 │
│  Mar 20 – ongoing                    │
│                                      │
│  Cold Showers                        │
│  Archived                            │
│  Mar 10 – Mar 12, 2026               │
│                                      │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │
└─────────────────────────────────────┘
```

**Interactions**:
- Tap tab → Filter by status
- Tap experiment → Opens Experiment Detail Screen
- Long-press experiment → Context menu (re-run, export, delete future)

---

### 4. Experiment Detail Screen

**Purpose**: View all data for a single experiment—logs, reflections, completion info.

**Components**:
- **Header**: Experiment name + status badge
- **Hypothesis Section**:
  - Title: "What I tested"
  - Hypothesis text
  - Why (if provided)
- **Progress Section** (if still active):
  - Days completed / total
  - Streak visual (with gaps)
  - Completion percentage
- **Daily Logs Timeline** (scrollable list, most recent first):
  - Date header (e.g., "March 30")
  - ✓ or ✗ icon
  - Mood rating (if logged)
  - Notes snippet
  - Tap to edit
- **Reflections Section** (if any):
  - "Week of March 23" header
  - Plus, Minus, Next (collapsed view, tap to expand)
- **Completion Info** (if experiment ended):
  - Final completion rate
  - Decision (Persist, Pivot, Pause)
  - Learnings (if provided)
- **Actions**:
  - [Pause] or [Resume] (if pausable)
  - [Edit] (future, to modify hypothesis/action)
  - [Delete] (future, with confirmation)

**Wireframe**:
```
┌─────────────────────────────────────┐
│  ← Morning Walks  [COMPLETED]        │
├─────────────────────────────────────┤
│                                      │
│  What I tested                       │
│  If I take a 15-min morning walk,   │
│  I'll think more clearly             │
│                                      │
│  Why: See if it helps with focus    │
│                                      │
│  ───────────────────────────────────  │
│                                      │
│  Progress: 13 of 14 days (93%)      │
│  ✓✓✓✓✓✗✓✓✓✓✓✓✓✓  (streak)         │
│                                      │
│  ───────────────────────────────────  │
│                                      │
│  Daily Logs                          │
│                                      │
│  ◼ March 30                          │
│  ✓ Completed  😊😊😊  "Energized"    │
│                                      │
│  ◼ March 29                          │
│  ✓ Completed  😊😊😊😊  "Great focus" │
│                                      │
│  ◼ March 28                          │
│  ✗ Skipped (rain)                   │
│                                      │
│  ───────────────────────────────────  │
│                                      │
│  Reflections                         │
│  Week of Mar 23                      │
│  ▼ Plus: Felt good                   │
│    Minus: One day skipped            │
│    Next: Bring umbrella              │
│                                      │
│  ───────────────────────────────────  │
│                                      │
│  Completion                          │
│  Kept: Will continue with walks     │
│                                      │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │
└─────────────────────────────────────┘
```

**Interactions**:
- Scroll: View all logs and reflections
- Tap on daily log: Edit mood/notes (future)
- Tap on reflection: Expand to full text
- Tap [Pause]: Pauses experiment, removes from active
- Tap [Resume]: Re-activates a paused experiment

---

### 5. Create Experiment Screen

**Purpose**: Setup a new experiment with guided inputs.

**Components**:
- **Header**: "What are you curious about?"
- **Step 1: Hypothesis Input**
  - Label: "What do you want to test?"
  - Input field (multi-line, 500 char limit)
  - Hint: "Example: Walking helps me think clearly"
  - Character count: "120 / 500"
- **Step 2: Action Input**
  - Label: "What will you do?"
  - Input field (multi-line, 500 char limit)
  - Hint: "Example: Take a 15-minute walk every morning"
  - Character count: "45 / 500"
- **Step 3: Duration**
  - Label: "How long will you test this?"
  - Duration picker (preset + custom):
    - [1 week] [2 weeks] [1 month] [Custom]
  - Shows end date dynamically
  - Min: 1 day, Max: 180 days
- **Pact Preview** (appears once action + duration are filled):
  - Displayed as: `"I will [action] for [duration]"`
  - Updates live as user types
  - Label: "Your pact:" (subtle, secondary text style)
  - Purpose: teaches the pact format through use, makes commitment feel real
- **Step 4: Why (Optional)**
  - Label: "Why are you curious about this?"
  - Input field (optional, 500 char max)
  - Hint: "What motivated this experiment?"
- **Create Button**: "Start Experiment"
- **Cancel**: Back to Home

**Wireframe**:
```
┌─────────────────────────────────────┐
│  ← What are you curious about?      │
├─────────────────────────────────────┤
│                                      │
│  What do you want to test?          │
│  ┌──────────────────────────────────┐│
│  │ If I meditate 10 min daily, will ││
│  │ I feel less stressed?            ││
│  │                     67 / 500      ││
│  └──────────────────────────────────┘│
│                                      │
│  What will you do?                   │
│  ┌──────────────────────────────────┐│
│  │ Sit quietly and follow a guided  ││
│  │ meditation app for 10 min each   ││
│  │ morning                  41 / 500 ││
│  └──────────────────────────────────┘│
│                                      │
│  How long?                           │
│  [1w] [2w] [4w] [Custom]            │
│  Ends: April 13, 2026               │
│                                      │
│  Your pact:                          │
│  "I will meditate for 10 min each   │
│   morning for 2 weeks."             │
│                                      │
│  Why? (optional)                     │
│  ┌──────────────────────────────────┐│
│  │ Been feeling overwhelmed lately  ││
│  │                      23 / 500     ││
│  └──────────────────────────────────┘│
│                                      │
│         [START EXPERIMENT]           │
│                                      │
└─────────────────────────────────────┘
```

**State Variations**:
- **Invalid input**: Error message (red text) below field, CREATE button disabled
- **Valid**: CREATE button enabled
- **Custom duration**: Calendar or number picker shows

**Interactions**:
- Type in hypothesis → Validate (required, 1-500 chars)
- Type in action → Validate (required, 1-500 chars)
- Select duration → Update end date
- Type in why → Optional, validates if provided
- Tap [START EXPERIMENT] → Validate all required fields, create experiment, navigate to Home, show confirmation toast

---

### 6. Reflection Screen (Weekly Prompt)

**Purpose**: Prompt user for weekly reflection using Plus-Minus-Next framework.

**Flow**:
- Appears as a bottom sheet or full-screen modal Sunday evening (or manually triggered)
- User fills in reflection (or skips)
- Closes and returns to Home

**Components**:
- **Header**: "Time to Reflect"
- **Experiment Name**: Shows which experiment this is for
- **Week Info**: "Week of March 23 - 29"
- **Completion Stats**: "You completed 6 of 7 days"
- **Plus Input**:
  - Label: "What went well?"
  - Input field (multi-line, optional)
  - Placeholder: "What went well? What did you enjoy?"
- **Minus Input**:
  - Label: "What didn't work?"
  - Input field (multi-line, optional)
  - Placeholder: "What didn't work? What felt off?"
- **Next Input**:
  - Label: "What's your next small tweak?"
  - Input field (multi-line, optional)
  - Placeholder: "What small tweak can you make based on what you've learned?"
- **Buttons**:
  - [SKIP] (dismiss for now, can reflect later)
  - [SAVE] (save reflection, dismiss)

**Wireframe**:
```
┌─────────────────────────────────────┐
│  Time to Reflect                     │
│  Morning Walks (Week of Mar 23)     │
│  Completed: 6 of 7 days             │
├─────────────────────────────────────┤
│                                      │
│  What went well?                     │
│  ┌──────────────────────────────────┐│
│  │ Felt good on all days, much      ││
│  │ clearer thinking                 ││
│  └──────────────────────────────────┘│
│                                      │
│  What didn't work?                   │
│  ┌──────────────────────────────────┐│
│  │ Skipped one day due to rain     ││
│  └──────────────────────────────────┘│
│                                      │
│  What's your next small tweak?       │
│  ┌──────────────────────────────────┐│
│  │ Bring umbrella on rainy days    ││
│  └──────────────────────────────────┘│
│                                      │
│  [SKIP]          [SAVE]              │
│                                      │
└─────────────────────────────────────┘
```

**Interactions**:
- Tap input → Keyboard opens, focus
- Tap [SKIP] → Dismiss modal, don't save (will prompt again later)
- Tap [SAVE] → Validate (at least one field should have text), save reflection, dismiss with confirmation

---

### 7. Completion Screen (End of Experiment)

**Purpose**: Guide user through decision-making when an experiment reaches its end date.

**Flow**:
- Triggered automatically when experiment duration ends
- Shown as primary screen or full-screen modal
- User reviews summary and makes decision
- Transitions to next action (keep → home, archive → archive, pivot → create new)

**Components**:
- **Header**: "Your experiment ended!"
- **Experiment Name**: "Morning Walks"
- **Summary Card**:
  - Duration: "March 16 – March 30 (2 weeks)"
  - Completion: "13 of 14 days (93%)"
  - Hypothesis: "Walking helps me think"
  - Observation: "✓ Yes, especially when stuck"
- **Final Reflection** (Plus-Minus-Next form):
  - Same as Reflection Screen
- **Steering Sheet** (guides the decision):
  - Label: "What's telling you what to do next?"
  - External signals (facts, context, practical limitations): brief free-text prompt
  - Internal signals (emotions, motivations, mental states): brief free-text prompt
  - Both optional — prompts help user think, but aren't required to proceed
- **What's Next?** (Decision buttons — from the Steering Sheet):
  - [PERSIST] → Experiment is working, keep going as is (new round)
  - [PIVOT] → Something feels off; adjust the approach or try a new pact
  - [PAUSE] → Set it aside for now; data saved for future reference

**Wireframe**:
```
┌─────────────────────────────────────┐
│  Your 2-Week Experiment Ended!      │
├─────────────────────────────────────┤
│                                      │
│  Morning Walks                       │
│  Completed: 13 of 14 days (93%)     │
│  March 16 – March 30                │
│                                      │
│  Hypothesis:                         │
│  Walking helps me think              │
│                                      │
│  What happened:                      │
│  ✓ Yes, especially when stuck       │
│                                      │
│  ───────────────────────────────────  │
│                                      │
│  Final Reflection                    │
│                                      │
│  What went well?                     │
│  ┌──────────────────────────────────┐│
│  │ [...]                            ││
│  └──────────────────────────────────┘│
│                                      │
│  What's telling you what to do next? │
│  External (facts, context...):       │
│  ┌──────────────────────────────────┐│
│  │ Completion rate was high         ││
│  └──────────────────────────────────┘│
│  Internal (feelings, motivation...): │
│  ┌──────────────────────────────────┐│
│  │ Felt great, want to continue     ││
│  └──────────────────────────────────┘│
│                                      │
│  [PERSIST]  [PIVOT]  [PAUSE]        │
│                                      │
└─────────────────────────────────────┘
```

**Decision Outcomes**:
- **[PERSIST]** → Old experiment ARCHIVED (decision=PERSIST), new identical experiment created ACTIVE
- **[PIVOT]** → Old experiment ARCHIVED (decision=PIVOT), user chooses: adjust this pact (pre-filled Create screen) or try a new pact (blank Create screen)
- **[PAUSE]** → Old experiment ARCHIVED (decision=PAUSE), no new experiment; data saved and visible in Archive

---

### 8. Settings Screen

**Purpose**: User preferences, data management, about information.

**Components**:
- **Preferences Section**:
  - Theme: Light / Dark / System (radio buttons)
  - Reflection day: Day of week picker (default: Sunday)
  - Reflection time: Time picker (default: 7 PM, future)
- **Data Section**:
  - [Export all data as JSON] → Exports JSON file
  - [Delete all experiments] → Confirmation dialog
- **More Tools Section**:
  - Brief intro: "Kokoromi covers the core Tiny Experiments tools. Here are more from the full toolkit:"
  - Each tool listed with a one-line description and a link to further reading (nesslabs.com/book):
    - **Kairos Ritual** — Find your peak focus windows
    - **Triple Check** — Understand what's blocking you
    - **Ambition Dials** — Choose where to aim for excellence
    - **Two-Step Reset** — Navigate disruptions with resilience
    - **Five Keys** — Build a generative career
  - Purely informational — no interactive features
- **About Section**:
  - App version
  - License: GPL-3.0
  - Link to source code (GitHub)
  - Link to CONTRIBUTING.md

**Wireframe**:
```
┌─────────────────────────────────────┐
│  Settings                            │
├─────────────────────────────────────┤
│                                      │
│  Preferences                         │
│  ───────────────────────────────────  │
│                                      │
│  Theme                               │
│  ◉ System  ○ Light  ○ Dark          │
│                                      │
│  Reflection Day                      │
│  ◉ Sunday  ○ Monday  ○ Tuesday ...  │
│                                      │
│  ───────────────────────────────────  │
│  Data Management                     │
│  ───────────────────────────────────  │
│                                      │
│  [Export all data as JSON]           │
│  [Delete all data] (warning)         │
│                                      │
│  ───────────────────────────────────  │
│  About                               │
│  ───────────────────────────────────  │
│                                      │
│  Kokoromi v1.0.0             │
│  Privacy: No tracking, no ads        │
│  License: GPL-3.0                    │
│  [View source code]                  │
│  [Contributing]                      │
│                                      │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │
└─────────────────────────────────────┘
```

---

### 9. Field Notes Screen

**Purpose**: A running log of free-form observations — timestamped notes about anything the user notices in their life. The raw material for forming hypotheses. Independent of any active experiment.

**Components**:
- **Header**: "Field Notes"
- **Subtitle**: "Observations about your life" (secondary text, small)
- **Notes List** (reverse chronological):
  - Timestamp: "Mar 31, 2026 · 9:14 AM"
  - Note preview: first ~150 characters, truncated with "..." if longer
  - Tap to open full note
- **Add Button**: Floating action button (+) or prominent "New note" link at top
- **Empty State**: "Nothing noted yet. Start by writing down something you've been noticing."

**Wireframe**:
```
┌─────────────────────────────────────┐
│  Field Notes                         │
│  Observations about your life        │
├─────────────────────────────────────┤
│                                      │
│  Mar 31 · 9:14 AM                   │
│  Noticed I have way more energy on  │
│  days when I skip the morning news  │
│  feed. Something about starting...  │
│                                      │
│  Mar 29 · 7:42 PM                   │
│  Felt completely drained after the  │
│  team meeting. Same meeting every   │
│  week — is it the length or the...  │
│                                      │
│  Mar 27 · 10:05 AM                  │
│  Really focused session this        │
│  morning. No coffee, just water.    │
│  Surprising.                         │
│                                      │
│                                  [+] │
├─────────────────────────────────────┤
│  Home  Notes  Archive  Settings     │
└─────────────────────────────────────┘
```

**Interactions**:
- Tap note → Opens View/Edit Note Screen
- Tap [+] → Opens Add Note Screen
- Long-press note → Quick delete with confirmation (future)

---

### 10. Add / Edit Note Screen

**Purpose**: Write a new field note or edit an existing one. Timestamp is editable so users can accurately log observations from earlier in the day.

**Components**:
- **Header**: "New Note" (add) or "Edit Note" (edit)
- **Timestamp Row**:
  - Shows current date/time by default for new notes; existing timestamp for edits
  - Tap to open date/time picker
  - Format: "March 31, 2026 · 9:14 AM"
- **Content Input**:
  - Large, multi-line text input filling available space
  - No character limit shown (5000 char max enforced silently)
  - Placeholder: "What have you been noticing?"
  - Autofocus on open
- **Save Button**: "Save Note" (disabled if content is empty)
- **Delete Button**: "Delete Note" (edit mode only, destructive — confirmation dialog)

**Wireframe**:
```
┌─────────────────────────────────────┐
│  ← New Note                          │
├─────────────────────────────────────┤
│                                      │
│  March 31, 2026 · 9:14 AM  [edit]  │
│                                      │
│  ┌──────────────────────────────────┐│
│  │ Noticed I have way more energy  ││
│  │ on days when I skip the morning ││
│  │ news feed. Something about      ││
│  │ starting the day with calm      ││
│  │ instead of noise...             ││
│  │                                  ││
│  │                                  ││
│  └──────────────────────────────────┘│
│                                      │
│           [SAVE NOTE]                │
│                                      │
│           [DELETE NOTE]              │  ← edit mode only, coral/warning color
│                                      │
└─────────────────────────────────────┘
```

**State Variations**:
- **New note**: Timestamp = now, content empty, Save disabled, no Delete button
- **Editing**: Timestamp = original, content pre-filled, Delete button shown
- **Timestamp picker open**: Date/time picker overlay
- **Delete tapped**: Confirmation dialog: "Delete this note? This can't be undone." [Delete] [Cancel]

**Interactions**:
- Tap timestamp → Date/time picker (system picker)
- Type content → Save button enables
- Tap [SAVE NOTE] → Validate content not empty, save, return to Field Notes list
- Tap [DELETE NOTE] → Confirmation dialog → delete → return to Field Notes list
- Back arrow with unsaved changes → Discard confirmation dialog

**Future Enhancement (v2)**: Surface patterns across field notes — e.g., highlight recurring words or themes, suggest hypotheses based on frequently observed topics.

---

## Interaction Patterns

### Confirmation Dialogs

For destructive actions (delete experiment, clear data):
```kotlin
AlertDialog(
    title = { Text("Delete experiment?") },
    text = { Text("This will remove all logs. You can't undo this.") },
    confirmButton = { Button(onClick = { delete() }) { Text("Delete") } },
    dismissButton = { Button(onClick = { dismiss() }) { Text("Cancel") } }
)
```

### Toast Notifications

Brief feedback after actions:
- "Experiment created!" (success)
- "Check-in logged" (success)
- "Reflection saved" (success)
- "Please fill in hypothesis" (error)

### Bottom Sheets

For secondary flows:
- Reflection prompts (modal)
- Experiment decision menu (optional)

### Loading States

Shimmer/skeleton cards while data loads:
```
┌─────────────────────────────────────┐
│  ▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌▌  (shimmer)   │
│                                      │
│  ▌▌▌▌▌▌▌▌▌▌▌▌  (shimmer)            │
└─────────────────────────────────────┘
```

### Empty States

When no data:
- Clear message ("No active experiments yet")
- Single call-to-action (e.g., "Create one to start")
- Optional illustration (simple, not busy)

---

## Accessibility

- **Color contrast**: 4.5:1 for text on backgrounds (WCAG AA)
- **Font size**: Minimum 16sp (readable without zoom)
- **Touch targets**: 48dp minimum for buttons
- **Screen reader**: All UI elements have meaningful labels
  - "Create Experiment button" not just "Button"
  - Experiment cards read: "Morning Walk, Day 6 of 14, Completed button, Skip button"
- **Keyboard navigation**: All screens are fully keyboard-navigable (no mouse-only interactions)
- **Reduced motion**: Animations respect system `prefers-reduced-motion` setting

---

## Text Display Conventions

These rules apply wherever user-entered text is displayed in the UI (cards, previews, labels). They do **not** apply to input fields, which show the raw text as typed.

### Action text (ExperimentCard, pact preview)
- **Capitalize the first character** — regardless of how the user typed it
- **Strip trailing punctuation** — remove `.`, `,`, `!`, `?`, `;`, `:` from the end

This ensures consistent, clean display:
- "do 10 pushups a day" → "Do 10 pushups a day"
- "Go for a walk." → "Go for a walk"
- "meditate for 10 minutes," → "Meditate for 10 minutes"

### Pact preview ("I will … for …")
- The action is additionally **lowercased at the first character** so it reads naturally mid-sentence
- Trailing punctuation is stripped before insertion (same rule as above)
- Result: `"I will do 10 pushups a day for 1 week."`

Apply these rules consistently in any new component that displays action or pact text.

---

## Dark Mode

All screens support system dark mode preference. Colors automatically adjust:
- Text: Light gray on dark background
- Backgrounds: Dark gray instead of white
- Cards: Slightly lighter gray (elevated feel)
- Buttons: Teal remains teal (Material 3 handles contrast)

No manual toggle needed; system preference is respected.

---

## Future Enhancements (Out of v1 Scope)

- Custom frequency selection (Mon/Wed/Fri, etc.)
- Experiment templates
- Analytics dashboard (with user privacy)
- Social features (optional sharing)
- Advanced editing (modify hypothesis, action mid-experiment)
- Undo/restore deleted experiments
- Multiple experiment collections

---

**Version**: 1.0  
**Last Updated**: March 2026
