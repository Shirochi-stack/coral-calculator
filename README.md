# Coral Calculator

A simple, offline calculator for Android with circular keys, coral accents, and answers that update as you type.

[Download the latest release](https://github.com/Shirochi-stack/coral-calculator/releases/latest) · Android 8.0 or later

<img width="300" src="https://github.com/user-attachments/assets/8e817e2f-632a-4c0c-92b4-abaedad6d420" />


## Features

- **Everyday calculations:** addition, subtraction, multiplication, division, percentages, and decimal numbers.
- **Live answers:** see the result while entering a calculation, with multiplication and division handled before addition and subtraction.
- **Memory and history:** store a value in memory or reuse any of your last 100 completed calculations.
- **Unit conversion:** convert length, mass, and temperature without an internet connection.
- **Currency conversion:** calculate amounts using an exchange rate you enter yourself.
- **Your preferred view:** switch to dark appearance or fullscreen, rotate to landscape, and turn key vibration on or off.
- **Convenient input:** labeled buttons, hardware keyboard support, and long-press result copying.

## Install

1. Open the [latest release](https://github.com/Shirochi-stack/coral-calculator/releases/latest) and download the `.apk` file under **Assets**.
2. Open the downloaded file on your Android device.
3. If Android asks, allow your browser or file manager to install apps from this source, then tap **Install**.

The current APK is a debug-signed preview build. The APK is the only download needed to install the app; `SHA256SUMS.txt` is available for optional download verification.

## Using the calculator

- Enter a calculation to see a live answer. Tap **=** to finish and add it to history. Tap **=** again to repeat the last operation.
- Use **AC** to clear the calculation, **⌫** to delete the last character, and **+/−** to change the current number's sign. **AC** keeps your memory and history.
- For percentages, `200 + 10%` gives `220`, while `200 × 10%` gives `20`.
- Tap the **history icon** above the memory row to view completed calculations. Tap an entry to use its result.
- Long-press the expression or result to copy the current answer.

### Memory

| Key | Action |
| --- | --- |
| **mc** | Clear the stored value. |
| **m+** | Add the current value to memory. |
| **m−** | Subtract the current value from memory. |
| **mr** | Replace the current number with the stored value. |

The **mr** key turns coral when memory contains a nonzero value. Your current calculation and memory are saved when you close the app.

### Converters and settings

The **unit converter** and **currency converter** sit beside history. Choose the units or currencies, enter an amount, and tap **Use result** to bring the converted value into the calculator. Currency conversion requires a positive exchange rate; the app does not fetch live rates.

Use the **top-right settings icon** to change appearance or key vibration. The icon beside it toggles fullscreen.

## Privacy

Coral Calculator works entirely offline. It has no ads, analytics, accounts, or network permission. Calculation history and settings are stored on your device. Clear saved calculations from the **History** dialog.

<details>
<summary>Build from source</summary>

Use JDK 17 and Android SDK platform 35 with build-tools 35.0.0. Set `ANDROID_HOME` to the SDK directory, or configure `sdk.dir` in a local `local.properties` file.

On Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

On macOS or Linux:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. The project includes the Gradle wrapper, and GitHub Actions builds and checks each push and pull request.

Arithmetic uses 16 significant decimal digits and supports decimal exponents from −100 to 100. See [verification results](docs/verification.md) for build and emulator checks.

</details>
