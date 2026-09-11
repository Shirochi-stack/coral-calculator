"""Verify operators and sign changes continue the completed equation on a test AVD.

Usage: python tools/operator_continuation_regression.py --serial emulator-5580 \
    --old-apk dist/Coral-Calculator-1.0.4.apk \
    --new-apk dist/Coral-Calculator-1.0.5.apk

Uses only coral_calculator_api35, upgrades without clearing calculator data, and
writes screenshots plus a short animation recording under dist. Continued
expressions retain standard precedence: 2+3, Equals, Multiply, 4, Equals is 14.
"""

import argparse
import pathlib
import subprocess
import time

from completed_display_regression import CompletedDisplayRegression
from equals_regression import AVD, PACKAGE


OPERATORS = (
    ("Add", "+", "10", "KEYCODE_PLUS"),
    ("Subtract", "−", "6", "KEYCODE_MINUS"),
    ("Multiply", "×", "11", "KEYCODE_STAR"),
    ("Divide", "÷", "6.5", "KEYCODE_SLASH"),
)


class OperatorContinuationRegression(CompletedDisplayRegression):
    def run(self, old_apk, new_apk):
        avd_name = self.adb("emu", "avd", "name").splitlines()[0].strip()
        if avd_name != AVD:
            raise RuntimeError(f"Use the dedicated {AVD} AVD; found {avd_name!r}")
        if self.shell("getprop", "sys.boot_completed") != "1":
            raise RuntimeError("Wait for the test emulator to finish booting")
        self.test_device_verified = True
        self.output.mkdir(parents=True, exist_ok=True)
        settings = (("system", "accelerometer_rotation", "0"),
                    ("system", "user_rotation", "0"),
                    ("global", "animator_duration_scale", "1"))
        saved = [(namespace, key, self.shell("settings", "get", namespace, key))
                 for namespace, key, _ in settings]
        try:
            for namespace, key, value in settings:
                self.shell("settings", "put", namespace, key, value)
            self.check_upgrade(old_apk, new_apk)
            self.check_operator_parity()
            self.check_repeat_pending_and_errors()
            self.check_keyboard()
            self.check_sign_continuation()
            self.record_multiply_transition()
            print("PASS: v1.0.4 completed state survives upgrade; every operator restores "
                  "the equation with standard precedence; undo/delete, repeated Equals, "
                  "pending-operator normalization, pending-state restart, zero-division "
                  "recovery, rapid keyboard operators, and completed-equation sign "
                  "changes work", flush=True)
        finally:
            for namespace, key, value in saved:
                if value == "null":
                    self.shell("settings", "delete", namespace, key)
                else:
                    self.shell("settings", "put", namespace, key, value)

    def check_upgrade(self, old_apk, new_apk):
        print("Checking saved completed equation after v1.0.4 upgrade...", flush=True)
        self.adb("install", "-r", "-d", old_apk)
        self.restart()
        self.enter("5+3=")
        self.display("5+3", "8", completed=True)
        self.shell("am", "force-stop", PACKAGE)
        self.adb("install", "-r", new_apk)
        self.launch()
        self.display("5+3", "8", completed=True)
        self.keys("Multiply")
        self.display("5+3×", "8", completed=False)
        self.snapshot("operator-continuation-multiply-edit.png")
        self.keys("2", "Equals")
        self.display("5+3×2", "11", completed=True)
        self.snapshot("operator-continuation-multiply-complete.png")

    def check_operator_parity(self):
        print("Checking touch operators and subtraction undo/delete...", flush=True)
        # Multiply has already been checked on the upgraded saved equation.
        for label, symbol, answer, _ in OPERATORS:
            if label == "Multiply":
                continue
            self.enter("5+3=")
            self.keys(label)
            self.display("5+3" + symbol, "8", completed=False)
            self.keys("2", "Equals")
            self.display("5+3" + symbol + "2", answer, completed=True)
            if label == "Subtract":
                self.keys("Backspace")
                self.display("5+3−2", "6", completed=False)
                self.keys("Backspace")
                self.display("5+3−", "8", completed=False)

    def check_repeat_pending_and_errors(self):
        print("Checking latest repeated equation, pending operators, restart and errors...", flush=True)
        self.enter("5+3==")
        self.display("8+3", "11", completed=True)
        self.keys("Multiply")
        self.display("8+3×", "11", completed=False)
        self.keys("2", "Equals")
        self.display("8+3×2", "14", completed=True)

        # A saved trailing operator is removed before the newly chosen one is added.
        self.enter("5+3*=")
        self.keys("Divide")
        self.display("5+3÷", "8", completed=False)
        self.restart()
        self.display("5+3÷", "8", completed=False)
        self.keys("Multiply")
        self.display("5+3×", "8", completed=False)
        self.restart()
        self.display("5+3×", "8", completed=False)
        self.keys("2", "Equals")
        self.display("5+3×2", "11", completed=True)

        self.enter("5+3=")
        self.keys("Divide", "0", "Equals")
        self.expect("Cannot divide by zero")
        self.keys("Backspace")
        self.display("5+3÷", "8", completed=False)
        self.keys("2", "Equals")
        self.display("5+3÷2", "6.5", completed=True)

    def check_keyboard(self):
        print("Checking rapid keyboard Equals/operator sequences for all four operators...", flush=True)
        for _, symbol, answer, keycode in OPERATORS:
            self.enter("5+3")
            self.shell("input", "keyevent", "KEYCODE_ENTER", keycode, "KEYCODE_2", "KEYCODE_ENTER")
            self.display("5+3" + symbol + "2", answer, completed=True)

    def check_sign_continuation(self):
        print("Checking completed-equation sign changes, undo and restart...", flush=True)
        self.enter("5+3=")
        self.keys("Change sign")
        self.display("5+−3", "2", completed=False)
        self.snapshot("operator-continuation-sign-edit.png")
        self.keys("Change sign")
        self.display("5+3", "8", completed=False)
        self.keys("Change sign")
        self.restart()
        self.display("5+−3", "2", completed=False)
        self.keys("Equals")
        self.display("5+−3", "2", completed=True)
        self.keys("Backspace")
        self.display("5+−3", "2", completed=False)
        self.keys("Equals")
        self.restart()
        self.display("5+−3", "2", completed=True)
        # The completed equation already has a negative final operand: ± removes
        # that operand's sign instead of negating the displayed answer 2.
        self.keys("Change sign")
        self.display("5+3", "8", completed=False)

    def record_multiply_transition(self):
        print("Recording multiplication continuation with standard precedence...", flush=True)
        self.enter("2+3=")
        self.display("2+3", "5", completed=True)
        remote = "/sdcard/coral-operator-continuation.mp4"
        recorder = subprocess.Popen(
            ["adb", "-s", self.serial, "shell", "screenrecord", "--time-limit", "3",
             "--bit-rate", "6000000", remote],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding="utf-8",
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
        try:
            time.sleep(.7)
            self.keys("Multiply")
            time.sleep(.7)
            self.keys("4", "Equals")
            output, _ = recorder.communicate(timeout=12)
            if recorder.returncode != 0:
                raise RuntimeError(f"screenrecord failed: {output}")
        finally:
            if recorder.poll() is None:
                recorder.terminate()
                recorder.communicate(timeout=5)
        self.adb("pull", remote, self.output / "operator-continuation-transition.mp4")
        self.display("2+3×4", "14", completed=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--old-apk", required=True, type=pathlib.Path)
    parser.add_argument("--new-apk", required=True, type=pathlib.Path)
    args = parser.parse_args()
    for apk in (args.old_apk, args.new_apk):
        if not apk.is_file():
            parser.error(f"APK does not exist: {apk}")
    output = pathlib.Path(__file__).resolve().parent.parent / "dist"
    regression = OperatorContinuationRegression(args.serial, output)
    try:
        regression.run(args.old_apk.resolve(), args.new_apk.resolve())
    except Exception:
        if regression.test_device_verified and output.is_dir():
            try:
                regression.snapshot("operator-continuation-failure.png")
                regression.nodes()
                regression.adb("pull", "/sdcard/coral-equals-test.xml",
                               output / "operator-continuation-failure.xml")
            except Exception as error:
                print(f"Could not capture failure diagnostics: {error}", flush=True)
        raise


if __name__ == "__main__":
    main()
