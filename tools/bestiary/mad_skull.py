"""
Mad Skull (wiki, page Monsters: Teleportation Labyrinth): a headless giant of mud, about 2.5 m tall and very broad,
with a human skull buried in its chest, its weak point; slow, shrugging off any blow to its mud, hiding the skull inside
when it senses danger; it slings mud or casts a spell like Stone Cannon. Drawn after the anime (S2 ep. 20): grey mud
cracked and pitted with holes, a black gash across the chest with three glowing orange eyes, a small skull beneath,
long heavy arms.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'mad_skull'
HEIGHT = 42.0

MUD = (124, 132, 142)
MUD_DARK = (74, 80, 92)
HOLE = (26, 28, 34)
BONE = (226, 222, 208)
GLOW = (255, 170, 70)

# hidden: the skull sinks back into the mud; shown: it stands out of the chest
HIDDEN = (0, 0, 2.6)
SHOWN = (0, 0, 0)


def build_model():
    m = Model(NAME, density=2)
    root = m.bone('Root', None, (0, 14, 0))
    chest = m.bone('Chest', root, (0, 18, 0))
    skull = m.bone('Skull', chest, (0, 30, -6))
    m.cube(root, (-8, 10, -5), (16, 9, 10), 'mud')
    m.cube(chest, (-11, 18, -6), (22, 18, 12), 'mud')
    m.cube(chest, (-12, 33, -5), (24, 6, 10), 'mud')
    m.cube(chest, (-7, 32.5, -6.6), (14, 2.4, 1), 'gash')
    m.cube(skull, (-1.8, 27.6, -7.2), (3.6, 4, 3.2), 'skull')
    m.cube(skull, (-1.2, 26.4, -7.0), (2.4, 1.4, 2.2), 'jaw')
    m.meta = {'eyes': [(-4.6, 33.7), (-2.4, 33.6), (3.8, 33.7)]}
    for name, side in (('L', -1), ('R', 1)):
        arm = m.bone('Arm_' + name, chest, (side * 12.5, 34, 0))
        fore = m.bone('Forearm_' + name, arm, (side * 13.5, 22, 0))
        m.cube(arm, (side * 12.5 - 3.5, 21, -3.5), (7, 14, 7), 'mud')
        m.cube(fore, (side * 13.5 - 3.8, 7, -3.8), (7.6, 15, 7.6), 'mud')
        m.cube(fore, (side * 13.5 - 4.4, 3, -4.6), (8.8, 5, 9.2), 'mud')
        thigh = m.bone('Thigh_' + name, root, (side * 4.5, 11, 0))
        shin = m.bone('Shin_' + name, thigh, (side * 4.5, 5, 0))
        m.cube(thigh, (side * 4.5 - 3.5, 4.5, -3.5), (7, 7, 7), 'mud')
        m.cube(shin, (side * 4.5 - 3.8, 0, -4.4), (7.6, 5.5, 8.6), 'mud')
    return m


def painter(model):
    eyes = model.meta['eyes']

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag in ('skull', 'jaw'):
            if tag == 'skull' and face.name == 'north':
                for x in (-0.8, 0.8):
                    if math.hypot(p[0] - x, p[1] - 29.6) < 0.6:
                        return rgba((20, 18, 18))  # empty eye sockets
                if abs(p[0]) < 0.3 and abs(p[1] - 28.6) < 0.4:
                    return rgba((40, 34, 30))
            if tag == 'jaw' and face.name == 'north' and int((p[0] + 2) * 2.5) % 2 == 0 and p[1] > 27.2:
                return rgba((60, 54, 48))  # teeth gaps
            return rgba(scale(BONE, 0.85 + 0.15 * max(0.0, n[1])))
        if tag == 'gash':
            if face.name == 'north':
                for ex, ey in eyes:
                    d = math.hypot(p[0] - ex, p[1] - ey)
                    if d < 0.75:
                        return rgba(mix((255, 240, 200), GLOW, d / 0.75))
            return rgba(HOLE)
        # mud: cracked, pitted with dark holes, darker below
        shade = fbm(np.array(p) * 0.25, 3, 71)
        c = mix(MUD_DARK, MUD, 0.35 + 0.45 * max(0.0, n[1]) + 0.35 * shade)
        crack = fbm(np.array(p) * 0.16, 2, 75)
        if abs(crack - 0.5) < 0.012:
            c = scale(c, 0.55)  # long cracks
        hole = fbm(np.array(p) * 0.2, 2, 73)
        if hole > 0.8 and face.name not in ('up', 'down'):
            c = HOLE if hole > 0.83 else scale(c, 0.6)
        return rgba(c)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _p(**bones):
    return {k: (v if isinstance(v[0], tuple) else (v, (0, 0, 0))) for k, v in bones.items()}


def _skull(shown):
    return {'Skull': ((0, 0, 0), tuple(h + (s - h) * shown for h, s in zip(HIDDEN, SHOWN)))}


def idle(length=3.2):
    def pose(t):
        b = wave(t, length)
        return _p(Root=((0, 0, 0), (0, 0.5 * b, 0)), Chest=((10 + 2 * b, 0, 0)),
                  Arm_L=((12, 0, -8)), Arm_R=((12, 0, 8)), Forearm_L=((-8, 0, 0)), Forearm_R=((-8, 0, 0)), **_skull(0.0))
    return Clip('idle', length, pose, loop=True)


def walk(length=1.6, amp=18.0, name='walk', lean=14.0):
    def pose(t):
        a = wave(t, length)
        return _p(Root=((0, 6 * a, 0), (0, 0.8 * abs(wave(t, length / 2)), 0)), Chest=((lean, -6 * a, 3 * a)),
                  Thigh_L=((amp * a, 0, 0)), Thigh_R=((-amp * a, 0, 0)),
                  Shin_L=((-25 * max(0.0, wave(t, length, 0.25)), 0, 0)), Shin_R=((-25 * max(0.0, wave(t, length, 0.75)), 0, 0)),
                  Arm_L=((-amp * 0.9 * a + 15, 0, -8)), Arm_R=((amp * 0.9 * a + 15, 0, 8)),
                  Forearm_L=((-12, 0, 0)), Forearm_R=((-12, 0, 0)), **_skull(0.0))
    return Clip(name, length, pose, loop=True)


def slam(length=1.4):
    """Both fists raised and brought down on the ground. Hit window ~0.6-0.78 s; the skull shows."""
    def pose(t):
        up = keys(t, [(0, 0), (0.5, 1), (0.6, 0), (length, 0)])
        down = keys(t, [(0, 0), (0.5, 0), (0.68, 1), (1.05, 1), (length, 0)], ease_in)
        show = keys(t, [(0, 0), (0.25, 1), (1.15, 1), (length, 0)])
        return _p(Root=((0, 0, 0), (0, -2 * down, keys(t, [(0, 0), (0.5, 0), (0.7, -7), (length, -7)]))),
                  Chest=((-12 * up + 35 * down, 0, 0)),
                  Arm_L=((160 * up + 85 * down, 0, -10)), Arm_R=((160 * up + 85 * down, 0, 10)),
                  Forearm_L=((-30 * up, 0, 0)), Forearm_R=((-30 * up, 0, 0)), **_skull(show))
    return Clip('slam', length, pose)


def throw(length=1.3):
    """Scoops mud from its own body and slings it. The ball leaves the hand at ~0.62 s; the skull shows."""
    def pose(t):
        scoop = keys(t, [(0, 0), (0.35, 1), (0.5, 1), (0.62, 0), (length, 0)])
        fling = keys(t, [(0, 0), (0.5, 0), (0.66, 1), (0.9, 1), (length, 0)], ease_out)
        show = keys(t, [(0, 0), (0.2, 1), (1.0, 1), (length, 0)])
        return _p(Chest=((10 * scoop - 6 * fling, -25 * scoop + 30 * fling, 0)),
                  Arm_R=((-40 * scoop + 150 * fling, 0, 20 * scoop)), Forearm_R=((-60 * scoop + 20 * fling, 0, 0)),
                  Arm_L=((20, 0, -20)), **_skull(show))
    return Clip('throw', length, pose)


def cast(length=1.6):
    """Hands brought together before the chest as a stone takes shape: the skull shows."""
    def pose(t):
        k = keys(t, [(0, 0), (0.3, 1), (length, 1)])
        return _p(Chest=((-5 * k, 0, 0)), Arm_L=((70 * k, -12 * k, 0)), Arm_R=((70 * k, 12 * k, 0)),
                  Forearm_L=((-25 * k, 0, 0)), Forearm_R=((-25 * k, 0, 0)), **_skull(k))
    return Clip('cast', length, pose)


def hit(length=0.5):
    def pose(t):
        k = keys(t, [(0, 0), (0.1, 1), (length, 0)], ease_out)
        return _p(Root=((0, 0, 0), (0, 0, 1.5 * k)), Chest=((-8 * k, 6 * k, 0)), **_skull(0.0))
    return Clip('hit', length, pose)


def death(length=2.0):
    """The mud gives way: the giant sinks into a heap, the skull rolling out."""
    def pose(t):
        k = keys(t, [(0, 0), (1.5, 1), (length, 1)], ease_in)
        return _p(Root=((0, 0, 0), (0, -12 * k, 0)), Chest=((40 * k, 0, 0), (0, -6 * k, 0)),
                  Thigh_L=((-80 * k, 0, -20 * k)), Thigh_R=((-80 * k, 0, 20 * k)),
                  Arm_L=((30 * k, 0, -70 * k)), Arm_R=((30 * k, 0, 70 * k)),
                  Skull=((60 * k, 0, 0), (0, -14 * k, -6 * k)))
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(1.0, 26.0, 'run', 22.0), slam(), throw(), cast(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
