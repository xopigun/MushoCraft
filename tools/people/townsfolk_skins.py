"""
Skins of the village folk (64x64, player layout), drawn from scratch for MushoCraft: country people of the Asura
Kingdom in linen tunics, aprons and dresses, belts and wooden-soled shoes. One set per sex and age group
(child, adult, elder), several variants each; colours and hair are drawn at random from small palettes with a fixed
seed, so the files are reproducible.
Usage: python townsfolk_skins.py <assets/mushocraft/textures/entity/townsfolk>
"""
import os
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
CLEAR = (0, 0, 0, 0)

SKINS = [(236, 196, 164), (222, 178, 140), (200, 150, 112), (176, 126, 92), (244, 210, 182)]
HAIR = [(92, 62, 38), (60, 40, 26), (200, 160, 90), (150, 90, 44), (36, 30, 28), (176, 120, 60), (120, 74, 40)]
GREY_HAIR = [(200, 200, 204), (168, 166, 170), (226, 224, 220)]
EYES = [(70, 100, 150), (80, 120, 70), (96, 70, 46), (60, 60, 70)]
CLOTH = [(206, 196, 168), (150, 110, 74), (98, 120, 76), (84, 104, 140), (150, 74, 64), (176, 150, 96), (120, 96, 120),
         (110, 120, 120)]
TROUSERS = [(96, 78, 58), (72, 70, 66), (110, 96, 72), (64, 72, 90)]
APRON = [(232, 226, 210), (214, 206, 186)]
SHOES = [(70, 50, 36), (56, 42, 32)]


def faces(u, v, w, h, d):
    yield 'top', u + d, v, w, d
    yield 'bottom', u + d + w, v, w, d
    yield 'right', u, v + d, d, h
    yield 'front', u + d, v + d, w, h
    yield 'left', u + d + w, v + d, d, h
    yield 'back', u + 2 * d + w, v + d, w, h


def paint(img, part, fn, rng):
    u, v, w, h, d = PARTS[part]
    for face, x0, y0, fw, fh in faces(u, v, w, h, d):
        for y in range(fh):
            for x in range(fw):
                c = fn(face, x, y, fw, fh)
                if c is None:
                    continue
                if len(c) == 4:
                    img.putpixel((x0 + x, y0 + y), c)
                    continue
                k = rng.randint(-6, 6)
                img.putpixel((x0 + x, y0 + y), tuple(max(0, min(255, ch + k)) for ch in c) + (255,))


def dark(c, k=0.75):
    return tuple(int(ch * k) for ch in c[:3])


def person(sex, group, rng):
    """The look of one person: a dictionary of colours and features."""
    elder = group == 'elder'
    return {
        'skin': rng.choice(SKINS),
        'hair': rng.choice(GREY_HAIR if elder else HAIR),
        'eyes': rng.choice(EYES),
        'shirt': rng.choice(CLOTH),
        'over': rng.choice(CLOTH),
        'trousers': rng.choice(TROUSERS),
        'apron': rng.choice(APRON) if sex == 'female' and rng.random() < 0.6 else None,
        'shoes': rng.choice(SHOES),
        'long_hair': sex == 'female' and rng.random() < 0.85,
        'beard': sex == 'male' and (elder and rng.random() < 0.7 or group == 'adult' and rng.random() < 0.3),
        'bald': sex == 'male' and elder and rng.random() < 0.4,
        'dress': sex == 'female' and rng.random() < 0.7,
    }


def head(p):
    skin, hair = p['skin'], p['hair']

    def fn(face, x, y, w, h):
        if face == 'top':
            return skin if p['bald'] and 2 <= x <= 5 and 2 <= y <= 5 else hair
        if face == 'bottom':
            return p['hair'] if p['beard'] else skin
        if face == 'back':
            return hair if not p['bald'] or y > 2 else skin
        if face in ('left', 'right'):
            if y < 2 or (p['long_hair'] and y < 8) or (face == 'right' and x < 2 and y < 5) or (face == 'left' and x > 5 and y < 5):
                return hair
            if p['beard'] and y >= 5:
                return hair
            return skin
        # front
        if y < 2 and not (p['bald'] and y == 1):
            return hair
        if y == 2 and (x in (0, 7) or (p['long_hair'] and x in (0, 1, 6, 7))):
            return hair
        if p['long_hair'] and x in (0, 7) and y < 7:
            return hair
        if y == 3 and x in (1, 2, 5, 6):
            return dark(hair, 0.7)  # brows
        if y == 4 and x in (1, 6):
            return (240, 240, 240)
        if y == 4 and x in (2, 5):
            return p['eyes']
        if p['beard'] and y >= 5 and 1 <= x <= 6 and not (y == 6 and 3 <= x <= 4):
            return hair
        if y == 6 and 3 <= x <= 4:
            return (170, 100, 90)
        return skin
    return fn


def hair_overlay(p):
    def fn(face, x, y, w, h):
        if p['long_hair'] and face in ('back', 'left', 'right') and y >= 4:
            return p['hair'] + (255,)
        return CLEAR
    return fn


def torso(p):
    def fn(face, x, y, w, h):
        if face == 'top':
            return p['shirt']
        if face == 'bottom':
            return p['trousers']
        if p['dress']:
            if face == 'front' and p['apron'] and y >= 3:
                return p['apron']
            if y == 6:
                return dark(p['over'], 0.6)  # waist cord
            return p['over'] if y >= 2 else p['shirt']
        if y == 8:
            return (70, 50, 34) if not (face == 'front' and 3 <= x <= 4) else (180, 160, 90)  # belt and buckle
        if y > 8:
            return p['over']
        if face == 'front' and y < 3 and 3 <= x <= 4:
            return p['skin'] if y < 2 else dark(p['shirt'], 0.8)  # open collar
        return p['over'] if face in ('front', 'back') and (x < 2 or x > 5) else p['shirt']
    return fn


def arm(p):
    def fn(face, x, y, w, h):
        if face == 'top':
            return p['shirt']
        if face == 'bottom' or y >= 9:
            return p['skin']
        if y == 8:
            return dark(p['shirt'], 0.8)  # cuff
        return p['shirt']
    return fn


def leg(p):
    def fn(face, x, y, w, h):
        if face == 'bottom' or y >= 10:
            return p['shoes']
        if p['dress']:
            return p['skin'] if y >= 7 else p['over']
        return p['trousers']
    return fn


def skirt(p):
    def fn(face, x, y, w, h):
        if p['dress'] and face in ('front', 'back', 'left', 'right') and y < 8:
            if face == 'front' and p['apron'] and y < 6:
                return p['apron'] + (255,)
            return (dark(p['over'], 0.6) if y == 7 else p['over']) + (255,)
        return CLEAR
    return fn


def draw(p, seed):
    rng = random.Random(seed)
    img = Image.new('RGBA', (64, 64), CLEAR)
    paint(img, 'head', head(p), rng)
    paint(img, 'hat', hair_overlay(p), rng)
    paint(img, 'body', torso(p), rng)
    for part in ('rarm', 'larm'):
        paint(img, part, arm(p), rng)
    for part in ('rleg', 'lleg'):
        paint(img, part, leg(p), rng)
    for part in ('rpants', 'lpants'):
        paint(img, part, skirt(p), rng)
    return img


GROUPS = {'child': 3, 'adult': 6, 'elder': 2}
os.makedirs(OUT, exist_ok=True)
for sex_index, sex in enumerate(('male', 'female')):
    for group, count in GROUPS.items():
        for i in range(count):
            seed = 1000 * sex_index + 100 * list(GROUPS).index(group) + i
            look = person(sex, group, random.Random(seed))
            draw(look, seed).save(os.path.join(OUT, '%s_%s_%d.png' % (sex, group, i)))
print('townsfolk skins ok')
