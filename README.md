# Kokoromi

An open-source Android "habit" tracker built on Anne-Laure Le Cunff's [Tiny Experiments](https://nesslabs.com/book) framework for living life through systematic curiosity.

**Status**: v0.1.0 — Milestones 1–6 complete, Milestone 7 (Archive, Field Notes, Settings & Export) in progress  
**License**: GPL-3.0  
**Platform**: Android 8.0+ (API 26+)

---

## What is Kokoromi?

Most habit apps are built on shame and streaks. Kokoromi is built on curiosity. Instead of setting goals, you run small, time-bound experiments: "I will [action] for [duration]." You log what actually happened, reflect weekly, and decide what's next — keep going, adjust, or set it aside. No badges. No guilt. Just honest data and what you learn from it.

---

## Core Principles

1. **Curiosity over achievement** — Experiments are questions, not goals
2. **Simplicity over gamification** — No badges, no leaderboards, no shame mechanics
3. **User agency** — No dark patterns, no pressure. Opt-in reminders only, fully user-controlled
4. **Privacy by default** — All data stays on your device, zero telemetry
5. **Offline-first** — Works fully without internet
6. **Accessibility** — WCAG 2.1 AA, built in from the start

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Architecture** | MVVM + Coroutines |
| **Database** | Room (SQLite) |
| **Dependency Injection** | Hilt |
| **Testing** | JUnit, Mockito, Compose Testing |
| **Build** | Gradle 8.13, Kotlin DSL |

---

## Getting Started

```bash
git clone https://github.com/[org]/kokoromi.git
cd kokoromi
./gradlew assembleDebug
```

Always use `./gradlew` rather than a system `gradle` command — the wrapper ensures everyone builds with the same Gradle version.

---

## Documentation

All design docs live in `docs/`. Start with [MANIFEST.md](docs/MANIFEST.md) — it has the full index, role-based reading guides, and a common questions FAQ.

---

## Known Issues & Remaining Work

**Backlog**
- Import data from JSON export (complement to existing export feature)

**Remaining M7 tasks**
- Unit tests for `GetReflectionPromptStateUseCase`, `GetActiveExperimentsWithLogsUseCase`, `UpdateExperimentStatusUseCase`
- Accessibility pass:
  - `ExperimentDetailScreen.kt` lines 336–339, 378–379: `LogItem` and `ReflectionItem` have empty `semantics(mergeDescendants = true) {}` blocks — hook is there but no `contentDescription`; timeline entries announce nothing to screen readers
  - `ArchiveScreen.kt` line 143: archive items describe hypothesis + status only; date range and completion rate (visible on screen) missing from `contentDescription`
  - `ExperimentCard.kt` lines 118–123: "Edit Log" button has no `contentDescription`; appears when today is already logged
  - Android Accessibility Scanner run
- ✅ Daily check-in reminder notification (opt-in) — complete
- Release CI: `.github/workflows/release.yml` — build and publish APK on git tag

---

## What's Not in v1

- ❌ Cloud sync (user can export to JSON)
- ❌ Push notifications, social alerts, or streak reminders
- ❌ Social features or sharing
- ❌ Experiment templates or suggestions
- ❌ Gamification (streaks that reset, badges, leaderboards)

---

## A Note on How This Was Built

This project was designed and built through **prompt-architecting with Claude Code** (Anthropic's AI coding assistant). The developer directed the vision, made key product decisions, and reviewed all output — with the AI contributing to architectural decisions, code generation, and documentation throughout.

This is sometimes called "vibe-coding." Being transparent about it feels consistent with this project's values of honesty and curiosity. The code works, the architecture is sound, and the philosophy is genuine — the tools used to build it don't change that.

---

## Contributing

Contributions are welcome. Please read [OPEN_SOURCE_GUIDE.md](docs/OPEN_SOURCE_GUIDE.md) before submitting a PR.

Feature ideas that add gamification, pressure-based notifications, social comparison, or cloud telemetry will not be merged — see [PHILOSOPHY.md](docs/PHILOSOPHY.md) for why.

---

## Attribution

Built on Anne-Laure Le Cunff's *Tiny Experiments: How to Live Freely in a Goal-Obsessed World* (Penguin, 2025). We are not affiliated with her, but we're grateful for the framework.

Color theme generated with [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).

---

**Maintained by**: toesocksguy  
**Last Updated**: April 2026
