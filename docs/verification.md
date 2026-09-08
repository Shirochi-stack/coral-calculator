# Verification — 1.0.0

Verified on September 8, 2026.

## Build and arithmetic

Windows / JDK 17 / Android SDK 35 / Gradle 8.11.1:

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
BUILD SUCCESSFUL
24 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 10 warnings
```

Lint warnings cover English-only text construction, view-editor construction, explicit left-to-right arithmetic alignment, and the optional Android backup-rule declaration. They do not prevent compilation or installation.

The same application source also passed [GitHub Actions](https://github.com/Shirochi-stack/coral-calculator/actions/runs/34233823123), including test/lint checks and APK/AAB artifact uploads.

## Running Android app

Installed on a dedicated Android 15/API 35 x86_64 emulator, 864×1960 display at density 384. `tools/smoke_test.py` passed:

- Reference `52+96` expression with live result `148`.
- Native control bounds without overlaps in portrait and landscape.
- Equals, contextual `200+10%=220`, memory storage/recall.
- Division-by-zero error and correction with backspace.
- Repeat equals with accurate history (`8+3=11`).
- Process restart preserving repeat equals and new-entry behavior.
- Meter-to-foot conversion and transfer of the result into the calculator.
- Currency conversion disabled until an explicit positive rate is entered, then correct multiplication and result transfer.
- Dark appearance and restoration of the light theme.
- Rotation retaining the current expression and live result.

Screenshots were captured from the running app and visually inspected: [portrait](screenshot.png), [dark appearance](dark.png), [landscape](landscape.png).

Android supplies the system status/navigation bars. Their appearance differs across devices. Physical-device testing and store distribution signing were not performed.

## Artifact checks

`apksigner` validated the installable debug APK signature (APK v2, RSA 2048). Package metadata confirms `com.coral.calculator`, version 1.0.0/code 1, minimum API 26 and target API 35. `jarsigner` confirms the release AAB is unsigned.

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| Coral-Calculator-1.0.0.apk | 67012 | `72bde2b78861c32e379e742c670cae9e9598726fa7fd0a31d6b58414c3f1caa1` |
| Coral-Calculator-1.0.0-unsigned.aab | 31034 | `34ff150edc5e103e0d2b286f601e5f70d2fa86e716cd6823cdf1c375e92e5330` |
