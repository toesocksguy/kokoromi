# Design Philosophy

## North Star

Build an app that **transforms curiosity into action** without breeding anxiety, shame, or burnout. Life isn't linear; success shouldn't be measured by achievement alone. — Anne-Laure Le Cunff, *Tiny Experiments*

---

## Core Principles

### 1. Curiosity Over Achievement

Experiments are questions you're testing, not goals you're chasing. A user doesn't "succeed" or "fail" — they *learn*. "Yes, this works for me" and "No, not my thing" are equally valid outcomes.

Streaks show gaps neutrally (data, not failure). Reflections ask "What did you learn?" not "Did you achieve your goal?" End states are Persist / Pivot / Pause — not Success / Failure.

### 2. Simplicity Over Gamification

No badges, no leaderboards, no social comparison, no pressure mechanics. Streaks show gaps rather than resetting on missed days. No notifications — users open the app when *they* decide. Reflections are gentle prompts, not mandatory forms.

Gamification hijacks the brain's reward system and creates dependency. We want sustainable intrinsic motivation: *I'm curious, so I experiment.*

### 3. User Agency

We enable, never manipulate. Users make all meaningful decisions. No dark patterns, no fake scarcity, no guilt loops. Users can pause, pivot, or archive without penalty. All data visible, editable, and exportable at any time.

### 4. Privacy by Default

All user data stays on device. No cloud storage, no telemetry, no tracking.

- SQLite database stored locally (not encrypted at rest in v1 — see [PERFORMANCE_RESPONSIVENESS_SECURITY.md](PERFORMANCE_RESPONSIVENESS_SECURITY.md))
- No analytics — we never know who uses the app or what they experiment with
- No ads, no sponsored content
- JSON export available anytime
- Open source for community audit
- No network calls in v1; future cloud sync would be user opt-in only

Habit experiments are deeply personal. Users share vulnerable experiments and private struggles. Privacy is not a feature — it's a foundation.

### 5. Offline-First

All core features work without internet. No sync-in-progress messages, no cloud-dependent workflows. Future cloud backup would be user opt-in, end-to-end encrypted, user-controlled.

### 6. Accessibility First

Built in from the start, not retrofitted:
- Color contrast: WCAG 2.1 AA (4.5:1 for text)
- Touch targets: 48dp minimum
- Typography: 16sp minimum
- Screen reader: all elements have meaningful labels
- No color-only information; no gesture-only interactions
- Reduced motion: animations respect system preferences

---

## Design Anti-Patterns (What We Avoid)

**Shame-based motivation**: "You missed 3 days in a row", "Your streak will be reset!", social comparison, punishment mechanics.

**Artificial urgency**: Limited-time challenges, countdown timers, pop-ups, interruptions.

**Addictive loops**: Variable rewards, social pressure via leaderboards, notification cascades, FOMO mechanics.

**Data extraction**: Analytics, behavior tracking, third-party data sharing, ad networks, excessive permissions.

---

## Three Mental Shifts (from the Book)

Le Cunff's *Tiny Experiments* identifies three shifts needed to live experimentally. The app is designed to support all three.

### 1. Anxious/Automatic → Autonomous/Curious

Uncertainty becomes an invitation — "I wonder what would happen if...?" — rather than a trigger for avoidance.

*App supports this by*: framing experiments as questions, treating missed days as data, removing all shame mechanics.

### 2. Ladder Model → Loop Model

Replace the "ladder" (fixed rungs, clear destination, falling off = failure) with the "loop" (experiment → observe → reflect → adjust → repeat).

*App supports this by*: building the Pact → Act → React → Impact cycle into the core flow; making Persist / Pivot / Pause equally valid end-states.

### 3. Outcome Focus → Output Focus

Focus on the action you take (output), not the result you hope to get (outcome).

*App supports this by*: the pact format ("I will [action] for [duration]") commits to output; Plus-Minus-Next asks "What did you learn?"; completion rate measures whether you did the thing, not whether it worked.

---

## Tone and Voice

**In copy**: curious not prescriptive ("What are you curious about?" not "Set a goal"), permission-giving ("no pressure"), honest about gaps, respectful of user intelligence.

**In interaction**: lightweight and fast, transparent (no hidden algorithms), reversible (skip today = no penalty), forgiving (editable entries, pausable experiments).

**Avoid**: gamification language ("crush", "win"), shame language ("failed", "broken"), urgency language ("hurry", "act now"), manipulation ("you're so close!").

---

## Key Decisions

**Action-first, not reflection-first**: low-friction daily logging comes before reflection. You can't reflect on nothing. Once logging is habitual, reflection follows naturally.

**Weekly reflections, not daily or monthly**: daily is intrusive; monthly loses context. Sunday is a natural pause point. Optional — no shame for skipping.

**No notifications**: removes obligation and anxiety; users open when they want; simpler code.

**Max 2 active experiments**: encourages focus, allows diversity across domains, sustainable without burnout.

**Show gaps in streaks**: gaps reveal patterns ("I skip Mondays") and remove the shame trigger that causes users to quit.

---

## Toolkit Coverage

The app implements Tools 1–3, 7, and 8 from Le Cunff's 12-tool Experimentalist Toolkit: Pact, Field Notes, Three Mental Shifts, Plus-Minus-Next, and Steering Sheet. The remaining tools (Kairos Ritual, Triple Check, Ambition Dials, Two-Step Reset, Five Keys) are surfaced as a static **"More Tools"** section in Settings with links to further reading — no interactive features, just awareness.

---

## References

- Anne-Laure Le Cunff — *Tiny Experiments: How to Live Freely in a Goal-Obsessed World* (Penguin, 2025)
- Ness Labs — nesslabs.com
