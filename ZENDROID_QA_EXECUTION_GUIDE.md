# ZenDroid Android App - QA Execution Guide

## Quick Start Testing

### Setup (5 minutes)
```bash
# 1. Connect device or start emulator
adb devices

# 2. Install APK
adb install path/to/app-debug.apk

# 3. Launch app
adb shell am start -n com.zendroid/.MainActivity
```

### Critical Path Testing (30 minutes)
1. **App Launch** (2 min)
   - [ ] App installs without errors
   - [ ] App launches successfully
   - [ ] No crashes on startup
   - [ ] Main screen displays correctly
   - [ ] Load time < 3 seconds

2. **Basic Navigation** (5 min)
   - [ ] Bottom navigation working
   - [ ] Menu items accessible
   - [ ] Back button functional
   - [ ] Screen transitions smooth

3. **Core Feature 1** (10 min)
   - [ ] Feature accessible from main menu
   - [ ] Input fields accept data
   - [ ] Operations execute without errors
   - [ ] Results display correctly
   - [ ] Data persists after restart

4. **Core Feature 2** (10 min)
   - [ ] Feature accessible
   - [ ] User workflows complete successfully
   - [ ] Error handling works
   - [ ] UI is responsive

5. **Data Validation** (5 min)
   - [ ] Form validation works
   - [ ] Invalid inputs rejected
   - [ ] Required fields enforced
   - [ ] Error messages clear

---

## Detailed Testing Scenarios

### Scenario 1: First-Time User Setup
**Duration**: 10 minutes
**Steps**:
1. Install fresh APK
2. Launch app for first time
3. Complete any onboarding flow (if applicable)
4. Navigate to main features
5. Perform basic action

**Expected**: All elements load, no crashes, intuitive flow

### Scenario 2: Data Input & Persistence
**Duration**: 15 minutes
**Steps**:
1. Open data entry form
2. Fill all fields with valid data
3. Submit form
4. Verify data appears in list/display
5. Close app completely
6. Reopen app
7. Verify data still present

**Expected**: Data saved and persists

### Scenario 3: Error Handling
**Duration**: 10 minutes
**Steps**:
1. Try submitting empty form
2. Try entering invalid email/phone
3. Try upload without network (offline)
4. Try with special characters
5. Observe error messages

**Expected**: Clear, helpful error messages

### Scenario 4: Orientation Changes
**Duration**: 10 minutes
**Steps**:
1. Launch app in portrait
2. Rotate to landscape
3. Rotate back to portrait
4. Perform action in landscape
5. Check data consistency

**Expected**: Layout adapts, no data loss

### Scenario 5: Performance Under Load
**Duration**: 15 minutes
**Steps**:
1. Create multiple records/items
2. Scroll through long lists
3. Open/close screens repeatedly
4. Monitor app responsiveness
5. Check memory usage (Android Monitor)

**Expected**: App remains responsive, no hangs

---

## Firebase-Specific Testing

### Authentication Testing
```
Test Case: User Login
1. [ ] Launch app
2. [ ] Enter valid credentials
3. [ ] Verify successful login
4. [ ] Check Firebase Auth logs
5. [ ] Verify user session persisted
6. [ ] Test logout
7. [ ] Verify session cleared
```

### Realtime Database Testing
```
Test Case: Data Sync
1. [ ] Create record in app
2. [ ] Verify write to Firestore/RTDB
3. [ ] Modify record from console
4. [ ] Verify app reflects change
5. [ ] Test offline capability
6. [ ] Go online and verify sync
```

### Cloud Storage Testing
```
Test Case: File Operations
1. [ ] Upload file from app
2. [ ] Verify in Firebase Storage
3. [ ] Download file
4. [ ] Verify file integrity
5. [ ] Test large file handling
6. [ ] Test upload cancellation
```

---

## Common Issues & Solutions

### Issue: App Crashes on Launch
**Diagnosis**:
```bash
adb logcat *:E | grep AndroidRuntime
```
**Solutions**:
- Clear app data: `adb shell pm clear com.zendroid`
- Reinstall: `adb uninstall com.zendroid && adb install app.apk`
- Check Java version

### Issue: Database Not Syncing
**Diagnosis**:
- Check Firebase console for errors
- Verify internet connection: `adb shell ping google.com`
- Check authentication status

**Solutions**:
- Verify Firebase credentials
- Check database rules
- Restart app and device

### Issue: Crashes During Rotation
**Diagnosis**:
- Monitor logcat during rotation
- Check AndroidManifest for orientation settings

**Solutions**:
- Implement lifecycle handling
- Save instance state properly
- Test with configuration changes

### Issue: Memory Leaks
**Diagnosis**:
```bash
# Monitor memory via Android Studio Profiler
Filters > Memory > Monitor heap allocations
```

**Solutions**:
- Check for retained references
- Implement proper cleanup in onDestroy()
- Use WeakReferences where appropriate

---

## Logging & Debugging

### View Real-time Logs
```bash
adb logcat | grep com.zendroid
```

### Save Logs to File
```bash
adb logcat > zendroid_$(date +%Y%m%d_%H%M%S).log
```

### Check Firebase Logs
- Navigate to Firebase Console
- Go to Logs
- Filter by error level

### Android Profiler (Android Studio)
- Memory: Detect memory leaks
- CPU: Monitor CPU usage
- Battery: Track battery impact
- Network: Monitor network calls

---

## Test Devices Checklist

- [ ] Pixel 6 (Android 14)
- [ ] Samsung Galaxy S21 (Android 13)
- [ ] Google Pixel 4a (Android 12)
- [ ] Android Emulator (API 33)
- [ ] Tablet (if applicable)
- [ ] Foldable (if applicable)

---

## Final Sign-Off

**Test Date**: ___________
**Tester Name**: ___________
**Device/OS**: ___________
**Build Version**: ___________

**Test Status**:
- [ ] All Critical Tests Passed
- [ ] No Blocking Issues Found
- [ ] Performance Acceptable
- [ ] Security Verified
- [ ] Ready for Release

**Issues Found**: ___________
**Recommendations**: ___________

