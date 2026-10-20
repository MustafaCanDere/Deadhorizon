"""Builds the abandoned town buildings as structure templates (.nbt) plus their worldgen JSON.

Run from the repo root: python3 tools/gen_structures.py

Each building is drawn with a tiny block-painting API: x runs left to right, y up, z from the front
(the door, at z = 0) to the back. Furniture "facing" is the direction its front looks at.
"""
import gzip
import json
import struct
from pathlib import Path

RES = Path("src/main/resources")
DATA = RES / "data/deadhorizon"
MOD = "deadhorizon"
DATA_VERSION = 3465  # 1.20.1


# ---------------------------------------------------------------- minimal NBT writer

def _str(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def _payload(v):
    if isinstance(v, str):
        return 8, _str(v)
    if isinstance(v, bool):
        return 1, struct.pack(">b", int(v))
    if isinstance(v, int):
        return 3, struct.pack(">i", v)
    if isinstance(v, dict):
        out = b""
        for k, item in v.items():
            t, p = _payload(item)
            out += struct.pack(">b", t) + _str(k) + p
        return 10, out + b"\x00"
    if isinstance(v, list):
        if not v:
            return 9, struct.pack(">bi", 10, 0)
        t = _payload(v[0])[0]
        return 9, struct.pack(">bi", t, len(v)) + b"".join(_payload(i)[1] for i in v)
    raise TypeError(v)


def write_nbt(path, root):
    path.parent.mkdir(parents=True, exist_ok=True)
    t, p = _payload(root)
    with gzip.open(path, "wb") as f:
        f.write(struct.pack(">b", t) + _str("") + p)


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


# ---------------------------------------------------------------- painter

PANES = ("minecraft:glass_pane", "minecraft:iron_bars")
SOLID_HINTS = ("planks", "bricks", "log", "concrete", "stone", "quartz", "sandstone", "terracotta", "wool", "glass")


class Building:
    def __init__(self, sx, sy, sz):
        self.size = (sx, sy, sz)
        self.blocks = {}
        for x in range(sx):
            for y in range(sy):
                for z in range(sz):
                    self.blocks[(x, y, z)] = ("minecraft:air", {}, None)

    def set(self, x, y, z, name, props=None, nbt=None):
        if not name.startswith("minecraft:") and ":" not in name:
            name = "minecraft:" + name
        self.blocks[(x, y, z)] = (name, {k: str(v).lower() for k, v in (props or {}).items()}, nbt)

    def fill(self, x1, y1, z1, x2, y2, z2, name, props=None):
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.set(x, y, z, name, props)

    def walls(self, x1, y1, z1, x2, y2, z2, name, corner=None):
        for x in range(x1, x2 + 1):
            for z in range(z1, z2 + 1):
                if x in (x1, x2) or z in (z1, z2):
                    edge = x in (x1, x2) and z in (z1, z2)
                    for y in range(y1, y2 + 1):
                        if edge and corner:
                            self.set(x, y, z, corner, {"axis": "y"} if "log" in corner else None)
                        else:
                            self.set(x, y, z, name)

    def door(self, x, y, z, facing="south", kind="oak_door"):
        self.set(x, y, z, kind, {"facing": facing, "half": "lower", "hinge": "left", "open": "false"})
        self.set(x, y + 1, z, kind, {"facing": facing, "half": "upper", "hinge": "left", "open": "false"})

    def mod(self, x, y, z, name, facing="north", loot=None, **props):
        p = {"facing": facing, **props}
        nbt = {"id": f"{MOD}:storage", "LootTable": f"{MOD}:containers/{loot}"} if loot else None
        self.set(x, y, z, f"{MOD}:{name}", p, nbt)

    def tall(self, x, y, z, name, facing="north", loot=None):
        self.mod(x, y, z, name, facing, loot, half="lower")
        self.mod(x, y + 1, z, name, facing, None, half="upper")

    def bed(self, x, y, z, facing="south", color="red"):
        dx, dz = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}[facing]
        self.set(x, y, z, f"{color}_bed", {"facing": facing, "part": "foot", "occupied": "false"})
        self.set(x + dx, y, z + dz, f"{color}_bed", {"facing": facing, "part": "head", "occupied": "false"})

    def _connects(self, pos):
        b = self.blocks.get(pos)
        if not b:
            return False
        name = b[0]
        return name in PANES or any(h in name for h in SOLID_HINTS) and "pane" not in name and "slab" not in name

    def save(self, name):
        for (x, y, z), (bname, props, nbt) in list(self.blocks.items()):
            if bname in PANES:
                for d, (dx, dz) in {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}.items():
                    props[d] = "true" if self._connects((x + dx, y, z + dz)) else "false"
        palette, index, blocks = [], {}, []
        for (x, y, z), (bname, props, nbt) in sorted(self.blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
            key = (bname, tuple(sorted(props.items())))
            if key not in index:
                index[key] = len(palette)
                entry = {"Name": bname}
                if props:
                    entry["Properties"] = dict(sorted(props.items()))
                palette.append(entry)
            block = {"pos": [x, y, z], "state": index[key]}
            if nbt:
                block["nbt"] = nbt
            blocks.append(block)
        write_nbt(DATA / f"structures/{name}.nbt", {"DataVersion": DATA_VERSION, "size": list(self.size),
                                                    "palette": palette, "blocks": blocks, "entities": []})


def gable_roof(b, x1, x2, z1, z2, y0, stairs, fill):
    """A roof rising from the front and back walls to a ridge along x; gable ends filled with `fill`."""
    depth = z2 - z1
    for i in range((depth + 2) // 2):
        y = y0 + i
        zf, zb = z1 + i, z2 - i
        if zf < zb:
            for x in range(x1, x2 + 1):
                b.set(x, y, zf, stairs, {"facing": "south", "half": "bottom", "shape": "straight"})
                b.set(x, y, zb, stairs, {"facing": "north", "half": "bottom", "shape": "straight"})
            for z in range(zf + 1, zb):
                b.set(x1, y, z, fill)
                b.set(x2, y, z, fill)
        elif zf == zb:
            for x in range(x1, x2 + 1):
                b.set(x, y, zf, stairs.replace("_stairs", "_slab"), {"type": "bottom"})


def flat_roof(b, x1, x2, z1, z2, y, block, rim):
    b.fill(x1, y, z1, x2, y, z2, block)
    for x in range(x1, x2 + 1):
        for z in (z1, z2):
            b.set(x, y + 1, z, rim, {"type": "bottom"})
    for z in range(z1, z2 + 1):
        for x in (x1, x2):
            b.set(x, y + 1, z, rim, {"type": "bottom"})


# ---------------------------------------------------------------- buildings

def house():
    b = Building(9, 9, 9)
    b.fill(0, 0, 0, 8, 0, 8, "cobblestone")
    b.fill(1, 0, 1, 7, 0, 7, "oak_planks")
    b.walls(0, 1, 0, 8, 3, 8, "bricks", corner="oak_log")
    for x in (2, 6):
        b.set(x, 2, 0, "glass_pane")
    for z in (2, 6):
        b.set(0, 2, z, "glass_pane")
        b.set(8, 2, z, "glass_pane")
    b.door(4, 1, 0)
    b.fill(1, 4, 1, 7, 4, 7, "oak_planks")  # ceiling
    gable_roof(b, 0, 8, 0, 8, 4, "spruce_stairs", "oak_planks")
    # kitchen (back left)
    for x in (1, 2, 3):
        b.mod(x, 1, 7, "kitchen_cabinet", "north", loot="kitchen" if x != 2 else None)
    b.tall(1, 1, 5, "fridge", "east", loot="fridge")
    b.mod(4, 1, 7, "trash_can")
    b.mod(3, 1, 4, "table")
    b.mod(3, 1, 3, "chair", "north")
    b.mod(3, 1, 5, "chair", "south")
    # bedroom (back right)
    b.bed(7, 1, 6, "south")
    b.tall(7, 1, 1, "wardrobe", "west", loot="wardrobe")
    b.mod(6, 2, 7, "medicine_cabinet", "north", loot="medicine")
    b.mod(7, 1, 4, "table_lamp", "west", lit="false")
    b.mod(4, 3, 4, "ceiling_lamp", "north", lit="false")
    b.set(1, 3, 1, "cobweb")
    b.set(7, 3, 7, "cobweb")
    b.set(5, 1, 2, "red_carpet")  # stain by the door
    b.save("house")


def pharmacy():
    b = Building(11, 6, 9)
    b.fill(0, 0, 0, 10, 0, 8, "smooth_stone")
    b.walls(0, 1, 0, 10, 3, 8, "white_concrete", corner="light_gray_concrete")
    for x in range(1, 10):
        if x != 5:
            b.fill(x, 1, 0, x, 2, 0, "glass_pane")
    b.door(5, 1, 0)
    b.fill(1, 3, 0, 9, 3, 0, "lime_concrete")  # the green sign band
    flat_roof(b, 0, 10, 0, 8, 4, "smooth_stone", "smooth_stone_slab")
    # shelves of medicine cabinets on the back wall, a counter in front of them
    for x in range(1, 10):
        b.mod(x, 2, 7, "medicine_cabinet", "north", loot="medicine" if x % 2 else None)
    for x in range(2, 9):
        b.mod(x, 1, 5, "kitchen_cabinet", "south", loot="medicine" if x in (3, 6) else None)
    b.mod(1, 1, 1, "crate", loot="medicine")
    b.mod(9, 1, 1, "trash_can")
    b.mod(5, 3, 3, "ceiling_lamp", lit="false")
    b.set(7, 1, 2, "white_carpet")
    b.save("pharmacy")


def gas_station():
    b = Building(13, 6, 13)
    b.fill(0, 0, 0, 12, 0, 12, "gray_concrete")
    for x in (2, 10):
        b.fill(x, 0, 2, x, 0, 6, "yellow_concrete")  # lane paint
    # canopy over the pumps
    for x, z in ((1, 1), (11, 1), (1, 6), (11, 6)):
        b.fill(x, 1, z, x, 3, z, "stone_brick_wall")
    b.fill(0, 4, 0, 12, 4, 7, "smooth_stone_slab", {"type": "bottom"})
    b.fill(0, 4, 0, 12, 4, 0, "red_concrete")
    for x in (4, 8):
        b.set(x, 1, 3, "red_concrete")
        b.set(x, 2, 3, "light_gray_concrete")
    b.mod(6, 1, 4, "traffic_cone")
    b.mod(4, 1, 5, "concrete_barrier", "east")
    # shop
    b.walls(2, 1, 8, 10, 3, 12, "sandstone", corner="smooth_sandstone")
    for x in (3, 4, 8, 9):
        b.set(x, 2, 8, "glass_pane")
    b.door(6, 1, 8)
    flat_roof(b, 2, 10, 8, 12, 4, "smooth_sandstone", "smooth_sandstone_slab")
    b.mod(3, 1, 11, "crate", loot="gas_station")
    b.mod(4, 1, 11, "kitchen_cabinet", loot="gas_station")
    b.tall(9, 1, 11, "fridge", "north", loot="fridge")
    b.mod(8, 1, 10, "kitchen_cabinet", "west", loot="kitchen")
    b.mod(5, 3, 10, "ceiling_lamp", lit="false")
    b.mod(11, 1, 10, "dumpster", "west", loot="dumpster")
    b.save("gas_station")


def police_station():
    b = Building(11, 6, 11)
    b.fill(0, 0, 0, 10, 0, 10, "polished_andesite")
    b.walls(0, 1, 0, 10, 3, 10, "stone_bricks", corner="chiseled_stone_bricks")
    for x in (2, 8):
        b.set(x, 2, 0, "iron_bars")
    for z in (3, 7):
        b.set(0, 2, z, "iron_bars")
        b.set(10, 2, z, "iron_bars")
    b.door(5, 1, 0)
    b.fill(1, 3, 0, 9, 3, 0, "blue_concrete")
    flat_roof(b, 0, 10, 0, 10, 4, "stone_bricks", "stone_brick_slab")
    # cells at the back
    b.fill(1, 1, 7, 9, 3, 7, "iron_bars")
    b.fill(5, 1, 7, 5, 3, 10, "stone_bricks")
    b.set(3, 1, 7, "iron_door", {"facing": "south", "half": "lower", "hinge": "left", "open": "true"})
    b.set(3, 2, 7, "iron_door", {"facing": "south", "half": "upper", "hinge": "left", "open": "true"})
    b.fill(1, 3, 8, 9, 3, 9, "air")
    b.bed(8, 1, 8, "south", "gray")
    b.set(1, 1, 9, "cauldron")
    # office and armoury
    b.mod(5, 1, 3, "table")
    b.mod(5, 1, 2, "chair", "south")
    b.mod(1, 1, 5, "safe", "east", loot="police")
    b.mod(9, 1, 5, "crate", "west", loot="police")
    b.mod(9, 1, 1, "crate", "west", loot="crate")
    b.mod(1, 2, 1, "medicine_cabinet", "east", loot="medicine")
    b.mod(5, 3, 4, "ceiling_lamp", lit="false")
    b.mod(2, 1, 1, "sandbags", "south")
    b.mod(8, 1, 1, "sandbags", "south")
    b.save("police_station")


def market():
    b = Building(13, 6, 11)
    b.fill(0, 0, 0, 12, 0, 10, "white_terracotta")
    b.walls(0, 1, 0, 12, 3, 10, "smooth_sandstone", corner="cut_sandstone")
    for x in range(1, 12):
        if x not in (6,):
            b.fill(x, 1, 0, x, 2, 0, "glass_pane")
    b.door(6, 1, 0)
    b.fill(1, 3, 0, 11, 3, 0, "orange_concrete")
    flat_roof(b, 0, 12, 0, 10, 4, "smooth_sandstone", "smooth_sandstone_slab")
    # two aisles of shelves
    for z in (3, 6):
        for x in range(2, 11):
            if x == 6:
                continue
            loot = "market" if (x + z) % 3 == 0 else None
            b.mod(x, 1, z, "kitchen_cabinet", "south" if z == 3 else "north", loot=loot)
    for x in (1, 3, 5, 7, 9, 11):
        b.tall(x, 1, 9, "fridge", "north", loot="fridge" if x % 4 == 1 else None)
    b.mod(10, 1, 1, "kitchen_cabinet", "west", loot="market")  # till
    b.mod(11, 1, 1, "stool", "west")
    b.mod(1, 1, 1, "trash_can")
    b.mod(4, 1, 1, "crate", loot="market")
    for x in (3, 9):
        b.mod(x, 3, 5, "ceiling_lamp", lit="false")
    b.set(7, 1, 5, "red_carpet")
    b.save("market")


BUILDINGS = {"house": (house, 6), "market": (market, 2), "pharmacy": (pharmacy, 2),
             "gas_station": (gas_station, 2), "police_station": (police_station, 1)}

ZOMBIES = [("walker", 10, 1, 3), ("runner", 3, 1, 2), ("crawler", 2, 1, 1), ("soldier", 1, 1, 1)]


def gen_worldgen():
    write(DATA / "tags/worldgen/biome/has_structure/town.json", {"replace": False, "values": [
        "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow", "minecraft:forest",
        "minecraft:birch_forest", "minecraft:savanna", "minecraft:taiga", "minecraft:snowy_plains",
        "minecraft:desert", "minecraft:dark_forest"]})
    write(DATA / "worldgen/processor_list/decay.json", {"processors": [{"processor_type": "minecraft:rule", "rules": [
        rule("minecraft:glass_pane", 0.4, "minecraft:air"),
        rule("minecraft:cobblestone", 0.3, "minecraft:mossy_cobblestone"),
        rule("minecraft:stone_bricks", 0.25, "minecraft:cracked_stone_bricks"),
        rule("minecraft:bricks", 0.05, "minecraft:air"),
        rule("minecraft:oak_planks", 0.05, "minecraft:air"),
    ]}]})
    for name, (_, _) in BUILDINGS.items():
        write(DATA / f"worldgen/template_pool/{name}.json", {"fallback": "minecraft:empty", "elements": [
            {"weight": 1, "element": {"element_type": "minecraft:single_pool_element", "location": f"{MOD}:{name}",
                                      "projection": "rigid", "processors": f"{MOD}:decay"}}]})
        write(DATA / f"worldgen/structure/{name}.json", {
            "type": "minecraft:jigsaw", "biomes": f"#{MOD}:has_structure/town", "step": "surface_structures",
            "spawn_overrides": {"monster": {"bounding_box": "piece", "spawns": [
                {"type": f"{MOD}:{z}", "weight": w, "minCount": lo, "maxCount": hi} for z, w, lo, hi in ZOMBIES]}},
            "terrain_adaptation": "beard_thin", "start_pool": f"{MOD}:{name}", "size": 1,
            "start_height": {"absolute": 0}, "project_start_to_heightmap": "WORLD_SURFACE_WG",
            "max_distance_from_center": 80, "use_expansion_hack": False})
    write(DATA / "worldgen/structure_set/town_buildings.json", {
        "structures": [{"structure": f"{MOD}:{n}", "weight": w} for n, (_, w) in BUILDINGS.items()],
        "placement": {"type": "minecraft:random_spread", "spacing": 14, "separation": 5, "salt": 48151623}})


def rule(block, chance, out):
    return {"input_predicate": {"predicate_type": "minecraft:random_block_match", "block": block, "probability": chance},
            "location_predicate": {"predicate_type": "minecraft:always_true"}, "output_state": {"Name": out}}


if __name__ == "__main__":
    for fn, _ in BUILDINGS.values():
        fn()
    gen_worldgen()
