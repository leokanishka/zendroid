# Firebase Crashlytics Setup Guide & Testing Strategy

## Current Status (December 26, 2025)

### ✅ COMPLETED
- Firebase Project Created: "ZenDroid" (Project ID: `zendroid-9badc`)
- ZenDroid Android App built (`app-debug.apk`)
- Project structure verified: Kotlin + Compose UI + Hilt DI + Room
- Test cases documented in `TESTER_CHECKLIST.md`
- Google Analytics enabled (for Crashlytics breadcrumbs)

### ⏳ IN PROGRESS
- Firebase Android App Registration
- Emulator environment setup
- Core feature testing

---

## Option A: Complete Firebase Crashlytics Setup

### Step 1: Register Android App
1. Go to: https://console.firebase.google.com/project/zendroid-9badc/overview
2. Click Android icon
3. Package name: `com.zendroid.launcher`
4. Click "Register app"

### Step 2: Download google-services.json
1. Click "Download google-services.json"
2. Save to: `app/google-services.json`

### Step 3: Verify Gradle Configuration
`app/build.gradle.kts` should contain:
```kotlin
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}
```

### Step 4: Rebuild APK
```bash
./gradlew clean assembleDebug
```

---

## Option B: Core Testing (Without Firebase)

### Step 1: Setup Android Emulator
```bash
nix develop
emulator -list-avds
emulator -avd Pixel_6_API_34
adb devices
```

### Step 2: Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Run Test Suite
Follow `TESTER_CHECKLIST.md` - all 7 feature categories.

---

## Core Features to Test

| Feature | Test Case | Expected Result |
|---------|-----------|-----------------|
| App Launch | Open app | Home screen visible |
| App Marking | Mark as RED/YELLOW/GREEN | Status persists |
| RED Intervention | Open RED app | Friction challenge |
| YELLOW Hold-to-Unlock | Hold button | Haptic + unlock |
| Focus Profiles | Create schedule | Restrictions apply |

---

## File Reference

| File | Purpose |
|------|---------|
| `/IDX_SETUP_GUIDE.md` | IDX setup instructions |
| `/TESTER_CHECKLIST.md` | Complete test cases |
| `/BUILD_TROUBLESHOOTING.md` | Build issue fixes |
| `/app/build.gradle.kts` | Gradle config |
| `/app/google-services.json` | Firebase config (add this) |

---

## Summary

**Build Status**: ✅ SUCCESSFUL  
**Firebase Project**: ✅ Created (`zendroid-9badc`)  
**Testing Framework**: ✅ Complete (7 categories, 20+ tests)  
**Firebase App**: ⏳ Pending registration
