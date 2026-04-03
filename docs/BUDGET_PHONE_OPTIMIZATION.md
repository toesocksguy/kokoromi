# Budget Phone Optimization Guide

## Target Device Profile

**Budget Phone Baseline**:
- **Device**: Typical budget Android (e.g., Samsung A series, Xiaomi Redmi, Motorola G)
- **RAM**: 2-3 GB (shared with OS, other apps)
- **Processor**: ARM Cortex-A53 or A55 (slower than flagship)
- **Storage**: 32-64 GB (may have < 5GB free)
- **Android Version**: API 26-28 (Android 8.0-9.0)
- **Screen**: 5.5" 720p IPS LCD (not AMOLED)
- **Battery**: Modest capacity, slow charging

**Real-world scenario**: User has WhatsApp, YouTube, 3-4 social media apps open. Free RAM: ~500-800 MB.

---

## Part 1: Memory Management

### 1.1 Memory Targets for Budget Phones

```
✅ TARGET MEMORY USAGE:

Baseline (app just launched):
- Target: 25-30 MB
- Budget phone constraint: Keep < 35 MB (avoid system kill)

Home Screen (with 2 active experiments):
- Target: 35-40 MB
- Budget phone constraint: Keep < 50 MB

Archive Screen (100 experiments loaded):
- Target: 50-60 MB
- Budget phone constraint: Absolutely max 80 MB (system will kill at ~150 MB)

Under memory pressure:
- App should gracefully reduce cache
- Archive should drop low-priority cache (images if added later)
- Never crash due to OOM
```

### 1.2 Memory Leaks Prevention

**Add to PROJECT_STRUCTURE.md / ARCHITECTURE.md**:

```kotlin
// ❌ MEMORY LEAK: Holding Activity reference
class ExperimentViewModel : ViewModel() {
    lateinit var activity: Activity  // LEAK! Activity can't be garbage collected
}

// ✅ CORRECT: Use context from ApplicationScope
class ExperimentViewModel(
    private val application: Application  // Never leaks; app lifecycle
) : ViewModel() {
    // Use application context for any system calls
}

// ❌ MEMORY LEAK: Holding View reference after destroy
class HomeViewModel : ViewModel() {
    private var view: View? = null  // Will leak if not cleared in onDestroy
}

// ✅ CORRECT: Never hold View references in ViewModel
class HomeViewModel : ViewModel() {
    // ViewModels should not hold View references at all
    // Use LiveData/StateFlow instead
}

// ❌ MEMORY LEAK: Unbounded cache
class ExperimentRepository {
    private val cache = mutableMapOf<String, Experiment>()  // Grows forever
}

// ✅ CORRECT: LruCache with bounded size
class ExperimentRepository {
    private val cache = LruCache<String, Experiment>(maxSize = 50)
    // Automatically evicts old items when full
}
```

### 1.3 Compose Memory Optimization

**Issue**: Compose can recompose frequently, creating temporary objects.

```kotlin
// ❌ INEFFICIENT: Creates new object on every recomposition
@Composable
fun ExperimentCard(experiment: Experiment) {
    val gradient = Brush.linearGradient(listOf(Color.Blue, Color.Purple))
    // ❌ 'gradient' recreated on every recomposition
    
    Card(modifier = Modifier.background(gradient)) {
        Text(experiment.hypothesis)
    }
}

// ✅ EFFICIENT: Create once, reuse
@Composable
fun ExperimentCard(experiment: Experiment) {
    val gradient = remember {
        Brush.linearGradient(listOf(Color.Blue, Color.Purple))
    }
    // ✅ gradient created once, reused
    
    Card(modifier = Modifier.background(gradient)) {
        Text(experiment.hypothesis)
    }
}

// ❌ INEFFICIENT: Large list renders all items
@Composable
fun ArchiveScreen(experiments: List<Experiment>) {
    Column {
        experiments.forEach { exp ->
            ExperimentCard(exp)  // ALL items rendered, ALL in memory
        }
    }
}

// ✅ EFFICIENT: Only render visible items
@Composable
fun ArchiveScreen(experiments: List<Experiment>) {
    LazyColumn {
        items(
            items = experiments,
            key = { it.id }  // Stable key prevents unnecessary recomposition
        ) { exp ->
            ExperimentCard(exp)  // Only ~8 items rendered (one screen)
        }
    }
}
```

### 1.4 Database Memory Optimization

```kotlin
// ❌ INEFFICIENT: Loads all 500 logs into memory
suspend fun getAllLogs(experimentId: String): List<DailyLog> {
    return dailyLogDao.getAll(experimentId)  // 500 items, ~2-3 MB
}

// ✅ EFFICIENT: Load paginated chunks
suspend fun getLogsPaginated(
    experimentId: String,
    pageSize: Int = 30,
    offset: Int = 0
): List<DailyLog> {
    return dailyLogDao.getLogsPaginated(
        experimentId,
        pageSize,
        offset
    )
}

// DAO Query:
@Dao
interface DailyLogDao {
    @Query("""
        SELECT * FROM daily_logs 
        WHERE experiment_id = :experimentId 
        ORDER BY date DESC
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun getLogsPaginated(
        experimentId: String,
        pageSize: Int,
        offset: Int
    ): List<DailyLogEntity>
}

// Usage in Archive Screen:
@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel) {
    var offset by remember { mutableIntStateOf(0) }
    val pageSize = 30
    
    LazyColumn {
        items(100) { index ->
            if (index == experiments.size - 5) {  // Load next page near bottom
                LaunchedEffect(Unit) {
                    viewModel.loadMoreExperiments()
                }
            }
            ExperimentCard(experiments[index])
        }
    }
}
```

### 1.5 Bitmap/Image Memory (Future-Proofing)

**Currently no images, but for future features**:

```kotlin
// ❌ INEFFICIENT: Full-size image loaded
val bitmap = BitmapFactory.decodeFile(imagePath)  // Could be 5-10 MB

// ✅ EFFICIENT: Scale on load
fun decodeSampledBitmapFromFile(
    filePath: String,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, this)
        
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
        inPreferredConfig = Bitmap.Config.RGB_565  // Not ARGB_8888
    }
    return BitmapFactory.decodeFile(filePath, options)
}

// If caching images:
val imageCache = LruCache<String, Bitmap>(
    maxSizeBytes = 5 * 1024 * 1024  // 5 MB max
) { bitmap ->
    bitmap.allocationByteCount  // Count real memory, not just object size
}
```

---

## Part 2: CPU & Thermal Performance

### 2.1 CPU Usage Targets

```
✅ TARGET CPU USAGE (budget phone):

Idle (home screen, no scroll):
- Single core: < 15% utilization
- Never max out one core (thermal throttling)

Scrolling Archive (smooth 60 FPS):
- Single core: < 50% (headroom for OS)
- Multiple cores: distributed load

App startup:
- Peak: < 80% (brief spike ok)
- Sustained (after 1s): < 30%

Database query + render:
- Peak: < 70%
- Duration: < 300ms
```

### 2.2 Coroutine Dispatchers (Critical for Budget Phones)

**Budget phones have fewer cores (4-8) vs flagships (8+). Use Dispatchers correctly.**

```kotlin
// ❌ WRONG: Blocking main thread
class CheckInViewModel : ViewModel() {
    fun logCheckIn(experimentId: String, completed: Boolean) {
        val log = DailyLog(...)
        repository.save(log)  // BLOCKS! Database I/O on main thread
        // UI will freeze for 100-200ms
    }
}

// ✅ CORRECT: Off main thread
class CheckInViewModel : ViewModel() {
    fun logCheckIn(experimentId: String, completed: Boolean) {
        viewModelScope.launch {
            // 1. Calculate on computation pool
            val log = withContext(Dispatchers.Default) {
                DailyLog(
                    id = UUID.randomUUID().toString(),
                    experimentId = experimentId,
                    completed = completed,
                    loggedAt = Instant.now()
                )
            }
            
            // 2. I/O on I/O dispatcher (database thread pool)
            withContext(Dispatchers.IO) {
                repository.save(log)
            }
            
            // 3. Update UI (back to main)
            // Launch { } already on main, no need for withContext
            showSuccessToast()
        }
    }
}

// ❌ WRONG: Too many coroutines at once
class ArchiveViewModel : ViewModel() {
    fun loadAllExperiments() {
        repeat(500) { index ->
            viewModelScope.launch {  // 500 concurrent coroutines! 🔥
                loadExperiment(index)
            }
        }
    }
}

// ✅ CORRECT: Batch or paginate
class ArchiveViewModel : ViewModel() {
    fun loadExperimentsInBatches() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                experiments.addAll(
                    repository.getExperimentsBatched(
                        batchSize = 50,
                        batchNumber = 0
                    )
                )
            }
        }
    }
}
```

### 2.3 Job Cancellation (Prevent Wasted CPU)

```kotlin
// ❌ INEFFICIENT: Job keeps running after user navigates away
class ExperimentDetailViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            while (true) {  // Background loop
                loadExperimentStats()
                delay(5000)
            }
        }
    }
}

// ✅ EFFICIENT: Cancel job when not needed
class ExperimentDetailViewModel : ViewModel() {
    private var statsRefreshJob: Job? = null
    
    fun startStatsRefresh() {
        statsRefreshJob?.cancel()  // Cancel any existing
        statsRefreshJob = viewModelScope.launch {
            while (isActive) {  // Will stop when job cancelled
                loadExperimentStats()
                delay(5000)
            }
        }
    }
    
    override fun onCleared() {
        statsRefreshJob?.cancel()  // Clean up when ViewModel destroyed
        super.onCleared()
    }
}
```

### 2.4 Animation Performance (Budget Phone Killer)

```kotlin
// ❌ EXPENSIVE: Complex animation
@Composable
fun LoadingSpinner() {
    val rotation = rememberInfiniteTransition()
    val rotationDegrees by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    
    // Every frame of animation causes recomposition
    Canvas(modifier = Modifier.size(100.dp)) {
        rotate(rotationDegrees) {
            drawCircle(Color.Blue)
        }
    }
}

// ✅ SIMPLER: Use system progress bar (hardware accelerated)
@Composable
fun LoadingSpinner() {
    CircularProgressIndicator(
        modifier = Modifier.size(100.dp)
    )
    // System handles animation efficiently
}

// ❌ EXPENSIVE: Animated list items
LazyColumn {
    items(experiments) { exp ->
        var expanded by remember { mutableStateOf(false) }
        
        AnimatedVisibility(expanded) {  // Animates size change
            ExperimentCard(exp)  // Every animation frame triggers recomposition
        }
    }
}

// ✅ SIMPLER: Fade instead of expand
LazyColumn {
    items(experiments) { exp ->
        var expanded by remember { mutableStateOf(false) }
        
        AnimatedContent(expanded) { isExpanded ->  // Uses Crossfade (faster)
            if (isExpanded) {
                ExpandedCard(exp)
            } else {
                CollapsedCard(exp)
            }
        }
    }
}

// ✅ BEST FOR BUDGET: No animation at all
LazyColumn {
    items(experiments) { exp ->
        if (expanded) {
            ExpandedCard(exp)
        } else {
            CollapsedCard(exp)
        }
        // Instant transition, zero animation overhead
    }
}
```

---

## Part 3: Storage & I/O Performance

### 3.1 SQLite Query Optimization (Critical)

```kotlin
// ❌ INEFFICIENT: Full table scan on every load
@Query("SELECT * FROM daily_logs ORDER BY date DESC")
suspend fun getAllLogs(): List<DailyLogEntity>

// ✅ EFFICIENT: Indexed query with limit
@Query("""
    SELECT * FROM daily_logs 
    WHERE experiment_id = :experimentId 
    ORDER BY date DESC 
    LIMIT 50 OFFSET :offset
""")
suspend fun getLogsForExperiment(
    experimentId: String,
    offset: Int = 0
): List<DailyLogEntity>

// Database indexes (critical for budget phones):
// In DatabaseMigration or onCreate:
database.execSQL("CREATE INDEX IF NOT EXISTS idx_daily_logs_exp_id ON daily_logs(experiment_id)")
database.execSQL("CREATE INDEX IF NOT EXISTS idx_daily_logs_date ON daily_logs(date DESC)")
database.execSQL("CREATE INDEX IF NOT EXISTS idx_reflections_exp_id ON reflections(experiment_id)")
```

### 3.2 Batch Operations (Reduce I/O)

```kotlin
// ❌ INEFFICIENT: Multiple database writes
suspend fun createMultipleExperiments(experiments: List<Experiment>) {
    experiments.forEach { exp ->
        repository.create(exp)  // 100 DB writes = 100 ms+
    }
}

// ✅ EFFICIENT: Single batch write
suspend fun createMultipleExperiments(experiments: List<Experiment>) {
    withContext(Dispatchers.IO) {
        repository.createBatch(experiments)  // 1 DB write = 5 ms
    }
}

@Dao
interface ExperimentDao {
    @Insert
    suspend fun insertBatch(experiments: List<ExperimentEntity>)
}
```

### 3.3 Export Performance (Streaming, Not Loading)

```kotlin
// ❌ INEFFICIENT: Load all data into memory, then write
suspend fun exportAllData(): String {
    val experiments = experimentRepository.getAll()  // 2 MB
    val logs = logRepository.getAll()  // 5 MB
    val reflections = reflectionRepository.getAll()  // 1 MB
    
    // Build entire JSON in memory (8 MB+)
    val json = buildString {
        append("[")
        experiments.forEach { append(gson.toJson(it)) }
        append("]")
    }
    return json  // 8 MB string created
}

// ✅ EFFICIENT: Stream to file, minimal memory
suspend fun exportAllData(file: File) = withContext(Dispatchers.IO) {
    file.bufferedWriter().use { writer ->
        writer.write("{\"experiments\":[")
        
        // Stream experiments one by one
        var first = true
        experimentRepository.getExperimentsPaginated(pageSize = 100).forEach { page ->
            page.forEach { exp ->
                if (!first) writer.write(",")
                writer.write(gson.toJson(exp))
                first = false
            }
        }
        
        writer.write("],\"logs\":[")
        
        // Stream logs one by one
        first = true
        logRepository.getLogsPaginated(pageSize = 100).forEach { page ->
            page.forEach { log ->
                if (!first) writer.write(",")
                writer.write(gson.toJson(log))
                first = false
            }
        }
        
        writer.write("]}")
    }
}
```

### 3.4 Cleanup on Low Storage

```kotlin
// In SettingsViewModel or background task:
@Composable
fun StorageManagement() {
    val context = LocalContext.current
    val storageManager = remember { StorageManager(context) }
    
    LaunchedEffect(Unit) {
        val freeSpace = storageManager.getAvailableSpace()
        
        if (freeSpace < 100 * 1024 * 1024) {  // Less than 100 MB free
            // Clear old cache
            storageManager.clearOldExports(olderThan = 7.days)
            
            // Warn user
            showToast("Storage low: Please review old exports")
        }
    }
}

class StorageManager(private val context: Context) {
    fun getAvailableSpace(): Long {
        return StatFs(context.filesDir.absolutePath).availableBytes
    }
    
    fun clearOldExports(olderThan: Duration) {
        val exportsDir = File(context.filesDir, "exports")
        exportsDir.listFiles()?.forEach { file ->
            val age = Duration.between(
                FileTime.fromMillis(file.lastModified()).toInstant(),
                Instant.now()
            )
            if (age > olderThan) {
                file.delete()
            }
        }
    }
}
```

---

## Part 4: Network Performance (Future Cloud Sync)

### 4.1 Battery-Efficient Network

```kotlin
// ❌ INEFFICIENT: Poll every 30 seconds
val job = viewModelScope.launch {
    while (isActive) {
        syncWithCloud()  // Wakes radio every 30s = massive battery drain
        delay(30000)
    }
}

// ✅ EFFICIENT: Use exponential backoff
class CloudSyncManager {
    private var backoffMs = 1000L
    
    suspend fun syncWithBackoff() {
        while (isActive) {
            try {
                syncWithCloud()
                backoffMs = 1000  // Reset on success
            } catch (e: Exception) {
                delay(backoffMs)
                backoffMs = (backoffMs * 1.5).toLong().coerceAtMost(300000)  // Max 5 min
            }
        }
    }
}

// ✅ BEST: WorkManager for periodic work
// Let system batch network calls, respect Doze mode
class SyncWorker : Worker {
    override suspend fun doWork(): Result {
        return try {
            syncWithCloud()
            Result.success()
        } catch (e: Exception) {
            Result.retry()  // System handles backoff
        }
    }
}

// In Application class:
fun scheduleSyncWork(context: Context) {
    val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
        Duration.ofHours(1)
    ).build()
    
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "cloud_sync",
        ExistingPeriodicWorkPolicy.KEEP,
        syncWork
    )
}
```

---

## Part 5: Build Configuration for Budget Phones

### 5.1 Gradle Build Optimization

```gradle
// app/build.gradle.kts

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 26  // Android 8.0
        targetSdk = 34
        
        // Reduce APK size
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
            // Exclude x86 (few budget phones use it)
        }
    }
    
    // Release optimization
    buildTypes {
        release {
            isMinifyEnabled = true
            shrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Bundle optimization
    bundle {
        language.enableSplit = true  // Users only get their language
        density.enableSplit = true   // Only assets for their screen density
    }
}

dependencies {
    // Core libraries (lightweight)
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Compose (optimized)
    implementation("androidx.compose.ui:ui:1.6.0")
    
    // Database
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // DI (lightweight)
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    
    // ❌ AVOID on budget phones:
    // - Firebase Analytics/Crashlytics
    // - Third-party ad networks
    // - Heavy ML libraries (TensorFlow Lite ok, full TF not ok)
}
```

### 5.2 ProGuard Rules for Budget Phones

```proguard
# proguard-rules.pro

# Keep app code intact, shrink everything else
-keep class com.kokoromi.** { *; }

# Keep Room database code
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep Hilt code
-keepclasseswithmembernames class * {
    @com.google.dagger.hilt.* <fields>;
}
-keepclasseswithmembernames class * {
    @com.google.dagger.hilt.* <methods>;
}

# Keep Compose code
-keep class androidx.compose.** { *; }

# Optimize everything else
-optimizationpasses 5
-allowaccessmodification

# Better stack traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
```

### 5.3 APK Size Monitoring

```kotlin
// Gradle task to check APK size before release
tasks.register("checkApkSize") {
    doLast {
        val apkFile = file("build/outputs/apk/release/app-release.apk")
        val sizeMb = apkFile.length() / (1024.0 * 1024.0)
        
        when {
            sizeMb > 15 -> {
                error("APK too large: ${sizeMb}MB (limit: 15MB)")
            }
            sizeMb > 12 -> {
                println("⚠️ WARNING: APK is ${sizeMb}MB (target: < 12MB)")
            }
            else -> {
                println("✅ APK size: ${sizeMb}MB")
            }
        }
    }
}
```

---

## Part 6: Testing on Budget Phones

### 6.1 Device Testing Checklist

```markdown
## Before v1 Release: Test on Real Budget Phone

Device to test on:
- [ ] Physical budget phone (2-3GB RAM, API 26-28)
- [ ] Alternatively: Android Emulator with 2GB RAM max

Test scenarios:
- [ ] App startup: < 2.5 seconds (cold start)
- [ ] Home screen: No lag, smooth experience
- [ ] Check-in: Tap [✓ YES] → screen updates instantly (< 500ms)
- [ ] Archive (100 items): Scroll smoothly, 60 FPS
- [ ] Export (100+ experiments): No ANR (Application Not Responding) dialog
- [ ] Memory usage: Monitor via Android Studio Profiler
  - Idle: < 35 MB
  - Archive open: < 60 MB
  - Never exceed 100 MB
- [ ] Battery drain: 1 hour usage → < 5% battery (rough estimate)
- [ ] Thermal: Device doesn't get hot during normal use

Performance profiling commands:
```bash
# Check memory usage in real-time
adb shell dumpsys meminfo com.kokoromi | grep TOTAL

# Monitor frame rate (Compose recompositions)
adb shell setprop debug.hwui.profile global

# Check CPU usage
adb shell top -n 1 | grep com.kokoromi
```

### 6.2 Android Studio Profiler Setup

1. **Memory Profiler**:
   - Record app startup: Should show < 35 MB baseline
   - Open archive: Should rise to ~50-60 MB
   - No memory leaks (GC should recover memory)

2. **CPU Profiler**:
   - Idle: Single core < 15% utilization
   - Scrolling archive: < 50% single core
   - No sustained high CPU (drain battery, heat up device)

3. **Network Profiler** (for future cloud sync):
   - Monitor data volume
   - Check for excessive requests
   - Verify batching works

---

## Part 7: Runtime Permissions & Features for Budget Phones

### 7.1 Feature Flags for Low-End Devices

```kotlin
// In KokoromiApp or MainActivity
object DeviceConfig {
    val isLowEndDevice: Boolean = {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        maxMemory < 256  // Device max heap < 256 MB = low-end
    }()
    
    val isSlowDevice: Boolean = {
        Build.DEVICE.contains("budget", ignoreCase = true) ||
        Build.MODEL.contains("redmi", ignoreCase = true) ||
        Build.MODEL.contains("moto g", ignoreCase = true)
    }()
    
    // Disable heavy features on budget devices
    val enableAnimations = !isLowEndDevice
    val enableBlurs = !isLowEndDevice
    val enableComplexShaders = !isLowEndDevice
    val maxListSize = if (isLowEndDevice) 50 else 100
}

// Usage in Compose:
@Composable
fun HomeScreen() {
    LazyColumn(
        // Limit items shown at once on budget phones
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (DeviceConfig.isLowEndDevice) {
                    Modifier  // No extra effects
                } else {
                    Modifier.background(Color.White)  // Can add effects on good phones
                }
            )
    ) {
        items(experiments.take(DeviceConfig.maxListSize)) { exp ->
            ExperimentCard(exp)
        }
    }
}
```

---

## Part 8: Monitoring Performance in Production

### 8.1 Telemetry (Privacy-Respecting)

**Important**: Only collect performance metrics, NEVER track user behavior or experiment data.

```kotlin
// Local performance logging (never sent to cloud)
class PerformanceLogger {
    private val file = File(context.filesDir, "perf_log.txt")
    
    fun logAppStartup(durationMs: Long) {
        if (durationMs > 3000) {  // Only log if slow
            file.appendText("startup: ${durationMs}ms\n")
        }
    }
    
    fun logQuery(name: String, durationMs: Long) {
        if (durationMs > 200) {  // Only log slow queries
            file.appendText("query[$name]: ${durationMs}ms\n")
        }
    }
    
    // Cleanup: delete old logs
    fun cleanup() {
        if (file.length() > 1024 * 1024) {  // > 1 MB
            file.delete()
        }
    }
}

// Usage:
class ExperimentViewModel : ViewModel() {
    fun loadExperiments() {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                repository.getActive()
            }
            val duration = System.currentTimeMillis() - start
            performanceLogger.logQuery("getActive", duration)
        }
    }
}
```

---

## Pre-Release Checklist: Budget Phone Optimization

### Memory ✅
- [ ] Baseline memory < 35 MB (idle)
- [ ] Archive screen < 60 MB (100 items)
- [ ] No memory leaks detected in profiler
- [ ] LruCache implemented for any cached data
- [ ] LazyColumn used for lists > 20 items
- [ ] `remember {}` used for computed values in Compose

### CPU & Thermal ✅
- [ ] Idle CPU < 15% single core
- [ ] Scrolling smooth (60 FPS on budget phone)
- [ ] No animations jank on low-end device
- [ ] All DB ops on Dispatchers.IO
- [ ] No blocking operations on main thread
- [ ] ViewModel jobs cancelled properly

### Storage & I/O ✅
- [ ] Database queries use indexes
- [ ] Pagination implemented (limit/offset)
- [ ] Export uses streaming (not all-in-memory)
- [ ] APK size < 15 MB (bundle < 10 MB)
- [ ] ProGuard minification enabled
- [ ] Resource shrinking enabled

### Battery ✅
- [ ] No polling (use WorkManager for periodic sync)
- [ ] Exponential backoff for retries
- [ ] No animations during idle
- [ ] Wake locks not held longer than necessary

### Testing ✅
- [ ] Tested on real budget phone or low-RAM emulator
- [ ] Android Studio Profiler shows good metrics
- [ ] No ANR dialogs during normal use
- [ ] Export doesn't freeze UI
- [ ] Archive scroll smooth (60 FPS)

### Code ✅
- [ ] Compose stable keys for LazyColumn items
- [ ] No View references in ViewModels
- [ ] Kotlin Flow/StateFlow instead of LiveData (more efficient)
- [ ] Hilt DI not causing app startup delays
- [ ] No memory warnings in Logcat

---

## Budget Phone Performance Targets Summary

| Metric | Target | Budget Phone | Method |
|--------|--------|--------------|--------|
| **App Startup** | < 2.5s | API 26, 2GB RAM | Time from `adb shell am start` to home screen |
| **Home Paint** | < 1s | 720p screen | Time to first interactive home screen |
| **Check-in Tap** | < 500ms | Normal response | Tap [✓ YES] → UI updates |
| **Archive Scroll** | 60 FPS | Smooth scroll | LazyColumn, no jank |
| **Memory Idle** | < 35 MB | Fresh start | Baseline after startup |
| **Memory Archive** | < 60 MB | 100 items | Large list open |
| **Query Time** | < 200ms | 50 logs | Database fetch |
| **APK Size** | < 15 MB | Play Store | Includes all assets |
| **Battery/Hour** | < 5% | Light use | Idle + 1 check-in + read |

---

## Summary: Making Budget Phones Sing

The key principles:

1. **Memory**: Use LazyColumn, paginate, cache wisely
2. **CPU**: Off-main-thread work, no unnecessary recompositions
3. **I/O**: Batch operations, stream large exports
4. **Storage**: Shrink APK with ProGuard, split by language/density
5. **Battery**: Batch network calls, respect system Doze
6. **Testing**: Real budget device testing before release

With these optimizations, your app will run smoothly on budget phones and provide an excellent user experience to the widest possible audience. 🚀

---

**Version**: 1.0  
**Target**: API 26+ (Android 8.0+)  
**Device**: Budget phones (2-3GB RAM)  
**Status**: Implementation Guide
