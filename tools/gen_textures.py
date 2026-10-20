"""Generates the placeholder zombie skins (64x64 vanilla zombie UV layout).

Run from the repo root: python3 tools/gen_textures.py
"""
import random
from pathlib import Path

from PIL import Image

OUT = Path("src/main/resources/assets/deadhorizon/textures/entity/zombie")


def jitter(rng, color, amount=14):
    return tuple(max(0, min(255, c + rng.randint(-amount, amount))) for c in color) + (255,)


def box(img, rng, u, v, w, h, d, color_fn):
    """Paints every face of a model box laid out at (u, v) with size w x h x d."""
    faces = [
        (u + d, v, w, d), (u + d + w, v, w, d),          # top, bottom
        (u, v + d, d, h), (u + d, v + d, w, h),          # right, front
        (u + d + w, v + d, d, h), (u + 2 * d + w, v + d, w, h),  # left, back
    ]
    for fx, fy, fw, fh in faces:
        for x in range(fx, fx + fw):
            for y in range(fy, fy + fh):
                img.putpixel((x, y), color_fn(x - fx, y - fy, fh))


CAMO = [(85, 95, 55), (60, 70, 40), (110, 105, 70), (45, 50, 35)]


def make(name, skin, shirt, pants, shoes, shirtless=False, seed=0, camo=False):
    rng = random.Random(seed)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    blood = (95, 15, 15)

    def skin_px(x, y, h):
        return jitter(rng, blood if rng.random() < 0.06 else skin)

    box(img, rng, 0, 0, 8, 8, 8, skin_px)
    # face: sunken eyes, open mouth
    for x, y in [(9, 12), (10, 12), (13, 12), (14, 12)]:
        img.putpixel((x, y), (20, 20, 20, 255))
    for x, y in [(10, 11), (13, 11)]:
        img.putpixel((x, y), (35, 45, 30, 255))
    for x in range(10, 14):
        img.putpixel((x, 14), (40, 10, 10, 255))
    img.putpixel((11, 15), (40, 10, 10, 255))
    img.putpixel((12, 15), (40, 10, 10, 255))

    def body_px(x, y, h):
        if shirtless:
            return skin_px(x, y, h)
        if rng.random() < 0.08:
            return jitter(rng, skin)  # rips in the shirt
        if camo:
            return jitter(rng, CAMO[(x // 2 + y // 3 + rng.randint(0, 1)) % len(CAMO)], 6)
        return jitter(rng, blood if rng.random() < 0.07 else shirt)

    box(img, rng, 16, 16, 8, 12, 4, body_px)

    def arm_px(x, y, h):
        if y < 5 and h == 12 and not shirtless:
            return body_px(x, y, h)
        return skin_px(x, y, h)

    box(img, rng, 40, 16, 4, 12, 4, arm_px)

    def leg_px(x, y, h):
        if h == 12 and y >= 10:
            return jitter(rng, shoes, 6)
        if camo:
            return jitter(rng, CAMO[(x // 2 + y // 2 + rng.randint(0, 1)) % len(CAMO)], 6)
        return jitter(rng, blood if rng.random() < 0.04 else pants)

    box(img, rng, 0, 16, 4, 12, 4, leg_px)

    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f"{name}.png")


make("walker", skin=(88, 128, 78), shirt=(60, 80, 110), pants=(70, 55, 40), shoes=(40, 32, 26), seed=1)
make("runner", skin=(150, 165, 140), shirt=(130, 40, 40), pants=(55, 55, 60), shoes=(30, 30, 30), seed=2)
make("brute", skin=(70, 95, 60), shirt=(0, 0, 0), pants=(60, 70, 45), shoes=(35, 30, 25), shirtless=True, seed=3)
make("screamer", skin=(190, 180, 165), shirt=(200, 200, 190), pants=(90, 90, 100), shoes=(40, 40, 40), seed=4)
make("spitter", skin=(140, 160, 60), shirt=(90, 110, 50), pants=(60, 70, 40), shoes=(40, 40, 30), seed=5)
make("bloater", skin=(110, 95, 110), shirt=(0, 0, 0), pants=(70, 60, 50), shoes=(40, 35, 30), shirtless=True, seed=6)
make("crawler", skin=(75, 85, 65), shirt=(55, 45, 35), pants=(45, 40, 35), shoes=(30, 25, 20), seed=7)
make("soldier", skin=(95, 120, 85), shirt=(0, 0, 0), pants=(0, 0, 0), shoes=(30, 28, 24), seed=8, camo=True)
