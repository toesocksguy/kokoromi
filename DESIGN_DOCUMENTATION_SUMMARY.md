# Kokoromi — Design Docs

## Documents

| File | What it covers |
|------|---------------|
| [README.md](README.md) | Vision, principles, stack, known issues, quick start |
| [PHILOSOPHY.md](docs/PHILOSOPHY.md) | North star, anti-patterns, tone, why behind decisions |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Layered MVVM, patterns, testing strategy |
| [DATA_MODEL.md](docs/DATA_MODEL.md) | Schema, entities, domain models, queries, migrations, export |
| [EXPERIMENT_LIFECYCLE.md](docs/EXPERIMENT_LIFECYCLE.md) | State machine, transitions, edge cases |
| [REFLECTION_SYSTEM.md](docs/REFLECTION_SYSTEM.md) | Plus-Minus-Next framework, weekly/daily/completion flows |
| [UI_DESIGN.md](docs/UI_DESIGN.md) | Screen specs, wireframes, design system, accessibility |
| [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | Directory layout, key classes, build/test commands |
| [MANIFEST.md](docs/MANIFEST.md) | Navigation guide — where to look for what |
| [OPEN_SOURCE_GUIDE.md](docs/OPEN_SOURCE_GUIDE.md) | Contributing guidelines |

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Action-first UX | Low friction builds habit |
| Offline-first | User owns their data, no dependency on connectivity |
| No notifications | Removes obligation and pressure |
| Show gaps in streaks | Data, not judgment |
| Max 2 active experiments | Prevents overcommitment |
| Weekly reflections | Plus-Minus-Next, optional |
| No gamification | Sustainable, authentic engagement |
| Privacy by default | Zero tracking, all data local |
