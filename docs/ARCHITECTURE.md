# Architecture

## Layer Diagram

```
┌──────────────────────────────────────────┐
│               UI Layer                   │
│  Compose screens + ViewModels            │
│  Observes StateFlow, emits via functions │
└──────────────────────┬───────────────────┘
                       │ calls use cases
                       ▼
┌──────────────────────────────────────────┐
│             Domain Layer                 │
│  Use cases — one business operation each │
│  Domain models — pure Kotlin, no Android │
│  Validation — pure functions, Result<T>  │
└──────────────────────┬───────────────────┘
                       │ reads/writes via interfaces
                       ▼
┌──────────────────────────────────────────┐
│              Data Layer                  │
│  Repository implementations              │
│  Room DAOs + entities                    │
│  DataStore for preferences               │
└──────────────────────────────────────────┘
```

Each layer only imports from the layer directly below. Data never calls domain or UI. Domain never imports Android framework classes.

---

## Patterns

**ViewModels**
- One per screen; single source of truth for screen state
- Expose `StateFlow<UiState>` — never raw mutable state to the UI
- Accept user actions as plain functions (no event sealed classes — too much ceremony for this app size)
- Call use cases only, never repositories directly
- No business logic

**Composables**
- Stateless: receive state + callbacks as parameters
- `*Screen.kt` — top-level, connected to ViewModel via `collectAsStateWithLifecycle()`
- `components/` — smaller Composables with no ViewModel dependency

**Use cases**
- One class, one `operator fun invoke()`
- Return `Result<T>` — success carries the value, failure carries the exception
- Pure Kotlin — no Android imports, trivially testable with fake repositories

**Repositories**
- Defined as interfaces; implementations in the data layer
- Accept and return domain models, never Room entities
- Entity ↔ domain mapping happens inside the implementation

**Concurrency**
- All use cases are `suspend`; called from `viewModelScope`
- Room handles IO dispatch internally for `Flow` and `suspend` DAO functions
- Rule: never block the main thread

**Error handling**
- Use cases return `Result<T>`; ViewModels map to `UiState.Error`
- No global error handler
- Expected errors (validation, empty state): caught and shown in UI
- Unexpected errors (DB corruption, OOM): not caught — crash and fix

---

## Testing Strategy

| Layer | Test type | How |
|-------|-----------|-----|
| Use cases + validators | JVM unit tests | Fake repositories (implement interface, in-memory list) |
| Repositories | Instrumented integration tests | In-memory Room DB |
| Compose screens | Instrumented UI tests | Fake ViewModel state passed directly to Composable |

**What we don't test**: Room SQL via mocks (use in-memory DB instead), Android framework internals, third-party library behavior.

---

## Architecture Decisions

**MVVM over MVI**: MVI adds sealed event classes and reducers that aren't needed at this app's size. `StateFlow` + sealed `UiState` gives unidirectional data flow with less boilerplate. Revisit if complex undo/redo or heavy async coordination is added.

**Use cases despite small app size**: Business logic (slot cap, validation, lifecycle transitions) lives only in use cases — not split across ViewModels and repositories. They're trivially unit-testable and keep ViewModels thin.

**Single module**: Multi-module (`:feature:home`, `:core:data`) is valuable for large teams. For a single-maintainer project, one module is correct. Refactor if the team grows or build times become a problem.

**Room over raw SQLite**: Compile-time SQL verification, coroutine/Flow support, type converters, migration framework. Worth the annotation processing overhead.

**Hilt over Koin or manual DI**: Compile-time verification and native ViewModel injection. Koin is simpler to set up but runtime-only. Manual DI doesn't scale past a few screens.

---

## Future Toolkit Tools: Architectural Notes

The Settings screen surfaces five Tiny Experiments toolkit tools as static informational links. Two have architectural implications if ever implemented as full features:

**Triple Check** — would add structured head/heart/hand fields to the Skip flow. Do not add these as columns on `DailyLog` — that requires a schema migration and couples unrelated concerns. Model as a separate entity linked to `DailyLog` via foreign key.

**Two-Step Reset** — could surface when a user pauses mid-experiment. Would link a Reset record to the PAUSED state transition, touching `UpdateExperimentStatusUseCase` and the schema. Keep the pause use case interface simple and extensible rather than tightly coupled to its current two-field signature.
