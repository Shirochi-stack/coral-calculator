"""Verify Plus continues the completed equation on the dedicated test Android AVD.

Usage: python tools/continue_sum_regression.py --serial emulator-5580 \
    --old-apk dist/Coral-Calculator-1.0.3.apk \
    --new-apk dist/Coral-Calculator-1.0.4.apk

Seeds synthetic calculator history/memory, upgrades without clearing app data,
and writes screenshots plus a short transition recording under dist.
"""

import argparse
import pathlib
import subprocess
import time

from completed_display_regression import CompletedDisplayRegression
from equals_regression import AVD, PACKAGE


class ContinueSumRegression(CompletedDisplayRegression):
    def fast_taps(self, *labels):
        clickable = [node for node in self.home if node.get("clickable") == "true"]
        for label in labels:
            x1, y1, x2, y2 = self.bounds(self.find(label, clickable))
            self.shell("input", "tap", (x1 + x2) // 2, (y1 + y2) // 2)

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
            self.check_upgrade_and_continue(old_apk, new_apk)
            self.check_repeat_and_other_input()
            self.record_plus_transition()
            print("PASS: v1.0.3 upgrade retains completed equation/history/memory; Plus "
                  "continues original and repeated equations, undo and restart work, "
                  "pending Plus stays singular, new digits/multiplication retain behavior, "
                  "and rapid touch/keyboard Equals-Plus sequences work", flush=True)
        finally:
            for namespace, key, value in saved:
                if value == "null":
                    self.shell("settings", "delete", namespace, key)
                else:
                    self.shell("settings", "put", namespace, key, value)

    def check_upgrade_and_continue(self, old_apk, new_apk):
        print("Checking in-place upgrade and the requested Plus continuation...", flush=True)
        self.adb("install", "-r", "-d", old_apk)
        self.restart()
        self.enter("7")
        self.keys("Memory clear", "Memory add")
        self.enter("5645+44646=")
        self.display("5,645+44,646", "50,291", completed=True)
        self.shell("am", "force-stop", PACKAGE)
        self.adb("install", "-r", new_apk)
        self.launch()
        self.display("5,645+44,646", "50,291", completed=True)
        self.grouped_history(("5,645+44,646", "50,291"))
        self.snapshot("continue-sum-completed.png")
        self.keys("Add")
        self.display("5,645+44,646+", "50,291", completed=False)
        self.snapshot("continue-sum-plus.png")
        self.restart()
        self.display("5,645+44,646+", "50,291", completed=False)
        self.keys("9", "Equals")
        self.display("5,645+44,646+9", "50,300", completed=True)
        self.keys("Backspace")
        self.display("5,645+44,646+9", "50,300", completed=False)
        self.keys("Backspace")
        self.display("5,645+44,646+", "50,291", completed=False)
        self.keys("All clear", "Memory recall")
        self.expression("7")

    def check_repeat_and_other_input(self):
        print("Checking repeated Equals, pending operators, restart and rapid input...", flush=True)
        self.enter("1000+2000==")
        self.display("3,000+2,000", "5,000", completed=True)
        self.restart()
        self.keys("Add")
        self.display("3,000+2,000+", "5,000", completed=False)
        self.keys("7", "Equals")
        self.display("3,000+2,000+7", "5,007", completed=True)
        self.enter("1000+2000+=")
        self.keys("Add", "Add")
        self.display("1,000+2,000+", "3,000", completed=False)

        self.enter("2+3=7")
        self.expression("7")
        self.preview("")
        self.enter("2+3=*4=")
        self.display("5×4", "20", completed=True)

        self.enter("2+3")
        self.fast_taps("Equals", "Add", "4", "Equals")
        self.display("2+3+4", "9", completed=True)
        self.enter("2+3")
        self.shell("input", "keyevent", "KEYCODE_ENTER", "KEYCODE_PLUS", "KEYCODE_4", "KEYCODE_ENTER")
        self.display("2+3+4", "9", completed=True)

    def record_plus_transition(self):
        print("Recording the completed-equation to Plus transition...", flush=True)
        self.enter("5645+44646=")
        self.display("5,645+44,646", "50,291", completed=True)
        remote = "/sdcard/coral-continue-sum.mp4"
        recorder = subprocess.Popen(
            ["adb", "-s", self.serial, "shell", "screenrecord", "--time-limit", "4",
             "--bit-rate", "6000000", remote],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding="utf-8",
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
        try:
            time.sleep(.8)
            self.keys("Add")
            time.sleep(.8)
            self.keys("9", "Equals")
            output, _ = recorder.communicate(timeout=15)
            if recorder.returncode != 0:
                raise RuntimeError(f"screenrecord failed: {output}")
        finally:
            if recorder.poll() is None:
                recorder.terminate()
                recorder.communicate(timeout=5)
        self.adb("pull", remote, self.output / "continue-sum-transition.mp4")
        self.display("5,645+44,646+9", "50,300", completed=True)


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
    regression = ContinueSumRegression(args.serial, output)
    try:
        regression.run(args.old_apk.resolve(), args.new_apk.resolve())
    except Exception:
        if regression.test_device_verified and output.is_dir():
            try:
                regression.snapshot("continue-sum-failure.png")
                regression.nodes()
                regression.adb("pull", "/sdcard/coral-equals-test.xml",
                               output / "continue-sum-failure.xml")
            except Exception as error:
                print(f"Could not capture failure diagnostics: {error}", flush=True)
        raise


if __name__ == "__main__":
    main()
