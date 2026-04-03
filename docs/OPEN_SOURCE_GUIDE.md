# Open Source Guide: Kokoromi

## Welcome

Tiny Experiments is an open source Android app for running small personal experiments using Anne-Laure Le Cunff's framework. We're glad you're here.

This guide covers everything you need to contribute: how we work, what we expect, and how to get your changes merged.

---

## License

**GPL-3.0**

All code is GPL-3.0 licensed. This means:
- You can use, study, and modify the code freely
- If you distribute a modified version, it must also be GPL-3.0
- You cannot use this code in a proprietary closed-source product

See the `LICENSE` file in the root of the repository.

---

## Code of Conduct

This project follows the [Contributor Covenant](https://www.contributor-covenant.org/) Code of Conduct.

The short version: be kind, assume good intent, be constructive in feedback. The app is built around curiosity and non-judgment — those values extend to how we treat each other.

Reports of harassment or violations go to [maintainer contact — fill this in].

---

## Ways to Contribute

You don't have to write code to contribute.

| Contribution Type | How |
|-------------------|-----|
| Bug reports | Open a GitHub Issue |
| Feature ideas | Open a GitHub Discussion |
| Documentation fixes | PR against `docs/` |
| Code — bug fix | PR with test |
| Code — new feature | Discuss first, then PR |
| Translations | See i18n section (future) |
| Design feedback | Open a Discussion |
| Testing on real devices | Report in Issues |

---

## Before You Start

### Check Existing Issues

Search open issues before creating a new one. If your bug or idea is already there, add a thumbs-up or a comment rather than a duplicate.

### Discuss Before Big Changes

For anything beyond a bug fix or small improvement, open a Discussion or comment on an existing issue first. This prevents you from spending time on something we won't merge — either because it conflicts with the philosophy or because someone else is already working on it.

**Design philosophy is non-negotiable**: features that add gamification, notifications, social comparison, cloud telemetry, or dark patterns will not be merged, regardless of implementation quality. See [PHILOSOPHY.md](PHILOSOPHY.md).

---

## Setting Up for Development

### Prerequisites

- Android Studio (latest stable — Hedgehog or newer)
- JDK 17+
- Android device or emulator (API 26+)
- Git

### Clone and Build

```bash
git clone https://github.com/[org]/kokoromi.git
cd kokoromi
./gradlew assembleDebug
```

Always use `./gradlew` (the Gradle wrapper) rather than a system `gradle` command. The wrapper is checked into the repo and ensures everyone builds with the same Gradle version regardless of what's installed locally.

### Run Tests

```bash
# Unit tests (no device needed)
./gradlew test

# UI and database tests (device or emulator required)
./gradlew connectedAndroidTest
```

### Code Style

We follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Android Studio's built-in formatter handles most of this automatically.

---

## Project Structure

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for the full directory layout.

Key directories:
- `app/src/main/kotlin/com/kokoromi/ui/` — Compose screens
- `app/src/main/kotlin/com/kokoromi/domain/` — Business logic, use cases
- `app/src/main/kotlin/com/kokoromi/data/` — Room database, repositories
- `docs/` — All design documentation

Architecture overview: [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Making a Pull Request

### 1. Branch Naming

```
fix/short-description        ← bug fixes
feature/short-description    ← new features
docs/short-description       ← documentation only
refactor/short-description   ← code cleanup, no behavior change
```

### 2. Commit Style

Use conventional commits:

```
fix: correct streak display when experiment spans month boundary
feat: add optional notes field to check-in screen
docs: clarify reflection schedule in REFLECTION_SYSTEM.md
refactor: extract date formatting to DateUtils
test: add missing test for pause → resume lifecycle transition
```

Keep commits focused. One logical change per commit. It's fine to squash before merging if your history is messy.

### 3. PR Description

Include:
- **What**: What does this change?
- **Why**: Why is this change needed?
- **How to test**: Steps to verify it works
- **Screenshots**: For UI changes, before/after screenshots are required

Template:
```
## What
[Short description of the change]

## Why
[Context, issue reference, or reason]

## How to Test
1. Build and run
2. Navigate to [screen]
3. [Steps to reproduce / verify]

## Screenshots (if UI change)
[Before] | [After]
```

### 4. PR Size

Keep PRs small and focused. A PR that touches 20 files is hard to review and will wait longer.

If you're building a large feature, break it into layers: data → domain → UI, each as its own PR.

### 5. Tests Required

- Bug fix → regression test
- New feature → unit tests for use case, UI test for the happy path
- Data model change → repository integration test

PRs without tests for new behavior will be asked to add them.

---

## Code Review

All PRs require at least one review before merge.

### What Reviewers Look For

- **Correctness**: Does it do what it says?
- **Philosophy alignment**: Does it fit the Kokoromi philosophy?
- **Architecture**: Does it follow the layered MVVM pattern?
- **Tests**: Are they meaningful?
- **Simplicity**: Is there a simpler way?

### Response Time

Maintainers aim to review within 1 week. If your PR has no response after 2 weeks, ping in the PR thread.

### Be Ready to Iterate

Code review means conversation, not rejection. Comments are requests to discuss, not demands. Push back thoughtfully if you disagree.

---

## Adding a New Feature: Step-by-Step

Following the pattern from [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md):

1. **Domain first**: Define any new enums or domain models in `domain/model/`
2. **Use case**: Add a use case in `domain/usecase/` with the business logic
3. **Data**: If needed, add Room entities and DAOs; update or add a repository
4. **DI**: Bind new dependencies in the appropriate Hilt module in `di/`
5. **ViewModel**: Add a ViewModel in `ui/yourfeature/`
6. **UI**: Add Composables in `ui/yourfeature/`
7. **Navigation**: Wire up routes in `KokoromiNavigation.kt`
8. **Tests**: Unit tests in `test/`, UI tests in `androidTest/`
9. **Docs**: Update the relevant design doc if behavior changed

---

## Database Migrations

If you change the database schema:

1. Increment the database version in `KokoromiDatabase.kt`
2. Create a migration: `Migration_X_Y.kt` in `data/db/migrations/`
3. Register the migration in the database builder
4. Add a schema test to verify the migration doesn't corrupt data
5. Document the change in the Migration section of [DATA_MODEL.md](DATA_MODEL.md)

Never use `fallbackToDestructiveMigration()` in production builds. User data must be preserved.

---

## Design Documentation

Docs live in `docs/`.

**Rule**: If your PR changes behavior, update the relevant doc in the same PR. Code and docs ship together.

Documents:
- `PHILOSOPHY.md` — Core values (rarely changes)
- `ARCHITECTURE.md` — Update if you change patterns or add layers
- `DATA_MODEL.md` — Update if you add/change entities or queries
- `EXPERIMENT_LIFECYCLE.md` — Update if you change state transitions
- `UI_DESIGN.md` — Update if you change a screen's behavior or layout
- `REFLECTION_SYSTEM.md` — Update if you change reflection logic

---

## Reporting Bugs

Use the GitHub Issues tracker. Include:

1. **Android version** (e.g., Android 12, API 31)
2. **Device** (e.g., Pixel 6, or "emulator")
3. **App version** (from Settings screen)
4. **Steps to reproduce** (numbered)
5. **Expected behavior**
6. **Actual behavior**
7. **Logs** (if relevant — from Android Studio Logcat)

---

## Feature Requests

Open a GitHub Discussion (not an Issue) for feature ideas. Explain:
- What you want
- Why you want it
- How it fits the Kokoromi philosophy

Features that conflict with the core philosophy (privacy, no gamification, agency) will be declined. Everything else is on the table.

---

## Release Process

Maintainers handle releases. Here's how it works:

1. Version bump in `build.gradle.kts` (versionCode + versionName)
2. Update changelog
3. Tag the release: `git tag v1.0.1`
4. GitHub Actions builds and signs the release APK
5. APK attached to the GitHub Release
6. (Future) Auto-upload to Google Play Store

Contributors don't need to worry about this — just get the PR merged.

---

## Security

If you discover a security vulnerability, **do not open a public Issue**.

Email [security contact — fill this in] with:
- Description of the vulnerability
- Steps to reproduce
- Potential impact

We'll respond within 72 hours and work with you on a fix before any public disclosure.

Because the app is offline-first with no server and no user accounts, the attack surface is small. Most security concerns are around: data export file permissions, local database encryption, and Android intent handling.

---

## Attribution

Contributors are listed in the GitHub contributors graph. Significant contributors may be listed in the app's About screen (Settings → About).

Anne-Laure Le Cunff's *Tiny Experiments* (2025) is the framework this app is built on. We are not affiliated with her, but we're grateful for the ideas.

The app's color theme was generated using [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/).

---

**Version**: 1.0
**Last Updated**: March 2026
**Status**: Draft — fill in maintainer contact details before publishing
