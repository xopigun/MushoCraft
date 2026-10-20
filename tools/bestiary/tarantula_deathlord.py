"""
Tarantula Deathlord (wiki, page Monsters: Teleportation Labyrinth): a huge poisonous spider that spits webs accurately
enough to disarm its target; its poison yields to elementary detoxification magic. Drawn after the anime (S2 ep. 20):
a glossy black body with blue highlights, a round abdomen banded in red with two red horns at its back, a cluster of
shining black eyes in red patches, black legs with blue joints and red thorns, heavy fangs.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'tarantula_deathlord'
HEIGHT = 24.0

BLACK = (30, 32, 42)
SHINE = (100, 124, 160)
RED = (176, 44, 40)
RED_DARK = (110, 24, 26)
JOINT = (70, 96, 130)
FANG = (60, 54, 62)

# name, hip z, splay (degrees from straight out: + forwards)
LEGS = [('1', -7.0, 42.0), ('2', -4.0, 14.0), ('3', -1.0, -14.0), ('4', 2.0, -40.0)]


def _segment(m, bone, a, b, width, tag):
    a, b = np.array(a, float), np.array(b, float)
    d = b - a
    length = float(np.linalg.norm(d))
    d /= length
    pitch = math.degrees(math.asin(max(-1.0, min(1.0, d[1]))))
    yaw = math.degrees(math.atan2(-d[0], -d[2]))
    c = (a + b) / 2
    m.cube(bone, (c[0] - width / 2, c[1] - width / 2, c[2] - length / 2), (width, width, length), tag,
           tilt=(pitch, yaw, 0), tilt_pivot=tuple(c))


def build_model():
    m = Model(NAME, density=2)
    root = m.bone('Root', None, (0, 10, 0))
    head = m.bone('Head', root, (0, 10, -9))
    fangs = m.bone('Fangs', head, (0, 8, -14))
    abdomen = m.bone('Abdomen', root, (0, 12, 1))
    m.cube(root, (-6, 6, -9), (12, 8, 10), 'body')
    m.cube(head, (-4.5, 6.5, -14), (9, 6.5, 5), 'head')
    for x in (-1.8, 1.8):
        m.cube(fangs, (x - 1.3, 3, -15.5), (2.6, 5, 2.5), 'fang')
    m.cube(abdomen, (-9, 6, 1), (18, 15, 20), 'abdomen')
    m.cube(abdomen, (-8, 5, 2), (16, 17, 18), 'abdomen')
    m.cube(abdomen, (-7.5, 7, 0), (15, 13, 22), 'abdomen')
    for x in (-4.5, 4.5):
        _segment(m, abdomen, (x, 19, 17), (x * 1.15, 25, 21), 2.0, 'horn')
        _segment(m, abdomen, (x * 1.15, 25, 21), (x * 1.05, 27.5, 18.5), 1.3, 'horn')
    for name, z, splay in LEGS:
        for side, s in (('L', -1), ('R', 1)):
            out = np.array([s * math.cos(math.radians(splay)), 0.0, -math.sin(math.radians(splay))])
            hip = np.array([s * 5.5, 10.0, z])
            knee = hip + out * 9.0 + np.array([0, 9.0, 0])
            foot = hip + out * 19.0 + np.array([0, -9.7, 0])
            leg = m.bone('Leg_%s%s' % (side, name), root, tuple(hip))
            lower = m.bone('Foot_%s%s' % (side, name), leg, tuple(knee))
            _segment(m, leg, hip, knee, 2.4, 'leg')
            _segment(m, lower, knee, foot, 2.0, 'leg')
            mid = (hip + knee) / 2 + np.array([0, 1.2, 0])
            _segment(m, leg, mid, mid + out * 0.6 + np.array([0, 2.2, 0]), 0.8, 'thorn')
            kn = knee + np.array([0, 0.8, 0])
            _segment(m, lower, kn, kn + out * 1.4 + np.array([0, 2.0, 0]), 0.9, 'thorn')
    m.meta = {'eyes': [(-2.2, 11.4, 1.0), (2.2, 11.4, 1.0), (-1.0, 12.2, 0.8), (1.0, 12.2, 0.8),
                       (-3.2, 10.2, 0.7), (3.2, 10.2, 0.7), (-1.2, 9.6, 0.6), (1.2, 9.6, 0.6)]}
    return m


def painter(model):
    eyes = model.meta['eyes']

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag in ('horn', 'thorn'):
            return rgba(mix(RED_DARK, RED, 0.5 + 0.5 * max(0.0, n[1])))
        if tag == 'fang':
            return rgba(scale(FANG, 0.9 + 0.2 * uv[1]))
        gloss = max(0.0, n[1]) * 0.6 + max(0.0, -n[2]) * 0.2
        c = mix(BLACK, SHINE, 0.15 + 0.5 * gloss ** 2)
        if tag == 'head' and face.name == 'north':
            for (ex, ey, r) in eyes:
                d = math.hypot(p[0] - ex, p[1] - ey)
                if d < r:
                    return rgba((240, 240, 250) if d < r * 0.3 and p[0] < ex else (14, 14, 20))
            if math.hypot(p[0], p[1] - 10.8) < 3.8:
                c = mix(c, RED, 0.85)  # the red patch round the eyes
        if tag == 'abdomen':
            band = (face.name == 'up' and 4.0 < abs(p[0]) < 7.5) or                    (face.name in ('east', 'west') and 11.5 < p[1] < 17.5) or                    (face.name == 'south' and 4.0 < abs(p[0]) < 7.5 and p[1] > 9)
            if band:
                c = mix(RED_DARK, RED, 0.4 + 0.6 * gloss)
            if fbm(np.array(p) * 0.4, 2, 81) > 0.72:
                c = mix(c, SHINE, 0.3)
        if tag == 'leg':
            ring = (p[1] * 0.9) % 4.5
            if ring < 0.8:
                c = JOINT
        return rgba(c)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _legs(t, period, swing, lift):
    pose = {}
    for name, *_ in LEGS:
        for side, s in (('L', -1), ('R', 1)):
            group = (int(name) + (0 if side == 'L' else 1)) % 2
            phase = 0.5 * group
            a = wave(t, period, phase)
            up = max(0.0, wave(t, period, phase + 0.25))
            pose['Leg_%s%s' % (side, name)] = ((0, swing * a, s * -lift * up), (0, 0, 0))
            pose['Foot_%s%s' % (side, name)] = ((0, 0, s * lift * 0.6 * up), (0, 0, 0))
    return pose


def idle(length=2.4):
    def pose(t):
        b = wave(t, length)
        p = _legs(t, length, 2.0, 2.0)
        p.update({'Root': ((0, 0, 0), (0, 0.3 * b, 0)), 'Abdomen': ((3 * b, 0, 0), (0, 0, 0)),
                  'Fangs': ((4 * max(0.0, wave(t, length / 2)), 0, 0), (0, 0, 0))})
        return p
    return Clip('idle', length, pose, loop=True)


def walk(length=0.7, name='walk', swing=22.0, lift=14.0):
    def pose(t):
        p = _legs(t, length, swing, lift)
        p.update({'Root': ((0, 0, 0), (0, 0.5 * abs(wave(t, length / 2)), 0)), 'Abdomen': ((4 * wave(t, length / 2), 3 * wave(t, length), 0), (0, 0, 0))})
        return p
    return Clip(name, length, pose, loop=True)


def bite(length=0.8, lunge=12.0):
    """Rears its front legs, lunges and drives the fangs in. Hit window ~0.32-0.5 s."""
    def pose(t):
        rear = keys(t, [(0, 0), (0.28, 1), (0.4, 0.3), (length, 0)])
        strike = keys(t, [(0, 0), (0.3, 0), (0.45, 1), (0.6, 1), (length, 0)], ease_out)
        p = _legs(0.0, 1.0, 0.0, 0.0)
        for side, s in (('L', -1), ('R', 1)):
            p['Leg_%s1' % side] = ((50 * rear, s * -25 * rear, s * -30 * rear), (0, 0, 0))
            p['Leg_%s2' % side] = ((30 * rear, 0, s * -15 * rear), (0, 0, 0))
        p.update({'Root': ((14 * rear - 8 * strike, 0, 0), (0, 1.5 * rear, keys(t, [(0, 0), (0.32, 0), (0.48, -lunge), (length, -lunge)], ease_out))),
                  'Head': ((10 * rear - 18 * strike, 0, 0), (0, 0, 0)),
                  'Fangs': ((-35 * rear + 40 * strike, 0, 0), (0, 0, 0)), 'Abdomen': ((-10 * rear, 0, 0), (0, 0, 0))})
        return p
    return Clip('bite', length, pose)


def spit(length=1.0):
    """Head raised, a jerk forwards as the web leaves the fangs at ~0.45 s."""
    def pose(t):
        aim = keys(t, [(0, 0), (0.35, 1), (0.45, 0.6), (0.7, 0.6), (length, 0)])
        jerk = keys(t, [(0, 0), (0.4, 0), (0.46, 1), (0.6, 0), (length, 0)], ease_out)
        p = _legs(0.0, 1.0, 0.0, 0.0)
        p.update({'Root': ((10 * aim - 6 * jerk, 0, 0), (0, 0, -1.5 * jerk)), 'Head': ((12 * aim, 0, 0), (0, 0, 0)),
                  'Fangs': ((-30 * aim, 0, 0), (0, 0, 0)), 'Abdomen': ((-14 * aim + 10 * jerk, 0, 0), (0, 0, 0))})
        return p
    return Clip('spit', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        p = _legs(0.0, 1.0, 0.0, 0.0)
        p.update({'Root': ((-8 * k, 0, 6 * k), (0, 0, 1.5 * k)), 'Abdomen': ((12 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('hit', length, pose)


def death(length=1.5):
    """Collapses and curls its legs in, as dead spiders do."""
    def pose(t):
        k = keys(t, [(0, 0), (1.0, 1), (length, 1)], ease_in)
        p = {}
        for name, *_ in LEGS:
            for side, s in (('L', -1), ('R', 1)):
                p['Leg_%s%s' % (side, name)] = ((0, 0, s * 55 * k), (0, 0, 0))
                p['Foot_%s%s' % (side, name)] = ((0, 0, s * 70 * k), (0, 0, 0))
        p.update({'Root': ((0, 0, 0), (0, -5 * k, 0)), 'Abdomen': ((15 * k, 0, 0), (0, -1 * k, 0)), 'Fangs': ((30 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.45, 'run', 28.0, 18.0), bite(), spit(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
