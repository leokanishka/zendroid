# ZenDroid APK Build Troubleshooting Guide

## 🔧 Phase 5 Build Issue Resolution

If `./gradlew assembleDebug` completes but no APK appears, follow these steps:

---

## Step 1: Check Build Status

Run with verbose output to see actual errors:

```bash
cd /home/user/zendroid
./gradlew assembleDebug --info 2>&1 | tee build.log
```

Check the last 50 lines for errors:
```bash
tail -50 build.log
```

---

## Step 2: Fix JAVA_HOME Warning

If you see "JAVA_HOME not set", run:

```bash
# Find Java installation
which java
ls /nix/store/ | grep -i openjdk

# Set JAVA_HOME (typical Nix path)
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo $JAVA_HOME

# Verify
java -version
```

---

## Step 3: Fix Android SDK Path

Create or update `local.properties`:

```bash
cat > local.properties << 'EOF'
sdk.dir=/home/user/Android/Sdk
EOF
```

Or check if SDK exists elsewhere:
```bash
find /home -name "adb" 2>/dev/null
find /nix -name "android-sdk*" 2>/dev/null
```

---

## Step 4: Run Clean Build

```bash
./gradlew clean
./gradlew assembleDebug --stacktrace
```

---

## Step 5: Verify APK Created

```bash
ls -la app/build/outputs/apk/debug/
```

Expected: `app-debug.apk` (10-20 MB)

---

## Step 6: Common Errors & Fixes

### Error: "SDK location not found"
```bash
# Check if Android SDK installed via Nix
nix-env -q | grep android

# If missing, add to dev.nix android section
# Then run: nix develop
```

### Error: "Could not determine java version"
```bash
# Use Nix Java
nix-shell -p jdk17
./gradlew assembleDebug
```

### Error: "AAPT2 error"
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
./gradlew assembleDebug
```

### Error: "Kotlin daemon connection failed"
```bash
./gradlew --stop
./gradlew assembleDebug --no-daemon
```

---

## Step 7: If All Else Fails - Use Pre-built APK

Request the pre-built APK from project owner:
- Location: `app/build/outputs/apk/debug/app-debug.apk`
- Transfer via Google Drive or similar
- Install directly on Android device/emulator

---

## Build Success Indicators ✅

When build succeeds, you'll see:
```
BUILD SUCCESSFUL in Xm Xs
XX actionable tasks: XX executed
```

And file exists:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## After Successful Build

Continue with Phase 6 (Emulator Setup) from IDX_SETUP_GUIDE.md
