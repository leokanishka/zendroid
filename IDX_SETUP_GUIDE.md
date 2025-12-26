# ZenDroid Firebase Studio (IDX) Setup Guide

## ⚠️ Issue: Wrong Repository Was Imported

The repository `slashmili/zendroid` (Scala/SBT project) was imported by mistake.
**Correct repository:** `leokanishka/zendroid` (Kotlin/Gradle Android app)

---

## Phase 1: Clean Up Incorrect Workspace

### Step 1.1: Delete Wrong Workspace
1. Go to [Firebase Studio Dashboard](https://idx.google.com)
2. Find workspace `zendroid-22891577` (the Scala one)
3. Click the **three-dot menu (⋮)** on the workspace card
4. Select **"Delete workspace"**
5. Confirm deletion

### Step 1.2: Verify Deletion
- Workspace should no longer appear in your dashboard
- If it still shows, refresh the page

---

## Phase 2: Import Correct Repository

### Step 2.1: Go to Import Page
1. Navigate to: **https://idx.google.com/import**

### Step 2.2: Enter Correct Repository URL
```
https://github.com/leokanishka/zendroid
```

### Step 2.3: Configure Import Settings
- ✅ **Enable:** "Mobile SDK Support (Flutter + Android Emulator)"
- Leave other settings as defaults

### Step 2.4: Click "Import"
- Firebase Studio will create a new workspace
- Wait for initialization (~2-3 minutes)

---

## Phase 3: Wait for Environment Build

### Step 3.1: Monitor Build Progress
The environment will show these stages:
1. ✓ Setting up workspace
2. ✓ Initializing environment
3. **⏳ Building environment** (this takes 5-10 minutes)
4. ✓ Finalizing

### Step 3.2: If Build Fails
- Click **"Launch Recovery"**
- In terminal, run: `nix develop`
- If still failing, contact the project owner

### Step 3.3: Verify Successful Build
When complete, you should see:
- VS Code editor with file tree
- Terminal at bottom
- No error banners

---

## Phase 4: Verify Correct Project

### Step 4.1: Check Project Structure
In the file explorer, verify these exist:
```
✓ app/build.gradle.kts
✓ app/src/main/java/com/zendroid/launcher/
✓ gradlew (executable)
✓ TESTER_CHECKLIST.md
```

### Step 4.2: Verify Gradle (in Terminal)
```bash
chmod +x ./gradlew
./gradlew --version
```

**Expected output:**
```
Gradle 8.x.x
Kotlin: 1.9.x
```

### Step 4.3: If You See `build.sbt` Instead
❌ Wrong repository! Go back to Phase 1.

---

## Phase 4.5: Configure Android SDK (CRITICAL)

> [!IMPORTANT]
> The Nix Android SDK is read-only. You must create a writable copy.

### Step 4.5.1: Find the Nix Android SDK
```bash
find /nix/store -path "*android-sdk/platforms" 2>/dev/null | head -1
```

**Example output:** `/nix/store/abc123-androidsdk/libexec/android-sdk/platforms`

### Step 4.5.2: Create Writable SDK Copy
```bash
SDK_PATH=$(find /nix/store -path "*android-sdk/platforms" 2>/dev/null | head -1 | sed 's|/platforms||')
cp -r "$SDK_PATH" ~/.androidsdk_writable
chmod -R u+w ~/.androidsdk_writable
```

### Step 4.5.3: Configure local.properties
```bash
echo 'sdk.dir=/home/user/.androidsdk_writable' > local.properties
```

### Step 4.5.4: Add AndroidX Flags
```bash
echo 'android.useAndroidX=true' >> gradle.properties
echo 'android.enableJetifier=false' >> gradle.properties
```

### Step 4.5.5: Verify
```bash
cat local.properties
ls ~/.androidsdk_writable/platforms/
```

---

## Phase 5: Build the APK

### Step 5.1: Run Debug Build
```bash
./gradlew assembleDebug --no-daemon -x checkDebugAarMetadata
```

> [!NOTE]
> The `-x checkDebugAarMetadata` flag skips a metadata check that fails in Firebase Studio cloud environment.

**Expected:** `BUILD SUCCESSFUL in ~3 minutes`

### Step 5.2: Locate APK
```bash
ls -la app/build/outputs/apk/debug/app-debug.apk
```

**Expected:** File size ~18 MB

---

## Phase 6: Set Up Android Emulator

### Step 6.1: Open Android Panel
- Click **Android icon** in left sidebar
- Or use Command Palette: `Android: Open Android Tool Window`

### Step 6.2: Create/Start Emulator
1. Click **"Create Virtual Device"** if none exists
2. Select **Pixel 6** or similar
3. Choose **API 34 (Android 14)**
4. Click **Start**

### Step 6.3: Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Phase 7: Begin Testing

### Step 7.1: Open Tester Checklist
- In VS Code, open: `TESTER_CHECKLIST.md`
- Follow all test cases systematically

### Step 7.2: Key Features to Test
| Feature | Location |
|---------|----------|
| App Categorization | Settings → Manage Apps |
| RED App Block | Open a RED-marked app |
| YELLOW App Friction | Open a YELLOW-marked app |
| Focus Profiles | Settings → Manage Schedules |
| Haptic Feedback | Hold-to-unlock button |

---

## Troubleshooting

### Error: "Could not connect to Kotlin daemon"
```bash
./gradlew --stop
./gradlew assembleDebug
```

### Error: "SDK location not found"
```bash
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew assembleDebug
```

### Error: Environment build keeps failing
1. Check if `.idx/dev.nix` exists in the project
2. If missing, the project may need manual SDK setup
3. Contact project owner for assistance

---

## Summary Checklist

- [ ] Deleted wrong workspace (slashmili/zendroid)
- [ ] Imported correct repo (leokanishka/zendroid)
- [ ] Environment built successfully
- [ ] Verified `app/build.gradle.kts` exists
- [ ] `./gradlew --version` works
- [ ] `./gradlew assembleDebug` succeeded
- [ ] Emulator running
- [ ] APK installed
- [ ] Testing with TESTER_CHECKLIST.md

---

**Questions?** Contact the project owner.

**Repository:** https://github.com/leokanishka/zendroid
