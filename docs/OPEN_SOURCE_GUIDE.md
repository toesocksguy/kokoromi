# Open Source Guide

## License

**GPL-3.0.** Use, study, modify freely. Distribute modified versions under GPL-3.0. Cannot be used in proprietary closed-source products. See `LICENSE`.

---

## Code of Conduct

[Contributor Covenant](https://www.contributor-covenant.org/). Short version: be kind, assume good intent, be constructive. Curiosity and non-judgment apply to how we treat each other too.

Reports of violations: [maintainer contact — fill in before publishing]

---

## Ways to Contribute

| Type | How |
|------|-----|
| Bug report | GitHub Issue |
| Feature idea | GitHub Discussion |
| Docs fix | PR against `docs/` |
| Bug fix | PR with regression test |
| New feature | Discuss first, then PR |
| Device testing | Report in Issues |

---

## Before You Start

- Search open issues before filing a new one
- For anything beyond a small bug fix, open a Discussion first — prevents work on something we won't merge
- **Philosophy is non-negotiable**: features adding gamification, notifications, social comparison, cloud telemetry, or dark patterns will not be merged regardless of implementation quality. See [PHILOSOPHY.md](PHILOSOPHY.md)

---

## Setup

**Prerequisites**: Android Studio (latest stable), JDK 17+, Android device or emulator (API 26+), Git.

```bash
git clone https://github.com/[org]/kokoromi.git
cd kokoromi
./gradlew assembleDebug        # always use the wrapper, not system gradle

./gradlew test                 # unit tests (no device needed)
./gradlew connectedAndroidTest # UI + DB tests (device/emulator required)
```

---

## Pull Requests

**Branch naming**
```
fix/short-description
feature/short-description
docs/short-description
refactor/short-description
```

**Commit style** — conventional commits:
```
fix: correct streak display when experiment spans month boundary
feat: add optional notes field to check-in screen
docs: clarify reflection schedule in REFLECTION_SYSTEM.md
test: add missing test for pause → resume lifecycle transition
```

**PR description** — include: what, why, how to test, before/after screenshots for UI changes.

**PR size** — keep small and focused. For large features, stack PRs by layer: data → domain → UI.

**Tests required**:
- Bug fix → regression test
- New feature → use case unit test + happy-path UI test
- Schema change → repository integration test

---

## Code Review

One review required before merge. Reviewers check: correctness, philosophy fit, architecture (layered MVVM), test quality, simplicity.

Aim for review within 1 week. Ping after 2 weeks with no response. Comments are discussion, not demands — push back thoughtfully.

---

## Database Migrations

If you change the schema:

1. Increment version in `KokoromiDatabase.kt`
2. Create `Migration_X_Y.kt` in `data/db/migrations/`
3. Register it in the database builder
4. Add a schema test verifying data is preserved
5. Document in [DATA_MODEL.md](DATA_MODEL.md)

**Never use `fallbackToDestructiveMigration()` in production.** User data must be preserved.

---

## Documentation

If your PR changes behavior, update the relevant doc in the same PR.

| Change | Doc to update |
|--------|--------------|
| Core values | PHILOSOPHY.md |
| Architecture or patterns | ARCHITECTURE.md |
| Schema, entities, queries | DATA_MODEL.md |
| Experiment state transitions | EXPERIMENT_LIFECYCLE.md |
| Screen layout or interaction | UI_DESIGN.md |
| Reflection logic | REFLECTION_SYSTEM.md |

---

## Reporting Bugs

GitHub Issue. Include: Android version, device, app version, steps to reproduce, expected vs. actual behavior, Logcat output if relevant.

---

## Feature Requests

GitHub Discussion. Explain what, why, and how it fits the philosophy. Features conflicting with privacy, no-gamification, or user agency will be declined.

---

## Security

**Do not open a public issue for security vulnerabilities.**

Email [security contact — fill in before publishing] with: description, steps to reproduce, potential impact. Response within 72 hours. Coordinated disclosure before any public announcement.

Primary attack surface: data export file permissions, local database (unencrypted in v1), Android intent handling.

---

## Attribution

Contributors listed in GitHub contributors graph. Significant contributors may appear in Settings → About.

Built on Anne-Laure Le Cunff's *Tiny Experiments* (Penguin, 2025). Not affiliated with her. Color theme from [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).
