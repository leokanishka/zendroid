# Project Learnings

## System Architecture
- ZenDroid uses a "hardened" approach to minimize digital distractions via Accessibility Services.
- Architecture: Kotlin + Compose UI + Hilt DI + Room Persistence.

## Technical Insights

### Foundation (Sprint 1-2)
- **AndroidX Compatibility**: Always set `android.useAndroidX=true` in `gradle.properties` when using modern libraries to avoid AAR metadata errors.
- **Theme Bridging**: When using Material3 in Compose but needing XML themes (e.g., for `InterventionActivity` overlays), use `Theme.MaterialComponents` as a parent and add the `com.google.android.material:material` dependency.
- **Composable Context**: `@Composable` invocations are strictly limited to Composable functions. Standard utility wrappers (like performance tracers) will break the context if not specifically designed for Compose.
- **Navigation DI**: Using `hiltViewModel()` in Composables requires the `androidx.hilt:hilt-navigation-compose` dependency.
- **Adaptive Icons**: Modern Android builds fail if `ic_launcher` is missing; always provide adaptive XML icons in `anydpi-v26`.

### Build System (Dec 2025)
- **KAPT + Compose Compatibility**: Add `kapt { correctErrorTypes = true }` to `build.gradle.kts` when using KAPT with Compose. Without this, KAPT generates `@error.NonExistentClass` annotations and fails.
- **Firebase Prerequisites**: Firebase dependencies (`firebase-bom`, `firebase-crashlytics`) require **both**:
  1. `google-services.json` in the `/app` folder
  2. Google Services & Crashlytics plugins in gradle files
  - Build will fail silently or with cryptic errors if either is missing.
- **KSP vs KAPT Coexistence**: Room uses KSP (`ksp("androidx.room:room-compiler")`), Hilt uses KAPT (`kapt("com.google.dagger:hilt-android-compiler")`). Both can coexist but must be configured correctly.

### Material3 API Evolution
- **CircularProgressIndicator Breaking Change**: Compose BOM `2023.08.00` uses `progress: Float` parameter. Newer versions use `progress: () -> Float` lambda. Match your API to your BOM version:
  ```kotlin
  // Old API (2023.08.00)
  CircularProgressIndicator(progress = 0.5f, ...)
  
  // New API (2024+)
  CircularProgressIndicator(progress = { 0.5f }, ...)
  ```

### Haptic Feedback Implementation
- **SDK Version Checks Required**: Vibration APIs changed across Android versions:
  ```kotlin
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(VibratorManager::class.java)
      vibrator = vibratorManager?.defaultVibrator
  } else {
      @Suppress("DEPRECATION")
      vibrator = context.getSystemService(Vibrator::class.java)
  }
  ```
- **VibrationEffect**: Use `VibrationEffect.createOneShot(ms, amplitude)` for API 26+, fallback to `vibrator.vibrate(ms)` for older.

### Testing Pitfalls
- **mockStatic(SystemClock)**: Can be flaky in certain JVM environments. Consider using `@Ignore` annotation or Robolectric for Android framework mocking.
- **Test Dispatcher**: Always use `StandardTestDispatcher()` with `runTest(testDispatcher)` for coroutine tests.

### Deep Codebase Review Value
- **Always search before creating**: In Sprint 3, deep review revealed `SchedulesActivity`, `ScheduleViewModel`, and full integration with `InterventionManager` already existed. Only a simple import fix was needed.
- **Use grep extensively**: `grep_search` across the codebase prevents duplicate work and reveals existing implementations.

### Google Firebase Studio (IDX)
- **Workspaces are personal**: Cannot directly share a running IDX workspace with testers.
- **Environment build failures**: Often caused by invalid `.idx/dev.nix` configuration or missing Android SDK setup.
- **Tester workflow**: Push to Git → Tester imports into their own IDX workspace, OR share pre-built APKs.

## Process Learnings

### "Ultrathink" Deep Review
Before implementing, always perform a deep analysis:
1. Search for existing implementations
2. Check for missing dependencies/configurations
3. Identify potential race conditions
4. Review edge cases (overnight schedules, timezone, etc.)
5. Verify manifest registrations

### Incremental Build Validation
- Run `.\gradlew.bat assembleDebug` after each significant change
- Check for warnings (unused parameters, deprecated APIs) as they hint at issues
- KAPT failures often indicate configuration problems, not code problems
