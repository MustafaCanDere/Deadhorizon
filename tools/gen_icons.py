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


def splint():
    img, d = canvas(16)
    for x in (4, 10):
        d.rectangle((x, 1, x + 2, 14), fill=(170, 125, 70, 255), outline=(90, 60, 30, 255))
    for y in (4, 9, 13):
        d.rectangle((3, y, 13, y + 1), fill=(235, 228, 210, 255))
    save(img, "item", "splint")


def bottle(name, liquid, cork=(150, 110, 60, 255)):
    img, d = canvas(16)
    d.rectangle((6, 1, 9, 3), fill=cork)
    d.rectangle((6, 4, 9, 5), fill=(200, 225, 235, 160), outline=OUTLINE)
    d.ellipse((2, 5, 13, 15), fill=(200, 225, 235, 120), outline=OUTLINE)
    d.ellipse((3, 8, 12, 14), fill=liquid)
    d.point((5, 9), fill=(255, 255, 255, 230))
    save(img, "item", name)


def can(name, body, band, top=(190, 190, 195, 255), tall=False):
    img, d = canvas(16)
    y0 = 1 if tall else 4
    d.rectangle((4, y0, 11, 15), fill=body, outline=OUTLINE)
    d.rectangle((4, y0, 11, y0 + 1), fill=top)
    d.rectangle((5, y0 + 4, 10, y0 + 7), fill=band)
    d.line((5, 14, 10, 14), fill=top)
    save(img, "item", name)


def energy_bar():
    img, d = canvas(16)
    d.polygon([(1, 10), (10, 2), (15, 6), (6, 14)], fill=(220, 160, 40, 255), outline=OUTLINE)
    d.line((4, 10, 10, 5), fill=(160, 40, 30, 255), width=2)
    save(img, "item", "energy_bar")


def mre():
    img, d = canvas(16)
    d.rectangle((2, 3, 13, 14), fill=(120, 100, 60, 255), outline=OUTLINE)
    d.rectangle((2, 3, 13, 5), fill=(95, 80, 45, 255))
    d.rectangle((5, 8, 10, 11), fill=(60, 50, 30, 255))
    save(img, "item", "mre")


def effect_fracture():
    img, d = canvas(18)
    d.rectangle((7, 1, 10, 7), fill=(235, 230, 210, 255), outline=(120, 110, 90, 255))
    d.polygon([(7, 7), (10, 7), (12, 9), (9, 9)], fill=(200, 40, 40, 255))
    d.rectangle((8, 10, 11, 16), fill=(235, 230, 210, 255), outline=(120, 110, 90, 255))
    d.ellipse((5, 0, 12, 3), fill=(235, 230, 210, 255))
    d.ellipse((6, 14, 13, 17), fill=(235, 230, 210, 255))
    save(img, "mob_effect", "fracture")


def gui_thirst():
    """27x9 strip: empty drop, full drop, half drop."""
    img = Image.new("RGBA", (27, 9), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for i, (fill, half) in enumerate([(None, False), ((60, 140, 230, 255), False), ((60, 140, 230, 255), True)]):
        ox = i * 9
        shape = [(ox + 4, 0), (ox + 7, 4), (ox + 7, 6), (ox + 5, 8), (ox + 3, 8), (ox + 1, 6), (ox + 1, 4)]
        d.polygon(shape, fill=(30, 30, 40, 255))
        if fill:
            inner = [(ox + 4, 1), (ox + 6, 4), (ox + 6, 6), (ox + 5, 7), (ox + 3, 7), (ox + 2, 6), (ox + 2, 4)]
            d.polygon(inner, fill=fill)
            d.point((ox + 3, 4), fill=(200, 230, 255, 255))
            if half:
                # Right half full: darken the left half of the water.
                for y in range(9):
                    for x in range(ox, ox + 4):
                        if img.getpixel((x, y))[2] > 100:
                            img.putpixel((x, y), (30, 30, 40, 255))
    (ROOT / "gui").mkdir(parents=True, exist_ok=True)
    img.save(ROOT / "gui" / "thirst.png")


acid_glob()
bandage()
first_aid_kit()
pill_bottle("painkillers", (240, 240, 240, 255), (40, 90, 200, 255), (250, 210, 60, 255))
pill_bottle("antibiotics", (230, 140, 40, 255), (250, 250, 250, 255), (250, 250, 250, 255))
antidote()
effect_infection()
effect_bleeding()
effect_immunity()
splint()
bottle("clean_water", (90, 170, 240, 220))
can("soda", (200, 40, 40, 255), (250, 250, 250, 255), tall=True)
can("energy_drink", (30, 30, 35, 255), (120, 230, 40, 255), tall=True)
can("canned_beans", (190, 190, 195, 255), (200, 90, 40, 255))
can("canned_soup", (190, 190, 195, 255), (200, 30, 30, 255))
energy_bar()
mre()
effect_fracture()
gui_thirst()
