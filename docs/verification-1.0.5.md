# Verification — 1.0.5

Verified on September 11, 2026.

## Consistent operator continuation

After equals, all four binary operators (`+`, `−`, `×`, and `÷`) restore the displayed equation and append the selected operator. Normal precedence applies to the full expression: `2+3`, equals, `×4`, equals displays `2+3×4` with answer `14`.

The sign-change key (`+/−`) also resumes the equation and toggles its final operand. `5+3`, equals, sign change becomes `5+−3` with preview `2`; another sign change returns it to `5+3`. This also applies to percentage and already-negative operands.

The operators share the existing continuation path, transition back to editing, pending-operator normalization, saved state, and undo behavior. Repeated equals continues from the most recent completed equation. At the expression-length limit, an operator leaves the saved answer and its undo intact.

## Local build

```text
gradlew.bat testDebugUnitTest lintDebug assembleDebug bundleRelease
BUILD SUCCESSFUL
67 tests, 0 failures, 0 errors, 0 skipped
Android lint: 0 errors, 12 warnings
```

Existing continuation tests were expanded in place to cover all four operators across normal precedence, repeated equals, percentages, pending suffixes, saved-state recreation, undo/editing, errors, fallback values, and length limits. Assertions for the former result-only multiplication/division behavior were updated. The existing lint warnings concern Android configuration, view constructors, localization, and right-to-left layout.

Three additional sign-change regressions cover restoring and reversing the final operand, negative and percentage operands, saved state and repeated equals, later undo, and length-limit rejection without discarding the saved answer.

## Android checks

The focused `tools/operator_continuation_regression.py` checks passed on the dedicated Android 15/API 35 emulator:

- A completed equation saved by the published 1.0.4 APK survives the in-place update and continues correctly with multiplication.
- After `5+3 =`, appending `+2`, `−2`, `×2`, or `÷2` keeps the full equation and gives `10`, `6`, `11`, or `6.5`, respectively.
- Subtraction undo/delete, latest repeated equations, pending-operator replacement, multiplication/division state after restart, and division-by-zero recovery work.
- Rapid keyboard sequences give the same results as the onscreen operators.
- Sign change after `5+3 =` restores `5+−3` with preview `2`; a second press restores `5+3`. Later equals/undo, saved editing and completed states, and an originally negative last operand all work.

Multiplication editing/completed screenshots and the final APK's sign-change screenshot were visually inspected. A short recording and sampled frames confirm the existing transition returns the equation to editing with the chosen operator. Artifacts are saved under the gitignored `dist` directory. Testing was performed on an emulator, not a physical phone.

Binary-operator checks passed before the sign-change extension; its focused checks then passed against the final APK, without repeating the unchanged binary suite.

## APK

- Version: 1.0.5, version code 6; Android 8.0/API 26 or later.
- Signature: verified Android debug signature, matching the published 1.0.4 APK for in-place updates.
- SHA-256: `87fd7049777e0fb1802a1c83364247e2f341f0e9178341f95fcd4e4d2304baec`.

The public release contains the installable APK, optional APK checksum, and MIT license.
