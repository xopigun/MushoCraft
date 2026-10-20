"""
Snow Drake scale armour, drawn for MushoCraft: the worn layers (64x32, vanilla humanoid armour layout) and the four
item icons, white scales with a pale blue sheen, a few dark blue scales as on the drake's back, steel-blue rims.
Usage: python drake_armour.py <assets/mushocraft directory>
"""
import os
import random
import sys

from PIL import Image

ROOT = sys.argv[1]

WHITE = (232, 234, 242)
SHADE = (184, 188, 206)
BLUE = (66, 74, 150)
RIM = (92, 108, 140)
CLEAR = (0, 0, 0, 0)


def scale_px(x, y, rng):
    """White scales in staggered rows, their lower edge shaded, now and then a dark blue one."""
    row = y // 2
    col = (x + (row % 2)) // 2
    if y % 2 == 1 and (x + row) % 2 == 0:
        return SHADE  # the lower edge of a scale, every other pixel
    random.seed(row * 131 + col * 17)
    return BLUE if random.random() < 0.07 else WHITE


def box(u, v, w, h, d):
    """Faces of a box unwrap: (face, x0, y0, width, height)."""
    return [('top', u + d, v, w, d), ('bottom', u + d + w, v, w, d), ('right', u, v + d, d, h), ('front', u + d, v + d, w, h),
            ('left', u + d + w, v + d, d, h), ('back', u + 2 * d + w, v + d, w, h)]


def paint(img, faces, keep, rng):
    for face, x0, y0, fw, fh in faces:
        for y in range(fh):
            for x in range(fw):
                c = keep(face, x, y, fw, fh)
                if c is None:
                    continue
                if c == 'scale':
                    c = scale_px(x0 + x, y0 + y, rng)
                img.putpixel((x0 + x, y0 + y), c + (255,) if len(c) == 3 else c)


def helmet(face, x, y, w, h):
    if face == 'front' and 1 <= x <= w - 2 and 3 <= y <= 6:
        return CLEAR  # the face opening
    if face in ('front', 'left', 'right', 'back') and y == h - 1:
        return RIM
    return 'scale'


def plate(face, x, y, w, h):
    if face in ('front', 'back', 'left', 'right') and (y == 0 or y == h - 1):
        return RIM
    return 'scale'


def boots(face, x, y, w, h):
    if face in ('front', 'back', 'left', 'right'):
        if y < h - 5:
            return CLEAR
        return RIM if y == h - 5 else 'scale'
    return 'scale' if face == 'bottom' else CLEAR


def leggings_body(face, x, y, w, h):
    if face in ('front', 'back', 'left', 'right'):
        if y < h - 4:
            return CLEAR
        return RIM if y == h - 4 else 'scale'
    return CLEAR


def leggings_leg(face, x, y, w, h):
    if face in ('front', 'back', 'left', 'right'):
        if y > h - 4:
            return CLEAR
        return RIM if y == 0 else 'scale'
    return 'scale' if face == 'top' else CLEAR


rng = random.Random(7)
HEAD, BODY, ARM, LEG = (0, 0, 8, 8, 8), (16, 16, 8, 12, 4), (40, 16, 4, 12, 4), (0, 16, 4, 12, 4)
layer1 = Image.new('RGBA', (64, 32), CLEAR)
paint(layer1, box(*HEAD), helmet, rng)
paint(layer1, box(*BODY), plate, rng)
paint(layer1, box(*ARM), plate, rng)
paint(layer1, box(*LEG), boots, rng)
layer2 = Image.new('RGBA', (64, 32), CLEAR)
paint(layer2, box(*BODY), leggings_body, rng)
paint(layer2, box(*LEG), leggings_leg, rng)
armour_dir = os.path.join(ROOT, 'textures/models/armor')
os.makedirs(armour_dir, exist_ok=True)
layer1.save(os.path.join(armour_dir, 'snow_drake_layer_1.png'))
layer2.save(os.path.join(armour_dir, 'snow_drake_layer_2.png'))

# item icons: a shape mask per piece, filled with scales and outlined
ICONS = {
    'helmet': ["................", "................", "....########....", "...##########...", "..############..",
               "..############..", "..###......###..", "..##........##..", "..##........##..", "..##........##..",
               "................", "................", "................", "................", "................", "................"],
    'chestplate': ["................", "..####....####..", "..############..", "..############..", "...##########...",
                   "....########....", "....########....", "....########....", "....########....", "....########....",
                   "....########....", "....########....", "................", "................", "................", "................"],
    'leggings': ["................", "....########....", "....########....", "....########....", "....###..###....",
                 "....###..###....", "....###..###....", "....###..###....", "....###..###....", "....###..###....",
                 "....###..###....", "....###..###....", "................", "................", "................", "................"],
    'boots': ["................", "................", "................", "................", "................",
              "................", "...###....###...", "...###....###...", "...###....###...", "..####...####...",
              ".#####..#####...", ".#####..#####...", "................", "................", "................", "................"],
}
for name, mask in ICONS.items():
    icon = Image.new('RGBA', (16, 16), CLEAR)
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch != '#':
                continue
            edge = any(not (0 <= x + dx < 16 and 0 <= y + dy < 16) or mask[y + dy][x + dx] != '#'
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            icon.putpixel((x, y), (RIM if edge else scale_px(x, y, rng)) + (255,))
    icon.save(os.path.join(ROOT, 'textures/item/snow_drake_%s.png' % name))
print('armour textures ok')
