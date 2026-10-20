"""Executioner skin (64x64, player layout), drawn from scratch for MushoCraft after the manga (LN vol. 3 ch. 12 adaptation):
full plate armour, a great helm with a barred visor and a rotten gaping mouth behind the bars, a torn dark tabard,
rotting flesh showing at the joints. The manga is black and white: the colours are chosen."""
import random
import sys

from PIL import Image

OUT = sys.argv[1]

PARTS = {
    'head': (0, 0, 8, 8, 8), 'hat': (32, 0, 8, 8, 8),
    'body': (16, 16, 8, 12, 4), 'jacket': (16, 32, 8, 12, 4),
    'rarm': (40, 16, 4, 12, 4), 'rsleeve': (40, 32, 4, 12, 4),
    'larm': (32, 48, 4, 12, 4), 'lsleeve': (48, 48, 4, 12, 4),
    'rleg': (0, 16, 4, 12, 4), 'rpants': (0, 32, 4, 12, 4),
    'lleg': (16, 48, 4, 12, 4), 'lpants': (0, 48, 4, 12, 4),
}

STEEL = (156, 158, 166)
STEEL_DARK = (98, 100, 110)
STEEL_LIGHT = (198, 200, 208)
RUST = (120, 82, 60)
GAP = (22, 20, 24)
FLESH = (104, 112, 92)
FLESH_DARK = (70, 74, 60)
TOOTH = (214, 206, 180)
CLOTH = (66, 58, 56)
CLOTH_DARK = (44, 38, 38)


def faces(u, v, w, h, d):
    yield 'top', u + d, v, w, d
    yield 'bottom', u + d + w, v, w, d
    yield 'right', u, v + d, d, h
    yield 'front', u + d, v + d, w, h
    yield 'left', u + d + w, v + d, d, h
    yield 'back', u + 2 * d + w, v + d, w, h


def grime(c, rng):
    """Armour scuffed and stained: noise, now and then a spot of rust or old blood."""
    if len(c) == 4:
        return c
    k = rng.randint(-9, 9)
    c = tuple(max(0, min(255, x + k)) for x in c)
    if rng.random() < 0.015:
        c = RUST
    return c + (255,)


def paint(img, part, fn, seed):
    rng = random.Random(seed)
    u, v, w, h, d = PARTS[part]
    for face, x0, y0, fw, fh in faces(u, v, w, h, d):
        for y in range(fh):
            for x in range(fw):
                c = fn(face, x, y, fw, fh)
                if c is not None:
                    img.putpixel((x0 + x, y0 + y), grime(c, rng))


def helm(face, x, y, w, h):
    """A great helm; the front is a barred visor over a gaping, rotten mouth full of teeth."""
    if face == 'top':
        return STEEL_LIGHT if x in (3, 4) else STEEL
    if face == 'bottom':
        return GAP
    if face != 'front':
        return STEEL_DARK if y == 7 else STEEL
    if y == 0 or y == 7 or x in (0, 7):
        return STEEL_DARK
    if x in (1, 3, 5) and y <= 5:
        return STEEL_LIGHT  # the bars
    # behind the bars: the mouth, open wide
    if y == 2 and x in (2, 4, 6) or y == 5 and x in (2, 4):
        return TOOTH
    if 3 <= y <= 4 and 2 <= x <= 6:
        return (60, 18, 20)
    return FLESH_DARK if y in (1, 6) else FLESH


def helm_overlay(face, x, y, w, h):
    """A ridge over the crown and a gorget rim."""
    if face == 'top' and x in (3, 4):
        return STEEL_LIGHT
    if face in ('left', 'right', 'back') and y == 6:
        return STEEL_DARK
    return (0, 0, 0, 0)


def breastplate(face, x, y, w, h):
    if face in ('top', 'bottom'):
        return STEEL_DARK
    if y < 7:
        if face == 'front' and x in (3, 4) and y > 0:
            return STEEL_LIGHT  # the ridge of the breastplate
        return STEEL_DARK if y == 6 else STEEL
    return CLOTH  # the tabard below


def tabard(face, x, y, w, h):
    """The torn tabard over the belly and thighs."""
    if face not in ('front', 'back') or y < 6:
        return (0, 0, 0, 0)
    if y == 11 and x % 3 == 1 or y == 10 and x % 4 == 2:
        return (0, 0, 0, 0)  # ragged hem
    return CLOTH_DARK if (x + y) % 5 == 0 else CLOTH


def arm(face, x, y, w, h):
    if face == 'top':
        return STEEL
    if face == 'bottom':
        return STEEL_DARK
    if y < 4:
        return STEEL_LIGHT if y == 0 else STEEL  # pauldron
    if y in (5, 6):
        return FLESH if x % 2 == 0 else FLESH_DARK  # rotten flesh at the elbow
    if y >= 9:
        return STEEL_DARK if y == 9 else STEEL  # gauntlet
    return STEEL


def pauldron(face, x, y, w, h):
    if y < 3 and face != 'bottom':
        return STEEL_LIGHT if y == 0 else STEEL
    return (0, 0, 0, 0)


def leg(face, x, y, w, h):
    if face in ('top', 'bottom'):
        return STEEL_DARK
    if y in (4, 5):
        return FLESH if (x + y) % 2 == 0 else FLESH_DARK  # the knee, rotting through
    if y >= 10:
        return STEEL_DARK  # sabaton
    return STEEL


img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
paint(img, 'head', helm, 1)
paint(img, 'hat', helm_overlay, 2)
paint(img, 'body', breastplate, 3)
paint(img, 'jacket', tabard, 4)
for part, seed in (('rarm', 5), ('larm', 6)):
    paint(img, part, arm, seed)
for part, seed in (('rsleeve', 7), ('lsleeve', 8)):
    paint(img, part, pauldron, seed)
for part, seed in (('rleg', 9), ('lleg', 10)):
    paint(img, part, leg, seed)
img.save(OUT)
print('skin ok')
