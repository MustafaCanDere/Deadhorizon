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


def mp5():
    img, d = canvas()
    d.rectangle((2, 6, 11, 8), fill=POLYMER)
    d.line((11, 7, 14, 7), fill=STEEL)
    d.rectangle((5, 9, 6, 12), fill=POLYMER)
    d.polygon([(8, 9), (9, 9), (10, 13), (9, 13)], fill=STEEL)
    d.line((0, 7, 2, 7), fill=STEEL_LIGHT)
    save(img, "mp5")


def revolver():
    img, d = canvas()
    d.line((5, 5, 14, 5), fill=STEEL_LIGHT, width=2)
    d.ellipse((5, 4, 9, 8), fill=STEEL, outline=POLYMER)
    d.polygon([(4, 7), (7, 7), (5, 13), (2, 13)], fill=WOOD)
    save(img, "revolver")


def suppressor():
    img, d = canvas()
    d.rectangle((2, 6, 13, 9), fill=POLYMER, outline=STEEL)
    d.line((3, 7, 12, 7), fill=STEEL_LIGHT)
    save(img, "suppressor")


def extended_mag():
    img, d = canvas()
    d.polygon([(5, 1), (9, 1), (12, 14), (8, 14)], fill=POLYMER, outline=STEEL)
    d.point((7, 2), fill=BRASS)
    save(img, "extended_mag")


def scope():
    img, d = canvas()
    d.rectangle((2, 6, 13, 9), fill=POLYMER)
    d.ellipse((11, 5, 15, 10), fill=STEEL)
    d.ellipse((12, 6, 14, 9), fill=(120, 180, 220, 255))
    d.rectangle((6, 10, 8, 11), fill=STEEL)
    save(img, "scope")


def frag_grenade():
    img, d = canvas()
    d.ellipse((3, 5, 12, 14), fill=(70, 90, 50, 255), outline=(40, 50, 30, 255))
    d.rectangle((6, 2, 9, 5), fill=STEEL)
    d.ellipse((9, 1, 12, 4), outline=(180, 180, 180, 255))
    d.line((5, 9, 10, 9), fill=(50, 65, 35, 255))
    save(img, "frag_grenade")


def molotov():
    img, d = canvas()
    d.rectangle((5, 6, 10, 15), fill=(90, 140, 70, 200), outline=(40, 60, 30, 255))
    d.rectangle((6, 3, 9, 6), fill=(90, 140, 70, 200))
    d.rectangle((6, 9, 9, 14), fill=(200, 140, 40, 220))
    d.polygon([(7, 3), (6, 0), (9, 1), (8, 3)], fill=(230, 220, 200, 255))
    d.point((7, 0), fill=(255, 150, 30, 255))
    save(img, "molotov")


def pipe_bomb():
    img, d = canvas()
    d.rectangle((2, 6, 13, 10), fill=(120, 122, 128, 255), outline=STEEL)
    d.rectangle((1, 5, 2, 11), fill=STEEL)
    d.rectangle((13, 5, 14, 11), fill=STEEL)
    d.rectangle((6, 4, 9, 6), fill=(30, 30, 30, 255))
    d.point((8, 5), fill=(255, 40, 40, 255))
    d.line((4, 8, 11, 8), fill=(200, 40, 40, 255))
    save(img, "pipe_bomb")


def radio():
    img, d = canvas()
    d.rectangle((4, 4, 12, 15), fill=(50, 52, 48, 255), outline=POLYMER)
    d.line((10, 4, 10, 0), fill=STEEL_LIGHT)
    d.rectangle((5, 5, 11, 8), fill=(120, 160, 90, 255))
    for y in (10, 12, 14):
        d.line((5, y, 11, y), fill=(30, 30, 30, 255))
    d.point((6, 6), fill=(200, 230, 150, 255))
    save(img, "radio")


def jerry_can():
    img, d = canvas()
    d.rectangle((3, 4, 12, 15), fill=(170, 40, 30, 255), outline=POLYMER)
    d.line((4, 6, 11, 13), fill=(120, 25, 20, 255))
    d.line((11, 6, 4, 13), fill=(120, 25, 20, 255))
    d.rectangle((5, 1, 9, 3), outline=POLYMER)                    # handle
    d.rectangle((10, 2, 12, 4), fill=STEEL_LIGHT)                 # spout
    save(img, "jerry_can")


def uzi():
    img, d = canvas()
    d.rectangle((3, 5, 11, 8), fill=POLYMER)
    d.line((11, 6, 13, 6), fill=STEEL)
    d.rectangle((6, 9, 7, 14), fill=STEEL)                        # magazine through the grip
    d.line((4, 5, 10, 5), fill=STEEL_LIGHT)
    save(img, "uzi")


def double_barrel():
    img, d = canvas()
    d.rectangle((0, 8, 4, 11), fill=WOOD)
    d.rectangle((4, 7, 7, 9), fill=STEEL)
    d.line((7, 7, 15, 7), fill=STEEL)
    d.line((7, 8, 15, 8), fill=STEEL_LIGHT)
    d.rectangle((8, 9, 12, 9), fill=WOOD_DARK)                    # fore-end
    save(img, "double_barrel")


def ruble():
    img, d = canvas()
    d.rectangle((1, 4, 14, 11), fill=(170, 150, 110, 255), outline=(90, 70, 50, 255))
    d.rectangle((2, 5, 13, 10), outline=(120, 100, 70, 255))
    d.ellipse((6, 6, 9, 9), outline=(90, 70, 50, 255))
    save(img, "ruble")


def anti_rad():
    img, d = canvas()
    d.rectangle((6, 2, 9, 12), fill=(230, 230, 235, 255), outline=POLYMER)       # syringe body
    d.rectangle((7, 4, 8, 10), fill=(220, 60, 60, 255))
    d.line((7, 0, 8, 0), fill=POLYMER)
    d.line((7, 1, 7, 2), fill=POLYMER)
    d.line((7, 13, 7, 15), fill=STEEL_LIGHT)                                      # needle
    save(img, "anti_rad")


def vodka():
    img, d = canvas()
    d.rectangle((6, 0, 9, 2), fill=(200, 40, 40, 255))
    d.rectangle((6, 3, 9, 5), fill=(220, 230, 235, 140), outline=OUTLINE_GLASS)
    d.rectangle((4, 6, 11, 15), fill=(220, 230, 235, 150), outline=OUTLINE_GLASS)
    d.rectangle((5, 9, 10, 12), fill=(240, 240, 240, 255))
    d.line((6, 10, 9, 10), fill=(200, 40, 40, 255))
    save(img, "vodka")


def geiger():
    img, d = canvas()
    d.rectangle((3, 5, 12, 14), fill=(200, 160, 40, 255), outline=POLYMER)
    d.rectangle((5, 7, 10, 10), fill=(30, 40, 30, 255))
    d.line((6, 9, 9, 8), fill=(120, 240, 90, 255))
    d.line((12, 8, 15, 5), fill=POLYMER)                                           # probe cable
    d.rectangle((13, 2, 15, 5), fill=STEEL_LIGHT)
    save(img, "geiger_counter")


def detector():
    img, d = canvas()
    d.rectangle((4, 3, 11, 14), fill=(60, 64, 60, 255), outline=POLYMER)
    d.ellipse((5, 4, 10, 9), fill=(20, 30, 20, 255), outline=(120, 220, 100, 255))
    d.point((7, 6), fill=(140, 255, 120, 255))
    d.rectangle((6, 11, 9, 12), fill=(200, 50, 50, 255))
    save(img, "detector")


def bolt():
    img, d = canvas()
    d.rectangle((6, 3, 9, 5), fill=STEEL_LIGHT, outline=POLYMER)                  # head
    d.line((7, 6, 7, 13), fill=STEEL, width=2)
    for y in (7, 9, 11):
        d.point((9, y), fill=STEEL_LIGHT)
    d.rectangle((5, 13, 10, 14), fill=(200, 60, 40, 255))                         # rag tied on
    save(img, "bolt")


def artifact(name, core, rim):
    img, d = canvas()
    d.ellipse((3, 3, 12, 12), fill=rim)
    d.ellipse((5, 5, 10, 10), fill=core)
    d.point([(6, 6), (7, 5)], fill=(255, 255, 255, 230))
    for x, y in ((2, 7), (13, 8), (7, 1), (8, 14)):
        d.point((x, y), fill=rim)
    save(img, name)


def anomaly(name, color):
    img, d = canvas()
    for r, a in ((7, 60), (5, 110), (3, 170)):
        d.ellipse((8 - r, 8 - r, 7 + r, 7 + r), outline=color + (a,))
    save(img, name)


OUTLINE_GLASS = (90, 110, 120, 255)


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
mp5()
revolver()
ammo("ammo_357", BRASS, (180, 180, 185, 255), 2)
suppressor()
extended_mag()
scope()
frag_grenade()
molotov()
pipe_bomb()
radio()
long_gun("sniper_rifle", (70, 80, 60, 255), STEEL, 15, scope=True)
uzi()
double_barrel()
ruble()
anti_rad()
vodka()
geiger()
detector()
bolt()
artifact("artifact_jellyfish", (120, 200, 255, 255), (60, 110, 200, 200))
artifact("artifact_stone_flower", (170, 200, 120, 255), (110, 120, 90, 230))
artifact("artifact_fireball", (255, 220, 90, 255), (230, 90, 30, 220))
anomaly("anomaly_burner", (255, 120, 30))
anomaly("anomaly_electro", (140, 200, 255))
anomaly("anomaly_springboard", (180, 140, 255))
jerry_can()
