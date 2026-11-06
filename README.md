# FF MAX ESP Training Simulator

A standalone, offline Android practice simulator. It does **not** connect to, read, modify, inject into, or interact with Free Fire MAX. All bots, radar blips, ESP-style brackets, and distances are generated locally inside this app.

## Included
- Landscape practice arena with a perspective 3D-style training scene
- Simulated target/bot indicators, distance labels, and radar
- ESP-style overlay and radar toggles
- Touch movement and aim controls
- Tap-to-shoot target practice, hit counter, accuracy, and headshot drill toggle
- Adjustable sensitivity value (tap − / +)
- Dark premium-style HUD

## Build an APK
1. Install **Android Studio** (latest stable) on a computer.
2. Open this project folder (`FF_MAX_ESP_Training_Simulator`) in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Android Studio will show the generated APK location (normally `app/build/outputs/apk/debug/app-debug.apk`).
6. Transfer the APK to your Android phone and install it. You may need to allow installation from that file manager.

## Notes
- Package: `com.sunil.ffmaxsim`
- Minimum Android: 6.0 (API 23)
- Target SDK: 35
- No internet permission or external game integration is included.
- This is a first playable prototype. The arena is rendered locally with Android Canvas perspective geometry; it is not a full polygonal 3D engine build.
