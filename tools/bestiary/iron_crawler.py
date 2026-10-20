"""
Iron Crawler (wiki, page Monsters: Teleportation Labyrinth, floors 2-3, rank B): a caterpillar monster with a tough
carapace and great weight, like a heavy tank, the front line of an attack; about one metre tall and two long, and
rather fast for its looks (LN vol. 12 ch. 2-3). Drawn after the anime (S2 ep. 20): a dull green body of overlapping,
ridged plates, a broad front plate split by an X of cracks, small red eyes on its sides, a round snout below, many
short dark legs underneath.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'iron_crawler'
HEIGHT = 16.0

GREEN = (98, 128, 106)
GREEN_DARK = (58, 80, 66)
SHINE = (168, 196, 172)
CRACK = (34, 44, 38)
UNDER = (40, 44, 50)
EYE = (210, 40, 46)

# segment bone, parent, pivot z, box (x0, y0, z0, w, h, d)
SEGMENTS = [
    ('Seg_F', 'Root', -6.0, (-10.0, 2.0, -12.0, 20.0, 13.0, 7.0)),
    ('Head', 'Seg_F', -12.0, (-9.0, 2.0, -18.0, 18.0, 11.0, 7.0)),
    ('Seg_B1', 'Root', 1.0, (-10.0, 2.0, 1.0, 20.0, 13.5, 6.5)),
    ('Seg_B2', 'Seg_B1', 7.0, (-9.0, 2.0, 7.0, 18.0, 12.0, 6.0)),
    ('Tail', 'Seg_B2', 12.5, (-7.5, 2.0, 12.5, 15.0, 10.0, 5.0)),
]
# which segment carries each pair of legs, and where along it
LEGS = [('1', 'Seg_F', -9.0), ('2', 'Root', -2.5), ('3', 'Seg_B1', 4.0), ('4', 'Seg_B2', 10.0), ('5', 'Tail', 15.0)]


def _plate(m, bone, box, tag):
    """One arched plate: a broad base and a narrower dome on top, so the back is rounded, not flat."""
    x0, y0, z0, w, h, d = box
    m.cube(bone, (x0, y0, z0), (w, h - 3.0, d), tag)
    m.cube(bone, (x0 + 3.0, y0 + h - 3.0, z0 + 0.3), (w - 6.0, 3.0, d - 0.6), 'dome')


def build_model():
    m = Model(NAME, density=2)
    bones = {'Root': m.bone('Root', None, (0, 8, 0))}
    _plate(m, bones['Root'], (-10.5, 2.0, -6.0, 21.0, 14.0, 7.0), 'plate')
    for name, parent, z, box in SEGMENTS:
        bones[name] = m.bone(name, bones[parent], (0, 8, z))
        _plate(m, bones[name], box, 'head' if name == 'Head' else 'plate')
    m.cube(bones['Head'], (-2.2, 2.4, -19.6), (4.4, 3.2, 1.8), 'snout')
    for name, segment, z in LEGS:
        for side, s in (('L', -1), ('R', 1)):
            leg = m.bone('Leg_%s%s' % (side, name), bones[segment], (s * 8.0, 3.5, z))
            m.cube(leg, (s * 8.0 - 1.2, 0, z - 1.0), (2.4, 3.6, 2.0), 'leg')
    return m


def painter(model):
    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'leg' or face.name == 'down':
            return rgba(scale(UNDER, 0.9 + 0.2 * uv[1]))
        if tag == 'snout':
            return rgba(mix(GREEN, SHINE, 0.45))
        light = 0.35 + 0.45 * max(0.0, n[1]) + 0.2 * fbm(np.array(p) * 0.25, 2, 121)
        c = mix(GREEN_DARK, GREEN, light)
        if tag == 'head' and face.name == 'north':
            # the X of cracks across the front plate, eyes at its sides
            for ex in (-7.2, 7.2):
                if math.hypot(p[0] - ex, p[1] - 7.0) < 0.75:
                    return rgba(EYE)
            dx, dy = abs(p[0]), p[1] - 8.5
            if abs(dy - dx * 0.45) < 0.35 and dx < 5.0 or abs(p[0]) < 0.3 and 3.5 < p[1] < 8.5:
                return rgba(CRACK)
            return rgba(c)
        if face.name in ('up', 'east', 'west'):
            # the ridges across each plate, and a pale glint on their crests
            groove = (p[2] + 30.0) % 3.2
            if groove < 0.45:
                return rgba(CRACK)
            if groove < 1.0 and face.name == 'up':
                return rgba(mix(c, SHINE, 0.45))
        if face.name == 'south':
            return rgba(scale(c, 0.85))
        return rgba(c)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _legs(t, period, swing):
    """The legs ripple from the head to the tail, a wave running down each side."""
    pose = {}
    for i, (name, _, _) in enumerate(LEGS):
        for side, phase in (('L', 0.0), ('R', 0.5)):
            pose['Leg_%s%s' % (side, name)] = ((swing * wave(t, period, phase + 0.12 * i), 0, 0), (0, 0, 0))
    return pose


def _body(t, period, sway, bob):
    pose = {'Root': ((0, 0, 0), (0, bob * abs(wave(t, period / 2)), 0))}
    for i, (name, *_) in enumerate(SEGMENTS):
        pose[name] = ((0, sway * wave(t, period, 0.15 * i), 0), (0, 0, 0))
    return pose


def idle(length=2.4):
    def pose(t):
        p = _legs(t, length, 6.0)
        p.update(_body(t, length, 1.5, 0.2))
        p['Head'] = ((3 * wave(t, length), 4 * wave(t, length * 2), 0), (0, 0, 0))
        return p
    return Clip('idle', length, pose, loop=True)


def walk(length=0.6, name='walk', swing=35.0, sway=4.0):
    def pose(t):
        p = _legs(t, length, swing)
        p.update(_body(t, length, sway, 0.4))
        return p
    return Clip(name, length, pose, loop=True)


def ram(length=1.3, rush=40.0):
    """Rears its front, then rushes forward with all its weight and runs its prey down. Hit window ~0.4-0.8 s."""
    def pose(t):
        rear = keys(t, [(0, 0), (0.3, 1), (0.42, 0), (length, 0)])
        drive = keys(t, [(0, 0), (0.38, 0), (0.5, 1), (0.85, 1), (length, 0)])
        p = _legs(t, 0.3, 40.0 * drive)
        p.update({'Root': ((6 * rear - 4 * drive, 0, 0),
                           (0, 1.5 * rear, keys(t, [(0, 0), (0.3, 2), (0.85, -rush), (length, -rush)], ease_out))),
                  'Seg_F': ((8 * rear - 4 * drive, 0, 0), (0, 0, 0)), 'Head': ((10 * rear - 12 * drive, 0, 0), (0, 0, 0))})
        return p
    return Clip('ram', length, pose)


def bite(length=0.8):
    """The front plate dips and snaps at the prey's legs. Hit window ~0.25-0.4 s."""
    def pose(t):
        lift = keys(t, [(0, 0), (0.2, 1), (0.3, 0), (length, 0)])
        snap = keys(t, [(0, 0), (0.24, 0), (0.34, 1), (0.5, 1), (length, 0)], ease_out)
        p = _legs(0.0, 1.0, 0.0)
        p.update({'Root': ((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.24, 0), (0.36, -5), (length, -5)]))),
                  'Seg_F': ((6 * lift - 6 * snap, 0, 0), (0, 0, 0)), 'Head': ((14 * lift - 20 * snap, 0, 0), (0, 0, 0))})
        return p
    return Clip('bite', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        p = _legs(0.0, 1.0, 0.0)
        p.update({'Root': ((0, 0, 0), (0, 0, 1.5 * k)), 'Seg_F': ((-5 * k, 0, 0), (0, 0, 0)), 'Head': ((-8 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('hit', length, pose)


def death(length=1.8):
    """Rolls over onto its back, its legs twitching then still."""
    def pose(t):
        k = keys(t, [(0, 0), (1.0, 1), (length, 1)], ease_in)
        twitch = max(0.0, 1.0 - t / length) * 30.0
        p = _legs(t, 0.25, twitch * k)
        p.update({'Root': ((0, 0, 175 * k), (0, 0, 0)), 'Head': ((-15 * k, 0, 0), (0, 0, 0)), 'Tail': ((-10 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.35, 'run', 45.0, 6.0), ram(), bite(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
