# Performance Profiling Guide

## Goal
Ensure ZenDroid runs efficiently on low-end devices (2GB RAM) without Out-Of-Memory (OOM) crashes.

## Key Areas to Monitor
1. **IconCache**: Should not exceed `Constants.ICON_CACHE_MAX_ICONS` (100) bitmaps.
2. **HistoryScreen**: Should handle large lists without jank or massive memory spikes.

## How to Profile
1. **Connect Device**: Low-end device preferred, or Emulator with 1.5GB/2GB RAM.
2. **Android Studio**: View > Tool Windows > Profiler.
3. **Session**:
   - Launch app.
   - Go to History.
   - Scroll rapidly.
   - Create new sessions.

## Benchmarks
- **Java/Kotlin Heap**: Should stay steady (sawtooth pattern), not continuously rising.
- **Native Heap**: Where Bitmaps live (Android 8+). Watch for "Graphics" memory.
   - `IconCache` should correspond to ~20-50MB max depending on screen density.
- **Jank**: No dropped frames on History scroll.

## Troubleshooting
- If memory rises indefinitely: Check for Bitmap leaks or uncached icons.
- If History janks: Check if `getAllSessionsFlow` is returning too many items (limit should be 100).
