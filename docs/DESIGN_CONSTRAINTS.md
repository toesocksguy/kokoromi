# Design Constraints: Budget Phone First

## Philosophy

**Budget phones are the baseline. Not an afterthought.**

If the app runs smoothly on a 2-3GB RAM budget phone with a slow CPU, it will run exceptionally well on flagship devices. The opposite is not true.

All design decisions must satisfy:
- **Target Device**: Budget Android (2-3GB RAM, ARM Cortex-A53/A55, API 26-28)
- **Performance Baseline**: Must perform acceptably on this device
- **Memory Ceiling**: Absolute maximum 35 MB baseline, 60 MB with data
- **CPU Ceiling**: No sustained > 50% single-core usage during normal use
- **APK Size Ceiling**: < 15 MB (split by language/density)
- **Startup Ceiling**: < 2.5 seconds cold start

These are **hard constraints**, not guidelines.

---

## 1. Architecture Constraints

### 1.1 MVVM + Coroutines (Locked In)

**Why**: Lightweight, non-blocking, efficient.

**Constraint**: All database operations MUST run on `Dispatchers.IO`.
```kotlin
// REQUIRED
suspend fun save(log: DailyLog) = withContext(Dispatchers.IO) {
    repository.save(log)
}

// FORBIDDEN
fun save(log: DailyLog) {
    repository.save(log)  // ❌ BLOCKS MAIN THREAD
}
```

### 1.2 No Heavy Frameworks

**Constraint**: No Firebase Analytics, Crashlytics, or other Google Play Services SDKs.
- Each adds 5-10 MB to APK size
- Each adds startup overhead
- Use local logging instead (write to file, clear when > 1 MB)

**Allowed**:
- ✅ Google Play Services Core (if absolutely necessary for future features)
- ✅ Firebase Realtime Database (if v2 adds cloud sync, and only with end-to-end encryption)

**Forbidden**:
- ❌ Firebase Analytics
- ❌ Crashlytics
- ❌ AdMob or any ad network
- ❌ Third-party crash reporting
- ❌ Heavy machine learning libraries

### 1.3 Dependency Injection (Hilt Only)

**Why**: Lightweight DI without reflection overhead.

**Constraint**: No manual dependency graph. Hilt handles all injection.

```kotlin
// ✅ REQUIRED
@HiltViewModel
class ExperimentViewModel @Inject constructor(
    private val repository: ExperimentRepository
) : ViewModel()

// ❌ FORBIDDEN
class ExperimentViewModel(
    private val repository: ExperimentRepository  // Manual injection = slower init
)
```

### 1.4 Kotlin Coroutines Only

**Constraint**: No RxJava, no callbacks, no threads.

**Why**: Coroutines are lightweight (< 1KB per coroutine vs threads at 1MB+).

```kotlin
// ✅ REQUIRED
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) { repository.load() }
    updateUI(data)
}

// ❌ FORBIDDEN (RxJava)
repository.load()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe { data -> updateUI(data) }
```

---

## 2. UI/UX Constraints

### 2.1 Jetpack Compose Only (No XML)

**Why**: XML layouts require parsing and inflation on every screen open. Compose is code-based = faster.

**Constraint**: Zero XML layouts. All UI in Compose.

```kotlin
// ✅ REQUIRED
@Composable
fun HomeScreen() {
    Column {
        Text("Experiments")
    }
}

// ❌ FORBIDDEN
// res/layout/home_screen.xml
// Then: setContentView(R.layout.home_screen)
```

### 2.2 Material 3 Design System (Locked In)

**Why**: System design patterns = predictable, fast, minimal custom drawing.

**Constraint**: 
- Use Material 3 tokens for colors, typography, spacing
- No custom Drawables (too slow to render)
- No custom shaders or complex Canvas drawing
- No rounded corner Drawables (use Shape modifiers)

```kotlin
// ✅ REQUIRED: Use Material 3 tokens
val colors = MaterialTheme.colorScheme
Card(
    modifier = Modifier.size(100.dp),
    shape = RoundedCornerShape(8.dp)
)

// ❌ FORBIDDEN: Custom drawable
<shape>
    <corners android:radius="8dp"/>
</shape>
// Too slow to render at scale
```

### 2.3 Minimal Animations

**Constraint**: Animations only for state transitions, not for every interaction.

**Allowed**:
- ✅ Crossfade (switching between two states)
- ✅ AnimatedVisibility (enter/exit)
- ✅ System progress bars (hardware-accelerated)

**Forbidden**:
- ❌ Infinite rotating spinners (use system CircularProgressIndicator)
- ❌ Custom canvas animations
- ❌ Parallax effects
- ❌ Complex gesture animations

```kotlin
// ✅ REQUIRED: Simple fade
@Composable
fun ScreenContent(showLoading: Boolean) {
    AnimatedContent(showLoading) { loading ->
        if (loading) LoadingScreen() else DataScreen()
    }
}

// ❌ FORBIDDEN: Expensive rotation animation
val rotation = rememberInfiniteTransition()
val angle by rotation.animateFloat(0f, 360f) { ... }
Canvas(...) { rotate(angle) { ... } }
```

### 2.4 Touch Targets: 48dp Minimum (Hard Constraint)

**Why**: Accessibility + error prevention on small screens.

**Constraint**: Every interactive element MUST be at least 48x48 dp (WCAG AAA).

```kotlin
// ✅ REQUIRED
Button(
    onClick = { },
    modifier = Modifier.size(48.dp.plus(padding))  // Minimum 48dp
)

// ❌ FORBIDDEN
Button(
    onClick = { },
    modifier = Modifier.size(32.dp)  // Too small
)
```

### 2.5 Dark Mode Support (Required)

**Constraint**: App MUST respect system dark mode preference. No forced light mode.

```kotlin
// ✅ REQUIRED: Theme adapts to system
val colors = if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme
MaterialTheme(colorScheme = colors) { ... }

// ❌ FORBIDDEN: Hardcoded light mode
MaterialTheme(colorScheme = lightColorScheme) { ... }
```

### 2.6 Reduced Motion Support (Required)

**Constraint**: Animations MUST respect `prefers-reduced-motion` system setting.

```kotlin
// ✅ REQUIRED: Check system setting
val motionDurationMs = if (isSystemMotionAnimationEnabled) 300 else 0
AnimatedContent(
    targetState = state,
    transitionSpec = {
        fadeIn(animationSpec = tween(motionDurationMs)) with
        fadeOut(animationSpec = tween(motionDurationMs))
    }
)
```

---

## 3. Performance Constraints

### 3.1 Memory Budgets (Hard Limits)

```
Absolute maximums (will crash or be killed by system):

Baseline (idle, no data):      35 MB
Home Screen (2 experiments):   40 MB
Archive (100 items):          60 MB
Archive (500 items):          80 MB (absolute max, should paginate)
Exporting data:               40 MB (stream, don't load all)
System buffer:                20 MB (headroom to prevent crashes)

NEVER exceed 100 MB on a 2GB device (system kills app).
```

**Enforcement**:
- Profile on real budget device or emulator with 2GB RAM limit
- Use Android Studio Memory Profiler before each release
- Fail CI/CD if baseline > 40 MB

### 3.2 Startup Time (Hard Limit)

```
Cold Start:             < 2.5 seconds (measured: launch to home interactive)
Warm Start:             < 1.5 seconds (app in memory, relaunched)
Home Screen Paint:      < 1 second (first interactive screen)
Check-in Screen Open:   < 500 ms (tap [✓] → screen visible)
Archive List Scroll:    60 FPS (smooth, no jank)
```

**Measurement**:
```bash
# Cold start (clear app data first)
adb shell cmd package clear com.kokoromi
adb shell am start-activity -S -W com.kokoromi/.MainActivity

# Look for "This method took X ms" in logcat
```

### 3.3 CPU Usage (Hard Limit)

```
Idle (home screen, no scroll):
  Single core utilization:   < 15%
  All cores:                 < 5%

Scrolling Archive:
  Single core utilization:   < 50%
  Maintain 60 FPS (frametime < 16.67ms)

App Startup:
  Peak (first 500ms):        < 80% (ok to spike briefly)
  Sustained (after 1s):      < 30%
```

**Forbidden CPU patterns**:
- ❌ Infinite loops on main thread
- ❌ Polling (checking for updates in a while loop)
- ❌ Blocking I/O on main thread
- ❌ Continuous animations while idle

### 3.4 Database Query Time (Hard Limit)

```
Single query (< 100 items):    < 200 ms
Batch insert (100 items):      < 500 ms
Full export (500+ items):      < 2 seconds (streamed, not loaded)
Complex join (rare):           < 1 second
```

**Constraint**: All queries MUST have indexes on commonly-filtered columns.

```kotlin
// ✅ REQUIRED: Index on experiment_id and date
database.execSQL("""
    CREATE INDEX IF NOT EXISTS idx_daily_logs_exp_date 
    ON daily_logs(experiment_id, date DESC)
""")

// ❌ FORBIDDEN: Full table scan
@Query("SELECT * FROM daily_logs ORDER BY date DESC")
suspend fun getAllLogs(): List<DailyLogEntity>  // No WHERE clause, no index
```

### 3.5 List Rendering (Hard Constraint)

**Constraint**: Lists > 20 items MUST use LazyColumn, never Column.

```kotlin
// ✅ REQUIRED: LazyColumn for archive
@Composable
fun ArchiveScreen(experiments: List<Experiment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = experiments,
            key = { it.id }  // STABLE KEY - prevents unnecessary recomposition
        ) { experiment ->
            ExperimentCard(experiment)
        }
    }
}

// ❌ FORBIDDEN: Column renders all items at once
@Composable
fun ArchiveScreen(experiments: List<Experiment>) {
    Column {
        experiments.forEach { experiment ->
            ExperimentCard(experiment)  // ALL items rendered, ALL in memory
        }
    }
}
```

### 3.6 Pagination (Hard Constraint for Large Datasets)

**Constraint**: If loading > 50 items, implement pagination.

```kotlin
// ✅ REQUIRED: Paginate archive
@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel) {
    var offset by remember { mutableIntStateOf(0) }
    val pageSize = 30
    val experiments by viewModel.getExperimentsPaginated(offset, pageSize).collectAsState(emptyList())
    
    LazyColumn {
        items(experiments) { exp ->
            // Load more when near bottom
            if (it == experiments.size - 5) {
                LaunchedEffect(Unit) {
                    offset += pageSize
                }
            }
            ExperimentCard(exp)
        }
    }
}

// DAO Query:
@Query("""
    SELECT * FROM experiments 
    ORDER BY created_at DESC
    LIMIT :pageSize OFFSET :offset
""")
suspend fun getExperimentsPaginated(pageSize: Int, offset: Int): List<ExperimentEntity>
```

### 3.7 APK Size (Hard Limit)

```
Target:                         < 12 MB
Absolute maximum:               < 15 MB (will fail CI/CD)

Includes:
- App code (Kotlin compiled):   ~3 MB
- Resources (strings, dimens):  ~1 MB
- Native libs (ARM 32+64):      ~2 MB (if included)
- Compose runtime:              ~2 MB
- Room + SQLite:                ~1.5 MB
```

**Constraint**: ProGuard minification REQUIRED in release build.

```gradle
android {
    buildTypes {
        release {
            isMinifyEnabled = true  // REQUIRED
            shrinkResources = true  // REQUIRED
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## 4. Data & Storage Constraints

### 4.1 Database Size (Hard Limit)

```
Per-user data limit:    < 50 MB
  - 500 experiments:    ~5 MB
  - 10k daily logs:     ~15 MB
  - 500 reflections:    ~5 MB
  - Other metadata:     ~1 MB

On 32GB device with ~5GB free: still acceptable.
On device with < 500MB free: app may not launch.
```

**Constraint**: Implement cleanup for old data (> 1 year old).

```kotlin
class DataCleanupWorker : Worker() {
    override suspend fun doWork(): Result {
        return try {
            val oneYearAgo = LocalDate.now().minusYears(1)
            experimentRepository.deleteCompletedBefore(oneYearAgo)
            Result.success()
        } catch (e: Exception) {
            Result.retry()  // System will retry later
        }
    }
}
```

### 4.2 Export Format (Hard Constraint)

**Constraint**: Exports MUST use streaming, not load entire database into memory.

```kotlin
// ✅ REQUIRED: Stream to file
suspend fun exportData(file: File) = withContext(Dispatchers.IO) {
    file.bufferedWriter().use { writer ->
        writer.write("[")
        var first = true
        
        // Stream items one page at a time
        var offset = 0
        while (true) {
            val items = repository.getExperimentsPaginated(pageSize = 100, offset = offset)
            if (items.isEmpty()) break
            
            items.forEach { item ->
                if (!first) writer.write(",")
                writer.write(gson.toJson(item))
                first = false
            }
            offset += 100
        }
        
        writer.write("]")
    }
}

// ❌ FORBIDDEN: Load all into memory
suspend fun exportData(): String {
    val experiments = repository.getAll()  // ALL in memory
    val logs = logRepository.getAll()      // ALL in memory
    val json = gson.toJson(mapOf("experiments" to experiments, "logs" to logs))
    return json
}
```

### 4.3 Encryption (Hard Constraint for Security)

**Constraint**: All sensitive data encrypted at rest using SQLCipher.

```kotlin
// ✅ REQUIRED: Encrypted database
val database = Room.databaseBuilder(
    context,
    KokoromiDatabase::class.java,
    "experiments.db"
)
.openHelperFactory(
    SQLCipherOpenHelperFactory(SupportFactory("user_password".toByteArray()))
)
.build()

// ✅ REQUIRED: Encrypted shared preferences for auth tokens (if added later)
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "encrypted_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## 5. Network Constraints (Future Cloud Sync)

### 5.1 No Polling (Hard Constraint)

**Constraint**: If cloud sync is added, use WorkManager, NOT polling loops.

```kotlin
// ❌ FORBIDDEN: Polling
viewModelScope.launch {
    while (isActive) {
        syncWithCloud()
        delay(60000)  // Every minute = massive battery drain
    }
}

// ✅ REQUIRED: WorkManager
class SyncWorker : Worker() {
    override suspend fun doWork(): Result {
        return try {
            syncWithCloud()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// In Application:
fun scheduleSync(context: Context) {
    val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
        Duration.ofHours(1)  // System decides when to run
    ).build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "cloud_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        syncWork
    )
}
```

### 5.2 TLS 1.2+ Only (Hard Constraint)

**Constraint**: All network requests over TLS 1.2 or higher.

```kotlin
// ✅ REQUIRED: Network security config
// res/xml/network_security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">api.example.com</domain>
        <pin-set expiration="2027-12-31">
            <pin digest="SHA-256">...</pin>
        </pin-set>
    </domain-config>
</network-security-config>

// In OkHttp client:
val client = OkHttpClient.Builder()
    .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
    .build()
```

---

## 6. Testing Constraints (Hard Requirements)

### 6.1 Testing Devices

**Constraint**: Before any release, test on:
1. ✅ Real budget phone (2-3GB RAM, API 26-28)
2. ✅ Android Emulator configured with 2GB RAM max
3. ✅ Android Studio Profiler (memory, CPU, frame rate)

**Forbidden**:
- ❌ Flagship-only testing
- ❌ Skipping profiler checks
- ❌ Releasing without testing on budget device

### 6.2 Performance Profiling (Before Every Release)

**Constraint**: Must pass all checks before shipping.

```bash
# 1. Memory profiling
adb shell dumpsys meminfo com.kokoromi | grep TOTAL
# Must show: baseline < 35 MB, archive < 60 MB

# 2. Cold startup
adb shell cmd package clear com.kokoromi
adb shell am start -W com.kokoromi/.MainActivity
# Must be < 2.5 seconds

# 3. Frame rate during scroll
adb shell dumpsys gfxinfo com.kokoromi reset
# Scroll for 5 seconds, then:
adb shell dumpsys gfxinfo com.kokoromi
# Must show 60 FPS (frame drops acceptable if < 5%)

# 4. CPU usage
adb shell top -n 1 | grep com.kokoromi
# Idle: < 15%, Scroll: < 50%

# 5. APK size
./gradlew assembleRelease
ls -lh app/build/outputs/apk/release/app-release.apk
# Must be < 15 MB
```

### 6.3 CI/CD Checks (Automated)

**Constraint**: These checks MUST pass before merging to main:

```gradle
// build.gradle.kts
tasks.register("preReleaseChecks") {
    doLast {
        // 1. Check APK size
        val apkFile = file("app/build/outputs/apk/release/app-release.apk")
        val sizeMb = apkFile.length() / (1024.0 * 1024.0)
        require(sizeMb < 15) { "APK too large: ${sizeMb}MB" }
        
        // 2. Check dependency vulnerabilities
        // ./gradlew dependencyCheckAnalyze
        
        // 3. Check for memory leaks in tests
        // ./gradlew connectedAndroidTest
    }
}

// GitHub Actions: .github/workflows/pre-release.yml
name: Pre-Release Checks
on: [pull_request]
jobs:
  checks:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Check APK size
        run: ./gradlew preReleaseChecks
```

---

## 7. Dependencies Constraint (Hard Limits)

### 7.1 Allowed Dependencies

```kotlin
// Core (required)
implementation("androidx.core:core:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")

// Compose (required)
implementation("androidx.compose.ui:ui:1.6.0")
implementation("androidx.compose.material3:material3:1.1.0")
implementation("androidx.navigation:navigation-compose:2.7.0")

// Database (required)
implementation("androidx.room:room-runtime:2.6.0")
implementation("androidx.room:room-ktx:2.6.0")

// Coroutines (required)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// DI (required)
implementation("com.google.dagger:hilt-android:2.51")

// Encryption (required)
implementation("androidx.security:security-crypto:1.1.0-alpha06")
implementation("net.zetetic:android-database-sqlcipher:4.5.4")

// Optional (future features)
// implementation("com.squareup.okhttp3:okhttp:4.11.0")  // For cloud sync
// implementation("com.google.code.gson:gson:2.10.1")     // For JSON export
```

### 7.2 Forbidden Dependencies

```kotlin
// ❌ NEVER add:
// Firebase Analytics, Crashlytics, Performance Monitoring
// Google Play Services (except Core, if necessary)
// AdMob or any ad network
// Heavy ML libraries (TensorFlow, PyTorch)
// RxJava (use Coroutines instead)
// Glide, Picasso (no image loading in v1)
// Retrofit (use OkHttp + manual JSON parsing)
// Moshi (use Gson, lighter)
// Hilt Compiler is ok, but never add other Hilt modules unnecessarily
```

### 7.3 Dependency Scanning (Automated)

**Constraint**: Check for known CVEs before each release.

```gradle
plugins {
    id("org.owasp.dependencycheck") version "8.1.0"
}

dependencyCheck {
    failBuildOnCVSS = 7.0f  // Fail on critical vulnerabilities
}

// Before release:
// ./gradlew dependencyCheckAnalyze
```

---

## 8. Android Manifest Constraints (Hard Requirements)

### 8.1 Permissions (Minimal)

**Constraint**: Request only absolutely necessary permissions.

```xml
<!-- AndroidManifest.xml -->

<!-- ✅ ALLOWED: Essential for app function -->
<!-- (None! Local storage doesn't require explicit permission on modern Android) -->

<!-- ❌ NEVER REQUEST: -->
<!-- <uses-permission android:name="android.permission.INTERNET" /> -->
<!-- (Not needed for v1; add only if cloud sync is added) -->

<!-- <uses-permission android:name="android.permission.CAMERA" /> -->
<!-- <uses-permission android:name="android.permission.LOCATION" /> -->
<!-- <uses-permission android:name="android.permission.READ_CONTACTS" /> -->
<!-- <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> -->
```

### 8.2 Features (Declare Truthfully)

**Constraint**: Only declare required features; don't declare optional features.

```xml
<!-- ✅ REQUIRED: App uses built-in features -->
<uses-feature android:name="android.hardware.screen.portrait" />

<!-- ❌ FORBIDDEN: Don't falsely declare optional features -->
<!-- <uses-feature android:name="android.hardware.camera" required="false" /> -->
<!-- (If not using it, don't declare it) -->
```

---

## 9. Code Quality Constraints

### 9.1 Null Safety (Hard Constraint)

**Constraint**: No nullable types unless absolutely necessary. Use `!!` operator only in tests.

```kotlin
// ✅ REQUIRED: Non-nullable
data class Experiment(
    val id: String,      // Non-null
    val hypothesis: String,  // Non-null
    val why: String? = null  // Explicitly optional
)

// ❌ FORBIDDEN: Excessive nullability
data class Experiment(
    val id: String?,
    val hypothesis: String?,
    val action: String?
)
// Too many null checks, harder to reason about
```

### 9.2 Immutability (Hard Constraint)

**Constraint**: Use `val` by default, `var` only when necessary.

```kotlin
// ✅ REQUIRED: Immutable data classes
data class DailyLog(
    val id: String,
    val date: LocalDate,
    val completed: Boolean
)

// ❌ FORBIDDEN: Mutable data classes
data class DailyLog(
    var id: String,     // Can be changed—danger!
    var date: LocalDate,
    var completed: Boolean
)
```

### 9.3 Type Safety (Hard Constraint)

**Constraint**: No `Any`, no unchecked casts, no raw types.

```kotlin
// ✅ REQUIRED: Strongly typed
fun save(experiment: Experiment): String { ... }
val result: String = save(exp)

// ❌ FORBIDDEN: Weak typing
fun save(anything: Any): Any { ... }
val result = save(exp) as String  // Unchecked cast
```

---

## 10. Release Checklist (Hard Requirements)

**Constraint**: Cannot ship without passing all checks.

- [ ] **Memory**: Baseline < 35 MB, archive < 60 MB (Android Studio Profiler)
- [ ] **Startup**: Cold start < 2.5s, warm start < 1.5s (adb measurement)
- [ ] **CPU**: Idle < 15% single core, scroll < 50% (top command)
- [ ] **Frames**: Archive scroll 60 FPS (gfxinfo, frame drops < 5%)
- [ ] **APK**: < 15 MB (ls -lh app-release.apk)
- [ ] **Minification**: ProGuard enabled, tested
- [ ] **Security**: SQLCipher enabled, no hardcoded secrets
- [ ] **Permissions**: Only necessary permissions requested
- [ ] **Lint**: Zero warnings (./gradlew lint)
- [ ] **Tests**: Unit tests pass, no memory leaks (connectedAndroidTest)
- [ ] **Real device**: Tested on physical 2-3GB RAM budget phone
- [ ] **Dependencies**: No known CVEs (dependencyCheckAnalyze)
- [ ] **Code review**: Approved, no performance regressions

---

## Summary: Constraints as Strategy

This is not a limitation—it's a **feature**. Designing with budget phone constraints:

| Benefit | Impact |
|---------|--------|
| **Works everywhere** | Same performance on budget and flagship |
| **Simpler design** | No complex features to optimize later |
| **Faster development** | Fewer performance bugs to fix |
| **Better UX** | Responsive on all devices |
| **Smaller APK** | Wider audience, lower bounce rate |
| **Lower barrier** | Accessible to more users globally |
| **Efficient code** | Fast load, fast interaction, low battery drain |

**Budget phones are not a compromise. They're the design baseline.**

When every user—regardless of device—gets a smooth, responsive, fast app, you've succeeded.

---

**Version**: 1.0  
**Status**: Design Constraints (Non-Negotiable)  
**Applies to**: All design decisions across architecture, UI, performance, security, and testing
