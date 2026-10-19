"""
Assault Dog (wiki, page Monsters: rank E, Asura Kingdom): canine monsters that move about like trained Dobermanns,
hunting in packs, often led by a Terminate Boar. Drawn after the manga panel of the wiki: black coat, gaping jaws full
of fangs, upright pointed ears, a ragged ridge along the back; the Dobermann's tan points on muzzle, brows, chest and
legs; red monster eyes.
"""
import numpy as np

import canine
from paint import band, fbm, fur, mix, rgba, scale, top_light

NAME = 'assault_dog'

SHAPE = canine.CanineShape(hip_y=17.0, chest_drop=11.0, withers=22.0, body_front=-10.0, body_back=8.0,
                           head=(8.5, 7.0, 8.0), muzzle=(5.2, 3.8, 8.5), ear=(2.8, 4.6), ear_spread=2.6,
                           neck_height=7.0, spikes=7, fangs=2.4, claw=1.6)

BLACK = (30, 31, 40)
SHEEN = (78, 86, 108)
TAN = (168, 98, 50)
TAN_DARK = (120, 64, 34)
IVORY = (232, 224, 200)
CLAW = (52, 48, 46)
MOUTH = (96, 26, 32)
EYE = (236, 52, 36)


def painter(model):
    meta = model.meta
    height = meta['height']

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'tooth':
            return rgba(mix(IVORY, (180, 170, 150), 1 - uv[1]))
        if tag == 'claw':
            return rgba(CLAW)
        if tag == 'nose':
            return rgba((18, 18, 22) if n[1] < 0.9 else (40, 40, 48))
        if tag == 'spike':
            return rgba(mix((52, 54, 62), (150, 146, 138), max(0.0, p[1] - (SHAPE.withers - 0.5)) / 2.6))
        for eye in meta['eyes']:
            d = np.linalg.norm(p - np.array(eye))
            if tag == 'skull' and d < 0.95:
                return rgba(mix((255, 214, 120), EYE, d / 0.95))
        if tag in ('jaw', 'muzzle') and ((tag == 'jaw' and face.name == 'up') or (tag == 'muzzle' and face.name == 'down')):
            return rgba(MOUTH)
        if tag == 'ear' and face.name == 'north' and 0.2 < uv[0] < 0.8 and uv[1] > 0.25:
            return rgba((86, 46, 52))

        strand = fur(p, density=1.6, seed=3)
        light = top_light(n, p[1], height)
        coat = mix(BLACK, SHEEN, 0.15 + 0.55 * light * band(n[1], 0.2, 0.6))
        coat = scale(coat, 0.86 + 0.28 * strand)

        tan = 0.0
        if tag in ('jaw',):
            tan = 1.0
        elif tag == 'muzzle':
            tan = band(meta['mouth_y'] + 1.6 - p[1], 0.0, 0.6) if face.name != 'up' else 0.0
        elif tag == 'brow':
            # two tan spots above the eyes
            for eye in meta['eyes']:
                if abs(p[0] - eye[0]) < 0.9 and face.name in ('north', 'up'):
                    tan = 1.0
        elif tag == 'shin' or tag == 'paw':
            tan = 1.0 if tag == 'paw' else band(6.0 - p[1], 0.0, 1.5)
        elif tag == 'body' and face.name == 'north':
            # the Dobermann's two chest patches
            for x in (-1.8, 1.8):
                if np.hypot(p[0] - x, (p[1] - SHAPE.chest_drop - 3.2) * 0.7) < 1.3:
                    tan = 1.0
        if tan > 0:
            t = mix(TAN_DARK, TAN, 0.4 + 0.6 * light)
            coat = mix(coat, scale(t, 0.9 + 0.2 * strand), tan)
        # rough, scarred fur: darker clumps
        if fbm(np.array(p) * 0.45, 2, 11) < 0.32 and tag in ('body', 'neck', 'leg'):
            coat = scale(coat, 0.8)
        return rgba(coat)

    return paint


def build():
    model = canine.build(NAME, SHAPE)
    model.pack()
    return model, painter(model), canine.all_clips(SHAPE)
