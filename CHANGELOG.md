# Changelog

## [1.1.0] - Hardened Production Release
### Added
- **Project Lazarus**: Self-healing mechanism (`ResurrectionWorker`) to restart GuardianService if killed by OEM battery optimizations.
- **Overlay Permission UI**: Dialog and notification to guide users to grant 'Draw over other apps' permission (required for background restarts on Android 12+).
- **Crash Reporting**: Integrated Firebase Crashlytics structure (needs `google-services.json`).
- **Automated Tests**: Added Unit tests for Repository and Integration tests for Workers.
- **CI/CD**: GitHub Actions workflow for build and test validation.

### Changed
- **History Performance**: Implemented pagination (limit 100) to prevent OOM on large history sets.
- **Icon Cache**: Dynamic memory sizing based on device RAM.
- **Configuration**: Constants (timeouts, limits) centralized in `com.zendroid.launcher.config`.

### Fixed
- **Bypass**: Back button no longer bypasses intervention friction.
- **Data Accuracy**: "Today" session count now correct across device reboots (uses wall-clock time).
- **Crashes**: Added missing Activity declarations in Manifest.
- **False Positives**: Robust Accessibility Service check logic.
