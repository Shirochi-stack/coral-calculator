"""Exercise an installed build on a dedicated Android emulator using native controls.

Usage: python tools/smoke_test.py --serial emulator-5580
Changes calculator history/settings and emulator rotation; use a test emulator.
"""
import argparse
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument("--serial", required=True)
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parent.parent
package = "com.coral.calculator"


def adb(*arguments):
    return subprocess.check_output(["adb", "-s", args.serial, *arguments], encoding="utf-8").strip()


def shell(*arguments):
    return adb("shell", *arguments)


def nodes():
    shell("uiautomator", "dump", "/sdcard/coral-test.xml")
    return list(ET.fromstring(shell("cat", "/sdcard/coral-test.xml")).iter("node"))


def find(label, tree=None):
    tree = nodes() if tree is None else tree
    return next(n for n in tree if n.get("content-desc") == label or
                n.get("text", "").casefold() == label.casefold() or
                (label == "Memory recall" and n.get("content-desc", "").startswith(label)))


def rect(node):
    return list(map(int, re.findall(r"\d+", node.get("bounds"))))


def tap(label, tree=None):
    x1, y1, x2, y2 = rect(find(label, tree))
    shell("input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))


def expect_expression(value):
    find("Expression: " + value)


def snapshot(name):
    shell("screencap", "-p", "/sdcard/coral-test.png")
    adb("pull", "/sdcard/coral-test.png", str(root / "docs" / name))


def assert_no_overlaps():
    tree = nodes()
    controls = [n for n in tree if n.get("package") == package and
                (n.get("clickable") == "true" or n.get("content-desc", "").startswith(("Expression:", "Result:")))]
    for i, a in enumerate(controls):
        ax1, ay1, ax2, ay2 = rect(a)
        for b in controls[i + 1:]:
            bx1, by1, bx2, by2 = rect(b)
            # One pixel of rounding between adjacent invisible hit areas is harmless.
            if min(ax2, bx2) - max(ax1, bx1) > 1 and min(ay2, by2) - max(ay1, by1) > 1:
                raise AssertionError("Overlapping controls: " + a.get("content-desc", "") + " / " + b.get("content-desc", ""))


shell("settings", "put", "system", "accelerometer_rotation", "0")
shell("settings", "put", "system", "user_rotation", "0")
shell("am", "force-stop", package)
shell("am", "start", "-W", "-n", package + "/.MainActivity")
time.sleep(.5)
home = nodes()


def keys(*labels):
    for label in labels:
        tap(label, home)


keys("All clear", "5", "2", "Add", "9", "6")
find("Result: 148")
assert_no_overlaps()
snapshot("screenshot.png")
keys("Equals")
expect_expression("148")
keys("Memory clear", "Memory add", "All clear", "Memory recall")
expect_expression("148")
keys("All clear", "2", "0", "0", "Add", "1", "0", "Percent", "Equals")
expect_expression("220")
keys("All clear", "8", "Divide", "0", "Equals")
find("Cannot divide by zero")
keys("Backspace", "2", "Equals")
expect_expression("4")
keys("All clear", "5", "Add", "3", "Equals", "Equals")
expect_expression("11")
tap("Calculation history", home)
find("8+3\n= 11")
tap("Done")

# Equals state and repeat operands survive an actual process restart.
keys("All clear", "5", "Add", "3", "Equals")
shell("am", "force-stop", package)
shell("am", "start", "-W", "-n", package + "/.MainActivity")
keys("Equals")
expect_expression("11")
shell("am", "force-stop", package)
shell("am", "start", "-W", "-n", package + "/.MainActivity")
keys("9")
expect_expression("9")

keys("All clear", "1")
tap("Unit converter", home)
find("Result: 3.280839895013123 ft")
tap("Use result")
expect_expression("3.280839895013123")
keys("All clear", "2")
tap("Currency converter", home)
tree = nodes()
assert find("Use result", tree).get("enabled") == "false"
rate = next(n for n in tree if n.get("class") == "android.widget.EditText" and "rate" in n.get("content-desc", "").lower())
tap(rate.get("content-desc"), tree)
shell("input", "text", "3")
shell("input", "keyevent", "KEYCODE_BACK")
tree = nodes()
assert find("Use result", tree).get("enabled") == "true"
tap("Use result", tree)
expect_expression("6")

tap("Settings", home)
tap("Dark appearance")
tap("Done")
snapshot("dark.png")
tap("Settings", home)
tap("Dark appearance")
tap("Done")

keys("All clear", "5", "2", "Add", "9", "6")
shell("settings", "put", "system", "user_rotation", "1")
time.sleep(.6)
find("Result: 148")
assert_no_overlaps()
snapshot("landscape.png")
shell("settings", "put", "system", "user_rotation", "0")
time.sleep(.6)
find("Result: 148")
keys("Memory clear")
snapshot("screenshot.png")
print("PASS: reference calculation, control geometry, arithmetic, errors, memory, history, persistence, converters, theme, and rotation")
