#!/usr/bin/env python3
"""Capture screenshots of the installed debug build from a connected device or emulator.

Usage: python3 tools/device_screenshots.py OUTPUT_DIR
Requires adb on PATH and the app already installed.
"""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "com.coral.calculator"
KEYS = {str(d): 7 + d for d in range(10)}
KEYS.update({"+": 157, "*": 155, "=": 66, "C": 111})


def adb(*args, binary=False):
    out = subprocess.run(["adb", *args], check=True, capture_output=True).stdout
    return out if binary else out.decode(errors="replace")


def press(sequence):
    for char in sequence:
        adb("shell", "input", "keyevent", str(KEYS[char]))


def shot(directory, name):
    (directory / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p", binary=True))
    print("saved", name)


def node(prefix):
    adb("shell", "uiautomator", "dump", "/sdcard/coral-shots.xml")
    tree = ET.fromstring(adb("exec-out", "cat", "/sdcard/coral-shots.xml", binary=True))
    found = next(n for n in tree.iter("node") if n.get("content-desc", "").startswith(prefix))
    print(prefix, "->", found.get("content-desc"), "| text:", found.get("text"), "|", found.get("bounds"))
    return found, tuple(map(int, re.findall(r"\d+", found.get("bounds"))))


def main():
    directory = Path(sys.argv[1])
    directory.mkdir(parents=True, exist_ok=True)
    adb("shell", "pm", "clear", PACKAGE)
    adb("shell", "am", "start", "-W", "-n", PACKAGE + "/.MainActivity")
    time.sleep(4)

    press("C12+7")
    shot(directory, "1-short-a")
    time.sleep(0.45)
    shot(directory, "1-short-b")

    press("C444466666+65656+123456789")
    time.sleep(0.3)
    shot(directory, "2-long-end")
    expression, (left, top, right, bottom) = node("Expression:")
    assert expression.get("text") == "444,466,666+65,656+123,456,789", expression.attrib

    y = (top + bottom) // 2
    adb("shell", "input", "swipe", str(left + 80), str(y), str(right - 80), str(y), "400")
    time.sleep(1.5)
    shot(directory, "3-long-swiped")

    press("=")
    time.sleep(1.2)
    shot(directory, "4-completed")
    node("Result:")

    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", "1")
    time.sleep(3)
    shot(directory, "5-landscape")
    adb("shell", "settings", "put", "system", "user_rotation", "0")


if __name__ == "__main__":
    main()
