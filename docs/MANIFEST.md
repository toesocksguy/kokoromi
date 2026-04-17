# Documentation Manifest

## Start Here

| Role | First read |
|------|-----------|
| New to the project | [README.md](../README.md) → [PHILOSOPHY.md](PHILOSOPHY.md) |
| Building a feature | [PHILOSOPHY.md](PHILOSOPHY.md) → [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) → [DATA_MODEL.md](DATA_MODEL.md) → [UI_DESIGN.md](UI_DESIGN.md) |
| Fixing a bug | [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) → [DATA_MODEL.md](DATA_MODEL.md) |
| Refactoring | [ARCHITECTURE.md](ARCHITECTURE.md) → [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) |
| Contributing | [PHILOSOPHY.md](PHILOSOPHY.md) → [OPEN_SOURCE_GUIDE.md](OPEN_SOURCE_GUIDE.md) |

---

## Documents

| Doc | What it covers |
|-----|---------------|
| [README.md](../README.md) | Vision, principles, stack, known issues, quick start |
| [PHILOSOPHY.md](PHILOSOPHY.md) | North star, anti-patterns, tone, why behind decisions |
| [ARCHITECTURE.md](ARCHITECTURE.md) | MVVM layers, patterns, concurrency, error handling, testing strategy, architectural decisions |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Directory layout, key files, constants, build commands, CI/CD, feature checklist |
| [DATA_MODEL.md](DATA_MODEL.md) | Schema, entities, domain models, validation rules, relationships, export format |
| [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) | State machine (ACTIVE → COMPLETED → ARCHIVED/PAUSED), transitions, edge cases |
| [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) | Plus-Minus-Next framework, weekly/daily/completion reflection flows, prompts |
| [UI_DESIGN.md](UI_DESIGN.md) | All 10 screens: wireframes, components, interaction notes, text display conventions |
| [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md) | Hard limits: architecture lock-ins, forbidden deps, permissions, release gate |
| [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md) | Performance targets, key principles, pre-release checklist for budget devices |
| [PERFORMANCE_RESPONSIVENESS_SECURITY.md](PERFORMANCE_RESPONSIVENESS_SECURITY.md) | Security status, known gaps (DB encryption), release security checklist |
| [LEGAL_CONSIDERATIONS.md](LEGAL_CONSIDERATIONS.md) | GPL-3.0 obligations, attribution, privacy (GDPR/CCPA), no-telemetry policy |
| [OPEN_SOURCE_GUIDE.md](OPEN_SOURCE_GUIDE.md) | Contributing, branch/commit conventions, PR process, security reporting |

---

## Common Questions

| Question | Where to look |
|----------|--------------|
| How does ACTIVE → ARCHIVED work? | [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) |
| What are the exact Plus/Minus/Next prompts? | [REFLECTION_SYSTEM.md](REFLECTION_SYSTEM.md) |
| What decisions can a user make at completion? | [EXPERIMENT_LIFECYCLE.md](EXPERIMENT_LIFECYCLE.md) — Persist / Pivot / Pause |
| How do I add a column to a table? | [DATA_MODEL.md](DATA_MODEL.md) → create `Migration_X_Y.kt` → update entity + DAO + tests |
| How do I add a new screen? | [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) (feature checklist) + [UI_DESIGN.md](UI_DESIGN.md) |
| Can I add notifications / gamification / social features? | No — [PHILOSOPHY.md](PHILOSOPHY.md) + [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md) |
| Can I add an analytics SDK? | No — [PHILOSOPHY.md](PHILOSOPHY.md) + [LEGAL_CONSIDERATIONS.md](LEGAL_CONSIDERATIONS.md) |
| Where do field notes live in the code? | `ui/fieldnotes/` — [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) + [DATA_MODEL.md](DATA_MODEL.md) |
| What's the JSON export format? | [DATA_MODEL.md](DATA_MODEL.md) — Export Format section |
| Will this run on a cheap phone? | [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md) |
| Is the database encrypted? | No — see [PERFORMANCE_RESPONSIVENESS_SECURITY.md](PERFORMANCE_RESPONSIVENESS_SECURITY.md) |

---

## When to Update Docs

Update the relevant doc in the same PR as the code change.

| Change | Update |
|--------|--------|
| Design principle or value | PHILOSOPHY.md |
| Directory structure or new files | PROJECT_STRUCTURE.md |
| Database schema, entity, or query | DATA_MODEL.md |
| Experiment state or transition | EXPERIMENT_LIFECYCLE.md |
| Reflection prompts, cadence, or triggers | REFLECTION_SYSTEM.md |
| Any screen layout or interaction | UI_DESIGN.md |
| Architectural pattern or DI setup | ARCHITECTURE.md |
| Contribution or review process | OPEN_SOURCE_GUIDE.md |
