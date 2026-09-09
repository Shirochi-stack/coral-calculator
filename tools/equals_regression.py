"""Verify the trailing-operator fix and an in-place APK upgrade on the test AVD.

Usage: python tools/equals_regression.py --serial emulator-5580 \
    --old-apk dist/Coral-Calculator-1.0.0.apk --new-apk path/to/new.apk

Requires adb on PATH and the dedicated coral_calculator_api35 emulator. Changes
its calculator state and history, but never clears app data or uninstalls it.
Screenshots and failure diagnostics are written under dist, not docs.
"""

import argparse
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET


PACKAGE = "com.coral.calculator"
AVD = "coral_calculator_api35"
SUM = "2265+500+1700+1000+15800+12200+2500+19300+3500"
PENDING_SUM = SUM + "+"
KEY_NAMES = {
    "+": "Add", "-": "Subtract", "*": "Multiply", "/": "Divide",
    ".": "Decimal point", "%": "Percent", "=": "Equals",
}


class Regression:
    def __init__(self, serial, output):
        self.serial = serial
        self.output = output
        self.home = None
        self.test_device_verified = False

    def adb(self, *arguments):
        result = subprocess.run(
            ["adb", "-s", self.serial, *map(str, arguments)],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding="utf-8",
            errors="replace", check=True, timeout=60,
        )
        return result.stdout.strip()

    def shell(self, *arguments):
        return self.adb("shell", *arguments)

    def nodes(self):
        self.shell("uiautomator", "dump", "/sdcard/coral-equals-test.xml")
        xml = self.shell("cat", "/sdcard/coral-equals-test.xml")
        return list(ET.fromstring(xml).iter("node"))

    @staticmethod
    def find(label, tree):
        for node in tree:
            description = node.get("content-desc", "")
            if (description == label
                    or node.get("text", "").casefold() == label.casefold()
                    or (label == "Memory recall" and description.startswith(label))):
                return node
        observed = [node.get("content-desc") or node.get("text") for node in tree]
        raise AssertionError(f"Missing {label!r}; visible labels: {observed}")

    def expect(self, label):
        return self.find(label, self.nodes())

    def expression(self, value):
        self.expect("Expression: " + value)

    def preview(self, value):
        self.expect("Result: " + value)

    def tap(self, label, tree=None):
        tree = self.nodes() if tree is None else tree
        # Expression/result views can contain the same text as a button.
        clickable = [node for node in tree if node.get("clickable") == "true"]
        node = self.find(label, clickable)
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
        self.shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)
        time.sleep(.1)

    def keys(self, *labels):
        for label in labels:
            self.tap(label, self.home)

    def enter(self, expression):
        self.keys("All clear")
        for character in expression:
            self.keys(KEY_NAMES.get(character, character))

    def launch(self):
        self.shell("am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
        time.sleep(.5)
        self.home = self.nodes()

    def restart(self):
        self.shell("am", "force-stop", PACKAGE)
        self.launch()

    def snapshot(self, name):
        self.shell("screencap", "-p", "/sdcard/coral-equals-test.png")
        self.adb("pull", "/sdcard/coral-equals-test.png", self.output / name)

    def history(self, *entries):
        self.tap("Calculation history", self.home)
        tree = self.nodes()
        for equation, result in entries:
            self.find(equation + "\n= " + result, tree)
        bad_equation = PENDING_SUM + "\n= 58765"
        assert all(node.get("text") != bad_equation for node in tree), (
            "History contains a dangling operator in the completed equation"
        )
        self.tap("Done", tree)

    def run(self, old_apk, new_apk):
        avd_name = self.adb("emu", "avd", "name").splitlines()[0].strip()
        if avd_name != AVD:
            raise RuntimeError(f"Use the dedicated {AVD} AVD; found {avd_name!r}")
        if self.shell("getprop", "sys.boot_completed") != "1":
            raise RuntimeError("Wait for the test emulator to finish booting")
        self.test_device_verified = True
        self.output.mkdir(parents=True, exist_ok=True)
        self.shell("settings", "put", "system", "accelerometer_rotation", "0")
        self.shell("settings", "put", "system", "user_rotation", "0")

        print("Installing v1.0.0 and reproducing the reported failure...", flush=True)
        # Debug APKs allow downgrade so the same regression can be rerun.
        self.adb("install", "-r", "-d", old_apk)
        self.restart()
        self.keys("All clear", "Memory clear", "7", "Memory add")
        self.enter("12+3=")
        self.expression("15")
        self.enter(PENDING_SUM)
        self.expression(PENDING_SUM)
        self.preview("")
        self.keys("Equals")
        self.expression(PENDING_SUM)
        self.preview("")
        self.snapshot("equals-regression-before.png")
        self.shell("am", "force-stop", PACKAGE)

        print("Upgrading without clearing data and checking the restored sum...", flush=True)
        self.adb("install", "-r", new_apk)
        self.launch()
        self.expression(PENDING_SUM)
        self.preview("58765")
        self.snapshot("equals-regression-preview.png")
        self.keys("Equals")
        self.expression("58765")
        self.snapshot("equals-regression-result.png")
        self.history((SUM, "58765"), ("12+3", "15"))
        self.keys("All clear", "Memory recall")
        self.expression("7")

        print("Checking continued entry, other pending operators and keyboard Enter...", flush=True)
        self.enter(PENDING_SUM)
        self.preview("58765")
        self.keys("1", "0", "0", "Equals")
        self.expression("58865")
        for operator in "-*/":
            self.enter("12+3" + operator)
            self.preview("15")
            self.keys("Equals")
            self.expression("15")

        self.enter("12+3+")
        self.preview("15")
        self.restart()
        self.expression("12+3+")
        self.preview("15")
        self.shell("input", "keyevent", "KEYCODE_ENTER")
        self.expression("15")
        self.history(("12+3", "15"))

        print("Checking that an invalid completed calculation still reports its error...", flush=True)
        self.enter("8/0+")
        self.keys("Equals")
        self.expect("Cannot divide by zero")
        self.restart()
        self.expect("Cannot divide by zero")

        # Leave the fixed exact calculation visible for manual inspection.
        self.enter(PENDING_SUM)
        self.keys("Equals")
        self.expression("58765")
        print("PASS: old failure reproduced; in-place upgrade retains expression, history, "
              "and memory; subtotal/Equals, continued entry, pending operators, process "
              "restart, keyboard Enter, and division errors work", flush=True)


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
    regression = Regression(args.serial, output)
    try:
        regression.run(args.old_apk.resolve(), args.new_apk.resolve())
    except Exception:
        if regression.test_device_verified and output.is_dir():
            try:
                regression.snapshot("equals-regression-failure.png")
                regression.shell("uiautomator", "dump", "/sdcard/coral-equals-test.xml")
                regression.adb("pull", "/sdcard/coral-equals-test.xml",
                               output / "equals-regression-failure.xml")
            except Exception as diagnostic_error:
                print(f"Could not capture failure diagnostics: {diagnostic_error}", flush=True)
        raise


if __name__ == "__main__":
    main()
