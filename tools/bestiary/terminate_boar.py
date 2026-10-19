"""
Terminate Boar (wiki, page Monsters: rank D, C or B with its pack; Asura Kingdom): a bipedal, four-armed porcine
monster that attacks with a loyal pack of Assault Dogs. Drawn after the anime (S2 ep. 0, Sylphiette against one): a
towering hunched body of near-black fur, a shaggy grey-brown mane over the back, a pale brown snout with a flat nose,
huge ivory tusks curving out and up, small orange eyes, four clawed arms.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, smooth, wave
from paint import fbm, fur, mix, rgba, scale, top_light
from rig import Model

NAME = 'terminate_boar'
HEIGHT = 50.0

FUR = (52, 40, 36)
FUR_DARK = (30, 23, 22)
MANE = (112, 98, 88)
SNOUT = (150, 112, 92)
NOSE = (120, 84, 72)
TUSK = (232, 222, 196)
TUSK_BASE = (186, 170, 140)
CLAW = (214, 204, 182)
HOOF = (46, 42, 42)
MOUTH = (90, 28, 30)
EYE = (236, 140, 40)

ARMS = ('UL', 'UR', 'LL', 'LR')


def _segment(m, bone, a, b, width, tag):
    """A box from point a to point b (model px), `width` thick: tilted to follow the segment."""
    a, b = np.array(a, float), np.array(b, float)
    d = b - a
    length = float(np.linalg.norm(d))
    d /= length
    pitch = math.degrees(math.asin(d[1]))
    yaw = math.degrees(math.atan2(-d[0], -d[2]))
    c = (a + b) / 2
    m.cube(bone, (c[0] - width / 2, c[1] - width / 2, c[2] - length / 2), (width, width, length), tag,
           tilt=(pitch, yaw, 0), tilt_pivot=tuple(c))


def build_model():
    m = Model(NAME, density=2)
    root = m.bone('Root', None, (0, 22, 2))
    chest = m.bone('Chest', root, (0, 28, 0))
    head = m.bone('Head', chest, (0, 41, -8))
    jaw = m.bone('Jaw', head, (0, 35, -10))
    m.cube(root, (-8, 17, -4), (16, 11, 10), 'body')
    m.cube(chest, (-11, 27, -9), (22, 18, 16), 'body')
    m.cube(chest, (-9, 42, -7), (18, 5, 15), 'mane', inflate=1.2)
    m.cube(chest, (-7, 36, 6), (14, 9, 3), 'mane', inflate=1.0)
    # head: skull, snout, flat nose, jaw, ears, teeth and the great tusks
    m.cube(head, (-6, 35, -18), (12, 11, 12), 'skull')
    m.cube(head, (-6.5, 43, -16), (13, 4, 9), 'mane', inflate=0.8)
    m.cube(head, (-4, 35, -25), (8, 6.5, 7), 'snout')
    m.cube(head, (-4.4, 35.2, -25.8), (8.8, 6.0, 1.0), 'nose')
    m.cube(jaw, (-3.6, 31.5, -23), (7.2, 3.5, 13), 'jaw')
    for side in (-1, 1):
        m.cube(head, (side * 5.5 - 1.5, 45, -11), (3, 5, 1.5), 'ear', tilt=(-10, 0, -25 * side),
               tilt_pivot=(side * 5.5, 45, -10))
        for k in range(2):
            m.cube(jaw, (side * (1.2 + 1.4 * k) - 0.4, 35, -22.5 + k), (0.8, 1.2, 0.8), 'tooth')
        pts = []
        for i in range(5):
            t = i / 4
            pts.append((side * (4.0 + 9.0 * t + 3.0 * t * t), 34.0 - 2.0 * t + 15.0 * t * t, -21.0 - 5.0 * t + 4.0 * t * t))
        for i in range(4):
            _segment(m, head, pts[i], pts[i + 1], 3.8 - 0.65 * i, 'tusk')
    # four arms: a strong upper pair, a lower pair, all clawed
    for name, (sx, sy, size) in {'UL': (-13.5, 43, 1.15), 'UR': (13.5, 43, 1.15), 'LL': (-12.5, 34, 0.95), 'LR': (12.5, 34, 0.95)}.items():
        side = -1 if sx < 0 else 1
        arm = m.bone('Arm_' + name, chest, (sx, sy, -2))
        fore = m.bone('Forearm_' + name, arm, (sx + side * 0.8, sy - 12 * size, -2))
        w = 6.0 * size
        x0 = sx + side * 1.2 - w / 2
        m.cube(arm, (x0, sy - 13 * size, -2 - w / 2 - 0.5), (w, 14 * size, w + 1), 'arm')
        m.cube(fore, (x0 + 0.25, sy - 25 * size, -2 - w / 2), (w - 0.5, 13 * size, w), 'forearm')
        m.cube(fore, (x0 + 0.5, sy - 27.5 * size, -2 - w / 2 - 0.3), (w - 1, 2.6 * size, w + 0.6), 'hand')
        for k in range(3):
            m.cube(fore, (x0 + 0.8 + k * (w - 1.6) / 2.6, sy - 29.5 * size, -2 - w / 2 - 0.6), (0.9, 2.2 * size, 1.0), 'claw')
    # two legs
    for name, x in (('L', -5), ('R', 5)):
        thigh = m.bone('Thigh_' + name, root, (x, 22, 1))
        shin = m.bone('Shin_' + name, thigh, (x, 12, 1))
        m.cube(thigh, (x - 4, 11, -3), (8, 12, 8), 'leg')
        m.cube(shin, (x - 3.5, 3, -2), (7, 9.5, 6), 'leg')
        m.cube(shin, (x - 3.8, 0, -4), (7.6, 3, 8), 'hoof')
    m.meta = {'eyes': [(-3.8, 42.3, -18.0), (3.8, 42.3, -18.0)]}
    return m


def painter(model):
    eyes = [np.array(e) for e in model.meta['eyes']]

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'tusk':
            k = min(1.0, max(0.0, (abs(p[0]) - 4.0) / 12.0))
            return rgba(scale(mix(TUSK_BASE, TUSK, k), 0.9 + 0.12 * (0.5 + 0.5 * n[1])))
        if tag in ('claw', 'tooth'):
            return rgba(CLAW if tag == 'claw' else TUSK)
        if tag == 'hoof':
            return rgba(scale(HOOF, 0.9 + 0.2 * fur(p, density=2.0, seed=4)))
        if tag == 'nose':
            if face.name == 'north':
                # two dark nostrils on the flat pig nose
                for x in (-1.9, 1.9):
                    if (p[0] - x) ** 2 / 1.4 + (p[1] - 37.6) ** 2 / 2.2 < 1.0:
                        return rgba((30, 18, 18))
            return rgba(NOSE)
        if tag == 'skull' and face.name == 'north':
            for e in eyes:
                d = math.hypot(p[0] - e[0], p[1] - e[1])
                if d < 1.25:
                    return rgba((20, 12, 8) if d < 0.45 else EYE)
                if d < 1.7 and p[1] > e[1]:
                    return rgba((18, 14, 14))  # heavy brow
        if (tag == 'jaw' and face.name == 'up') or (tag == 'snout' and face.name == 'down'):
            return rgba(MOUTH)
        strand = fur(p, density=0.9, seed=13)
        if tag == 'mane':
            edge = min(uv[0], 1 - uv[0], uv[1], 1 - uv[1])
            if face.name != 'up' and edge < 0.3 and fur(p * 1.5, density=0.8, seed=21) > 0.2 + edge * 2.4:
                return (0, 0, 0, 0)
            return rgba(scale(mix(MANE, (150, 136, 124), strand), 0.8 + 0.3 * strand))
        light = top_light(n, p[1], HEIGHT)
        if tag in ('snout', 'jaw'):
            base = mix(scale(SNOUT, 0.75), SNOUT, 0.4 + 0.6 * light)
            return rgba(scale(base, 0.9 + 0.15 * strand))
        if tag == 'ear' and face.name == 'north':
            return rgba((70, 44, 40))
        coat = mix(FUR_DARK, FUR, 0.3 + 0.7 * light)
        coat = scale(coat, 0.75 + 0.45 * strand)
        if strand > 0.8:
            coat = mix(coat, (96, 80, 72), 0.4)
        if fbm(np.array(p) * 0.35, 2, 3) < 0.3:
            coat = scale(coat, 0.85)
        return rgba(coat)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _arms(**rot):
    return {'Arm_' + k: (v, (0, 0, 0)) for k, v in rot.items()}


def _fore(**rot):
    return {'Forearm_' + k: (v, (0, 0, 0)) for k, v in rot.items()}


def idle(length=3.0):
    def pose(t):
        b = wave(t, length)
        return {
            'Chest': ((2 + 1.5 * b, 0, 0), (0, 0.3 * b, 0)),
            'Head': ((-3 + 2 * b, 0, 0), (0, 0, 0)),
            'Jaw': ((-4 - 3 * max(0.0, b), 0, 0), (0, 0, 0)),
            **_arms(UL=(18 + 4 * b, 0, -28), UR=(18 + 4 * b, 0, 28), LL=(8 - 3 * b, 0, -22), LR=(8 - 3 * b, 0, 22)),
            **_fore(UL=(-30, 0, 0), UR=(-30, 0, 0), LL=(-35, 0, 0), LR=(-35, 0, 0)),
        }
    return Clip('idle', length, pose, loop=True)


def walk(length=1.3):
    def pose(t):
        a = wave(t, length)
        lift_l = max(0.0, wave(t, length, 0.25))
        lift_r = max(0.0, wave(t, length, 0.75))
        return {
            'Root': ((0, 4 * a, 0), (0, 1.0 * abs(wave(t, length / 2)), 0)),
            'Chest': ((6, -5 * a, 2 * a), (0, 0, 0)),
            'Head': ((-4, 3 * a, 0), (0, 0, 0)),
            'Thigh_L': ((24 * a, 0, 0), (0, 0, 0)), 'Thigh_R': ((-24 * a, 0, 0), (0, 0, 0)),
            'Shin_L': ((-30 * lift_l, 0, 0), (0, 0, 0)), 'Shin_R': ((-30 * lift_r, 0, 0), (0, 0, 0)),
            **_arms(UL=(-18 * a, 0, -8), UR=(18 * a, 0, 8), LL=(14 * a, 0, -12), LR=(-14 * a, 0, 12)),
            **_fore(UL=(-15, 0, 0), UR=(-15, 0, 0), LL=(-22, 0, 0), LR=(-22, 0, 0)),
        }
    return Clip('walk', length, pose, loop=True)


def run(length=0.75):
    def pose(t):
        a = wave(t, length)
        return {
            'Root': ((0, 6 * a, 0), (0, 2.0 * abs(wave(t, length / 2)), 0)),
            'Chest': ((22, -6 * a, 0), (0, 0, 0)),
            'Head': ((-16, 0, 0), (0, 0, 0)),
            'Thigh_L': ((40 * a, 0, 0), (0, 0, 0)), 'Thigh_R': ((-40 * a, 0, 0), (0, 0, 0)),
            'Shin_L': ((-45 * max(0.0, wave(t, length, 0.25)), 0, 0), (0, 0, 0)),
            'Shin_R': ((-45 * max(0.0, wave(t, length, 0.75)), 0, 0), (0, 0, 0)),
            **_arms(UL=(-30 * a - 20, 0, -12), UR=(30 * a - 20, 0, 12), LL=(25 * a, 0, -18), LR=(-25 * a, 0, 18)),
            **_fore(UL=(-35, 0, 0), UR=(-35, 0, 0), LL=(-30, 0, 0), LR=(-30, 0, 0)),
        }
    return Clip('run', length, pose, loop=True)


def swipe(side, length=0.9):
    """A sweeping swipe of the upper and lower claws of one side. Hit window ~0.32-0.5 s."""
    s = 1 if side == 'R' else -1
    other = 'L' if side == 'R' else 'R'
    up, low = 'U' + side, 'L' + side

    def pose(t):
        wind = keys(t, [(0, 0), (0.3, 1), (0.42, 0), (length, 0)])
        strike = keys(t, [(0, 0), (0.3, 0), (0.45, 1), (0.6, 1), (length, 0)], ease_out)
        twist = keys(t, [(0, 0), (0.3, -1), (0.45, 1), (0.65, 1), (length, 0)])
        return {
            'Root': ((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.3, 0), (0.48, -6), (length, -6)]))),
            'Chest': ((8 * strike, 22 * s * twist, 0), (0, 0, 0)),
            'Head': ((-6 * strike, -10 * s * twist, 0), (0, 0, 0)),
            **_arms(**{up: (70 * wind + 40 * strike, 30 * s * wind - 50 * s * strike, s * (60 * wind - 10 * strike)),
                       low: (40 * wind + 60 * strike, 20 * s * wind - 45 * s * strike, s * (40 * wind)),
                       'U' + other: (-10, 0, -8 * s), 'L' + other: (-6, 0, -12 * s)}),
            **_fore(**{up: (-40 * wind, 0, 0), low: (-35 * wind, 0, 0)}),
        }
    return Clip('swipe_' + side.lower(), length, pose)


def smash(length=1.4):
    """Both upper arms raised high and brought down together. Hit window ~0.6-0.78 s."""
    def pose(t):
        raise_ = keys(t, [(0, 0), (0.5, 1), (0.62, 0), (length, 0)])
        down = keys(t, [(0, 0), (0.5, 0), (0.68, 1), (1.05, 1), (length, 0)], ease_in)
        return {
            'Root': ((0, 0, 0), (0, -3 * down, keys(t, [(0, 0), (0.5, 0), (0.7, -8), (length, -8)]))),
            'Chest': ((-14 * raise_ + 30 * down, 0, 0), (0, 0, 0)),
            'Head': ((10 * raise_ - 14 * down, 0, 0), (0, 0, 0)),
            'Jaw': ((-30 * raise_, 0, 0), (0, 0, 0)),
            'Thigh_L': ((-20 * down, 0, 0), (0, 0, 0)), 'Thigh_R': ((10 * down, 0, 0), (0, 0, 0)),
            'Shin_L': ((-30 * down, 0, 0), (0, 0, 0)),
            **_arms(UL=(165 * raise_ + 70 * down, 0, -10 * raise_), UR=(165 * raise_ + 70 * down, 0, 10 * raise_),
                    LL=(30 * raise_ + 50 * down, 0, -25), LR=(30 * raise_ + 50 * down, 0, 25)),
            **_fore(UL=(-50 * raise_, 0, 0), UR=(-50 * raise_, 0, 0), LL=(-40, 0, 0), LR=(-40, 0, 0)),
        }
    return Clip('smash', length, pose)


def gore(length=1.5, lunge=40.0):
    """A charge, head low, then the tusks tossed up. Hit window ~0.45-0.95 s."""
    def pose(t):
        lower = keys(t, [(0, 0), (0.35, 1), (0.95, 1), (1.05, 0), (length, 0)])
        toss = keys(t, [(0, 0), (0.85, 0), (1.0, 1), (length, 0)])
        stride = wave(t, 0.36) * keys(t, [(0, 0), (0.35, 1), (0.95, 1), (1.05, 0), (length, 0)])
        return {
            'Root': ((10 * lower, 0, 0), (0, 0, keys(t, [(0, 0), (0.35, 0), (1.0, -lunge), (length, -lunge)], smooth))),
            'Chest': ((30 * lower - 20 * toss, 0, 0), (0, 0, 0)),
            'Head': ((-25 * lower + 45 * toss, 0, 0), (0, 0, 0)),
            'Jaw': ((-20 * toss, 0, 0), (0, 0, 0)),
            'Thigh_L': ((40 * stride, 0, 0), (0, 0, 0)), 'Thigh_R': ((-40 * stride, 0, 0), (0, 0, 0)),
            **_arms(UL=(-40 * lower, 0, -25 * lower), UR=(-40 * lower, 0, 25 * lower),
                    LL=(-20 * lower, 0, -30 * lower), LR=(-20 * lower, 0, 30 * lower)),
        }
    return Clip('gore', length, pose)


def roar(length=2.2):
    """Arms flung wide, head thrown back: the call that brings the dogs."""
    def pose(t):
        k = keys(t, [(0, 0), (0.45, 1), (1.8, 1), (length, 0)])
        q = 1.5 * wave(t, 0.1) * keys(t, [(0, 0), (0.5, 0), (0.6, 1), (1.7, 1), (1.8, 0), (length, 0)])
        return {
            'Chest': ((-16 * k, 0, 0), (0, 0, 0)),
            'Head': ((30 * k + q, 0, 0), (0, 0, 0)),
            'Jaw': ((-38 * k, 0, 0), (0, 0, 0)),
            **_arms(UL=(8 * k, 0, -110 * k), UR=(8 * k, 0, 110 * k), LL=(4 * k, 0, -70 * k), LR=(4 * k, 0, 70 * k)),
            **_fore(UL=(0, 0, -25 * k), UR=(0, 0, 25 * k), LL=(0, 0, -20 * k), LR=(0, 0, 20 * k)),
        }
    return Clip('roar', length, pose)


def hit(length=0.45):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        return {
            'Root': ((0, 0, 0), (0, 0, 2 * k)),
            'Chest': ((-10 * k, 8 * k, 0), (0, 0, 0)),
            'Head': ((14 * k, -8 * k, 0), (0, 0, 0)),
            'Jaw': ((-25 * k, 0, 0), (0, 0, 0)),
        }
    return Clip('hit', length, pose)


def death(length=1.8):
    """Staggers, then topples backwards with a crash."""
    def pose(t):
        k = keys(t, [(0, 0), (0.4, 0.15), (1.2, 1), (length, 1)], ease_in)
        sag = keys(t, [(0, 0), (0.4, 1), (length, 1)])
        return {
            'Root': ((-85 * k, 0, 0), (0, -18 * k - 2 * sag * (1 - k), 10 * k)),
            'Chest': ((-10 * sag, 0, 0), (0, 0, 0)),
            'Head': ((20 * k, 0, 0), (0, 0, 0)),
            'Jaw': ((-20 * k, 0, 0), (0, 0, 0)),
            'Thigh_L': ((60 * k, 0, 0), (0, 0, 0)), 'Thigh_R': ((45 * k, 0, 0), (0, 0, 0)),
            'Shin_L': ((-30 * k, 0, 0), (0, 0, 0)),
            **_arms(UL=(20 * k, 0, -60 * k), UR=(30 * k, 0, 70 * k), LL=(10 * k, 0, -40 * k), LR=(5 * k, 0, 50 * k)),
        }
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), run(), swipe('R'), swipe('L'), smash(), gore(), roar(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
