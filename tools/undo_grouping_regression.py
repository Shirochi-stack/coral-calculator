"""Verify undo Equals, grouped display numbers, and an in-place Android upgrade.

Usage: python tools/undo_grouping_regression.py --serial emulator-5580 \
    --old-apk dist/Coral-Calculator-1.0.1.apk \
    --new-apk dist/Coral-Calculator-1.0.2.apk

Requires adb on PATH and the dedicated coral_calculator_api35 AVD. Seeds synthetic
calculator history/memory and replaces its APK without uninstalling or clearing
app data. Screenshots and failure diagnostics are written under dist, not docs.
"""

import argparse
import pathlib

from equals_regression import AVD, PACKAGE, Regression


class UndoGroupingRegression(Regression):
    def grouped_history(self, *entries, reuse=None):
        self.tap("Calculation history", self.home)
        tree = self.nodes()
        for equation, result in entries:
            self.find(equation + "\n= " + result, tree)
        if reuse is None:
            self.tap("Done", tree)
        else:
            self.tap(reuse[0] + "\n= " + reuse[1], tree)

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

        print("Seeding v1.0.1 expression, history and memory before an in-place upgrade...",
              flush=True)
        self.adb("install", "-r", "-d", old_apk)
        self.restart()
        self.enter("12000")
        self.keys("Memory clear", "Memory add")
        self.enter("1234+66=")
        self.expression("1300")
        self.shell("am", "force-stop", PACKAGE)
        self.adb("install", "-r", new_apk)
        self.launch()
        self.expression("1,300")
        self.grouped_history(("1,234+66", "1,300"))
        # Legacy snapshots contain no saved pre-Equals input. Their first delete
        # must edit the result instead of inventing an equation from history.
        self.keys("Backspace")
        self.expression("130")
        self.keys("All clear", "Memory recall")
        self.expression("12,000")
        self.keys("Add", "1", "Equals")
        self.expression("12,001")

        print("Checking exact reported calculation, one-step undo and further editing...",
              flush=True)
        self.enter("5645+44646")
        self.expression("5,645+44,646")
        self.preview("50,291")
        self.snapshot("undo-grouping-preview.png")
        self.keys("Equals")
        self.expression("50,291")
        self.preview("")
        self.snapshot("undo-grouping-result.png")
        self.keys("Backspace")
        self.expression("5,645+44,646")
        self.preview("50,291")
        self.snapshot("undo-grouping-restored.png")
        self.keys("Backspace")
        self.expression("5,645+4,464")
        self.preview("10,109")
        self.keys("7", "Equals")
        self.expression("50,292")
        self.keys("Backspace")
        self.expression("5,645+44,647")

        print("Checking repeated Equals, pending operators, restart and keyboard Delete...",
              flush=True)
        self.enter("1000+2000==")
        self.expression("5,000")
        self.keys("Backspace")
        self.expression("3,000+2,000")
        self.preview("5,000")
        self.keys("Backspace")
        self.expression("3,000+200")
        self.preview("3,200")
        self.enter("1000+2000+")
        self.preview("3,000")
        self.keys("Equals")
        self.expression("3,000")
        self.keys("Backspace")
        self.expression("1,000+2,000+")
        self.preview("3,000")
        self.keys("Backspace")
        self.expression("1,000+2,000")

        self.enter("5645+44646=")
        self.restart()
        self.expression("50,291")
        self.shell("input", "keyevent", "KEYCODE_DEL")
        self.expression("5,645+44,646")
        self.preview("50,291")
        self.shell("input", "keyevent", "KEYCODE_DEL")
        self.expression("5,645+4,464")
        self.preview("10,109")

        print("Checking thousands boundaries, negative decimals and raw history reuse...",
              flush=True)
        self.enter("999")
        self.expression("999")
        self.keys("9")
        self.expression("9,999")
        self.keys("Backspace")
        self.expression("999")
        for raw, grouped in (("1000", "1,000"), ("10000", "10,000"),
                             ("100000", "100,000")):
            self.enter(raw)
            self.expression(grouped)
            self.keys("Equals")
            self.expression(grouped)
        self.enter("12345.6789")
        self.keys("Change sign")
        self.expression("−12,345.6789")
        self.keys("Add", "1")
        self.preview("−12,344.6789")
        self.keys("Equals")
        self.expression("−12,344.6789")
        self.keys("Backspace")
        self.expression("−12,345.6789+1")
        self.preview("−12,344.6789")

        self.enter("5645+44646=")
        entry = ("5,645+44,646", "50,291")
        self.grouped_history(entry, reuse=entry)
        self.expression("50,291")
        self.keys("Add", "9", "Equals")
        self.expression("50,300")
        self.keys("Backspace")
        self.expression("50,291+9")

        print("Checking converter input and Use result preserve plain numeric values...",
              flush=True)
        self.enter("3048")
        self.tap("Unit converter", self.home)
        # Default conversion is meters to feet; 3,048 m equals exactly 10,000 ft.
        tree = self.nodes()
        inputs = [node for node in tree if node.get("class") == "android.widget.EditText"]
        amount = self.find("Amount", inputs)
        assert amount.get("text") == "3048", (
            f"Converter received formatted display text: {amount.get('text')!r}"
        )
        conversion_result = [node for node in tree
                             if node.get("content-desc", "").startswith("Result: ")]
        assert len(conversion_result) == 1, "Expected one conversion result"
        assert conversion_result[0].get("text", "") == "10,000 ft"
        assert conversion_result[0].get("content-desc", "") == "Result: 10,000 ft"
        self.tap("Use result", tree)
        self.expression("10,000")
        self.keys("Add", "1", "Equals")
        self.expression("10,001")
        self.keys("Backspace")
        self.expression("10,000+1")

        # Leave the requested example visible, with undo still available.
        self.enter("5645+44646=")
        self.expression("50,291")
        print("PASS: v1.0.1 upgrade retains expression/history/memory; Equals undo, "
              "further editing, repeated Equals, trailing operators, process restart, "
              "keyboard Delete, grouped integers/negative decimals, history reuse and "
              "converter Use result all work", flush=True)


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
    regression = UndoGroupingRegression(args.serial, output)
    try:
        regression.run(args.old_apk.resolve(), args.new_apk.resolve())
    except Exception:
        if regression.test_device_verified and output.is_dir():
            try:
                regression.snapshot("undo-grouping-failure.png")
                regression.nodes()
                regression.adb("pull", "/sdcard/coral-equals-test.xml",
                               output / "undo-grouping-failure.xml")
            except Exception as diagnostic_error:
                print(f"Could not capture failure diagnostics: {diagnostic_error}", flush=True)
        raise


if __name__ == "__main__":
    main()
