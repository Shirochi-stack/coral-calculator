"""Check completed-equation display and transitions on the dedicated test AVD.

Usage: python tools/completed_display_regression.py --serial emulator-5580 \
    --old-apk dist/Coral-Calculator-1.0.2.apk \
    --new-apk dist/Coral-Calculator-1.0.3.apk

Uses native controls and keyboard input. Upgrades without clearing calculator
data, changes synthetic test history/memory, and temporarily adjusts rotation,
theme and animation settings. Images/video/diagnostics are written under dist.
"""

import argparse
import pathlib
import re
import subprocess
import time

from equals_regression import AVD, PACKAGE
from undo_grouping_regression import UndoGroupingRegression


class CompletedDisplayRegression(UndoGroupingRegression):
    @staticmethod
    def bounds(node):
        return tuple(map(int, re.findall(r"\d+", node.get("bounds", ""))))

    def display(self, equation, result, completed=None):
        tree = self.nodes()
        expression_view = self.find("Expression: " + equation, tree)
        result_view = self.find("Result: " + result, tree)
        assert expression_view.get("text") == equation, expression_view.attrib
        assert result_view.get("text") == result, result_view.attrib
        ex, ey, er, eb = self.bounds(expression_view)
        rx, ry, rr, rb = self.bounds(result_view)
        assert ex < er and ey < eb and rx < rr and ry < rb
        assert eb <= ry, f"Equation must be above result: {expression_view.attrib}, {result_view.attrib}"
        if completed is True:
            assert rb - ry > eb - ey, "Completed result must be taller than its equation"
        elif completed is False:
            assert eb - ey > rb - ry, "Editing equation must be taller than its live preview"
        return tree

    def set_dark(self, enabled):
        self.tap("Settings", self.home)
        tree = self.nodes()
        switch = self.find("Dark appearance", tree)
        if (switch.get("checked") == "true") != enabled:
            self.tap("Dark appearance", tree)
        self.tap("Done")
        self.home = self.nodes()

    def rotate(self, rotation):
        self.shell("settings", "put", "system", "user_rotation", rotation)
        time.sleep(.8)
        self.home = self.nodes()

    def record_transition(self):
        self.enter("5645+44646")
        self.display("5,645+44,646", "50,291")
        remote = "/sdcard/coral-completed-display.mp4"
        recorder = subprocess.Popen(
            ["adb", "-s", self.serial, "shell", "screenrecord", "--time-limit", "8",
             "--bit-rate", "6000000", remote],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding="utf-8",
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
        try:
            time.sleep(1)
            self.keys("Equals")
            time.sleep(1.2)
            self.keys("Backspace")
            time.sleep(1.2)
            self.keys("Equals")
            output, _ = recorder.communicate(timeout=20)
            if recorder.returncode != 0:
                raise RuntimeError(f"screenrecord failed: {output}")
        finally:
            if recorder.poll() is None:
                recorder.terminate()
                recorder.communicate(timeout=5)
        self.adb("pull", remote, self.output / "completed-display-transition.mp4")
        self.display("5,645+44,646", "50,291")

    def run(self, old_apk, new_apk):
        avd_name = self.adb("emu", "avd", "name").splitlines()[0].strip()
        if avd_name != AVD:
            raise RuntimeError(f"Use the dedicated {AVD} AVD; found {avd_name!r}")
        if self.shell("getprop", "sys.boot_completed") != "1":
            raise RuntimeError("Wait for the test emulator to finish booting")
        self.test_device_verified = True
        self.output.mkdir(parents=True, exist_ok=True)
        saved_rotation = self.shell("settings", "get", "system", "user_rotation")
        saved_accelerometer = self.shell("settings", "get", "system", "accelerometer_rotation")
        saved_animation = self.shell("settings", "get", "global", "animator_duration_scale")
        self.shell("settings", "put", "system", "accelerometer_rotation", "0")
        self.shell("settings", "put", "system", "user_rotation", "0")
        self.shell("settings", "put", "global", "animator_duration_scale", "1")
        try:
            self.check_upgrade(old_apk, new_apk)
            self.check_completed_and_undo()
            self.check_lifecycle_and_theme()
            self.check_errors_and_interruptions()
            print("Recording Equals and undo transitions...", flush=True)
            self.record_transition()
            print("PASS: v1.0.2 upgrade preserves completed equation, undo, history and memory; "
                  "completed equation/result ordering, undo/edit, repeated Equals, process restart, "
                  "rotation, dark theme, errors, rapid input and disabled animations work", flush=True)
        finally:
            for namespace, key, value in (
                    ("system", "user_rotation", saved_rotation),
                    ("system", "accelerometer_rotation", saved_accelerometer),
                    ("global", "animator_duration_scale", saved_animation)):
                if value == "null":
                    self.shell("settings", "delete", namespace, key)
                else:
                    self.shell("settings", "put", namespace, key, value)

    def check_upgrade(self, old_apk, new_apk):
        print("Seeding v1.0.2 completed equation, history and memory before upgrade...", flush=True)
        self.adb("install", "-r", "-d", old_apk)
        self.restart()
        self.set_dark(False)
        self.enter("12000")
        self.keys("Memory clear", "Memory add")
        self.enter("5645+44646=")
        self.expression("50,291")
        self.shell("am", "force-stop", PACKAGE)
        self.adb("install", "-r", new_apk)
        self.launch()
        self.display("5,645+44,646", "50,291")
        self.snapshot("completed-display-upgrade.png")
        self.grouped_history(("5,645+44,646", "50,291"))
        self.keys("Backspace")
        self.display("5,645+44,646", "50,291")
        self.keys("Backspace")
        self.display("5,645+4,464", "10,109")
        self.keys("All clear", "Memory recall")
        self.expression("12,000")
        self.keys("Add", "1", "Equals")
        self.display("12,000+1", "12,001")

    def check_completed_and_undo(self):
        print("Checking equation/result display, undo/edit and repeated Equals...", flush=True)
        self.enter("5645+44646")
        self.display("5,645+44,646", "50,291", completed=False)
        self.snapshot("completed-display-before.png")
        self.keys("Equals")
        self.display("5,645+44,646", "50,291", completed=True)
        self.snapshot("completed-display-after.png")
        self.keys("Backspace")
        self.display("5,645+44,646", "50,291", completed=False)
        self.snapshot("completed-display-undo.png")
        self.keys("Backspace", "7", "Equals")
        self.display("5,645+44,647", "50,292")
        self.enter("1000+2000==")
        self.display("3,000+2,000", "5,000")
        self.keys("Backspace")
        self.display("3,000+2,000", "5,000")
        self.keys("Backspace")
        self.display("3,000+200", "3,200")
        self.enter("1000+2000+=")
        self.display("1,000+2,000", "3,000")
        self.keys("Backspace")
        self.display("1,000+2,000+", "3,000")

    def check_lifecycle_and_theme(self):
        print("Checking restart, rotation and dark theme keep the completed display...", flush=True)
        self.enter("5645+44646=")
        self.restart()
        self.display("5,645+44,646", "50,291")
        self.rotate("1")
        self.display("5,645+44,646", "50,291")
        self.snapshot("completed-display-landscape.png")
        self.rotate("0")
        self.display("5,645+44,646", "50,291")
        self.set_dark(True)
        self.display("5,645+44,646", "50,291")
        self.snapshot("completed-display-dark.png")
        self.restart()
        self.display("5,645+44,646", "50,291")
        self.keys("Backspace")
        self.display("5,645+44,646", "50,291")
        self.set_dark(False)

    def check_errors_and_interruptions(self):
        print("Checking errors, rapid transition interruptions and disabled animations...", flush=True)
        self.enter("8/0=")
        self.expect("Cannot divide by zero")
        self.keys("Backspace", "2", "Equals")
        self.display("8÷2", "4")

        # One adb invocation dispatches the key events without screenshot/dump waits
        # between them, so Equals transitions can be interrupted by subsequent input.
        self.enter("5645+44646")
        self.shell("input", "keyevent", "KEYCODE_ENTER", "KEYCODE_DEL", "KEYCODE_9")
        self.display("5,645+446,469", "452,114")
        self.enter("5645+44646")
        self.shell("input", "keyevent", "KEYCODE_ENTER", "KEYCODE_7")
        self.expression("7")
        self.preview("")
        self.enter("1000+2000")
        self.shell("input", "keyevent", "KEYCODE_ENTER", "KEYCODE_ENTER", "KEYCODE_DEL")
        self.display("3,000+2,000", "5,000")

        self.shell("settings", "put", "global", "animator_duration_scale", "0")
        self.restart()
        self.enter("5645+44646=")
        self.display("5,645+44,646", "50,291")
        self.snapshot("completed-display-no-animation.png")
        self.keys("Backspace", "Backspace")
        self.display("5,645+4,464", "10,109")
        self.shell("settings", "put", "global", "animator_duration_scale", "1")
        self.restart()


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
    regression = CompletedDisplayRegression(args.serial, output)
    try:
        regression.run(args.old_apk.resolve(), args.new_apk.resolve())
    except Exception:
        if regression.test_device_verified and output.is_dir():
            try:
                regression.snapshot("completed-display-failure.png")
                regression.nodes()
                regression.adb("pull", "/sdcard/coral-equals-test.xml",
                               output / "completed-display-failure.xml")
            except Exception as error:
                print(f"Could not capture failure diagnostics: {error}", flush=True)
        raise


if __name__ == "__main__":
    main()
