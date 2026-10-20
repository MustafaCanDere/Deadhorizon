"""Generates placeholder item (16x16) and status effect (18x18) icons.

Run from the repo root: python3 tools/gen_icons.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path("src/main/resources/assets/deadhorizon/textures")
OUTLINE = (40, 30, 30, 255)


def canvas(size):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def save(img, kind, name):
    (ROOT / kind).mkdir(parents=True, exist_ok=True)
    img.save(ROOT / kind / f"{name}.png")


def bandage():
    img, d = canvas(16)
    d.ellipse((2, 3, 13, 12), fill=(235, 228, 210, 255), outline=(150, 140, 120, 255))
    d.ellipse((5, 6, 10, 9), fill=(200, 190, 170, 255))
    d.line((10, 10, 14, 14), fill=(235, 228, 210, 255), width=2)
    d.point((7, 7), fill=(170, 30, 30, 255))
    save(img, "item", "bandage")


def first_aid_kit():
    img, d = canvas(16)
    d.rectangle((1, 4, 14, 14), fill=(200, 35, 35, 255), outline=OUTLINE)
    d.rectangle((5, 2, 10, 4), outline=OUTLINE)
    d.rectangle((7, 6, 8, 12), fill=(250, 250, 250, 255))
    d.rectangle((5, 8, 10, 9), fill=(250, 250, 250, 255))
    save(img, "item", "first_aid_kit")


def pill_bottle(name, body, cap, label):
    img, d = canvas(16)
    d.rectangle((4, 1, 11, 3), fill=cap, outline=OUTLINE)
    d.rectangle((3, 4, 12, 15), fill=body, outline=OUTLINE)
    d.rectangle((4, 7, 11, 12), fill=label)
    d.line((5, 9, 10, 9), fill=OUTLINE)
    save(img, "item", name)


def antidote():
    img, d = canvas(16)
    d.rectangle((6, 0, 9, 2), fill=(90, 90, 90, 255))
    d.rectangle((5, 3, 10, 14), fill=(200, 220, 230, 255), outline=OUTLINE)
    d.rectangle((6, 7, 9, 13), fill=(60, 200, 120, 255))
    d.line((7, 15, 8, 15), fill=(160, 160, 160, 255))
    save(img, "item", "antidote")


def effect_infection():
    img, d = canvas(18)
    d.ellipse((2, 2, 15, 15), fill=(70, 120, 50, 255), outline=(30, 50, 20, 255))
    for cx, cy in [(9, 4), (5, 11), (13, 11)]:
        d.ellipse((cx - 2, cy - 2, cx + 2, cy + 2), fill=(20, 30, 15, 255))
    d.ellipse((7, 7, 10, 10), fill=(200, 230, 120, 255))
    save(img, "mob_effect", "infection")


def effect_bleeding():
    img, d = canvas(18)
    d.polygon([(9, 1), (14, 10), (4, 10)], fill=(170, 10, 10, 255))
    d.ellipse((4, 6, 14, 16), fill=(170, 10, 10, 255), outline=(90, 0, 0, 255))
    d.point((7, 10), fill=(240, 120, 120, 255))
    save(img, "mob_effect", "bleeding")


def effect_immunity():
    img, d = canvas(18)
    d.polygon([(9, 1), (16, 4), (15, 11), (9, 17), (3, 11), (2, 4)], fill=(80, 160, 225, 255), outline=(30, 70, 120, 255))
    d.rectangle((8, 5, 10, 13), fill=(250, 250, 250, 255))
    d.rectangle((5, 8, 13, 10), fill=(250, 250, 250, 255))
    save(img, "mob_effect", "immunity")


def acid_glob():
    img, d = canvas(16)
    d.ellipse((3, 4, 12, 13), fill=(150, 200, 40, 255), outline=(70, 100, 20, 255))
    d.ellipse((5, 6, 8, 9), fill=(210, 240, 120, 255))
    d.point([(12, 3), (2, 11), (13, 13)], fill=(150, 200, 40, 255))
    save(img, "item", "acid_glob")


acid_glob()
bandage()
first_aid_kit()
pill_bottle("painkillers", (240, 240, 240, 255), (40, 90, 200, 255), (250, 210, 60, 255))
pill_bottle("antibiotics", (230, 140, 40, 255), (250, 250, 250, 255), (250, 250, 250, 255))
antidote()
effect_infection()
effect_bleeding()
effect_immunity()
