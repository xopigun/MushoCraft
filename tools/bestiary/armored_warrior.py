"""
Armored Warrior (wiki, page Monsters: rank A, floors 4-5 of the Teleportation Labyrinth): an animated suit of rusty
armour with four arms, each holding a razor-sharp blade, that counters physical and magical attacks with the Water God
Style. Drawn after the anime (S2 ep. 21, the two warriors at the labyrinth door): full plate, a barrel helm with a narrow
slit, an upper pair of arms raised with open gauntlets, a lower pair holding long blades, a short fauld. Here the
upper hands hold blades too (the wiki: each holds one).
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'armored_warrior'
HEIGHT = 34.0

STEEL = (150, 156, 168)
STEEL_DARK = (78, 82, 94)
RUST = (130, 72, 40)
LEATHER = (82, 58, 40)
BLADE = (214, 222, 232)
SLIT = (12, 14, 18)


def _blade(m, bone, hand, length, side):
    """A long blade held in a fist, carrying on the line of the forearm."""
    x, y, z = hand
    m.cube(bone, (x - 0.4, y - 1.6, z - 0.4), (0.8, 2.0, 0.8), 'grip')
    m.cube(bone, (x - 1.2, y - 2.1, z - 0.5), (2.4, 0.6, 1.0), 'guard')
    m.cube(bone, (x - 0.25, y - 2.1 - length, z - 0.6), (0.5, length, 1.2), 'blade')


def build_model():
    m = Model(NAME, density=2)
    root = m.bone('Root', None, (0, 15, 0))
    chest = m.bone('Chest', root, (0, 17, 0))
    head = m.bone('Head', chest, (0, 27, 0))
    m.cube(root, (-4.5, 12, -2.8), (9, 5, 5.6), 'plate')
    m.cube(root, (-4.8, 9.5, -3.0), (9.6, 4, 6), 'fauld')
    m.cube(chest, (-5.5, 16.5, -3.2), (11, 10.5, 6.4), 'plate')
    m.cube(chest, (-4.0, 18.5, -3.6), (8, 6, 0.6), 'breast')
    m.cube(head, (-3.2, 27, -3.4), (6.4, 7, 6.4), 'helm')
    m.cube(head, (-3.4, 30.2, -3.6), (6.8, 0.8, 0.4), 'slit')
    for name, side in (('L', -1), ('R', 1)):
        m.cube(chest, (side * 5.5 - 2.2, 24.5, -3.4), (4.4, 3.2, 6.8), 'pauldron')
    for name, (sx, sy, size, lower) in {'UL': (-7.0, 25.5, 1.0, False), 'UR': (7.0, 25.5, 1.0, False),
                                         'LL': (-6.5, 21.0, 0.9, True), 'LR': (6.5, 21.0, 0.9, True)}.items():
        side = -1 if sx < 0 else 1
        arm = m.bone('Arm_' + name, chest, (sx, sy, 0))
        fore = m.bone('Forearm_' + name, arm, (sx + side * 0.3, sy - 6.5 * size, 0))
        w = 3.0 * size
        m.cube(arm, (sx - w / 2, sy - 7 * size, -w / 2), (w, 7.5 * size, w), 'plate')
        m.cube(fore, (sx + side * 0.3 - w / 2 - 0.2, sy - 13 * size, -w / 2 - 0.2), (w + 0.4, 6.5 * size, w + 0.4), 'plate')
        m.cube(fore, (sx + side * 0.3 - w / 2 - 0.3, sy - 15 * size, -w / 2 - 0.4), (w + 0.6, 2.2 * size, w + 0.8), 'gauntlet')
        _blade(m, fore, (sx + side * 0.3, sy - 14 * size, -0.2), 13.0 if lower else 10.0, side)
    for name, x in (('L', -2.4), ('R', 2.4)):
        thigh = m.bone('Thigh_' + name, root, (x, 12, 0))
        shin = m.bone('Shin_' + name, thigh, (x, 6.5, 0))
        m.cube(thigh, (x - 1.9, 6.0, -2.0), (3.8, 6.5, 4.0), 'plate')
        m.cube(shin, (x - 1.8, 1.2, -1.9), (3.6, 5.8, 3.8), 'plate')
        m.cube(shin, (x - 2.0, 0, -3.0), (4.0, 1.6, 5.0), 'plate')
    return m


def painter(model):
    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'blade':
            edge = abs(uv[0] - 0.5) * 2 if face.name in ('north', 'south') else 0.0
            return rgba(mix(BLADE, (150, 160, 175), edge * 0.6 + (0.2 if n[1] < 0 else 0.0)))
        if tag in ('grip',):
            return rgba(LEATHER)
        if tag == 'slit':
            return rgba(SLIT if face.name == 'north' else STEEL_DARK)
        if tag == 'fauld':
            if p[1] < 10.2 and fbm(np.array(p) * 1.6, 2, 61) < 0.45:
                return (0, 0, 0, 0)
            return rgba(scale(LEATHER, 0.8 + 0.3 * fbm(np.array(p), 2, 63)))
        # steel plates: lit from above, edges and rivets darker, rust eating at them
        light = 0.45 + 0.4 * max(0.0, n[1]) + 0.15 * (1 - abs(n[0]))
        steel = mix(STEEL_DARK, STEEL, light)
        a, b = uv
        if min(a, 1 - a, b, 1 - b) < 0.08:
            steel = scale(steel, 0.7)
        if tag in ('plate', 'pauldron', 'helm', 'breast') and (int(a * 6) + int(b * 6)) % 7 == 0 and min(a, 1 - a, b, 1 - b) < 0.16:
            steel = mix(steel, (210, 210, 200), 0.5)  # rivets
        rust = fbm(np.array(p) * 0.55, 3, 65)
        if rust > 0.58:
            steel = mix(steel, RUST, min(1.0, (rust - 0.58) * 4))
        if tag == 'helm' and face.name == 'north' and abs(a - 0.5) < 0.06 and b > 0.55:
            steel = scale(steel, 0.75)  # the helm's ridge
        if tag == 'guard':
            steel = mix(steel, (170, 140, 80), 0.5)
        return rgba(steel)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

ARMS = ('UL', 'UR', 'LL', 'LR')


def _p(**bones):
    return {k: (v if isinstance(v[0], tuple) else (v, (0, 0, 0))) for k, v in bones.items()}


def _guard(k=1.0, b=0.0):
    """The warrior's stance: lower blades held forwards, upper blades raised over the head."""
    return dict(Arm_LL=((50 * k + 3 * b, 0, -10 * k)), Arm_LR=((50 * k - 3 * b, 0, 10 * k)),
                Forearm_LL=((25 * k, 0, 0)), Forearm_LR=((25 * k, 0, 0)),
                Arm_UL=((150 * k, 0, 25 * k)), Arm_UR=((150 * k, 0, -25 * k)),
                Forearm_UL=((-30 * k, 0, 0)), Forearm_UR=((-30 * k, 0, 0)))


def idle(length=2.6):
    def pose(t):
        b = wave(t, length)
        return _p(Root=((0, 0, 0), (0, 0.3 * b, 0)), Chest=((3, 4 * wave(t, length * 2), 0)),
                  Thigh_L=((-6, 0, -3)), Thigh_R=((6, 0, 3)), Shin_L=((8, 0, 0)), **_guard(1.0, b))
    return Clip('idle', length, pose, loop=True)


def walk(length=1.1, amp=24.0, name='walk', lean=4.0):
    def pose(t):
        a = wave(t, length)
        g = _guard(0.85, a)
        return _p(Root=((0, 5 * a, 0), (0, 0.6 * abs(wave(t, length / 2)), 0)), Chest=((lean, -4 * a, 0)),
                  Thigh_L=((amp * a, 0, 0)), Thigh_R=((-amp * a, 0, 0)),
                  Shin_L=((-30 * max(0.0, wave(t, length, 0.25)), 0, 0)), Shin_R=((-30 * max(0.0, wave(t, length, 0.75)), 0, 0)),
                  **g)
    return Clip(name, length, pose, loop=True)


def cross_slash(length=0.9):
    """Both lower blades cut across in an X. Hit window ~0.32-0.5 s."""
    def pose(t):
        wind = keys(t, [(0, 0), (0.3, 1), (0.4, 0), (length, 0)])
        cut = keys(t, [(0, 0), (0.3, 0), (0.48, 1), (0.6, 1), (length, 0)], ease_out)
        g = _guard(1.0)
        g.update(Arm_LL=((70 * wind + 40 * cut, 40 * wind - 70 * cut, -50 * wind)),
                 Arm_LR=((70 * wind + 40 * cut, -40 * wind + 70 * cut, 50 * wind)),
                 Forearm_LL=((-30 * wind, 0, 0)), Forearm_LR=((-30 * wind, 0, 0)))
        return _p(Root=((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.32, 0), (0.5, -6), (length, -6)]))),
                  Chest=((-6 * wind + 14 * cut, 0, 0)), Thigh_L=((-20 * cut, 0, 0)), Shin_L=((-15 * cut, 0, 0)), **g)
    return Clip('cross_slash', length, pose)


def overhead(length=1.1):
    """The two upper blades brought down together, then a thrust of the lower pair. Hits ~0.4-0.6 s."""
    def pose(t):
        down = keys(t, [(0, 0), (0.35, 0), (0.52, 1), (0.8, 1), (length, 0)], ease_in)
        thrust = keys(t, [(0, 0), (0.55, 0), (0.7, 1), (0.85, 1), (length, 0)], ease_out)
        g = _guard(1.0)
        g.update(Arm_UL=((170 - 110 * down, 0, 20)), Arm_UR=((170 - 110 * down, 0, -20)),
                 Forearm_UL=((-50 + 40 * down, 0, 0)), Forearm_UR=((-50 + 40 * down, 0, 0)),
                 Arm_LL=((50 + 40 * thrust, 0, -5)), Arm_LR=((50 + 40 * thrust, 0, 5)),
                 Forearm_LL=((25 - 25 * thrust, 0, 0)), Forearm_LR=((25 - 25 * thrust, 0, 0)))
        return _p(Root=((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.4, 0), (0.75, -9), (length, -9)]))),
                  Chest=((-10 + 26 * down, 0, 0)), Head=((-10 * down, 0, 0)), **g)
    return Clip('overhead', length, pose)


def parry(length=0.6):
    """Flow: a blade meets the blow and turns it aside."""
    def pose(t):
        k = keys(t, [(0, 0), (0.1, 1), (0.35, 1), (length, 0)], ease_out)
        g = _guard(1.0)
        g.update(Arm_LR=((80 * k + 50 * (1 - k), -40 * k, 30 * k)), Forearm_LR=((-20 * k + 25 * (1 - k), 0, 0)))
        return _p(Root=((0, 25 * k, 0), (0, 0, 2 * k)), Chest=((0, 15 * k, 0)), **g)
    return Clip('parry', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        return _p(Root=((0, 0, 0), (0, 0, 1.2 * k)), Chest=((-10 * k, 8 * k, 0)), Head=((12 * k, 0, 0)), **_guard(1.0 - 0.3 * k))
    return Clip('hit', length, pose)


def death(length=1.6):
    """The animated suit falls apart: it drops to its knees and the pieces fall."""
    def pose(t):
        k = keys(t, [(0, 0), (0.6, 0.6), (1.2, 1), (length, 1)], ease_in)
        return _p(Root=((20 * k, 0, 0), (0, -9 * k, 0)), Chest=((25 * k, 0, 10 * k)), Head=((40 * k, 0, 20 * k), (0, -6 * k, -2 * k)),
                  Thigh_L=((-85 * k, 0, -10 * k)), Thigh_R=((-80 * k, 0, 10 * k)), Shin_L=((85 * k, 0, 0)), Shin_R=((85 * k, 0, 0)),
                  Arm_UL=((10 * k, 0, 60 * k), (0, -4 * k, 0)), Arm_UR=((10 * k, 0, -60 * k), (0, -4 * k, 0)),
                  Arm_LL=((-20 * k, 0, 20 * k)), Arm_LR=((-20 * k, 0, -20 * k)))
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.7, 34.0, 'run', 12.0), cross_slash(), overhead(), parry(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
