# Kokoromi ProGuard rules

# Keep Room entities and DAOs
-keep class com.kokoromi.data.db.entity.** { *; }
-keep interface com.kokoromi.data.db.dao.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# Keep data models used for JSON export
-keep class com.kokoromi.data.model.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
