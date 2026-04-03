# Design Documentation Manifest

**Kokoromi** — Complete Design Documentation

---

## Quick Navigation

### For First-Time Readers

**Want to understand the vision?**  
Start here → [PHILOSOPHY.md](PHILOSOPHY.md) (15 min read)

**Want to understand the codebase?**  
Start here → [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) (10 min read)

**Want to see what users will experience?**  
Start here → [UI_DESIGN.md](UI_DESIGN.md) (20 min read)

---

### For Developers

#### Building a New Feature

1. Read **PHILOSOPHY.md** — Understand the design principles
2. Read **EXPERIMENT_LIFECYCLE.md** — Understand state flow (if feature affects experiments)
3. Read **DATA_MODEL.md** — Understand database schema (if feature stores data)
4. Read **UI_DESIGN.md** — Understand screen layouts (if feature has UI)
5. Read **PROJECT_STRUCTURE.md** — Understand where to put code
6. Implement in feature branch
7. Add tests (unit + UI)
8. Submit PR with reference to relevant design docs

#### Fixing a Bug

1. Read **EXPERIMENT_LIFECYCLE.md** — Understand state transitions
2. Read **DATA_MODEL.md** — Understand data flow
3. Write a test that reproduces the bug
4. Fix the bug
5. Verify test passes
6. Submit PR with clear description of the issue

#### Refactoring Code

1. Read **ARCHITECTURE.md** — Understand architectural decisions
2. Read **PROJECT_STRUCTURE.md** — Understand module organization
3. Plan refactoring (sketch on paper or in comment)
4. Implement refactoring
5. Run all tests
6. Update design docs if you change architecture
7. Submit PR with detailed explanation

---

### For Contributors

1. Read **PHILOSOPHY.md** — Understand the project's values
2. Read **OPEN_SOURCE_GUIDE.md** — Understand contribution process
3. Read **PROJECT_STRUCTURE.md** — Understand where code lives
4. Pick an issue or propose a feature
5. Follow the "For Developers" workflow above

---

## Document Descriptions

### Core Docs (read these first)

#### [README.md](README.md)
**What**: Project overview, tech stack, key design decisions, navigation links
**When**: Start here if you're new to the project
**Length**: 5 min · **Audience**: Everyone

#### [PHILOSOPHY.md](PHILOSOPHY.md)
**What**: Core principles, the three mental shifts, design anti-patterns, tone and voice
**When**: Before any coding or design decision — this is the north star
**Length**: 20 min · **Audience**: Everyone

#### [MANIFEST.md](MANIFEST.md) ← you are here
**What**: Navigation guide, document index, workflows, common questions
**When**: Orienting yourself, onboarding others, finding where something lives
**Length**: 10 min · **Audience**: Everyone

---

### Architecture & Structure

#### [ARCHITECTURE.md](ARCHITECTURE.md)
**What**: MVVM layers, ViewModel/Composable patterns, Hilt DI, concurrency, error handling, testing strategy, key architectural decisions
**When**: Making architectural changes, understanding overall app structure, code review
**Length**: 25 min · **Audience**: Developers

#### [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
**What**: Full directory layout with annotations, module organization, build commands, key files
**When**: Setting up dev environment, adding new files, finding where code lives
**Length**: 15 min · **Audience**: Developers

---

### Features & Data

#### [DATA_MODEL.md](DATA_MODEL.md)
**What**: All database entities (Experiment, DailyLog, Reflection, Completion, FieldNote), schemas, DAOs, repositories, migrations, JSON export format
**When**: Working on the data layer, adding/changing fields, debugging data issues
**Length**: 30 min · **Audience**: Backend/data developers

#### [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md)
**What**: Full state machine (ACTIVE → PAUSED → COMPLETED → ARCHIVED), all transitions with guards and side effects, check-in logic, Steering Sheet decisions (Persist/Pivot/Pause), edge cases
**When**: Implementing anything that changes experiment state
**Length**: 20 min · **Audience**: Feature developers

#### [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md)
**What**: Plus-Minus-Next framework (exact prompts), weekly prompted reflections, optional daily reflections, completion reflections, scheduling logic
**When**: Working on reflection features
**Length**: 15 min · **Audience**: Feature developers

#### [UI_DESIGN.md](UI_DESIGN.md)
**What**: All 10 screens with wireframes and interaction specs: Home, Check-In, Field Notes, Add/Edit Note, Archive, Experiment Detail, Create Experiment, Reflection, Completion, Settings
**When**: Implementing any screen, designing new UI, checking interaction patterns
**Length**: 30 min · **Audience**: UI/UX developers, designers

---

### Constraints & Compliance

#### [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md)
**What**: Hard limits and tradeoffs that shape all decisions — what we won't build and why
**When**: Evaluating new feature ideas, resolving design disagreements
**Length**: 20 min · **Audience**: Everyone

#### [PERFORMANCE_RESPONSIVENESS_SECURITY.md](PERFORMANCE_RESPONSIVENESS_SECURITY.md)
**What**: Performance targets, UI responsiveness requirements, local security (KeyStore encryption), offline-first guarantees
**When**: Optimizing performance, reviewing security posture, testing on low-end devices
**Length**: 20 min · **Audience**: Developers

#### [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md)
**What**: Specific guidance for targeting low-end Android devices (2GB RAM, slow CPUs, limited storage) — the app must run well on budget phones
**When**: Optimizing, profiling, choosing between implementation approaches
**Length**: 20 min · **Audience**: Developers

#### [LEGAL_CONSIDERATIONS.md](LEGAL_CONSIDERATIONS.md)
**What**: GPL-3.0 license obligations, attribution requirements, data privacy (GDPR/CCPA), no-telemetry policy, open terminology notes
**When**: Adding third-party dependencies, publishing updates, open-sourcing
**Length**: 15 min · **Audience**: Maintainers, contributors

---

### Community

#### [OPEN_SOURCE_GUIDE.md](OPEN_SOURCE_GUIDE.md)
**What**: Contributing guidelines, branch naming, commit style, PR process, code review expectations, database migration rules, security reporting
**When**: Contributing to the project, reviewing PRs, onboarding contributors
**Length**: 15 min · **Audience**: Contributors

---

## Document Status

| Document | Status | Last Updated | Notes |
|----------|--------|--------------|-------|
| README.md | ✅ Complete | March 2026 | Project overview and navigation |
| PHILOSOPHY.md | ✅ Complete | March 2026 | Three mental shifts added from book |
| PROJECT_STRUCTURE.md | ✅ Complete | March 2026 | Field Notes files added |
| DATA_MODEL.md | ✅ Complete | March 2026 | FieldNote entity added; DecisionType aligned to book |
| UI_DESIGN.md | ✅ Complete | March 2026 | Field Notes screens, pact preview, Steering Sheet, 4-tab nav |
| EXPERIMENT_LIFECYCLE.md | ✅ Complete | March 2026 | Persist/Pivot/Pause aligned to Steering Sheet |
| REFLECTION_SYSTEM.md | ✅ Complete | March 2026 | Daily optional reflections added; prompts aligned to book |
| ARCHITECTURE.md | ✅ Complete | March 2026 | MVVM, Hilt, testing patterns |
| OPEN_SOURCE_GUIDE.md | ✅ Complete | March 2026 | Needs maintainer contact filled in before publishing |
| DESIGN_CONSTRAINTS.md | ✅ Complete | March 2026 | — |
| PERFORMANCE_RESPONSIVENESS_SECURITY.md | ✅ Complete | March 2026 | — |
| BUDGET_PHONE_OPTIMIZATION.md | ✅ Complete | March 2026 | — |
| LEGAL_CONSIDERATIONS.md | ✅ Complete | March 2026 | Verify trademark section before launch |
| MANIFEST.md | ✅ Complete | March 2026 | This file |

---

## Key Concepts Cross-Reference

### The Experimentalist Framework (from the book)

- **Pact format** ("I will [action] for [duration]") → [PHILOSOPHY.md](PHILOSOPHY.md) · [UI_DESIGN.md](UI_DESIGN.md) (Create Experiment Screen)
- **Three Mental Shifts** → [PHILOSOPHY.md](PHILOSOPHY.md) (Three Mental Shifts section)
- **Plus Minus Next** → [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) · [UI_DESIGN.md](UI_DESIGN.md) (Reflection Screen)
- **Steering Sheet** (Persist / Pivot / Pause) → [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) · [UI_DESIGN.md](UI_DESIGN.md) (Completion Screen)
- **Field Notes** (observation → hypothesis) → [DATA_MODEL.md](DATA_MODEL.md) (FieldNote entity) · [UI_DESIGN.md](UI_DESIGN.md) (Field Notes Screen)

### Experiment Lifecycle

- **Create** → [UI_DESIGN.md](UI_DESIGN.md) (Create Experiment Screen) · [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) (ACTIVE state)
- **Daily Check-in** → [UI_DESIGN.md](UI_DESIGN.md) (Check-In Screen) · [DATA_MODEL.md](DATA_MODEL.md) (DailyLog entity)
- **Weekly Reflection** → [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) · [UI_DESIGN.md](UI_DESIGN.md) (Reflection Screen)
- **Daily Reflection** (optional) → [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) (Daily Reflection section) · [UI_DESIGN.md](UI_DESIGN.md) (Check-In Screen)
- **Completion + Decision** → [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) (transitions) · [UI_DESIGN.md](UI_DESIGN.md) (Completion Screen) · [DATA_MODEL.md](DATA_MODEL.md) (Completion entity)
- **Archive** → [UI_DESIGN.md](UI_DESIGN.md) (Archive Screen) · [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) (ARCHIVED state)

### Data Flow

- **From UI to Database** → [ARCHITECTURE.md](ARCHITECTURE.md) (UI → Domain → Data layers) · [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) (directory layout)
- **From Database to UI** → [ARCHITECTURE.md](ARCHITECTURE.md) (repositories + ViewModels + StateFlow) · [DATA_MODEL.md](DATA_MODEL.md) (queries)
- **Validation** → [DATA_MODEL.md](DATA_MODEL.md) (validation rules per entity) · [ARCHITECTURE.md](ARCHITECTURE.md) (use cases)
- **Export** → [DATA_MODEL.md](DATA_MODEL.md) (JSON format) · [UI_DESIGN.md](UI_DESIGN.md) (Settings Screen)

### User Experience

- **Design principles** → [PHILOSOPHY.md](PHILOSOPHY.md)
- **What we won't build** → [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md) · [PHILOSOPHY.md](PHILOSOPHY.md) (Design Anti-Patterns)
- **Screen flows** → [UI_DESIGN.md](UI_DESIGN.md)
- **Accessibility** → [UI_DESIGN.md](UI_DESIGN.md) (Accessibility section)
- **Performance on low-end devices** → [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md) · [PERFORMANCE_RESPONSIVENESS_SECURITY.md](PERFORMANCE_RESPONSIVENESS_SECURITY.md)

---

## Common Questions

**Q: Where do I add a new column to the experiments table?**
A: [DATA_MODEL.md](DATA_MODEL.md) (migrations section) → create `Migration_X_Y.kt` → update entity + DAO → add tests

**Q: How do I create a new screen?**
A: [UI_DESIGN.md](UI_DESIGN.md) (find the screen spec) → [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) (where to put files) → create Screen + ViewModel → wire up in `KokoromiNavigation.kt`

**Q: Should I add notifications / gamification / social features?**
A: [PHILOSOPHY.md](PHILOSOPHY.md) (Design Anti-Patterns) + [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md) → these are explicitly out of scope

**Q: How does an experiment move from ACTIVE to ARCHIVED?**
A: [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) (state machine + transitions section)

**Q: What are the exact Plus Minus Next prompts?**
A: [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) (Default Prompts section)

**Q: What decisions can a user make at the end of an experiment?**
A: Persist / Pivot / Pause — see [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) and [UI_DESIGN.md](UI_DESIGN.md) (Completion Screen)

**Q: Where do field notes live in the codebase?**
A: [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) (`ui/fieldnotes/`) · [DATA_MODEL.md](DATA_MODEL.md) (FieldNote entity)

**Q: How do users export their data?**
A: [DATA_MODEL.md](DATA_MODEL.md) (Backup & Export + JSON format) · [UI_DESIGN.md](UI_DESIGN.md) (Settings Screen)

**Q: Will this run well on a cheap Android phone?**
A: [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md) — read before making any performance-sensitive implementation choice

**Q: Can I add a third-party analytics SDK?**
A: No — [PHILOSOPHY.md](PHILOSOPHY.md) (Privacy by Default) + [LEGAL_CONSIDERATIONS.md](LEGAL_CONSIDERATIONS.md)

---

## How to Use This Documentation

### During Development

- **Before writing code**: Skim the relevant design doc (5 min)
- **While implementing**: Reference specific sections (code examples, patterns)
- **During code review**: Check if implementation matches design doc intent
- **For tests**: Use design doc as spec for what to test

### During Design Reviews

- Compare proposed changes against [PHILOSOPHY.md](PHILOSOPHY.md)
- Ask: "Does this align with our core principles?"
- Reference [UI_DESIGN.md](UI_DESIGN.md) for UX consistency

### For Onboarding New Contributors

- Start them with [README.md](README.md) + [PHILOSOPHY.md](PHILOSOPHY.md)
- Have them read [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)
- Assign them a small task (bug fix or small feature)
- Let them reference docs while working

### For Handoffs

- Give new owner full documentation suite
- Highlight sections most relevant to their role
- Schedule knowledge transfer session referencing docs

---

## Updating This Documentation

When you make changes to the app, update the relevant doc in the same PR:

| Change | Update |
|--------|--------|
| Design principle or value | [PHILOSOPHY.md](PHILOSOPHY.md) |
| Directory structure or new files | [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) |
| Database schema, entity, or query | [DATA_MODEL.md](DATA_MODEL.md) |
| Experiment states or transitions | [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) |
| Reflection prompts, cadence, or triggers | [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) |
| Any screen layout or interaction | [UI_DESIGN.md](UI_DESIGN.md) |
| Architectural pattern or DI setup | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Contribution or review process | [OPEN_SOURCE_GUIDE.md](OPEN_SOURCE_GUIDE.md) |
| Any of the above | Also update the status table in this file |

**Rule**: Docs should never be more than 2 weeks out of sync with code. If code changes, docs must be updated in the same PR.

---

## Generating Documentation from Code

**Future enhancement**: Generate API docs from code comments using Dokka:

```bash
./gradlew dokka
```

This will generate HTML API reference from KDoc comments in source files. See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for details.

---

## Feedback & Improvements

Found an error in the docs? Ambiguous wording? Missing section?

1. Open an issue on GitHub describing the problem
2. Or: submit a PR with improvements
3. Or: start a discussion in GitHub Discussions

We want these docs to be useful and accurate. Your feedback helps!

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | March 30, 2026 | Initial design documentation suite |
| 1.1 | March 31, 2026 | Added EXPERIMENT_LIFECYCLE, REFLECTION_SYSTEM, ARCHITECTURE, OPEN_SOURCE_GUIDE |
| 1.2 | March 31, 2026 | Aligned to book: Pact format, Three Mental Shifts, Plus Minus Next prompts, Steering Sheet (Persist/Pivot/Pause), PACT cycle framing |
| 1.3 | March 31, 2026 | Added Field Notes (FieldNote entity, Field Notes screen, Add/Edit Note screen, 4-tab nav); optional daily reflections |

---

**Created**: March 2026  
**Maintained by**: [Your team/org]  
**License**: These docs are part of the GPL-3.0 licensed project
