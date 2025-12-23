# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.zendroid.launcher.data.db.** { *; }

# Hilt
-keep class com.google.dagger.** { *; }
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.HiltAndroidApp class *

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class *

# Models/Entities
-keepclassmembers class com.zendroid.launcher.data.db.** { *; }

# Accessibility Service
-keep class com.zendroid.launcher.service.ZenDroidAccessibilityService { *; }
-keep class com.zendroid.launcher.service.GuardianService { *; }
