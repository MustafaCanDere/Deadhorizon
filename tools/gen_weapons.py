"""Generates placeholder weapon and ammo icons (16x16).

Run from the repo root: python3 tools/gen_weapons.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

OUT = Path("src/main/resources/assets/deadhorizon/textures/item")
STEEL = (55, 58, 64, 255)
STEEL_LIGHT = (95, 100, 108, 255)
WOOD = (120, 75, 40, 255)
WOOD_DARK = (85, 50, 25, 255)
POLYMER = (35, 35, 38, 255)
BRASS = (200, 160, 60, 255)


def canvas():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def save(img, name):
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f"{name}.png")


def pistol():
    img, d = canvas()
    d.rectangle((3, 5, 13, 7), fill=STEEL, outline=POLYMER)       # slide
    d.line((4, 5, 12, 5), fill=STEEL_LIGHT)
    d.rectangle((4, 8, 7, 13), fill=POLYMER)                      # grip
    d.rectangle((8, 8, 9, 9), outline=POLYMER)                    # trigger guard
    save(img, "pistol")


def long_gun(name, stock, body, barrel_end, mag=None, pump=None, scope=False):
    img, d = canvas()
    d.rectangle((0, 7, 4, 10), fill=stock)                        # stock
    d.rectangle((4, 7, 9, 9), fill=body)                          # receiver
    d.line((9, 7, barrel_end, 7), fill=STEEL)                     # barrel
    d.line((9, 8, barrel_end - 1, 8), fill=STEEL_LIGHT)
    d.rectangle((5, 10, 6, 12), fill=POLYMER)                     # grip
    if mag:
        d.polygon(mag, fill=POLYMER)
    if pump:
        d.rectangle(pump, fill=WOOD_DARK)
    if scope:
        d.rectangle((5, 4, 10, 5), fill=POLYMER)
        d.point((10, 4), fill=(120, 180, 220, 255))
    save(img, name)


def ammo(name, case, tip, count=3):
    img, d = canvas()
    for i in range(count):
        x = 3 + i * 4
        d.rectangle((x, 6, x + 2, 13), fill=case)
        d.polygon([(x, 6), (x + 2, 6), (x + 1, 3)], fill=tip)
        d.line((x, 13, x + 2, 13), fill=(120, 95, 40, 255))
    save(img, name)


def shells():
    img, d = canvas()
    for i in range(3):
        x = 1 + i * 5
        d.rectangle((x, 4, x + 3, 11), fill=(180, 30, 30, 255))
        d.line((x, 4, x, 11), fill=(220, 70, 70, 255))
        d.rectangle((x, 11, x + 3, 14), fill=BRASS)
    save(img, "ammo_12g")


def handheld(name, head, handle=WOOD, shaft=((3, 13), (12, 4)), width=2, extra=None):
    img, d = canvas()
    d.line((*shaft[0], *shaft[1]), fill=handle, width=width)
    if head:
        head(d)
    if extra:
        extra(d)
    save(img, name)


def bat():
    img, d = canvas()
    d.line((2, 14, 6, 10), fill=WOOD_DARK, width=2)
    d.line((6, 10, 13, 3), fill=(180, 140, 90, 255), width=3)
    save(img, "baseball_bat")


def nail_bat():
    img, d = canvas()
    d.line((2, 14, 6, 10), fill=WOOD_DARK, width=2)
    d.line((6, 10, 13, 3), fill=(180, 140, 90, 255), width=3)
    for x, y in [(9, 5), (12, 4), (10, 8), (13, 6), (8, 7)]:
        d.point((x, y), fill=(200, 200, 205, 255))
    save(img, "nail_bat")


def machete():
    img, d = canvas()
    d.line((2, 14, 5, 11), fill=POLYMER, width=2)
    d.line((5, 11, 14, 2), fill=(200, 202, 208, 255), width=3)
    d.line((6, 12, 15, 3), fill=STEEL_LIGHT, width=1)
    save(img, "machete")


def crowbar():
    img, d = canvas()
    d.line((3, 14, 12, 3), fill=(170, 30, 30, 255), width=2)
    d.line((12, 3, 14, 4), fill=(170, 30, 30, 255), width=2)
    d.line((3, 14, 2, 12), fill=(170, 30, 30, 255), width=1)
    save(img, "crowbar")


def katana():
    img, d = canvas()
    d.line((1, 15, 4, 12), fill=(40, 40, 60, 255), width=2)
    d.line((3, 11, 6, 14), fill=BRASS, width=1)
    d.line((5, 11, 14, 1), fill=(220, 222, 228, 255), width=1)
    d.line((5, 12, 14, 2), fill=(170, 172, 178, 255), width=1)
    save(img, "katana")


def fire_axe():
    img, d = canvas()
    d.line((3, 14, 11, 3), fill=(200, 40, 30, 255), width=2)
    d.rectangle((10, 1, 14, 6), fill=(200, 200, 205, 255))
    d.line((14, 1, 14, 6), fill=(235, 235, 240, 255))
    d.rectangle((7, 2, 9, 3), fill=STEEL)
    save(img, "fire_axe")


pistol()
long_gun("shotgun", WOOD, STEEL, 15, pump=(10, 9, 13, 9))
long_gun("hunting_rifle", WOOD, STEEL, 15, scope=True)
long_gun("ak47", WOOD, STEEL, 15, mag=[(8, 10), (10, 10), (11, 14), (9, 14)])
long_gun("m4", POLYMER, POLYMER, 15, mag=[(8, 10), (10, 10), (10, 14), (8, 14)])
ammo("ammo_9mm", BRASS, (170, 120, 60, 255))
ammo("ammo_556", BRASS, (150, 150, 155, 255))
ammo("ammo_762", (160, 130, 60, 255), (120, 90, 50, 255))
shells()
bat()
nail_bat()
machete()
crowbar()
katana()
fire_axe()
