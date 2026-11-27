"""
Marks in RunVanity.java which drawn outfits (and parts) are on sale: those that have a picture for every sheet of her, in
vanity/src/main/assets/vanity/. Run it after tools/make_vanity_outfit.py has packed something; it only changes the READY lines.

    python tools/vanity_ready.py
"""
import importlib.util, pathlib, re

ROOT = pathlib.Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location("layers", ROOT / "tools/make_vanity_layers.py")
layers = importlib.util.module_from_spec(spec)
spec.loader.exec_module(layers)
SHEETS = list(layers.SHEETS)
ASSETS = ROOT / "vanity/src/main/assets/vanity"
JAVA = ROOT / "app/src/main/java/com/coral/calculator/RunVanity.java"


def complete(folder, suffixes=(".webp",)):
    return all((folder / (sheet + suffix)).exists() for sheet in SHEETS for suffix in suffixes)


def main():
    text = JAVA.read_text(encoding="utf-8")
    styles = re.search(r"static final Style\[\] STYLES = \{(.*?)\};", text, re.S).group(1)
    keys = re.findall(r'new Style\("(\w+)"\)|new Style\(0x', styles)
    names = []
    for m in re.finditer(r'new Style\("(\w+)"\)|new Style\(0x', styles):
        names.append(m.group(1))
    ready = [(n is None) or complete(ASSETS / n) for n in names]
    new_line = "static final boolean[] READY = {" + ", ".join("true" if r else "false" for r in ready) + "};"
    text = re.sub(r"static final boolean\[\] READY = \{[^}]*\};", new_line, text)
    ears = re.search(r"EARS_ART = \{([^}]*)\}", text).group(1)
    horns = re.search(r"HORNS_ART = \{([^}]*)\}", text).group(1)
    def parts(names_text):
        out = []
        for item in names_text.split(","):
            item = item.strip()
            out.append(item == "null" or complete(ASSETS / "heads" / item.strip('"'), (".webp", "_m.webp")))
        return out
    er, hr = parts(ears), parts(horns)
    text = re.sub(r"EARS_READY = \{[^}]*\}, HORNS_READY = \{[^}]*\};", "EARS_READY = {" + ", ".join("true" if r else "false" for r in er) + "}, HORNS_READY = {" + ", ".join("true" if r else "false" for r in hr) + "};", text)
    rose = complete(ASSETS / "rose")
    text = re.sub(r"static final boolean ROSE_READY = \w+;", "static final boolean ROSE_READY = %s;" % ("true" if rose else "false"), text)
    JAVA.write_text(text, encoding="utf-8")
    print("outfits ready:", sum(ready), "of", len(ready), "| ears", er, "horns", hr, "| rose", rose)


if __name__ == "__main__":
    main()
