# Verification — 1.0.3

Verified on September 11, 2026.

## Completed display

Pressing equals retains the completed equation in a smaller muted line above a larger answer. Backspace restores the original editing layout and expression. The completed equation represents the operation that produced the current answer, including after repeated equals; it is separate from the operation that the next equals press would perform.

The display reuses the existing saved undo state, so 1.0.2 calculations restore with their equation and answer after updating. Snapshot format, arithmetic, commas, history, and memory are unchanged. Legacy results without an original equation use their saved value as the equation.

A 260 ms transition animates text size, position, and color between editing and results. Repeated equals has a short fade and movement. A new key can interrupt the transition; restored screens render immediately, and disabled Android animations skip it.

Calculator keyboard shortcuts are handled before focused buttons, so Enter calculates consistently instead of activating a toolbar control. Dialogs retain their own keyboard input.

## Local build

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
BUILD SUCCESSFUL
56 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 12 warnings
```

Six new engine tests cover the displayed completed equation for first and repeated equals, percentages, pending input, process recreation, edits and errors, and lone or legacy values. The existing undo and number-formatting tests continue to pass. The existing lint warnings concern Android configuration, view constructors, localization, and right-to-left layout.

## Android and animation checks

`tools/completed_display_regression.py` exercises the dedicated Android 15/API 35 emulator. Checks covered:

- Updating the published 1.0.2 APK without uninstalling or clearing data preserves the completed equation, undo, history, and memory.
- The requested `5,645+44,646` example remains above `50,291`. The result is larger after equals; backspace returns the equation to its larger editing size and a second backspace edits it.
- Repeated equals displays the latest equation, and undo retains pending trailing input.
- Completed display and undo survive process restart and portrait/landscape rotation. Dark mode maintains the smaller muted equation and brighter answer.
- Division errors remain visible and editable. Rapid equals, backspace, and numeric input settle into the correct display.
- Hardware Enter completes and repeats without activating focused toolbar buttons. Calculator shortcuts leave converter dialog input working normally.
- Disabling Android animations produces the final layout immediately and leaves undo/editing functional.

Portrait, landscape, dark-mode, and undo screenshots were visually inspected. A screen recording and frames sampled at 50 ms intervals confirmed the equation shrinking and moving upward as the answer grows and darkens, with the reverse movement on undo and no observed flashing or clipping. Screenshots, video, and frame sheets are saved under the gitignored `dist` directory. Testing was performed on an emulator, not a physical phone.

The initial keyboard-focus failure was reproduced and fixed. Its checks, rapid input, disabled animations, and converter keyboard input passed against the final APK; display and lifecycle checks passed on the same layout implementation before the keyboard-only correction.

## APK

- Version: 1.0.3, version code 4; Android 8.0/API 26 or later.
- Signature: verified Android debug signature, matching the published 1.0.2 APK for in-place updates.
- SHA-256: `18207fcfdaea729209f491b660f21a0f9797347b632732834203967a1a437eb8`.

The public release contains the installable APK, optional APK checksum, and MIT license.
