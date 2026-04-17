# Security

Performance and responsiveness constraints are in [BUDGET_PHONE_OPTIMIZATION.md](BUDGET_PHONE_OPTIMIZATION.md) and [DESIGN_CONSTRAINTS.md](DESIGN_CONSTRAINTS.md).

---

## What's Implemented

| Control | Status | Notes |
|---------|--------|-------|
| No INTERNET permission | ✅ | v1 is fully offline |
| No analytics / telemetry SDKs | ✅ | Zero third-party data collection |
| No ad networks | ✅ | No AdMob, Facebook AN, etc. |
| Local-only storage | ✅ | SQLite on device; no cloud |
| FileProvider for export sharing | ✅ | Content URIs, not `file://` URIs |
| ProGuard minification (release) | ✅ | `isMinifyEnabled = true` |
| GPL-3.0 open source | ✅ | Community-auditable |

---

## Known Gaps

**Database not encrypted at rest.**
SQLCipher is not in the dependency tree. The Room database is plain SQLite. On a rooted device or via ADB backup, experiment data is readable.
- Fix: add `net.zetetic:android-database-sqlcipher` + `androidx.security:security-crypto`
- Key management: derive from Android KeyStore (not from user password)

**Input validators don't block malicious patterns.**
`ExperimentValidator` enforces length and blank checks, but does not strip or reject SQL-injection-style strings or script tags. This is low risk for a local-only app with parameterized Room queries, but worth noting if network features are added.

**Export file is plaintext.**
JSON export is unencrypted. It's written to `context.filesDir` (app-private) and shared via FileProvider — both correct. But the file itself is not encrypted, so a user who shares it via the system share sheet exposes their data in plaintext.

---

## Release Security Checklist

- [ ] No hardcoded secrets, API keys, or credentials in code
- [ ] `INTERNET` permission absent from manifest
- [ ] No analytics/tracking SDK in `build.gradle.kts`
- [ ] ProGuard minification passes without stripping Room/Hilt
- [ ] Export shared via FileProvider (not raw `file://` URI) — verify in test
- [ ] `./gradlew dependencyCheckAnalyze` passes (no CVSS ≥ 7.0)
- [ ] DB encryption gap acknowledged / addressed before any cloud or backup feature is added
