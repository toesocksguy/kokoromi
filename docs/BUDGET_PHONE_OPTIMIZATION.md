# Budget Phone Optimization

## Target Device

- **RAM**: 2–3 GB (shared with OS + other apps; ~500–800 MB free in practice)
- **CPU**: ARM Cortex-A53/A55
- **Android**: API 26–28
- **Screen**: 720p IPS LCD
- **Examples**: Samsung A series, Xiaomi Redmi, Motorola G

---

## Performance Targets

| Metric | Target |
|--------|--------|
| App startup (cold) | < 2.5s |
| Home screen paint | < 1s |
| Check-in tap response | < 500ms |
| Archive scroll | 60 FPS |
| Memory — idle | < 35 MB |
| Memory — archive (100 items) | < 60 MB |
| Memory — absolute max | 80 MB |
| DB query (50 logs) | < 200ms |
| APK size | < 15 MB |
| Battery — light use/hour | < 5% |

---

## Key Principles

1. **LazyColumn for any list > 20 items** — never `Column` with `forEach`
2. **All DB and I/O on `Dispatchers.IO`** — nothing blocking on main thread
3. **Paginate queries** — `LIMIT`/`OFFSET`, never load full table
4. **Stream exports** — write to file incrementally, not one giant string in memory
5. **`remember {}` for computed values in Compose** — avoid recreating objects per recomposition
6. **Stable keys in `LazyColumn`** — `key = { it.id }` prevents unnecessary recomposition
7. **No polling** — reactive `Flow` queries; no `while(true) { delay() }` loops
8. **Cancel jobs in `onCleared()`** — no orphaned coroutines after ViewModel destruction

---

## Build Config

- `isMinifyEnabled = true` + `shrinkResources = true` on release
- ProGuard: keep Room entities/DAOs, Hilt classes, Compose
- Bundle splits: language + density (users only download what they need)
- ABI filter: `armeabi-v7a`, `arm64-v8a` (skip x86)

---

## Pre-Release Checklist

**Memory**
- [ ] Idle < 35 MB (Android Studio Profiler)
- [ ] Archive open < 60 MB (100 items)
- [ ] No leaks — GC recovers memory after navigating away

**CPU**
- [ ] Idle < 15% single core
- [ ] Archive scroll smooth at 60 FPS
- [ ] No sustained high CPU on budget device

**I/O**
- [ ] DB queries indexed (`experiment_id`, `date`)
- [ ] Archive uses pagination
- [ ] Export streams to file (no all-in-memory JSON build)
- [ ] APK < 15 MB

**Testing**
- [ ] Tested on physical budget phone or 2 GB RAM emulator (API 26–28)
- [ ] No ANR during export
- [ ] No jank scrolling archive

**Profiling commands**
```bash
adb shell dumpsys meminfo com.kokoromi | grep TOTAL
adb shell top -n 1 | grep com.kokoromi
```
