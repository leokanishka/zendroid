# ZenDroid Android APK - Testing Execution Log

## Test Session Information

**Date**: 26 Dec 2025, 2 PM IST  
**APK Version**: 1.1.0 (18.5 MB)  
**APK File**: app-debug.apk  
**Build Status**: ✅ SUCCESSFUL  
**APK Location**: `~/zendroid/app/build/outputs/apk/debug/app-debug.apk`

---

## Environment Status

### Build Environment (Firebase Studio)
- ✅ Android SDK: Configured at ~/.androidsdk_writable
- ✅ Gradle: Successfully built APK
- ✅ APK: Generated and verified

### Testing Environment
- ⏳ Emulator: Not running in cloud environment (expected)
- ℹ️ **Next Step**: Execute testing on local machine or cloud emulator instance

---

## APK Installation Instructions

### Verify APK File:
```bash
ls -lh ~/zendroid/app/build/outputs/apk/debug/app-debug.apk
# Output: 18.5 MB debug APK
```

### Install on Android Emulator:
```bash
# 1. Start emulator (Pixel 6, API 34 recommended)
emulator -avd Pixel6_API34 &

# 2. Wait for device to be ready
adb wait-for-device

# 3. Verify device is connected
adb devices
# Expected output: device in "device" state

# 4. Install APK
adb install ~/zendroid/app/build/outputs/apk/debug/app-debug.apk
# Expected output: "Success"

# 5. Verify installation
adb shell pm list packages | grep zendroid
# Expected output: com.zendroid.launcher
```

### Install on Physical Device:
```bash
# 1. Enable USB Debugging on device
# 2. Connect via USB
# 3. Verify connection
adb devices

# 4. Install APK
adb install -r ~/zendroid/app/build/outputs/apk/debug/app-debug.apk
```

---

## App Launch Instructions

### Launch Main Activity:
```bash
adb shell am start -n com.zendroid.launcher/.MainActivity
```

### View App Logs:
```bash
adb logcat | grep -i "zendroid\|error\|exception"
```

### Monitor Performance:
```bash
# Memory usage
adb shell dumpsys meminfo com.zendroid.launcher

# CPU usage
adb shell dumpsys cpuinfo com.zendroid.launcher
```

---

## Testing Checklist

### Phase 1: First Launch (✅ To Be Executed)

**Objective**: Verify app launches and main UI is displayed

- [ ] APK installs without errors
- [ ] App launches without crash
- [ ] Home screen displays
- [ ] App grid is visible with app icons
- [ ] Settings icon/button is present
- [ ] No ANR (Application Not Responding) errors in first 10 seconds

**Notes**: ___________________________________________________________

### Phase 2: App Categorization (✅ To Be Executed)

**Objective**: Test app classification into RED/YELLOW/GREEN categories

**Steps**:
1. Tap Settings icon
2. Navigate to "Manage Apps"
3. Select an app from list
4. Categorize as RED (blocked)
5. Select another app
6. Categorize as YELLOW (friction)
7. Select another app
8. Categorize as GREEN (allowed)

**Test Cases**:
- [ ] Settings screen opens correctly
- [ ] "Manage Apps" option visible and clickable
- [ ] App list displays all installed apps
- [ ] Can select apps from list
- [ ] RED category can be applied
- [ ] YELLOW category can be applied
- [ ] GREEN category can be applied
- [ ] Category changes are saved
- [ ] Home screen reflects category changes (color indicators)

**Notes**: ___________________________________________________________

### Phase 3: RED App Intervention (✅ To Be Executed)

**Objective**: Test blocking intervention flow

**Steps**:
1. Return to home screen
2. Tap on a RED-marked app
3. Intervention dialog appears
4. Enter reason in "Why do you need this?" field
5. Select duration (5/10/15/30 min)
6. Complete friction challenge:
   - Option A: Breathing exercise (4 deep breaths)
   - Option B: Math problem (solve 3+5)
   - Option C: Hold button (hold for 5 seconds)
7. Upon completion, app opens

**Test Cases**:
- [ ] RED app tap triggers intervention dialog
- [ ] "Why do you need this?" prompt displays
- [ ] Text input field works
- [ ] Duration selector has 4 options (5/10/15/30 min)
- [ ] Duration can be selected
- [ ] Friction challenge options display
- [ ] Breathing challenge: displays count, 4 breaths required
- [ ] Math challenge: shows problem, accepts input, validates
- [ ] Hold challenge: countdown timer appears, unlocks after 5s
- [ ] Friction completion unlocks app
- [ ] App opens in restricted session (timer active)
- [ ] Cancel button returns to home (intervention aborted)

**Test RED App Cancel Flow**:
1. Tap RED app again
2. Complete intervention
3. Before friction, tap Cancel
4. Return to home screen (app NOT opened)

- [ ] Cancel button in intervention dialog works
- [ ] App does not open if intervention cancelled

**Notes**: ___________________________________________________________

### Phase 4: YELLOW App Intervention (✅ To Be Executed)

**Objective**: Test friction intervention (less restrictive than RED)

**Steps**:
1. Tap on a YELLOW-marked app
2. Bottom sheet appears with "Hold to Unlock" button
3. Press and hold the button for 3 seconds
4. Haptic feedback felt on press
5. Haptic feedback felt on unlock
6. App opens

**Alternative Flow - Cancel**:
1. Tap on YELLOW app
2. Tap "Nevermind" button
3. Return to home (app NOT opened)

**Test Cases**:
- [ ] YELLOW app tap shows bottom sheet
- [ ] "Hold to Unlock" button visible
- [ ] "Nevermind" button visible
- [ ] Hold-to-unlock requires ~3 seconds
- [ ] Haptic feedback on button press
- [ ] Haptic feedback on unlock completion
- [ ] App opens after successful hold
- [ ] App does NOT open if "Nevermind" tapped
- [ ] Session timer may or may not apply to YELLOW (check requirement)

**Notes**: ___________________________________________________________

### Phase 5: GREEN App (✅ To Be Executed)

**Objective**: Test unrestricted app access

**Steps**:
1. Tap on a GREEN-marked app
2. App opens immediately
3. No intervention, dialog, or friction required

**Test Cases**:
- [ ] GREEN app opens immediately
- [ ] No intervention dialog appears
- [ ] No hold-to-unlock appears
- [ ] App starts normally

**Notes**: ___________________________________________________________

### Phase 6: Focus Profiles / Schedules (✅ To Be Executed)

**Objective**: Test schedule/focus profile management

**Steps**:
1. From Settings, go to "Manage Schedules" or "Focus Profiles"
2. Tap "Add Schedule"
3. Configure schedule:
   - Name (e.g., "Bedtime", "Work")
   - Time range
   - Days of week
   - Which apps are blocked/restricted
4. Toggle schedule on/off
5. Delete schedule

**Test Cases**:
- [ ] Schedules section loads
- [ ] Can add new schedule
- [ ] Can set schedule name
- [ ] Can set time range
- [ ] Can select days of week
- [ ] Can assign app categories to schedule
- [ ] Schedule toggle works (on/off)
- [ ] When toggled on, restrictions apply at scheduled time
- [ ] Can delete schedule
- [ ] Schedule deletion confirmed

**Notes**: ___________________________________________________________

### Phase 7: Session Management (✅ To Be Executed)

**Objective**: Test session timer and history

**Steps**:
1. Open a RED app and complete intervention
2. Check for active session indicator
3. Session timer should count down
4. Within session, opening same app again should bypass intervention
5. After session expires, intervention required again
6. View session history

**Test Cases**:
- [ ] Session timer visible during active session
- [ ] Timer counts down correctly
- [ ] Same app during session opens without intervention
- [ ] Different app still requires its own intervention
- [ ] After session expires, new intervention required
- [ ] Session history shows app access records
- [ ] Can view time spent in each app
- [ ] Can clear history

**Notes**: ___________________________________________________________

### Phase 8: Error Handling (✅ To Be Executed)

**Objective**: Test graceful error handling

**Test Cases**:
- [ ] Rotate device during intervention - dialog persists or recovers
- [ ] Low battery - app continues to work
- [ ] No network (if applicable) - local features work
- [ ] Back button behavior tested
- [ ] Home button behavior tested
- [ ] App switching behavior tested

**Notes**: ___________________________________________________________

---

## Performance & Stability

**Memory Usage**:
```bash
adb shell dumpsys meminfo com.zendroid.launcher
# Expected: < 100 MB for idle state
```

**Battery Usage**:
- [ ] App doesn't cause excessive battery drain
- [ ] Background usage minimal
- [ ] No continuous wake locks

**Stability**:
- [ ] No crashes during testing
- [ ] No ANR errors
- [ ] Smooth animations
- [ ] Responsive UI

---

## Known Limitations & Notes

- Cloud Firebase Studio doesn't have emulator running (expected)
- APK is in DEBUG mode for testing
- Some permissions may need manual grant on first launch
- Haptic feedback requires physical device or emulator with haptics support

---

## Test Results Summary

| Phase | Status | Pass/Fail | Notes |
|-------|--------|-----------|-------|
| First Launch | Pending | | |
| App Categorization | Pending | | |
| RED Intervention | Pending | | |
| YELLOW Intervention | Pending | | |
| GREEN Access | Pending | | |
| Schedules | Pending | | |
| Session Management | Pending | | |
| Error Handling | Pending | | |

**Overall Status**: READY FOR TESTING ✅

---

## Bugs/Issues Found

1. **[Pending]** - To be documented during testing

---

## Tester Information

**Tester Name**: ___________________  
**Test Date**: ___________________  
**Device/Emulator**: ___________________  
**Android Version**: ___________________  

---

**Status**: APK READY FOR FUNCTIONAL TESTING
