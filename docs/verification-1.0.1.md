# Verification — 1.0.1

Verified on September 9, 2026.

## Regression

In 1.0.0, entering `2265+500+1700+1000+15800+12200+2500+19300+3500+` leaves the result preview blank and pressing equals does nothing. The trailing operator made the expression incomplete; the input did not exceed the number or expression limits.

Version 1.0.1 keeps the completed subtotal visible while waiting for another operand. Equals evaluates that completed expression, producing **58765**, and history records the equation without a dangling operator. Errors in the completed expression, such as `8÷0+`, are still reported.

## Local build

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug
BUILD SUCCESSFUL
31 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 12 warnings
```

Seven regression tests were added and two previous expectations for trailing operators updated. Before the engine fix, all nine changed cases failed; after the fix, all 31 tests passed. Coverage includes continued editing, precedence, percentages, pending unary signs, repeated equals, memory, error recovery, and saved state.

## Android upgrade and UI checks

`tools/equals_regression.py` runs on the dedicated Android 15/API 35 emulator. It reproduces the original failure using the published 1.0.0 APK, then installs 1.0.1 over it with `adb install -r` without clearing data or uninstalling the app.

Verified behavior:

- The original pending expression restores with a visible `58765` subtotal; tapping equals displays `58765` as the answer.
- Existing history and memory survive the update; completed equations have no dangling operator in history.
- Continuing the original pending sum with `100` gives `58865`.
- Trailing addition, subtraction, multiplication, and division operators retain the subtotal and allow equals to finish.
- Pending expressions and division errors survive a process restart; keyboard Enter also completes the calculation.

The test saves before/preview/result screenshots under the gitignored `dist` directory. The preview and final answer were visually inspected. Testing was performed on the emulator, not a physical phone.

## APK

- Version: 1.0.1, version code 2; Android 8.0/API 26 or later.
- Signature: verified Android debug signature, matching the published 1.0.0 APK so it can update in place.
- SHA-256: `63cbde07f5072f9de181fc65b7f5fd640961ae65e7a9eb597b1cdb51cb1ee55a`.

The public release contains the installable APK, optional APK checksum, and MIT license.
