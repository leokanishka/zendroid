# ZenDroid IDX Device Testing Execution Plan

**Date:** December 27, 2025  
**Tester:** QA Automation Agent (Comet)  
**Environment:** Google Firebase Studio (IDX)  
**Status:** Ready for Execution

---

## Prerequisites Verification Status

✅ **All Prerequisites Verified:**

| Requirement | Check | Result | Status |
|-------------|-------|--------|--------|
| Google IDX | Access idx.google.com | ✅ Active | Ready |
| Java 21 | `java -version` | OpenJDK 21.0.3+9 | ✅ Pass |
| Gradle Wrapper | `./gradlew -v` | Gradle 8.13 | ✅ Pass |
| Android SDK | ADB version | 35.0.1-android-tools | ✅ Pass |
| APK Build | app-debug.apk | 18 MB, exists | ✅ Pass |
| APK MD5 | Checksum | 646b62a92867e60d3722c8489438e2b2 | ✅ Pass |
| ADB Daemon | Started | Successfully running | ✅ Pass |

---

## Device Testing Execution Steps

### Phase 1: Emulator Setup

**Status:** Pending (IDX UI Action Required)

Actions:
1. Click **"Emulator"** button in IDX sidebar
2. Create New Device:
   - Device: Pixel 5 or higher
   - Android Version: 12+ (API 31+)
3. Click **Start** and wait for boot (2-3 minutes)
4. Verify emulator appears in:
   ```bash
   adb devices
   # Should list: emulator-5554  device
   ```

### Phase 2: APK Installation

**Command:**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected Output:**
```
Installing app/build/outputs/apk/debug/app-debug.apk
Success
```

---

## 7-Section Test Checklist

### Section 1: First Launch ✅

**Test Action:**
1. Open ZenDroid app from launcher
2. Observe home screen

**Success Criteria:**
- ✅ App launches without crash
- ✅ Home grid with installed apps visible
- ✅ Settings icon present (gear/cog)
- ✅ No error dialogs

**Evidence:** Screenshot `test_01_first_launch.png`

---

### Section 2: App Categorization ✅

**Test Actions:**
1. Go to Settings (tap gear icon)
2. Tap "Manage Apps" or "App Categories"
3. Select ANY system app (e.g., Calculator, Maps)
4. Mark it RED (Blocked) - long press or tap color button
5. Verify UI updates to show RED state
6. Mark same app YELLOW (Friction) - verify state change
7. Mark same app GREEN (Allowed) - verify state change
8. Mark app RED again

**Success Criteria:**
- ✅ Settings screen opens
- ✅ Can mark apps RED/YELLOW/GREEN
- ✅ UI color reflects selection
- ✅ State persists when re-opening

**Evidence:** Screenshots `test_02_categorization_*.png` (show RED, YELLOW, GREEN)

---

### Section 3: RED App Intervention ✅

**Test Actions:**
1. From home, tap the RED-marked app
2. Observe intervention screen appears
3. Read "Why do you need this?" prompt
4. Select duration: 5 min (any option works)
5. Choose friction challenge: Breathing (simplest to test)
6. Complete the breathing exercise (follow on-screen instructions)
7. Observe app opens
8. Go back to home (Android Back button)
9. Tap same RED app again
   - If within session: opens without intervention
   - If session expired: intervention appears again

**Success Criteria:**
- ✅ Intervention screen shows
- ✅ "Why do you need this?" prompt visible
- ✅ Duration selector (5/10/15/30 min) works
- ✅ Friction challenge (Breathing/Math/Hold) appears
- ✅ Completing challenge opens app
- ✅ During session: app opens without friction
- ✅ Cancel button returns to home

**Evidence:** Video `test_03_red_intervention.mp4` (30 seconds)

**Recording Command:**
```bash
adb shell screenrecord /sdcard/test_03_red_intervention.mp4
# Ctrl+C after 30s, then:
adb pull /sdcard/test_03_red_intervention.mp4 idx-test-results/
```

---

### Section 4: YELLOW App Intervention ✅

**Test Actions:**
1. Go to Settings > Manage Apps
2. Mark a DIFFERENT app as YELLOW
3. Return to home
4. Tap YELLOW-marked app
5. Observe bottom-sheet dialog appears
6. Press and HOLD the "Unlock" or "Press to Continue" button
7. Feel/observe haptic feedback (vibration)
8. Check logcat for vibration event:
   ```bash
   adb logcat | grep -i 'vibrat\|haptic'
   ```
9. Release button - app opens
10. Go back, tap app again
11. Tap "Nevermind" button - cancel and return to home

**Success Criteria:**
- ✅ Bottom-sheet appears (not full screen)
- ✅ Hold-to-unlock button functional
- ✅ Haptic feedback on press (device vibrates)
- ✅ App opens after unlock
- ✅ Nevermind cancels and returns home
- ✅ Vibration logged in Logcat

**Evidence:** Screenshots `test_04_yellow_*.png` + Logcat `test_04_haptic.log`

**Logcat Command:**
```bash
adb logcat > idx-test-results/test_04_haptic.log &
# Do the test
# Ctrl+C to stop logging
```

---

### Section 5: Focus Profiles (Schedules) ✅

**Test Actions:**
1. Go to Settings > "Manage Schedules" or "Focus Profiles"
2. Tap "Add Schedule" or "+" button
3. Enter schedule details:
   - Name: "Night Mode" (example)
   - Start Time: 22:00 (10 PM)
   - End Time: 06:00 (6 AM)
4. Toggle schedule ON (enable)
5. Go back to home
6. Tap a RED app - if within schedule window:
   - Intervention should apply
   - If outside window: app opens normally
7. Go back to Schedules
8. Toggle schedule OFF (disable)
9. Delete schedule (tap delete/trash icon)
10. Confirm deletion

**Success Criteria:**
- ✅ Can add schedule with times
- ✅ Can toggle schedule on/off
- ✅ When ON: restrictions enforced during time window
- ✅ When OFF: restrictions ignored
- ✅ Can delete schedule
- ✅ Deletion confirmed

**Evidence:** Screenshots `test_05_schedule_*.png` (add, toggle, delete)

---

### Section 6: Session Management ✅

**Test Actions:**
1. Tap a RED app
2. Complete the friction challenge
3. App opens - observe session timer on screen (top-right, usually)
4. Time should show remaining minutes (e.g., "4:59")
5. Keep app open for 1-2 minutes
6. Go back to home
7. Tap Settings > "Session History" or "Stats"
8. Verify the session entry appears:
   - App name
   - Unlock time
   - Duration granted
9. Tap Back to close history

**Success Criteria:**
- ✅ Session timer visible and counting down
- ✅ App remains accessible during session
- ✅ Session entry in history
- ✅ History shows app name and duration
- ✅ After session expires: RED app needs friction again

**Evidence:** Screenshots `test_06_session_*.png` (timer + history)

---

### Section 7: Edge Cases ✅

#### 7a: Back Button Disabled During Intervention

**Test Action:**
1. Tap RED app
2. Intervention screen appears
3. Press Android Back button (physical or on-screen)
4. Observe: Screen should remain on intervention (Back disabled)

**Expected:** Back button does nothing

#### 7b: Overnight Schedule Crossing Midnight

**Test Action:**
1. Create schedule: 23:00 - 06:00 (crosses midnight)
2. Enable schedule
3. Verify behavior:
   - At 23:30: Restrictions active
   - (Simulate time passing - may require system clock adjustment)
   - After midnight (00:30): Restrictions still active
   - At 06:30: Restrictions disabled

**Expected:** Schedule respects the overnight window

#### 7c: Service Survives Device Reboot

**Test Action:**
1. Enable a schedule for current time
2. Mark a RED app
3. Reboot device:
   ```bash
   adb reboot
   # Wait 30s for reboot
   adb devices  # Should show device again
   ```
4. After reboot, tap RED app
5. Verify: Intervention still appears (service running)

**Expected:** App restrictions enforced after reboot

**Success Criteria (All 3):**
- ✅ Back button blocked during intervention
- ✅ Overnight schedule works across midnight
- ✅ Service persists and runs after reboot

**Evidence:** Screenshots/video `test_07_edge_cases.mp4`

---

## Evidence Collection Instructions

### Screenshots
```bash
# In IDX emulator, press Ctrl+Shift+S to take screenshot
# Or via adb:
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png idx-test-results/
```

### Screen Recording
```bash
# Start recording
adb shell screenrecord /sdcard/video.mp4

# Do the test (max 3 min per recording)

# Stop: Ctrl+C

# Pull to computer
adb pull /sdcard/video.mp4 idx-test-results/
```

### Logcat
```bash
# Start logging
adb logcat > idx-test-results/logcat_full.log &

# Do tests

# Stop: Ctrl+C

# Search for specific events:
adb logcat | grep -i 'zendroid\|vibrat\|haptic\|error\|crash'
```

---

## Failure Handling

If any test fails, create a bug report:

**File:** `idx-test-results/bug-report-section-X.md`

**Template:**
```markdown
# Bug Report: Section X

**Device:** IDX Emulator (Pixel 5, Android 12)
**Android Version:** 12 (API 31)

## Steps to Reproduce:
1. [First action]
2. [Second action]
3. [Third action]

## Expected Result:
[What should happen]

## Actual Result:
[What actually happened]

## Screenshot/Video:
`screenshot_or_video_name.png`

## Logcat Output (if relevant):
```
[Paste relevant logcat lines]
```
```

---

## Final Test Summary

After all tests complete, create: `TESTING_SUMMARY.txt`

**Template:**
```markdown
# ZenDroid IDX Testing Summary - December 27, 2025

| Section | Status | Notes |
|---------|--------|-------|
| 1. First Launch | ✅ Pass / ❌ Fail | – |
| 2. App Categorization | ✅ Pass / ❌ Fail | – |
| 3. RED Intervention | ✅ Pass / ❌ Fail | – |
| 4. YELLOW Intervention | ✅ Pass / ❌ Fail | Haptic feedback: [OK/Issue] |
| 5. Focus Profiles | ✅ Pass / ❌ Fail | – |
| 6. Session Management | ✅ Pass / ❌ Fail | – |
| 7. Edge Cases | ✅ Pass / ❌ Fail | – |
| **Overall** | ✅ PASS / ⚠️ PASS WITH ISSUES / ❌ FAIL | [Summary] |

## Issues Found:
[List any bugs with reference to bug-report files]

## Conclusion:
[Overall assessment]
```

---

## Commit & Push Results

```bash
# Create branch for test results
git checkout -b idx-testing-results

# Stage all test artifacts
git add idx-test-results/
git add TESTING_SUMMARY.txt

# Commit
git commit -m "feat: Complete IDX device testing results - all 7 sections"

# Push
git push origin idx-testing-results
```

---

## Next Steps After Testing

1. ✅ All tests pass → Submit for release
2. ⚠️ Minor issues found → File bugs, create hotfix branch
3. ❌ Critical issues → Hold release, debug

---

## Tester Notes

**Estimated Time:** 2-3 hours (all 7 sections + evidence collection)

**Key Points:**
- Take multiple screenshots per section
- Record videos for complex interactions
- Check Logcat for haptic/vibration events
- Document any deviations from expected behavior
- Save all evidence in `idx-test-results/` folder

**Success Definition:** All 7 sections pass without critical issues

