# ZenDroid Tester Checklist

## 📋 Pre-Testing Setup

### Option A: Using Google Firebase Studio (IDX)
1. Go to [Firebase Studio](https://idx.google.com)
2. Import from GitHub: `https://github.com/[your-repo]/zendroid`
3. Wait for environment build (~5-10 minutes)
4. Open terminal and run: `./gradlew assembleDebug`
5. APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Using Pre-built APK
1. Download APK from shared location
2. Enable "Install unknown apps" on Android device
3. Install and open ZenDroid

---

## 🔥 Firebase Setup (Optional)

### Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project" → Name: "ZenDroid"
3. Disable Google Analytics (optional for testing)
4. Click "Create project"

### Add Android App
1. Click Android icon in project overview
2. Package name: `com.zendroid.launcher`
3. App nickname: "ZenDroid"
4. Click "Register app"
5. **Download `google-services.json`**
6. Place in `/app/` folder
7. Rebuild: `./gradlew assembleDebug`

---

## ✅ Test Checklist

### 1. First Launch
- [ ] App opens without crash
- [ ] Home screen displays with app grid
- [ ] Settings icon visible

### 2. App Categorization
- [ ] Settings → "Manage Apps" opens
- [ ] Can mark app as RED (blocked)
- [ ] Can mark app as YELLOW (friction)
- [ ] Can mark app as GREEN (allowed)

### 3. RED App Intervention
- [ ] Open a RED app
- [ ] Intervention screen appears
- [ ] "Why do you need this?" prompt shows
- [ ] Duration selector works (5/10/15/30 min)
- [ ] Friction challenge appears (Breathing/Math/Hold)
- [ ] Complete friction → App opens
- [ ] Cancel → Returns to home

### 4. YELLOW App Intervention
- [ ] Open a YELLOW app
- [ ] Bottom sheet appears
- [ ] Hold-to-unlock button works
- [ ] **Haptic feedback felt on press and unlock**
- [ ] "Nevermind" cancels

### 5. Focus Profiles (Schedules)
- [ ] Settings → "Manage Schedules" opens
- [ ] Can add new schedule
- [ ] Schedule toggle works
- [ ] Can delete schedule

### 6. Session Management
- [ ] After unlocking RED app, session timer active
- [ ] App accessible during session without intervention
- [ ] Session history shows in app

### 7. Edge Cases
- [ ] Back button disabled during intervention
- [ ] Overnight schedule works (10pm - 6am)
- [ ] App survives device reboot

---

## 🐛 Bug Report Template
```
**Device:** [e.g., Pixel 7, Samsung S23]
**Android Version:** [e.g., 14]
**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Result:**

**Actual Result:**

**Screenshot/Video:** [attach if possible]
```

---

## 📝 Notes
- APK Location: `app/build/outputs/apk/debug/app-debug.apk`
- Accessibility Service must be enabled for full functionality
- Overlay permission required for interventions
