# Design Constraints

Budget phones are the baseline. If it runs well on 2–3 GB RAM / ARM Cortex-A53, it runs well everywhere. Performance targets and testing checklist: see [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md).

---

## Architecture (Locked In)

| Decision | Reason |
|----------|--------|
| MVVM + Coroutines | Lightweight, non-blocking |
| Jetpack Compose only — no XML layouts | Faster than inflate-based layouts |
| Material 3 tokens — no custom drawables | Predictable, hardware-accelerated |
| Hilt for all DI | Consistent, no manual graphs |
| Room + SQLite | No network dependency |
| Kotlin Coroutines — no RxJava, no raw threads | Coroutines ~1 KB; threads ~1 MB |

**All DB/IO must run on `Dispatchers.IO`. Nothing blocking on main thread.**

---

## UI Constraints

- **Touch targets**: 48 × 48 dp minimum on all interactive elements (WCAG AA)
- **Dark mode**: must respect system setting — no forced light mode
- **Reduced motion**: animations must respect `prefers-reduced-motion`
- **Animations**: crossfade and `AnimatedVisibility` only; no infinite custom canvas animations, no parallax
- **Lists > 20 items**: `LazyColumn` with stable keys — never `Column` + `forEach`

---

## Forbidden Dependencies

Never add:
- Firebase Analytics, Crashlytics, Performance Monitoring
- AdMob or any ad network
- Google Play Services beyond Core
- RxJava
- Heavy ML libraries (TensorFlow, PyTorch)
- Glide / Picasso (no image loading in v1)
- Any dependency that sends data off-device without explicit user action

---

## Permissions

v1 requests **zero permissions**. Local Room storage requires none on modern Android.

Do not add:
- `INTERNET` (not needed until cloud sync, which is not in v1)
- `CAMERA`, `LOCATION`, `READ_CONTACTS`, or any sensor permission

---

## Data

- Exports must stream to file — never load entire DB into memory
- Queries on `daily_logs` and `reflections` must be indexed on `experiment_id`
- Pagination required for any list that could exceed 50 items

---

## Release Gate

Cannot ship without:
- [ ] Memory: idle < 35 MB, archive < 60 MB (Profiler)
- [ ] Cold start < 2.5s
- [ ] Archive scroll 60 FPS
- [ ] APK < 15 MB
- [ ] ProGuard minification enabled
- [ ] No `INTERNET` or other unnecessary permissions
- [ ] Lint: zero warnings
- [ ] Unit tests pass
- [ ] Tested on physical budget device or 2 GB RAM emulator
