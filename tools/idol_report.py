"""
For each shape and language of the idol song: how many lines are over their mouth (target 0), how many were left out for want of a mouth, how many forms were shortened,
and how many seconds of her voice fall where no mouth moves (idol_sync). python tools/idol_report.py
"""
import pathlib
import re
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
D = pathlib.Path(r"C:\Users\ADMIN\Documents\ChatGPT\Calculator APP\dist\cutscenes\idol")


def main():
    print(f"{'shape':6} {'lang':4} {'lines over':>10} {'left out':>9} {'shortened':>10} {'voice, no mouth s':>18} {'mouths, no voice s':>19}")
    for shape in ("9x16", "16x9"):
        for lang in ("en", "ja", "ko", "zh"):
            notes = (D / "retimed" / f"idol-{shape}-{lang}-notes.txt").read_text(encoding="utf-8")
            over = int(re.search(r"lines over their mouth: (\d+)", notes).group(1))
            out = notes.count("LEFT OUT")
            short = len(re.findall(r"form [12] ", notes))
            r = subprocess.run([sys.executable, str(ROOT / "tools" / "idol_sync.py"), "x", str(D / "retimed" / f"idol-{shape}-{lang}-voice.wav"), str(D / f"idol-{shape}-vocals.wav"), "--bucket", "90"], capture_output=True, text=True, encoding="utf-8")
            tot = next(l for l in r.stdout.splitlines() if l.strip().startswith("0-90")).split()
            print(f"{shape:6} {lang:4} {over:>10} {out:>9} {short:>10} {float(tot[5]):>18.1f} {float(tot[4]):>19.1f}")


if __name__ == "__main__":
    main()
