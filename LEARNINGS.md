# Project Learnings

## System Architecture
- ZenDroid uses a "hardened" approach to minimize digital distractions via Accessibility Services.
- Architecture: Kotlin + Compose UI + Hilt DI + Room Persistence.

## Technical Insights
- **AndroidX Compatibility**: Always set `android.useAndroidX=true` in `gradle.properties` when using modern libraries to avoid AAR metadata errors.
- **Theme Bridging**: When using Material3 in Compose but needing XML themes (e.g., for `InterventionActivity` overlays), use `Theme.MaterialComponents` as a parent and add the `com.google.android.material:material` dependency.
- **Composable Context**: `@Composable` invocations are strictly limited to Composable functions. Standard utility wrappers (like performance tracers) will break the context if not specifically designed for Compose.
- **Navigation DI**: Using `hiltViewModel()` in Composables requires the `androidx.hilt:hilt-navigation-compose` dependency.
- **Adaptive Icons**: Modern Android builds fail if `ic_launcher` is missing; always provide adaptive XML icons in `anydpi-v26`.
