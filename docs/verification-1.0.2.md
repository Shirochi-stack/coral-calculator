# Verification — 1.0.2

Verified on September 11, 2026.

## Undo equals and number formatting

After a completed calculation, the first backspace restores the expression that produced the answer. Further backspaces edit that expression normally. Repeated equals saves the equation for the latest answer, and a trailing pending operator is preserved when undoing. The saved undo state survives restarting the app. Existing 1.0.1 snapshots restore without inventing undo information they did not contain.

Thousands separators are added only for display: expressions, answers, history, and conversion results. Calculator state, arithmetic, stored history values, clipboard values, and converter inputs remain plain decimal strings. Fractional digits and precision are preserved.

## Local build

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
BUILD SUCCESSFUL
50 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 12 warnings
```

The 42 engine tests include 11 new regressions for undo, subsequent edits, repeated equals, percentages, memory, error recovery, and valid, legacy, or malformed saved state. Eight formatter tests cover grouping boundaries, the requested example, signs, fractional digits, incomplete input, operators, very large values, existing grouping, and exponent notation. The existing lint warnings concern Android configuration, view constructors, localization, and right-to-left layout.

## Android upgrade and UI checks

`tools/undo_grouping_regression.py` runs on the dedicated Android 15/API 35 emulator. It seeds synthetic state in the published 1.0.1 APK, then installs 1.0.2 with `adb install -r` without clearing data or uninstalling the app.

Verified behavior:

- Existing expression, history, and memory survive the update. A result completed in 1.0.1 retains ordinary backspace behavior because that version did not save the original expression.
- `5,645+44,646` displays a `50,291` preview and answer. Backspace restores the entire expression, the next backspace changes its final number to `4,464`, and editing and evaluating again works.
- After repeated equals, backspace restores the latest equation. A pending trailing `+` is restored exactly.
- Force-stop and relaunch preserve undo; keyboard Delete restores the expression and then edits normally.
- `1,000`, `10,000`, `100,000`, negative decimals, and history entries display grouping correctly. Reusing a history result supports further arithmetic.
- The unit converter receives plain input `3048`, displays `10,000 ft`, and returns a usable numeric result to the calculator.

Result and restored-expression screenshots under the gitignored `dist` directory were visually inspected. The existing layout is preserved. Testing was performed on an emulator, not a physical phone.

## APK

- Version: 1.0.2, version code 3; Android 8.0/API 26 or later.
- Signature: verified Android debug signature, matching the published 1.0.1 APK for in-place updates.
- SHA-256: `d5f25e8bae3a261ba56af9e3f8d68f752b58667625a6634d91ec339c9f952cce`.

The public release contains the installable APK, optional APK checksum, and MIT license.
