# Verification — 1.0.4

Verified on September 11, 2026.

## Continue adding after equals

Pressing `+` after a completed calculation restores its original equation and appends one plus sign. For example, `5,645+44,646 = 50,291` continues as `5,645+44,646+`, with `50,291` still visible as the subtotal. Adding `9` completes `5,645+44,646+9 = 50,300`.

The existing transition returns the display to editing. Subsequent equals saves the expanded equation for display, history, and backspace undo. Repeated equals continues from the most recent equation; pending trailing operators are normalized to one plus sign. Existing snapshots remain compatible.

This change applies to plus. Subtraction, multiplication, and division retain their existing behavior of acting on the finished answer. If the original equation is already at the 256-character limit, pressing plus preserves the answer and its undo state.

## Local build

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
BUILD SUCCESSFUL
64 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 12 warnings
```

Eight new engine regressions cover the requested example, later undo, repeated equals, pending suffixes, saved state, percentages and rounding boundaries, other operators, legacy/lone values and errors, and the expression limit. Previous plus-after-equals expectations were updated. The existing lint warnings concern Android configuration, view constructors, localization, and right-to-left layout.

## Android checks

`tools/continue_sum_regression.py` passed on the dedicated Android 15/API 35 emulator:

- In-place update from the published 1.0.3 APK preserves the completed equation, history, and memory.
- Plus restores `5,645+44,646+` and the `50,291` subtotal; adding `9` gives `50,300` with the full equation above it. Backspace restores and then edits the expanded equation.
- Continuation works after restart, after repeated equals, and with a pending operator, without duplicate plus signs.
- Typing a new digit still starts a fresh calculation; `2+3 = ×4 =` still produces `20`.
- Rapid touch and keyboard equals/plus sequences display and calculate correctly.

Completed and continued screenshots were visually inspected. A four-second recording and sampled transition frames confirm the equation expands back into editing with the appended plus and the subtotal stays visible. Artifacts are under the gitignored `dist` directory. Testing was performed on an emulator, not a physical phone.

## APK

- Version: 1.0.4, version code 5; Android 8.0/API 26 or later.
- Signature: verified Android debug signature, matching the published 1.0.3 APK for in-place updates.
- SHA-256: `8afa0d7e10d7cca79cd455d74dfdcd63f93309cdf7b76d0af07aadde99c97734`.

The public release contains the installable APK, optional APK checksum, and MIT license.
