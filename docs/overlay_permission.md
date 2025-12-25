# Overlay Permission (Draw Over Other Apps)

## Why is it needed?
On Android 12 and above, apps are restricted from starting Foreground Services directly from the background (e.g., from a WorkManager job). 
There is an exemption for apps that hold the `SYSTEM_ALERT_WINDOW` (Draw over other apps) permission.

**Project Lazarus** (`ResurrectionWorker`) relies on this exemption to restart the `GuardianService` if it gets killed by the system/OEM battery manager.

## User Experience
1. **Initial Check**: On app launch (`MainActivity`), we check if the permission is granted.
   - If missing, an `OverlayPermissionDialog` prompts the user.
   - User grants permission in System Settings.
2. **Background Check**: If the permission is revoked later, `ResurrectionWorker` will detect this failure during its periodic run.
   - It posts a **Notification** ("Shield Permission Missing").
   - Tapping the notification opens the overlay permission settings directly.

## Implementation Details
- **MainActivity**: Checks `Settings.canDrawOverlays()` in `onResume()`.
- **ResurrectionWorker**: Checks before calling `startForegroundService`. If missing, calls `showOverlayPermissionNotification()`.
- **Dialog/Notification**: Both direct the user to `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` with the app's package URI.
