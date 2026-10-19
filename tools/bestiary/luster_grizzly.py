"""
Luster Grizzly (wiki, page Monsters: rank B, Lake Cuculu in the north of the Central Continent): a large bear with a
white coat and a single black stripe running down its middle, living in sizeable packs; it sees poorly in the dark and
is sluggish at night. Drawn after the anime (S2 ep. 1, the pack on the snowy slope) and the manga: white fur shaded
blue-grey, the black stripe from the snout over the brow and down the spine, red eyes, a dark nose.

Built on the four-legged rig of canine.py (heavier proportions), with its own attacks: a mauling bite, rearing up to
bring both forepaws down, and a standing roar.
"""
import numpy as np

import canine
from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, fur, mix, rgba, scale, top_light

NAME = 'luster_grizzly'

SHAPE = canine.CanineShape(hip_y=17.0, chest_drop=7.0, withers=26.0, half_width=7.0, body_front=-14.0, body_back=11.0,
                           leg_width=5.5, paw_length=6.0, neck_length=5.0, neck_height=10.0, neck_tilt=8.0, head_lift=0.05,
                           rump_height=15.0,
                           head=(9.5, 8.0, 8.0), muzzle=(5.6, 4.4, 5.0), ear=(3.0, 2.4), ear_spread=3.8,
                           tail=(3.0, 3.0, 1.5), tail_lift=15.0, spikes=0, fangs=1.6, claw=1.8)

WHITE = (234, 236, 242)
SHADE = (176, 186, 206)
STRIPE = (30, 30, 36)
NOSE = (52, 52, 60)
CLAW = (60, 58, 62)
MOUTH = (110, 34, 44)
EYE = (210, 40, 44)


def painter(model):
    meta = model.meta
    height = meta['height']

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'tooth':
            return rgba((236, 230, 214))
        if tag == 'claw':
            return rgba(CLAW)
        if tag == 'nose':
            return rgba(scale(NOSE, 0.8 if face.name == 'north' else 1.0))
        for eye in meta['eyes']:
            if tag == 'skull' and np.linalg.norm(p - np.array(eye)) < 0.8:
                return rgba(EYE)
        if (tag == 'jaw' and face.name == 'up') or (tag == 'muzzle' and face.name == 'down'):
            return rgba(MOUTH)
        if tag == 'ear' and face.name == 'north' and 0.25 < uv[0] < 0.75 and uv[1] > 0.3:
            return rgba((150, 130, 140))
        strand = fur(p, density=0.9, seed=17)
        light = top_light(n, p[1], height)
        coat = mix(SHADE, WHITE, 0.2 + 0.8 * light)
        coat = scale(coat, 0.9 + 0.14 * strand)
        if fbm(np.array(p) * 0.3, 2, 8) < 0.3:
            coat = scale(coat, 0.93)
        # the single black stripe down the middle: snout ridge, brow, neck and spine
        width = 1.3 + 0.9 * fbm(np.array(p) * 0.6, 2, 4)
        on_top = n[1] > 0.25 or (tag in ('muzzle', 'skull', 'brow') and face.name in ('north', 'up'))
        if on_top and abs(p[0]) < width and tag in ('muzzle', 'skull', 'brow', 'neck', 'body', 'tail') \
                and not (tag == 'muzzle' and face.name == 'north' and p[1] < meta['mouth_y'] + 2.6):
            coat = scale(STRIPE, 0.9 + 0.3 * strand)
        if tag == 'muzzle' and face.name == 'north' and p[1] < meta['mouth_y'] + 1.2:
            coat = mix(coat, NOSE, 0.6)
        return rgba(coat)

    return paint


# ---------------------------------------------------------------------------------------------------- bear moves

def maul(length=0.9, lunge=10.0):
    """A heavy lunge and a crushing bite. Hit window ~0.36-0.52 s."""
    def pose(t):
        crouch = keys(t, [(0, 0), (0.3, 1), (0.38, 0), (length, 0)])
        reach = keys(t, [(0, 0), (0.3, 0), (0.45, 1), (0.6, 1), (length, 0)])
        jaw = keys(t, [(0, 0), (0.3, 0.6), (0.4, 1), (0.48, 0), (length, 0)])
        return {
            'Root': ((-4 * crouch + 3 * reach, 0, 0), (0, -1.5 * crouch, keys(t, [(0, 0), (0.32, 0), (0.48, -lunge), (length, -lunge)], ease_out))),
            'Chest': ((-6 * crouch + 5 * reach, 0, 0), (0, 0, 0)),
            'Neck': ((-14 * crouch, 0, 0), (0, 0, 0)),
            'Head': ((-4 * crouch + 6 * reach, 0, 10 * reach * wave(t, 0.2)), (0, 0, 0)),
            'Jaw': ((-40 * jaw, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((25 * reach, 0, 0), (0, 0, 0)), 'Thigh_FR': ((20 * reach, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((15 * crouch - 15 * reach, 0, 0), (0, 0, 0)), 'Thigh_BR': ((15 * crouch - 15 * reach, 0, 0), (0, 0, 0)),
        }
    return Clip('maul', length, pose)


def rear_swipe(length=1.5):
    """Rears up on the hind legs, then brings both forepaws crashing down. Hit window ~0.68-0.88 s."""
    def pose(t):
        up = keys(t, [(0, 0), (0.55, 1), (0.7, 0.15), (length, 0)])
        down = keys(t, [(0, 0), (0.55, 0), (0.75, 1), (1.05, 1), (length, 0)], ease_in)
        return {
            'Root': ((62 * up, 0, 0), (0, 2 * up, keys(t, [(0, 0), (0.55, 0), (0.78, -8), (length, -8)]))),
            'Chest': ((-6 * up + 10 * down, 0, 0), (0, 0, 0)),
            'Neck': ((-20 * up - 10 * down, 0, 0), (0, 0, 0)),
            'Head': ((-15 * up, 0, 0), (0, 0, 0)),
            'Jaw': ((-30 * up, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((80 * up - 20 * down, 0, -15 * up), (0, 0, 0)),
            'Thigh_FR': ((80 * up - 20 * down, 0, 15 * up), (0, 0, 0)),
            'Shin_FL': ((-40 * up, 0, 0), (0, 0, 0)), 'Shin_FR': ((-40 * up, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((-55 * up, 0, 0), (0, 0, 0)), 'Thigh_BR': ((-55 * up, 0, 0), (0, 0, 0)),
            'Shin_BL': ((30 * up, 0, 0), (0, 0, 0)), 'Shin_BR': ((30 * up, 0, 0), (0, 0, 0)),
        }
    return Clip('swipe', length, pose)


def roar(length=2.2):
    """Stands tall and roars: the call of the pack."""
    def pose(t):
        up = keys(t, [(0, 0), (0.5, 1), (1.8, 1), (length, 0)])
        call = keys(t, [(0, 0), (0.55, 0), (0.7, 1), (1.6, 1), (1.8, 0), (length, 0)])
        q = 1.5 * wave(t, 0.1) * call
        return {
            'Root': ((70 * up, 0, 0), (0, 2 * up, 0)),
            'Chest': ((-8 * up, 0, 0), (0, 0, 0)),
            'Neck': ((-30 * up + 10 * call, 0, 0), (0, 0, 0)),
            'Head': ((-20 * up + 15 * call + q, 0, 0), (0, 0, 0)),
            'Jaw': ((-42 * call, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((70 * up, 0, -30 * up), (0, 0, 0)), 'Thigh_FR': ((70 * up, 0, 30 * up), (0, 0, 0)),
            'Shin_FL': ((-50 * up, 0, 0), (0, 0, 0)), 'Shin_FR': ((-50 * up, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((-62 * up, 0, 0), (0, 0, 0)), 'Thigh_BR': ((-62 * up, 0, 0), (0, 0, 0)),
            'Shin_BL': ((35 * up, 0, 0), (0, 0, 0)), 'Shin_BR': ((35 * up, 0, 0), (0, 0, 0)),
        }
    return Clip('roar', length, pose)


def clips():
    return [canine.idle(3.0), canine.walk(1.2), canine.run(0.62), maul(), rear_swipe(), roar(), canine.hit(0.45),
            canine.death(1.6, rest_height=SHAPE.half_width, hip_y=SHAPE.hip_y)]


def build():
    model = canine.build(NAME, SHAPE)
    model.pack()
    return model, painter(model), clips()
