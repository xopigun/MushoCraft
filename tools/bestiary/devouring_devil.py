"""
Devouring Devil (wiki, page Monsters: Teleportation Labyrinth, floors 5-6, rank A): a monster with a large gaping mouth
and razor-sharp fangs, long limbs and pointed claws that let it scale walls and ceilings to get into the middle of a
party; quick and hard-hitting, but with low defences. The smoke of burnt Talfro roots drives it down to the floor.
Drawn after the anime (S2 ep. 21): a crawling body of very dark blue, a violet back dotted with round pores, a long
violet head ringed like a larva, ending in a round mouth between two great curved white tusks, long thin limbs with
four-fingered hands tipped by white claws, a thin tail with a fin.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'devouring_devil'
HEIGHT = 16.0

DARK = (34, 34, 58)
SHINE = (96, 100, 150)
VIOLET = (118, 84, 140)
VIOLET_DARK = (78, 54, 98)
PORE = (52, 36, 66)
CLAW = (232, 232, 236)
MOUTH = (30, 16, 30)


def _segment(m, bone, a, b, width, tag, height=None):
    a, b = np.array(a, float), np.array(b, float)
    d = b - a
    length = float(np.linalg.norm(d))
    d /= length
    pitch = math.degrees(math.asin(max(-1.0, min(1.0, d[1]))))
    yaw = math.degrees(math.atan2(-d[0], -d[2]))
    c = (a + b) / 2
    h = width if height is None else height
    m.cube(bone, (c[0] - width / 2, c[1] - h / 2, c[2] - length / 2), (width, h, length), tag,
           tilt=(pitch, yaw, 0), tilt_pivot=tuple(c))


def build_model():
    m = Model(NAME, density=2)
    root = m.bone('Root', None, (0, 9, 5))
    chest = m.bone('Chest', root, (0, 10, -2))
    head = m.bone('Head', chest, (0, 11, -10))
    m.cube(root, (-4, 6.5, -2), (8, 6, 10), 'body')
    m.cube(chest, (-4.5, 7, -11), (9, 6.5, 10), 'body')
    # the head: a ringed tube, narrowing towards the round mouth
    for i, (w, z0) in enumerate(((5.6, -14), (5.0, -18), (4.4, -22))):
        m.cube(head, (-w / 2, 11 - w / 2, z0), (w, w, 4), 'ring')
    m.cube(head, (-2.2, 8.8, -24), (4.4, 4.4, 2), 'mouth')
    for s in (-1, 1):
        a = np.array([s * 2.2, 10.0, -21.0])
        b = a + np.array([s * 2.2, -1.8, -2.5])
        c = b + np.array([s * 0.4, -3.2, -1.2])
        _segment(m, head, a, b, 1.2, 'claw')
        _segment(m, head, b, c, 0.9, 'claw')
    for side, s in (('L', -1), ('R', 1)):
        shoulder = np.array([s * 4.0, 11.0, -7.0])
        elbow = shoulder + np.array([s * 6.0, 4.5, -2.5])
        wrist = elbow + np.array([s * 1.0, -14.3, -3.0])
        arm = m.bone('Arm_' + side, chest, tuple(shoulder))
        fore = m.bone('Forearm_' + side, arm, tuple(elbow))
        _segment(m, arm, shoulder, elbow, 2.6, 'limb')
        _segment(m, fore, elbow, wrist, 2.2, 'limb')
        m.cube(fore, (wrist[0] - 2.4, 0, wrist[2] - 1.6), (4.8, 1.3, 3.0), 'limb')
        for k in range(4):
            x = wrist[0] + (k - 1.5) * 1.1
            m.cube(fore, (x - 0.45, 0, wrist[2] - 4.5), (0.9, 0.9, 4.5), 'limb')
            m.cube(fore, (x - 0.3, 0, wrist[2] - 5.8), (0.6, 0.7, 1.3), 'claw')
        hip = np.array([s * 3.5, 9.0, 6.0])
        knee = hip + np.array([s * 5.5, 5.5, 2.0])
        ankle = knee + np.array([s * 0.5, -13.5, 3.0])
        thigh = m.bone('Thigh_' + side, root, tuple(hip))
        shin = m.bone('Shin_' + side, thigh, tuple(knee))
        _segment(m, thigh, hip, knee, 3.0, 'limb')
        _segment(m, shin, knee, ankle, 2.4, 'limb')
        m.cube(shin, (ankle[0] - 1.8, 0, ankle[2] - 4.5), (3.6, 1.0, 5.0), 'limb')
        m.cube(shin, (ankle[0] - 1.5, 0, ankle[2] - 5.6), (3.0, 0.7, 1.1), 'claw')
    tail = m.bone('Tail', root, (0, 9, 8))
    tip = m.bone('TailTip', tail, (0, 8.5, 19))
    _segment(m, tail, (0, 9, 8), (0, 8.5, 19), 2.0, 'limb')
    _segment(m, tip, (0, 8.5, 19), (0, 8.0, 30), 1.2, 'limb')
    m.cube(tip, (-0.2, 6.5, 22), (0.4, 3.2, 9), 'fin')
    return m


def painter(model):
    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'claw':
            return rgba(scale(CLAW, 0.85 + 0.15 * max(0.0, n[1])))
        if tag == 'mouth':
            if face.name == 'north':
                r = math.hypot(p[0], p[1] - 11.0)
                return rgba(MOUTH if r < 1.5 else VIOLET_DARK if r < 1.9 else VIOLET)
            return rgba(VIOLET_DARK)
        if tag == 'ring':
            # ringed like a larva: darker grooves across the length
            groove = (p[2] % 4.0) < 0.6
            c = mix(VIOLET_DARK, VIOLET, 0.4 + 0.6 * max(0.0, n[1]))
            return rgba(scale(c, 0.7) if groove else c)
        if tag == 'fin':
            return rgba(mix(DARK, VIOLET_DARK, 0.5))
        light = 0.25 + 0.45 * max(0.0, n[1]) + 0.15 * fbm(np.array(p) * 0.3, 2, 101)
        c = mix(DARK, SHINE, light * 0.6)
        if tag == 'body' and face.name == 'up' and abs(p[0]) < 3.0:
            c = mix(VIOLET_DARK, VIOLET, 0.5 + 0.5 * fbm(np.array(p) * 0.2, 2, 103))
            # round pores
            cx, cz = round(p[0] / 2.4) * 2.4, round(p[2] / 2.6) * 2.6
            if math.hypot(p[0] - cx, p[2] - cz) < 0.6 and fbm(np.array([cx, 0.0, cz]), 1, 107) > 0.4:
                c = PORE
        return rgba(c)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _crawl(t, period, swing, lift, sway):
    a = wave(t, period)
    up_l = max(0.0, wave(t, period, 0.25))
    up_r = max(0.0, wave(t, period, 0.75))
    return {
        'Arm_L': ((0, -swing * a, -lift * up_l), (0, 0, 0)), 'Thigh_R': ((0, swing * a, lift * up_l), (0, 0, 0)),
        'Arm_R': ((0, -swing * a, lift * up_r), (0, 0, 0)), 'Thigh_L': ((0, swing * a, -lift * up_r), (0, 0, 0)),
        'Chest': ((0, sway * a, 0), (0, 0, 0)), 'Root': ((0, -sway * 0.5 * a, 0), (0, 0.4 * abs(wave(t, period / 2)), 0)),
        'Head': ((0, -sway * 0.7 * a, 0), (0, 0, 0)),
        'Tail': ((0, sway * wave(t, period, 0.15), 0), (0, 0, 0)), 'TailTip': ((0, sway * 1.2 * wave(t, period, 0.3), 0), (0, 0, 0)),
    }


def idle(length=2.0):
    def pose(t):
        p = _crawl(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((0, 0, 0), (0, 0.4 * wave(t, length), 0)), 'Head': ((4 * wave(t, length, 0.2), 10 * wave(t, length * 2), 0), (0, 0, 0)),
                  'Tail': ((0, 12 * wave(t, length), 0), (0, 0, 0)), 'TailTip': ((0, 18 * wave(t, length, 0.25), 0), (0, 0, 0))})
        return p
    return Clip('idle', length, pose, loop=True)


def walk(length=0.8, name='walk', swing=24.0, lift=24.0, sway=9.0):
    def pose(t):
        return _crawl(t, length, swing, lift, sway)
    return Clip(name, length, pose, loop=True)


def claw(length=0.7):
    """Rises on its hind legs and rakes down with the right hand. Hit window ~0.25-0.4 s."""
    def pose(t):
        up = keys(t, [(0, 0), (0.22, 1), (0.4, 0.4), (length, 0)])
        rake = keys(t, [(0, 0), (0.22, 0), (0.36, 1), (0.5, 1), (length, 0)], ease_out)
        p = _crawl(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((18 * up, 0, 0), (0, 1.5 * up, keys(t, [(0, 0), (0.24, 0), (0.38, -6), (length, -6)]))),
                  'Chest': ((12 * up - 20 * rake, -15 * up + 20 * rake, 0), (0, 0, 0)),
                  'Arm_R': ((70 * up - 40 * rake, 30 * up - 50 * rake, 50 * up - 30 * rake), (0, 0, 0)),
                  'Forearm_R': ((40 * up - 30 * rake, 0, 0), (0, 0, 0)), 'Head': ((-15 * up + 10 * rake, 0, 0), (0, 0, 0))})
        return p
    return Clip('claw', length, pose)


def pounce(length=1.0, leap=36.0):
    """Crouches, springs at its prey from afar and lands on it, claws first. Hit window ~0.35-0.6 s."""
    def pose(t):
        crouch = keys(t, [(0, 0), (0.25, 1), (0.32, 0), (length, 0)])
        air = keys(t, [(0, 0), (0.3, 0), (0.45, 1), (0.6, 0), (length, 0)])
        reach = keys(t, [(0, 0), (0.3, 0), (0.42, 1), (0.7, 1), (length, 0)])
        p = _crawl(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((-10 * crouch + 12 * air, 0, 0),
                           (0, -2.5 * crouch + 5 * air, keys(t, [(0, 0), (0.3, 0), (0.6, -leap), (length, -leap)], ease_out))),
                  'Arm_L': ((-40 * reach, 0, 0), (0, 0, 0)), 'Arm_R': ((-40 * reach, 0, 0), (0, 0, 0)),
                  'Forearm_L': ((95 * reach, 0, 0), (0, 0, 0)), 'Forearm_R': ((95 * reach, 0, 0), (0, 0, 0)),
                  'Thigh_L': ((-50 * air, 0, 0), (0, 0, 0)), 'Thigh_R': ((-50 * air, 0, 0), (0, 0, 0)),
                  'Head': ((-12 * reach, 0, 0), (0, 0, 0)), 'Tail': ((20 * air, 0, 0), (0, 0, 0))})
        return p
    return Clip('pounce', length, pose)


def bite(length=0.8):
    """The long head draws back and stabs forward, tusks first. Hit window ~0.3-0.45 s."""
    def pose(t):
        back = keys(t, [(0, 0), (0.25, 1), (0.32, 0), (length, 0)])
        stab = keys(t, [(0, 0), (0.28, 0), (0.38, 1), (0.55, 1), (length, 0)], ease_out)
        p = _crawl(0.0, 1.0, 0, 0, 0)
        p.update({'Chest': ((10 * back - 8 * stab, 0, 0), (0, 0, 2 * back - 3 * stab)),
                  'Head': ((25 * back - 12 * stab, 0, 0), (0, 0, -3 * stab)),
                  'Root': ((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.28, 0), (0.4, -5), (length, -5)])))})
        return p
    return Clip('bite', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.07, 1), (length, 0)], ease_out)
        p = _crawl(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((0, 0, 0), (0, 0, 2.5 * k)), 'Chest': ((-10 * k, 10 * k, 0), (0, 0, 0)), 'Head': ((-20 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('hit', length, pose)


def death(length=1.5):
    """Collapses flat, limbs splayed, the head dropping last."""
    def pose(t):
        k = keys(t, [(0, 0), (1.0, 1), (length, 1)], ease_in)
        return {'Root': ((0, 0, 20 * k), (0, -6 * k, 0)), 'Chest': ((5 * k, 0, 0), (0, 0, 0)),
                'Arm_L': ((0, 0, -50 * k), (0, 0, 0)), 'Arm_R': ((0, 0, 50 * k), (0, 0, 0)),
                'Thigh_L': ((0, 0, -45 * k), (0, 0, 0)), 'Thigh_R': ((0, 0, 45 * k), (0, 0, 0)),
                'Head': ((20 * k, 15 * k, 0), (0, 0, 0)), 'Tail': ((0, 20 * k, 0), (0, 0, 0))}
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.5, 'run', 30.0, 30.0, 12.0), claw(), pounce(), bite(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
