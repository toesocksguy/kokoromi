# Design Philosophy: Kokoromi

## Our North Star

To build an app that **transforms curiosity into action** without breeding anxiety, shame, or burnout. We embrace Anne-Laure Le Cunff's insight: *Life isn't linear, and success shouldn't be measured by achievement alone.*

---

## Core Principles

### 1. Curiosity Over Achievement

**Principle**: Experiments are questions you're testing, not goals you're chasing.

**What this means**:
- A user doesn't "succeed" or "fail" at an experiment—they *learn* from it
- The outcome is always valuable: "Yes, this works for me" or "No, not my thing" or "Interesting, it depends..."
- We never use achievement language ("You crushed it!") or shame language ("You missed 3 days")

**In the UI**:
- Completion streaks show *what happened* (gaps displayed neutrally)
- Reflections ask "What did you learn?" not "Did you achieve your goal?"
- End-of-experiment decisions include "Archive" (learned enough) and "Pivot" (try something else)—not just "Success" or "Failure"

**Why this matters**: Traditional productivity apps are built on the assumption that shame drives action. It doesn't—it drives anxiety. When users see a broken streak, they feel guilty and quit. When they see gaps as data, they're curious about what happened.

---

### 2. Simplicity Over Gamification

**Principle**: No badges, no leaderboards, no social comparison, no pressure mechanics.

**What this means**:
- Streaks show gaps (honest data), not resets (shame)
- No notifications. Users open the app when *they* decide
- No "achievements unlocked" or daily challenges
- No time limits or artificial urgency
- Reflections are gentle prompts, not mandatory forms

**In the UI**:
- Home screen shows 2 active experiments with one big button: "Did you do it?"
- Everything else is optional (mood, notes, reflection)
- Archive is celebration ("You learned what you needed"), not relegation

**Why this matters**: Gamification works in the short term because it hijacks your brain's reward system. But it creates dependency—without the badge, the app feels pointless. We want sustainable intrinsic motivation: *I'm curious, so I experiment.*

---

### 3. User Agency

**Principle**: We enable, never manipulate. Users make all meaningful decisions.

**What this means**:
- No dark patterns (fake scarcity, manipulation via notifications)
- Users choose when to open the app—never forced
- Users own their data completely—can export anytime
- Users can pause, pivot, or archive any experiment without guilt
- Users can skip weekly reflections without penalty
- UI is transparent—users know what data is being collected (nothing secret)

**In the UI**:
- All data visible and editable (users can correct entries)
- Clear decision points ("What's next?" after an experiment)
- Multiple paths forward (keep going, adjust, archive, pivot, pause)
- Settings prioritize user control (toggle optional features, view data)

**Why this matters**: People often abandon apps because they feel controlled or guilty. Giving real agency—real *permission* to take a different path—builds trust and sustainability.

---

### 4. Privacy by Default

**Principle**: All user data stays on the device. Zero cloud storage, zero telemetry, zero tracking.

**What this means**:
- SQLite database on user's phone (encrypted at rest with Android KeyStore)
- No analytics (we never know who uses the app or what they experiment with)
- No crash reporting (unless user explicitly enables it—future)
- No ads, no sponsored content
- Data export available anytime as JSON (user can back up or migrate)
- Open source code so users can audit it themselves

**Implementation**:
- All data queries happen locally
- No network calls except for optional future cloud sync (user opt-in)
- No third-party SDKs for analytics or ads
- App requires minimal permissions: INTERNET (for future sync, disabled by default), nothing else

**Why this matters**: Habit experiments are deeply personal. Users share vulnerable experiments ("Am I good at meditation?"), private struggles, and introspection. We don't earn that trust by selling data or using it for ads. Privacy is not a feature—it's a foundation.

---

### 5. Offline-First

**Principle**: The app works fully without internet. Cloud features are future, optional, and always under user control.

**What this means**:
- All core features work offline (create experiments, log actions, reflect, archive)
- No "sync in progress" messages or cloud-dependent workflows
- Future: optional cloud backup (user chooses provider, we never hold the key)
- Future: optional device-to-device sync (end-to-end encrypted)
- No background services requiring internet

**Implementation**:
- Room + SQLite for local persistence
- Optional SharedPreferences for user settings (local only, v1)
- Future: optional Firebase Realtime DB or custom server (behind feature flag, user opt-in)

**Why this matters**: Connectivity is unreliable (spotty signal, no service, offline trips). More importantly, it keeps the app simple and puts users in control. They can use the app anywhere, anytime, and know exactly where their data is.

---

### 6. Accessibility First

**Principle**: Built for everyone, not retrofitted for accessibility.

**What this means**:
- Color contrast: WCAG 2.1 AA (4.5:1 for text)
- Touch targets: 48dp minimum (easy to tap)
- Typography: 16sp minimum (readable without squinting)
- Screen reader support: all UI elements have meaningful labels
- No color-only information (icons + text, not just color)
- Keyboard navigation throughout (no mouse-only interactions)
- Reduced motion support (animations respect system preferences)

**Implementation**:
- Compose semantics for screen reader labels
- No custom gesture-only interactions
- Clear visual hierarchy (size, weight, color) not just proximity
- Testing with accessibility tools in development

**Why this matters**: Accessibility isn't a feature—it's inclusivity. We want everyone to be curious and experiment, regardless of ability. Plus, accessible design is just good design (larger text benefits everyone, clear labels help everyone).

---

## Design Anti-Patterns (What We Avoid)

### ❌ Shame-Based Motivation

We don't use:
- Guilt ("You missed 3 days in a row")
- Fear of loss ("Your streak will be reset!")
- Social comparison ("Others completed 30 days")
- Punishment ("Come back tomorrow to recover")

### ❌ Artificial Scarcity or Urgency

We don't use:
- Limited-time challenges
- Countdown timers
- "Get this badge today or miss out"
- Pop-ups or interruptions

### ❌ Addictive Loops

We don't use:
- Variable rewards (unpredictable streaks to keep users hooked)
- Social pressure (leaderboards, sharing for status)
- Notification cascades (pinging multiple times per day)
- FOMO mechanics (fear of missing out on daily bonuses)

### ❌ Data Extraction

We don't:
- Sell user data
- Use invasive analytics
- Track behavior across apps
- Require excessive permissions
- Share data with third parties (including ads networks)

---

## Three Mental Shifts (from the Book)

Le Cunff identifies three shifts people need to make to live experimentally. Our app is designed to support all three. These are named in the book as the foundation of **Nonlinear Goal-Setting** (Chapter 1).

### 1. Anxious/Automatic → Autonomous/Curious

**The shift**: Moving from an anxious, automatic response to uncertainty toward an autonomous one of curiosity and exploration.

**Before**: Uncertainty triggers avoidance, procrastination, or rigid planning to eliminate it.
**After**: Uncertainty becomes an invitation — "I wonder what would happen if...?"

**Our app supports this by**:
- Framing every experiment as a question, not a goal ("What do you want to test?")
- Treating missed days as data, not failure
- Removing all shame mechanics (no streak resets, no guilt language)

### 2. Ladder Model → Loop Model

**The shift**: Replacing the "ladder" mental model of predetermined, linear progress with a "loop" model of iterative learning.

**Before**: Progress is a ladder — fixed rungs, clear destination, falling off = failure.
**After**: Progress is a loop — experiment, observe, reflect, adjust, repeat.

**Our app supports this by**:
- Building the Pact → Act → React → Impact cycle into the core flow
- Making Persist/Pivot/Pause equally valid end-states (no "correct" outcome)
- Showing the full history of loops in the Archive (each experiment is one iteration)

### 3. Outcome Focus → Output Focus

**The shift**: Focusing on your output (the action you take, the process you follow) rather than the outcome (the result you hope to get).

**Before**: "If I meditate for a year, I'll become a calm person." (arrival fallacy)
**After**: "I'll meditate for 10 minutes each morning for 2 weeks and see what I notice."

**Our app supports this by**:
- The pact format ("I will [action] for [duration]") commits to output, not outcome
- Plus-Minus-Next asks "What did you learn?" not "Did you achieve your goal?"
- Completion rate measures whether you did the thing — not whether it worked

---

## Tone and Voice

### In Copy

- **Curious, not prescriptive**: "What are you curious about?" not "Set a goal"
- **Permission-giving**: "No pressure to complete" not "Don't break your streak"
- **Honest**: "Gaps in your streak show when you didn't do it" not "Your streak is paused"
- **Respectful**: Assume users are smart and know what they need

### In Interaction

- **Lightweight**: Fast, frictionless, minimal taps
- **Transparent**: Show what's happening (no hidden calculations or algorithms)
- **Reversible**: Tapping "Skip" today doesn't lock the experiment or reset anything
- **Forgiving**: Typo in a note? Edit it. Changed your mind? Pause the experiment.

### Avoid

- Gamification language ("crush", "win", "dominate")
- Shame language ("failure", "broken", "missed")
- Urgency language ("hurry", "limited time", "act now")
- Manipulation ("you're so close!", "don't let them down!")

---

## Decisions We've Made

### Why Action-First (Not Reflection-First)

**Considered**: Guiding users through the hypothesis-building process first, then tracking  
**Chose**: Daily logging first, reflection second

**Rationale**:
- Low-friction wins: Tapping "yes" takes 2 seconds and gives immediate feedback
- Data comes first: You can't reflect on nothing. Gather data first, analyze later
- Less cognitive load: One thing (did I do it?) vs. three things (hypothesis, action, frequency)
- Users graduate to reflection: Once daily logging is a habit, they naturally reflect

### Why Weekly (Not Daily) Reflections

**Considered**: Prompt for reflection right after each action, or monthly reviews  
**Chose**: Weekly prompts (Sundays, customizable)

**Rationale**:
- Weekly is the sweet spot: Daily is intrusive (too much thinking), monthly is too sparse (forgotten context)
- Sunday evening: Natural pause point in the week, reflective mode
- Plus-Minus-Next: Simple enough for 2 minutes, deep enough to reveal patterns
- Optional: Users can skip or delay (no shame)

### Why No Notifications

**Considered**: Gentle reminders, daily check-in nudges  
**Chose**: Zero notifications

**Rationale**:
- Removes obligation: Users open the app because they want to, not because they're reminded
- Reduces anxiety: No "you forgot" messages, no pressure
- Respects autonomy: Users manage their own schedule, not the app's
- Simpler code: No notification scheduling or permission handling

### Why Up to 2 Active Experiments

**Considered**: Unlimited experiments, or strict 1-only  
**Chose**: 2 as the default limit

**Rationale**:
- Encourages focus: 2 is enough to stay curious without diluting attention
- Allows diversity: Test different domains (e.g., sleep + writing)
- Sustainable: Most users can maintain 2 experiments without burnout
- Flexibility: Power users can fork the app or build on it

### Why Show Gaps in Streaks

**Considered**: Reset streaks on missed days, hide gaps  
**Chose**: Show gaps as data

**Rationale**:
- Honesty: The data says what actually happened
- Learning: Gaps reveal patterns ("I skip on Mondays" or "I need a specific trigger")
- No shame: A gap isn't a failure, it's information
- Sustainable: Removing shame removes the quitting trigger

---

## How We Know We're Succeeding

### Users Will Say:

- "I learned something about myself I didn't expect"
- "This doesn't judge me for missing days"
- "I can actually pause and come back without guilt"
- "I love that there's no pressure to sync to the cloud"
- "I feel in control of my data"

### Users Will Do:

- Create 5+ experiments over a few months (comfortable iterating)
- Come back to the app after months away (no guilt barrier)
- Contribute ideas or code to the project (trust in the vision)
- Recommend it to friends (word-of-mouth growth)

### We Should Avoid:

- Users chasing streaks at the expense of actual learning
- Users feeling guilty for skipping or pivoting
- Users anxious about their privacy
- Users unable to use the app offline
- Users misunderstanding what an experiment is

---

## Future Enhancements: More Tools

The app covers Tools 1–3, 7, and 8 from Anne-Laure Le Cunff's 12-tool Experimentalist Toolkit (the Pact, Field Notes, Three Mental Shifts, Plus Minus Next, and Steering Sheet). The remaining tools — Kairos Ritual, Triple Check, Ambition Dials, Two-Step Reset, and Five Keys — are not implemented as features but will be surfaced in a **"More Tools"** section in Settings.

This section gives users a high-level overview of each tool with links to further reading, educating them about the full framework without adding complexity to the core app. It keeps the app focused while pointing curious users toward the book and Ness Labs for deeper exploration.

---

## Evolving This Philosophy

This document will evolve as the app grows. Community feedback, real usage patterns, and new ideas from Anne-Laure's work should all inform updates.

**Process for changes**:
1. Open an issue describing the proposed change
2. Discuss with maintainers and community
3. Update this doc (and related docs) if approved
4. Announce change in release notes

---

## References

- **Anne-Laure Le Cunff**: *Tiny Experiments: How to Live Freely in a Goal-Obsessed World* (Penguin, 2025)
- **Ness Labs**: https://nesslabs.com (essays on mindful productivity)
- **B.J. Fogg**: *Tiny Habits* (framework for behavior design)
- **Oliver Burkeman**: *Four Thousand Weeks* (rethinking productivity)

---

**Version**: 1.0  
**Last Updated**: March 2026  
**Status**: Stable
