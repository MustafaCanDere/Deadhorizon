"""Generates blockstates, models, item models, loot tables, tags, recipes and placeholder
textures for the decoration blocks.

Run from the repo root: python3 tools/gen_blocks.py
"""
import json
import random
from pathlib import Path

from PIL import Image, ImageDraw

RES = Path("src/main/resources")
ASSETS = RES / "assets/deadhorizon"
DATA = RES / "data/deadhorizon"
MOD = "deadhorizon"


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


# ---------------------------------------------------------------- textures

def noisy(base, amount=10, seed=0):
    rng = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            img.putpixel((x, y), tuple(max(0, min(255, c + rng.randint(-amount, amount))) for c in base) + (255,))
    return img


def darker(c, f=0.7):
    return tuple(int(v * f) for v in c)


def wood(base, seed):
    img = noisy(base, 8, seed)
    d = ImageDraw.Draw(img)
    for y in (3, 8, 13):
        d.line((0, y, 15, y), fill=darker(base, 0.8) + (255,))
    return img


def panel(base, seed):
    img = noisy(base, 6, seed)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), outline=darker(base, 0.65) + (255,))
    d.rectangle((3, 3, 12, 12), outline=darker(base, 0.8) + (255,))
    return img


def stripes(a, b, seed, width=4):
    img = noisy(a, 6, seed)
    for x in range(16):
        for y in range(16):
            if ((x + y) // width) % 2:
                img.putpixel((x, y), b + (255,))
    return img


def sandbags(seed):
    base = (196, 174, 120)
    img = noisy(base, 10, seed)
    d = ImageDraw.Draw(img)
    for row, y in enumerate((0, 5, 10)):
        off = 4 if row % 2 else 0
        for x in range(-8 + off, 16, 8):
            d.rounded_rectangle((x, y, x + 7, y + 4), radius=2, outline=darker(base, 0.6) + (255,))
    return img


def barbed_wire():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    grey = (150, 150, 155, 255)
    for y in (4, 11):
        d.line((0, y, 15, y + 1), fill=grey)
    for x in range(1, 16, 4):
        d.line((x - 1, 2, x + 1, 6), fill=(110, 110, 115, 255))
        d.line((x + 1, 9, x - 1, 13), fill=(110, 110, 115, 255))
    return img


def crate(seed):
    base = (176, 132, 82)
    img = wood(base, seed)
    d = ImageDraw.Draw(img)
    edge = darker(base, 0.55) + (255,)
    d.rectangle((0, 0, 15, 15), outline=edge)
    d.line((0, 0, 15, 15), fill=edge)
    return img


def planks(cracked, seed):
    base = (150, 110, 70)
    img = wood(base, seed)
    d = ImageDraw.Draw(img)
    d.point([(2, 3), (13, 3), (2, 12), (13, 12)], fill=(70, 70, 70, 255))  # nails
    if cracked:
        d.line((5, 2, 8, 8, 6, 14), fill=darker(base, 0.4) + (255,))
        d.line((11, 0, 10, 6), fill=darker(base, 0.4) + (255,))
    return img


def glow(color):
    img = noisy(color, 4, 1)
    d = ImageDraw.Draw(img)
    d.ellipse((4, 4, 11, 11), fill=tuple(min(255, c + 30) for c in color) + (255,))
    return img


TEXTURES = {
    "wood_light": lambda: wood((181, 140, 92), 1),
    "wood_dark": lambda: wood((96, 66, 40), 2),
    "wood_panel": lambda: panel((168, 128, 84), 3),
    "wood_panel_dark": lambda: panel((90, 62, 38), 4),
    "counter_top": lambda: noisy((190, 190, 185), 12, 5),
    "handle": lambda: noisy((60, 60, 65), 4, 6),
    "metal_white": lambda: noisy((225, 228, 230), 5, 7),
    "metal_grey": lambda: noisy((140, 142, 148), 8, 8),
    "metal_dark": lambda: noisy((62, 64, 70), 6, 9),
    "metal_green": lambda: noisy((52, 96, 58), 8, 10),
    "fabric_red": lambda: noisy((150, 40, 40), 12, 11),
    "glass": lambda: noisy((180, 210, 220), 8, 12),
    "lamp_glow": lambda: glow((255, 236, 170)),
    "lamp_off": lambda: noisy((120, 115, 100), 6, 13),
    "orange_plastic": lambda: noisy((235, 110, 20), 8, 14),
    "white_plastic": lambda: noisy((235, 235, 235), 5, 15),
    "concrete": lambda: noisy((160, 160, 155), 14, 16),
    "concrete_stripe": lambda: stripes((230, 200, 40), (30, 30, 30), 17),
    "sandbags": lambda: sandbags(18),
    "barbed_wire": barbed_wire,
    "crate": lambda: crate(19),
    "barricade": lambda: planks(False, 20),
    "barricade_cracked": lambda: planks(True, 21),
}


def gen_textures():
    out = ASSETS / "textures/block"
    out.mkdir(parents=True, exist_ok=True)
    for name, fn in TEXTURES.items():
        fn().save(out / f"{name}.png")


# ---------------------------------------------------------------- models

def el(frm, to, tex, faces=("north", "south", "east", "west", "up", "down")):
    return {"from": list(frm), "to": list(to), "faces": {f: {"texture": f"#{tex}"} for f in faces}}


def model(name, elements, textures, particle=None):
    tex = {k: f"{MOD}:block/{v}" for k, v in textures.items()}
    tex["particle"] = tex.get(particle) if particle else next(iter(tex.values()))
    write(ASSETS / f"models/block/{name}.json", {"parent": "minecraft:block/block", "textures": tex, "elements": elements})


def legs(x1, z1, x2, z2, h, w, tex):
    return [el((x, 0, z), (x + w, h, z + w), tex) for x in (x1, x2 - w) for z in (z1, z2 - w)]


MODELS = {
    "kitchen_cabinet": ([
        el((0, 0, 1), (16, 14, 16), "body"), el((0, 14, 0), (16, 16, 16), "top"),
        el((1, 1, 0.5), (7.5, 13, 1), "door"), el((8.5, 1, 0.5), (15, 13, 1), "door"),
        el((6, 10, 0), (7, 12, 0.5), "handle"), el((9, 10, 0), (10, 12, 0.5), "handle"),
    ], {"body": "wood_light", "top": "counter_top", "door": "wood_panel", "handle": "handle"}),
    "fridge_lower": ([
        el((1, 0, 1), (15, 16, 15), "body"), el((12, 7, 0.5), (13, 15, 1), "handle"),
    ], {"body": "metal_white", "handle": "metal_grey"}),
    "fridge_upper": ([
        el((1, 0, 1), (15, 16, 15), "body"), el((12, 1, 0.5), (13, 7, 1), "handle"),
        el((1, 9, 0.75), (15, 9.5, 1), "handle"),
    ], {"body": "metal_white", "handle": "metal_grey"}),
    "wardrobe_lower": ([
        el((0, 0, 2), (16, 16, 16), "body"),
        el((1, 1, 1.5), (7.5, 16, 2), "door"), el((8.5, 1, 1.5), (15, 16, 2), "door"),
        el((6.5, 13, 1), (7, 15.5, 1.5), "handle"), el((9, 13, 1), (9.5, 15.5, 1.5), "handle"),
    ], {"body": "wood_dark", "door": "wood_panel_dark", "handle": "handle"}),
    "wardrobe_upper": ([
        el((0, 0, 2), (16, 15, 16), "body"), el((0, 15, 1), (16, 16, 16), "body"),
        el((1, 0, 1.5), (7.5, 14, 2), "door"), el((8.5, 0, 1.5), (15, 14, 2), "door"),
    ], {"body": "wood_dark", "door": "wood_panel_dark"}),
    "medicine_cabinet": ([
        el((2, 2, 11), (14, 14, 16), "body"), el((3, 3, 10.5), (13, 13, 11), "glass"),
    ], {"body": "metal_white", "glass": "glass"}),
    "safe": ([
        el((1, 0, 1), (15, 14, 15), "body"), el((2, 1, 0.5), (14, 13, 1), "door"),
        el((7, 6, 0), (9, 8, 0.5), "dial"), el((11, 5, 0), (12, 9, 0.5), "dial"),
    ], {"body": "metal_dark", "door": "metal_grey", "dial": "handle"}),
    "trash_can": ([
        el((3, 0, 3), (13, 12, 13), "body"), el((2.5, 12, 2.5), (13.5, 13, 13.5), "lid"),
        el((7, 13, 6), (9, 14, 10), "lid"),
    ], {"body": "metal_grey", "lid": "metal_dark"}),
    "crate": ([el((1, 0, 1), (15, 15, 15), "body")], {"body": "crate"}),
    "chair": ([
        el((2, 7, 2), (14, 8, 14), "seat"), *legs(2, 2, 14, 14, 7, 1.5, "leg"),
        el((2, 8, 12), (14, 16, 13.5), "seat"),
    ], {"seat": "wood_light", "leg": "wood_dark"}),
    "stool": ([
        el((4, 9, 4), (12, 10, 12), "seat"), *legs(4.5, 4.5, 11.5, 11.5, 9, 1, "leg"),
    ], {"seat": "wood_light", "leg": "wood_dark"}),
    "sofa": ([
        el((0, 0, 2), (16, 4, 16), "base"), el((0, 4, 2), (16, 8, 13), "cushion"),
        el((0, 4, 13), (16, 16, 16), "cushion"),
    ], {"base": "wood_dark", "cushion": "fabric_red"}),
    "table": ([
        el((0, 14, 0), (16, 16, 16), "top"), *legs(1, 1, 15, 15, 14, 2, "leg"),
    ], {"top": "wood_light", "leg": "wood_dark"}),
    "dumpster": ([
        el((0, 0, 0), (16, 13, 16), "body"), el((-0.5, 13, -0.5), (16.5, 14, 16.5), "lid"),
    ], {"body": "metal_green", "lid": "metal_dark"}),
    "sandbags": ([el((0, 0, 0), (16, 16, 16), "bags")], {"bags": "sandbags"}),
    "barbed_wire": ([
        el((0, 0, 8), (16, 8, 8), "wire", ("north", "south")),
        el((8, 0, 0), (8, 8, 16), "wire", ("east", "west")),
    ], {"wire": "barbed_wire"}),
    "traffic_cone": ([
        el((3, 0, 3), (13, 1, 13), "orange"), el((5, 1, 5), (11, 4, 11), "orange"),
        el((5.5, 4, 5.5), (10.5, 7, 10.5), "white"), el((6.5, 7, 6.5), (9.5, 12, 9.5), "orange"),
    ], {"orange": "orange_plastic", "white": "white_plastic"}),
    "concrete_barrier": ([
        el((1, 0, 3), (15, 4, 13), "concrete"), el((1, 4, 4.5), (15, 12, 11.5), "concrete"),
        el((1, 12, 5.5), (15, 16, 10.5), "stripe"),
    ], {"concrete": "concrete", "stripe": "concrete_stripe"}),
}

LAMPS = {
    "table_lamp": [
        el((6, 0, 6), (10, 1, 10), "metal"), el((7.5, 1, 7.5), (8.5, 6, 8.5), "metal"),
        el((5, 6, 5), (11, 12, 11), "light"),
    ],
    "ceiling_lamp": [
        el((7.5, 13, 7.5), (8.5, 16, 8.5), "metal"), el((4, 10, 4), (12, 13, 12), "shade"),
        el((6, 9, 6), (10, 10, 10), "light"),
    ],
    "street_lamp": [
        el((7, 0, 7), (9, 13, 9), "metal"), el((4, 13, 4), (12, 16, 12), "shade"),
        el((5, 12, 5), (11, 13, 11), "light"),
    ],
}

BOARDS = [((0, 2, 7), (16, 5, 9)), ((0, 7, 7), (16, 10, 9)), ((0, 12, 7), (16, 15, 9))]


def gen_models():
    for name, (elements, textures) in MODELS.items():
        model(name, elements, textures)
    for name, elements in LAMPS.items():
        for lit in (True, False):
            model(name if lit else f"{name}_off", elements,
                  {"metal": "metal_dark", "shade": "metal_grey", "light": "lamp_glow" if lit else "lamp_off"}, "metal")
    for damage, count in enumerate((3, 3, 2, 1)):
        tex = "barricade" if damage == 0 else "barricade_cracked"
        model(f"barricade_{damage}", [el(f, t, "planks") for f, t in BOARDS[:count]], {"planks": tex})


# ---------------------------------------------------------------- blockstates, items, loot

FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}

SIMPLE = ["kitchen_cabinet", "medicine_cabinet", "safe", "trash_can", "crate", "chair", "stool", "sofa", "table",
          "dumpster", "sandbags", "traffic_cone", "concrete_barrier"]
TALL = ["fridge", "wardrobe"]


def variants(name, extra=None):
    """extra: (property, {value: model_suffix})"""
    out = {}
    for facing, rot in FACINGS.items():
        if extra is None:
            out[f"facing={facing}"] = {"model": f"{MOD}:block/{name}", "y": rot}
        else:
            prop, values = extra
            for value, model_name in values.items():
                out[f"facing={facing},{prop}={value}"] = {"model": f"{MOD}:block/{model_name}", "y": rot}
    for v in out.values():
        if v["y"] == 0:
            del v["y"]
    return {"variants": out}


def drop_self(name, condition=None):
    pool = {"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{MOD}:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}
    if condition:
        pool["conditions"].append(condition)
    write(DATA / f"loot_tables/blocks/{name}.json", {"type": "minecraft:block", "pools": [pool]})


def item_model(name, block_model):
    write(ASSETS / f"models/item/{name}.json", {"parent": f"{MOD}:block/{block_model}"})


def gen_states():
    states = ASSETS / "blockstates"
    for name in SIMPLE:
        write(states / f"{name}.json", variants(name))
        item_model(name, name)
        drop_self(name)
    for name in TALL:
        write(states / f"{name}.json", variants(name, ("half", {"lower": f"{name}_lower", "upper": f"{name}_upper"})))
        item_model(name, f"{name}_lower")
        drop_self(name, {"condition": "minecraft:block_state_property", "block": f"{MOD}:{name}",
                         "properties": {"half": "lower"}})
    for name in LAMPS:
        write(states / f"{name}.json", variants(name, ("lit", {"true": name, "false": f"{name}_off"})))
        item_model(name, name)
        drop_self(name)
    write(states / "barricade.json", variants("barricade", ("damage", {str(d): f"barricade_{d}" for d in range(4)})))
    item_model("barricade", "barricade_0")
    drop_self("barricade")
    write(states / "barbed_wire.json", {"variants": {"": {"model": f"{MOD}:block/barbed_wire"}}})
    write(ASSETS / "models/item/barbed_wire.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:block/barbed_wire"}})
    drop_self("barbed_wire")


def gen_tags():
    axe = ["kitchen_cabinet", "wardrobe", "crate", "chair", "stool", "sofa", "table", "barricade", "rain_collector",
           "weapon_bench"]
    pickaxe = ["fridge", "medicine_cabinet", "safe", "trash_can", "dumpster", "table_lamp", "ceiling_lamp",
               "street_lamp", "barbed_wire", "traffic_cone", "concrete_barrier",
               "generator", "solar_panel", "electric_fence", "floodlight"]
    shovel = ["sandbags"]
    tags = RES / "data/minecraft/tags/blocks/mineable"
    for tool, names in (("axe", axe), ("pickaxe", pickaxe), ("shovel", shovel)):
        write(tags / f"{tool}.json", {"replace": False, "values": [f"{MOD}:{n}" for n in names]})


# ---------------------------------------------------------------- recipes

def shaped(name, pattern, key, count=1):
    keys = {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in key.items()}
    write(DATA / f"recipes/{name}.json", {"type": "minecraft:crafting_shaped", "category": "building",
                                          "pattern": pattern, "key": keys,
                                          "result": {"item": f"{MOD}:{name}", "count": count}})


def shapeless(name, items, count=1):
    ing = [({"tag": v[1:]} if v.startswith("#") else {"item": v}) for v in items]
    write(DATA / f"recipes/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "building",
                                          "ingredients": ing, "result": {"item": f"{MOD}:{name}", "count": count}})


def gen_recipes():
    P, S, I, C = "#minecraft:planks", "minecraft:stick", "minecraft:iron_ingot", "minecraft:chest"
    shaped("kitchen_cabinet", ["TTT", "PCP", "PPP"], {"T": "minecraft:smooth_stone_slab", "P": P, "C": C})
    shaped("fridge", ["III", "ICI", "IRI"], {"I": I, "C": C, "R": "minecraft:redstone"})
    shaped("wardrobe", ["PPP", "PCP", "PCP"], {"P": P, "C": C})
    shaped("medicine_cabinet", ["IGI", "ICI"], {"I": I, "G": "minecraft:glass_pane", "C": C})
    shaped("safe", ["III", "ICI", "III"], {"I": I, "C": C})
    shaped("trash_can", ["ITI", "I I", "III"], {"I": I, "T": "minecraft:iron_trapdoor"})
    shaped("crate", ["SPS", "P P", "SPS"], {"S": S, "P": P})
    shaped("chair", ["P  ", "PPP", "S S"], {"P": P, "S": S}, 2)
    shaped("stool", ["PPP", "S S"], {"P": P, "S": S}, 2)
    shaped("sofa", ["W  ", "WWW", "PPP"], {"W": "#minecraft:wool", "P": P}, 2)
    shaped("table", ["PPP", "S S", "S S"], {"P": P, "S": S})
    shaped("table_lamp", [" W ", "WTW", " I "], {"W": "minecraft:white_wool", "T": "minecraft:torch", "I": I})
    shaped("ceiling_lamp", [" C ", "IGI"], {"C": "minecraft:chain", "I": I, "G": "minecraft:glowstone"}, 2)
    shaped("street_lamp", ["IGI", " I ", " I "], {"I": I, "G": "minecraft:glowstone"})
    shaped("dumpster", ["TTT", "ICI", "III"], {"T": "minecraft:iron_trapdoor", "I": I, "C": C})
    shapeless("sandbags", ["minecraft:sand", "minecraft:sand", "#minecraft:wool"], 2)
    shaped("barbed_wire", ["N N", " N ", "N N"], {"N": "minecraft:iron_nugget"}, 4)
    shaped("barricade", ["P P", "PPP", "P P"], {"P": P}, 2)
    shaped("traffic_cone", [" T ", " W ", "TTT"], {"T": "minecraft:orange_terracotta", "W": "minecraft:white_wool"}, 2)
    shaped("concrete_barrier", [" C ", "CCC"], {"C": "minecraft:gray_concrete"}, 2)


# ---------------------------------------------------------------- rain collector

def gen_rain_collector():
    noisy((60, 110, 200), 12, 41).save(ASSETS / "textures/block/rain_water.png")
    walls = [el((2, 0, 2), (14, 14, 3), "wood"), el((2, 0, 13), (14, 14, 14), "wood"),
             el((2, 0, 3), (3, 14, 13), "wood"), el((13, 0, 3), (14, 14, 13), "wood"),
             el((3, 0, 3), (13, 1, 13), "wood"),
             el((1.5, 3, 1.5), (14.5, 4, 14.5), "band", ("north", "south", "east", "west")),
             el((1.5, 10, 1.5), (14.5, 11, 14.5), "band", ("north", "south", "east", "west"))]
    for level in range(4):
        water = [el((3, 1, 3), (13, 1 + level * 4, 13), "water", ("up",))] if level else []
        model(f"rain_collector_{level}", walls + water,
              {"wood": "wood_dark", "band": "metal_dark", "water": "rain_water"}, "wood")
    write(ASSETS / "blockstates/rain_collector.json",
          {"variants": {f"level={i}": {"model": f"{MOD}:block/rain_collector_{i}"} for i in range(4)}})
    item_model("rain_collector", "rain_collector_2")
    drop_self("rain_collector")
    shaped("rain_collector", ["P P", "PBP", "PPP"], {"P": "#minecraft:planks", "B": "minecraft:bucket"})


# ---------------------------------------------------------------- electricity and workshop

def solar_cells():
    img = noisy((30, 50, 110), 6, 51)
    d = ImageDraw.Draw(img)
    for i in (0, 5, 10, 15):
        d.line((i, 0, i, 15), fill=(170, 175, 185, 255))
        d.line((0, i, 15, i), fill=(170, 175, 185, 255))
    return img


def fence_wire():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for y in (2, 6, 10, 14):
        d.line((0, y, 15, y), fill=(150, 120, 70, 255))
    for x in range(0, 16, 4):
        d.point((x, 2), fill=(230, 230, 230, 255))
        d.point((x + 2, 10), fill=(230, 230, 230, 255))
    return img


def warning_sign():
    img = noisy((235, 200, 30), 6, 52)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), outline=(30, 30, 30, 255))
    d.line((9, 2, 6, 8, 10, 8, 7, 14), fill=(30, 30, 30, 255))
    return img


def generator_side(running):
    img = noisy((170, 40, 35), 8, 53)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), outline=(40, 40, 40, 255))
    for y in (4, 7, 10):
        d.line((3, y, 12, y), fill=(60, 20, 20, 255))
    d.rectangle((11, 12, 13, 13), fill=(90, 230, 90, 255) if running else (40, 60, 40, 255))
    return img


def workbench_top():
    img = wood((110, 80, 50), 54)
    d = ImageDraw.Draw(img)
    d.rectangle((2, 2, 6, 5), fill=(70, 72, 78, 255))  # vice
    d.line((9, 10, 14, 10), fill=(140, 142, 148, 255))  # file
    d.line((9, 12, 13, 13), fill=(140, 142, 148, 255))
    return img


POWER_TEXTURES = {
    "generator_side": lambda: generator_side(False),
    "generator_side_on": lambda: generator_side(True),
    "solar_cells": solar_cells,
    "fence_wire": fence_wire,
    "warning_sign": warning_sign,
    "workbench_top": workbench_top,
    "floodlight_lens": lambda: noisy((200, 205, 200), 6, 55),
    "floodlight_lens_on": lambda: glow((255, 250, 225)),
}


def gen_power():
    for name, fn in POWER_TEXTURES.items():
        fn().save(ASSETS / f"textures/block/{name}.png")

    frame = [el((1, 0, 3), (15, 2, 13), "frame"), el((1, 10, 3), (15, 12, 13), "frame"),
             el((1, 2, 3), (2, 10, 4), "frame"), el((14, 2, 3), (15, 10, 4), "frame"),
             el((1, 2, 12), (2, 10, 13), "frame"), el((14, 2, 12), (15, 10, 13), "frame")]
    for suffix, side in (("", "generator_side"), ("_on", "generator_side_on")):
        model(f"generator{suffix}", frame + [el((3, 2, 4), (13, 10, 12), "body"),
                                             el((10, 12, 6), (13, 14, 9), "frame"),   # fuel cap
                                             el((2, 6, 13), (4, 8, 16), "frame")],    # exhaust, at the back
              {"body": side, "frame": "metal_dark"}, "body")
    write(ASSETS / "blockstates/generator.json", variants("generator", ("lit", {"false": "generator", "true": "generator_on"})))
    item_model("generator", "generator")

    model("solar_panel", [el((0, 3, 0), (16, 5, 16), "cells", ("up",)),
                          el((0, 3, 0), (16, 5, 16), "frame", ("north", "south", "east", "west", "down")),
                          el((7, 0, 7), (9, 3, 9), "frame")], {"cells": "solar_cells", "frame": "metal_grey"}, "cells")
    write(ASSETS / "blockstates/solar_panel.json",
          variants("solar_panel", ("powered", {"false": "solar_panel", "true": "solar_panel"})))
    item_model("solar_panel", "solar_panel")

    model("electric_fence", [el((0, 0, 7), (2, 16, 9), "post"), el((14, 0, 7), (16, 16, 9), "post"),
                             el((2, 0, 8), (14, 16, 8), "wire", ("north", "south")),
                             el((5, 5, 7.5), (11, 9, 8.5), "sign", ("north", "south"))],
          {"post": "metal_dark", "wire": "fence_wire", "sign": "warning_sign"}, "post")
    write(ASSETS / "blockstates/electric_fence.json",
          variants("electric_fence", ("powered", {"false": "electric_fence", "true": "electric_fence"})))
    item_model("electric_fence", "electric_fence")

    for suffix, lens in (("", "floodlight_lens"), ("_on", "floodlight_lens_on")):
        model(f"floodlight{suffix}", [el((3, 0, 3), (13, 4, 13), "metal"), el((7, 4, 7), (9, 10, 9), "metal"),
                                      el((2, 10, 1), (14, 16, 6), "metal", ("south", "east", "west", "up", "down")),
                                      el((2, 10, 0), (14, 16, 1), "lens", ("north",)),
                                      el((2, 10, 0), (14, 16, 1), "metal", ("east", "west", "up", "down"))],
              {"metal": "metal_dark", "lens": lens}, "metal")
    write(ASSETS / "blockstates/floodlight.json",
          variants("floodlight", ("powered", {"false": "floodlight", "true": "floodlight_on"})))
    item_model("floodlight", "floodlight")

    model("weapon_bench", [el((0, 13, 0), (16, 15, 16), "top", ("up",)),
                           el((0, 13, 0), (16, 15, 16), "wood", ("north", "south", "east", "west", "down"))]
          + legs(1, 1, 13, 13, 13, 2, "wood"),
          {"top": "workbench_top", "wood": "wood_dark"}, "wood")
    write(ASSETS / "blockstates/weapon_bench.json", variants("weapon_bench"))
    item_model("weapon_bench", "weapon_bench")

    for name in ("generator", "solar_panel", "electric_fence", "floodlight", "weapon_bench"):
        drop_self(name)
    I = "minecraft:iron_ingot"
    shaped("generator", ["III", "IFI", "IRI"], {"I": I, "F": "minecraft:furnace", "R": "minecraft:redstone"})
    shaped("solar_panel", ["GGG", "IDI"], {"G": "minecraft:glass", "I": I, "D": "minecraft:daylight_detector"})
    shaped("electric_fence", ["BCB", "BCB"], {"B": "minecraft:iron_bars", "C": "minecraft:copper_ingot"}, 4)
    shaped("floodlight", ["ILI", " I ", "III"], {"I": I, "L": "minecraft:redstone_lamp"})
    shaped("weapon_bench", ["ISI", "PPP", "P P"], {"I": I, "S": "minecraft:smithing_table", "P": "#minecraft:planks"})


# ---------------------------------------------------------------- container loot (used by structures)

def entry(item, weight, lo=1, hi=1):
    e = {"type": "minecraft:item", "name": item, "weight": weight}
    if hi > 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
    return e


def gun(item, weight, loaded):
    """A found gun: worn somewhere between 10% and 70%, with a few rounds left in it."""
    return {"type": "minecraft:item", "name": item, "weight": weight, "functions": [
        {"function": "minecraft:set_damage", "damage": {"type": "minecraft:uniform", "min": 0.3, "max": 0.9}},
        {"function": "minecraft:set_nbt", "tag": "{Ammo:%d}" % loaded}]}


def chest(name, rolls, entries):
    write(DATA / f"loot_tables/containers/{name}.json", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]}, "entries": entries}]})


def gen_container_loot():
    m = lambda n: f"minecraft:{n}"
    d = lambda n: f"{MOD}:{n}"
    chest("kitchen", (2, 5), [entry(m("bread"), 10, 1, 3), entry(m("baked_potato"), 8, 1, 3), entry(m("cookie"), 6, 2, 6),
                              entry(m("sugar"), 6, 1, 4), entry(m("bowl"), 5), entry(m("glass_bottle"), 6, 1, 3),
                              entry(m("honey_bottle"), 2), entry(d("painkillers"), 2), entry(m("iron_sword"), 1),
                              entry(d("canned_beans"), 6, 1, 2), entry(d("canned_soup"), 6, 1, 2), entry(d("energy_bar"), 4, 1, 3),
                              entry(d("clean_water"), 4, 1, 2), entry(d("coffee"), 3)])
    chest("fridge", (1, 4), [entry(m("cooked_beef"), 6, 1, 2), entry(m("cooked_chicken"), 6, 1, 2), entry(m("apple"), 8, 1, 3),
                             entry(m("carrot"), 6, 1, 4), entry(m("milk_bucket"), 3), entry(m("rotten_flesh"), 8, 1, 3),
                             entry(m("melon_slice"), 5, 1, 4), entry(d("soda"), 6, 1, 3), entry(d("energy_drink"), 3),
                             entry(d("clean_water"), 5, 1, 3)])
    chest("medicine", (1, 3), [entry(d("bandage"), 10, 1, 3), entry(d("painkillers"), 8, 1, 2), entry(d("antibiotics"), 4),
                               entry(d("first_aid_kit"), 2), entry(d("antidote"), 1), entry(m("glass_bottle"), 4),
                               entry(d("splint"), 4)])
    chest("wardrobe", (2, 5), [entry(m("leather_chestplate"), 4), entry(m("leather_leggings"), 4), entry(m("leather_boots"), 4),
                               entry(m("leather_helmet"), 3), entry(m("white_wool"), 6, 1, 3), entry(m("string"), 8, 2, 6),
                               entry(m("paper"), 5, 1, 4), entry(d("bandage"), 2)])
    chest("crate", (2, 6), [entry(m("iron_ingot"), 6, 1, 3), entry(m("coal"), 8, 2, 8), entry(m("torch"), 8, 4, 12),
                            entry(m("arrow"), 6, 4, 16), entry(m("iron_nugget"), 8, 3, 9), entry(m("oak_planks"), 6, 4, 16),
                            entry(m("iron_axe"), 2), entry(m("bow"), 2), entry(m("shield"), 1),
                            entry(d("mre"), 3), entry(d("canned_beans"), 4, 1, 3), entry(d("clean_water"), 3, 1, 2),
                            entry(d("ammo_9mm"), 3, 2, 10), entry(d("ammo_12g"), 3, 1, 6), entry(d("crowbar"), 2),
                            entry(d("baseball_bat"), 2), gun(d("pistol"), 1, 4), gun(d("shotgun"), 1, 2),
                            entry(d("molotov"), 2), entry(d("pipe_bomb"), 1), entry(d("ammo_357"), 2, 2, 8),
                            entry(d("jerry_can"), 3, 1, 2)])
    chest("dumpster", (2, 6), [entry(m("rotten_flesh"), 10, 1, 4), entry(m("bone"), 8, 1, 3), entry(m("string"), 6, 1, 3),
                               entry(m("paper"), 8, 1, 4), entry(m("glass_bottle"), 6, 1, 2), entry(m("leather"), 4),
                               entry(m("iron_nugget"), 5, 1, 4), entry(d("bandage"), 1),
                               entry(d("canned_beans"), 2), entry(d("soda"), 2),
                               entry(d("jerry_can"), 1)])
    chest("supply_drop", (5, 9), [entry(d("clean_water"), 8, 2, 4), entry(d("mre"), 8, 1, 3), entry(d("canned_beans"), 6, 2, 4),
                                  entry(d("first_aid_kit"), 4), entry(d("antibiotics"), 4, 1, 2), entry(d("bandage"), 6, 2, 5),
                                  entry(d("splint"), 3), entry(d("ammo_9mm"), 5, 8, 20), entry(d("ammo_556"), 4, 10, 30),
                                  entry(d("ammo_762"), 4, 8, 24), entry(d("ammo_12g"), 4, 4, 12), entry(d("frag_grenade"), 2, 1, 3),
                                  gun(d("m4"), 1, 30), gun(d("shotgun"), 1, 6), entry(d("energy_drink"), 3, 1, 2),
                                  entry(d("jerry_can"), 4, 1, 2)])
    chest("safe", (1, 4), [entry(m("gold_ingot"), 8, 1, 4), entry(m("emerald"), 6, 1, 3), entry(m("diamond"), 2, 1, 2),
                           entry(d("antidote"), 3), entry(m("golden_apple"), 2), entry(m("name_tag"), 2),
                           gun(d("pistol"), 4, 8), entry(d("ammo_9mm"), 4, 4, 15), gun(d("hunting_rifle"), 1, 3),
                           entry(d("ammo_762"), 2, 2, 8),
                           gun(d("revolver"), 2, 3), entry(d("suppressor"), 1), entry(d("scope"), 1),
                           entry(d("extended_mag"), 1), entry(d("frag_grenade"), 1, 1, 2)])


if __name__ == "__main__":
    gen_textures()
    gen_models()
    gen_states()
    gen_tags()
    gen_recipes()
    gen_container_loot()
    gen_rain_collector()
    gen_power()
