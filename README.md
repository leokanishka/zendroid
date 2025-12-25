# ZenDroid

ZenDroid is a digital wellbeing launcher designed to reduce screen time through gentle interventions ("Mindful Pauses") and "Void Mode" styling.

## Key Features
- **Mindful Interventions**: Pauses before launching distracting apps.
- **Void Mode**: Monochrome interface to reduce visual stimulation.
- **Resilience**: "Project Lazarus" ensures the app stays active against OEM battery optimization.
- **Privacy First**: Fully offline, no data tracking.

## Architecture
- **MVVM + Clean Architecture**: Separation of concerns.
- **Hilt**: Dependency Injection.
- **Jetpack Compose**: Modern UI toolkit.
- **Room**: Local database.
- **WorkManager**: Background resilience.

## Configuration
**Constants** are centralised in `com.zendroid.launcher.config.Constants.kt`.
- `HISTORY_LIMIT`: Max sessions stored/displayed (default 100).
- `RESURRECTION_INTERVAL_MINUTES`: Frequency of "Project Lazarus" checks (default 15m).
- `ICON_CACHE_MIN/MAX_ICONS`: Memory bounds for icon cache.

## Project Lazarus (Resilience)
To combat aggressive OEM battery killers (e.g., Samsung, Xiaomi), ZenDroid employs **Project Lazarus**:
- **ResurrectionWorker**: A periodic worker that checks if the `GuardianService` is running.
- If the service is dead, it is restarted securely.
- Requires `SYSTEM_ALERT_WINDOW` permissions on Android 12+ to start foreground services from the background.

## Setup
1. Clone the repo.
2. Add `google-services.json` to the `/app` module for Crashlytics support.
3. Build and install.

## Testing
Run unit tests:
```bash
./gradlew testDebugUnitTest
```
Run UI/Integration tests:
```bash
./gradlew connectedDebugAndroidTest
```
