"""
Wraith (wiki, page Monsters: undead formed from restless souls, worldwide; the Deadly Wraith, rank A, casts attack
magic): a translucent humanoid figure without legs, floating in the air. Drawn after the manga panel of the wiki: a
tall robed figure trailing off into wisps, long bony clawed hands, a strap across the chest, pale flames rising from the
head. The manga is black and white: the colours are a choice (slate-blue robe, bone, cold white flames).
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, fur, mix, rgba, scale
from rig import Model

NAME = 'wraith'
HEIGHT = 36.0

ROBE = (60, 66, 88)
ROBE_DARK = (30, 32, 46)
BONE = (214, 208, 190)
BONE_DARK = (150, 140, 120)
FLAME = (220, 242, 255)
STRAP = (58, 44, 36)
EYE = (170, 240, 255)


def build_model():
    m = Model(NAME, density=3)
    root = m.bone('Root', None, (0, 16, 0))
    tail = m.bone('Robe_Tail', root, (0, 8, 0.5))
    chest = m.bone('Chest', root, (0, 17, 0))
    head = m.bone('Head', chest, (0, 26, -0.5))
    m.cube(root, (-5, 8, -3.5), (10, 9.5, 7), 'robe')
    m.cube(tail, (-4.2, 0, -3), (8.4, 8.5, 6), 'wisp')
    m.cube(chest, (-4.5, 16.5, -2.8), (9, 10, 5.6), 'robe')
    m.cube(chest, (-4.8, 22.5, -3.1), (9.6, 4.5, 6.2), 'collar')
    m.cube(chest, (-4.7, 18, -3.0), (9.4, 1.4, 0.4), 'strap', tilt=(0, 0, -32), tilt_pivot=(0, 21, -3))
    m.cube(head, (-3, 26, -3.8), (6, 6.5, 6), 'skull')
    m.cube(head, (-3.4, 31.5, -3.4), (6.8, 3.2, 6.4), 'flame')
    m.cube(head, (-2.0, 34, -2.4), (4, 3.5, 4), 'flame')
    for name, side in (('L', -1), ('R', 1)):
        arm = m.bone('Arm_' + name, chest, (side * 5.6, 25, -0.3))
        fore = m.bone('Forearm_' + name, arm, (side * 5.8, 17.5, -0.3))
        m.cube(arm, (side * 5.6 - 1.7, 16.5, -1.9), (3.4, 9.2, 3.4), 'sleeve')
        m.cube(fore, (side * 5.8 - 0.9, 11, -0.9), (1.8, 7, 1.8), 'bone')
        m.cube(fore, (side * 5.8 - 1.3, 9.2, -1.3), (2.6, 2.0, 2.6), 'bone')
        for k in (-1, 0, 1):
            m.cube(fore, (side * 5.8 + k * 0.8 - 0.25, 5.2, -0.6 + abs(k) * 0.2), (0.5, 4.2, 0.5), 'claw',
                   tilt=(-12, 0, k * 8), tilt_pivot=(side * 5.8 + k * 0.8, 9.2, 0))
    m.meta = {'eyes': [(-1.3, 29.6, -3.8), (1.3, 29.6, -3.8)]}
    return m


def painter(model):
    eyes = [np.array(e) for e in model.meta['eyes']]

    def ragged(p, uv, face, depth, seed):
        """Edges thinning into transparent tatters: below for a robe, everywhere for wisps and flames."""
        n = fur(np.array(p) * 1.5, flow=(0, 1, 0), density=1.0, seed=seed)
        return n < depth

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'skull':
            if face.name == 'north':
                for e in eyes:
                    d = math.hypot(p[0] - e[0], p[1] - e[1])
                    if d < 0.38:
                        return rgba(EYE)  # a cold light deep in the socket
                    if d < 1.35:
                        return rgba((18, 16, 20) if d > 0.6 else (60, 120, 140))
                if abs(p[1] - 27.2) < 0.4 and abs(p[0]) < 1.8 and (int((p[0] + 2) * 2) % 2 == 0):
                    return rgba((40, 36, 34))  # teeth gaps
            return rgba(scale(mix(BONE_DARK, BONE, 0.5 + 0.5 * n[1] + 0.2 * fbm(p * 0.8, 2, 3)), 1.0))
        if tag == 'flame':
            k = (p[1] - 31.5) / 6.0
            if fur(np.array(p) * 1.8, flow=(0, 1, 0), density=1.0, seed=51) < 0.25 + k * 0.6:
                return (0, 0, 0, 0)
            return rgba(mix(FLAME, (120, 190, 255), k), 220)
        if tag == 'wisp':
            k = (8.5 - p[1]) / 8.5
            if face.name == 'down' or fur(np.array(p) * 1.4, flow=(0, 1, 0), density=1.0, seed=53) < 0.15 + k * 0.85:
                return (0, 0, 0, 0)
            return rgba(scale(ROBE, 0.8 - 0.3 * k), int(230 - 120 * k))
        if tag in ('bone', 'claw'):
            return rgba(scale(mix(BONE_DARK, BONE, 0.4 + 0.4 * n[1] + 0.2 * fbm(p * 1.2, 2, 5)), 1.0 if tag == 'bone' else 0.85))
        if tag == 'strap':
            return rgba(STRAP)
        # robe, sleeves, collar: heavy cloth, darker in the folds, tattered at the hem
        if tag == 'sleeve' and p[1] < 18.2 and ragged(p, uv, face, 0.5, 55):
            return (0, 0, 0, 0)
        if tag == 'robe' and p[1] < 9.5 and ragged(p, uv, face, 0.45, 57):
            return (0, 0, 0, 0)
        fold = 0.5 + 0.5 * math.sin(p[0] * 1.7 + fbm(p * 0.6, 2, 59) * 4)
        cloth = mix(ROBE_DARK, ROBE, 0.35 + 0.5 * fold + 0.15 * n[1])
        if tag == 'collar':
            cloth = scale(cloth, 0.8)
        return rgba(cloth, 235)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _p(**bones):
    return {k: (v if isinstance(v[0], tuple) else (v, (0, 0, 0))) for k, v in bones.items()}


def idle(length=3.0):
    def pose(t):
        b = wave(t, length)
        return _p(Root=((2, 0, 0), (0, 1.0 * b, 0)), Robe_Tail=((-4 + 5 * wave(t, length, 0.3), 6 * wave(t, length / 2), 0)),
                  Chest=((8 + 2 * b, 0, 0)), Head=((-6, 10 * wave(t, length, 0.1), 0)),
                  Arm_L=((10 + 4 * b, 0, -8)), Arm_R=((10 - 4 * b, 0, 8)), Forearm_L=((-15, 0, 0)), Forearm_R=((-15, 0, 0)))
    return Clip('idle', length, pose, loop=True)


def drift(name, length, lean):
    def pose(t):
        b = wave(t, length)
        return _p(Root=((lean, 0, 3 * b), (0, 1.0 * wave(t, length / 2), 0)),
                  Robe_Tail=((-lean * 1.6 + 6 * b, 8 * wave(t, length, 0.25), 0)),
                  Chest=((lean * 0.5, 0, 0)), Head=((-lean * 0.6, 0, 0)),
                  Arm_L=((-lean * 1.2 + 6 * b, 0, -12)), Arm_R=((-lean * 1.2 - 6 * b, 0, 12)),
                  Forearm_L=((-10, 0, 0)), Forearm_R=((-10, 0, 0)))
    return Clip(name, length, pose, loop=True)


def claw(length=0.9):
    """A raking swipe of the right claws, the body lunging after them. Hit window ~0.3-0.48 s."""
    def pose(t):
        wind = keys(t, [(0, 0), (0.28, 1), (0.38, 0), (length, 0)])
        rake = keys(t, [(0, 0), (0.28, 0), (0.46, 1), (0.6, 1), (length, 0)], ease_out)
        return _p(Root=((10 * rake, 0, 0), (0, 0, keys(t, [(0, 0), (0.3, 0), (0.48, -8), (length, -8)]))),
                  Chest=((5 + 15 * rake, -25 * wind + 30 * rake, 0)), Head=((-8, 15 * wind - 20 * rake, 0)),
                  Arm_R=((100 * wind + 40 * rake, 40 * wind - 60 * rake, 40 * wind)), Forearm_R=((-50 * wind, 0, 0)),
                  Arm_L=((20, 0, -30)), Forearm_L=((-30, 0, 0)), Robe_Tail=((-20 * rake, 0, 0)))
    return Clip('claw', length, pose)


def cast(length=1.6):
    """Both hands forward, gathering the spell: held while it chants."""
    def pose(t):
        k = keys(t, [(0, 0), (0.3, 1), (length, 1)])
        q = 3 * wave(t, 0.3) * k
        return _p(Root=((0, 0, 0), (0, 1.5 * k, 0)), Chest=((-6 * k, 0, 0)), Head=((-8 * k, 0, 0)),
                  Arm_L=((80 * k + q, 0, 18 * k)), Arm_R=((80 * k - q, 0, -18 * k)),
                  Forearm_L=((-20 * k, 0, 0)), Forearm_R=((-20 * k, 0, 0)), Robe_Tail=((-10 * k, 0, 0)))
    return Clip('cast', length, pose)


def raise_dead(length=2.0):
    """Arms flung up, head thrown back: the dead rise around it."""
    def pose(t):
        k = keys(t, [(0, 0), (0.5, 1), (1.6, 1), (length, 0)])
        return _p(Root=((0, 0, 0), (0, 3 * k, 0)), Chest=((-14 * k, 0, 0)), Head=((25 * k, 0, 0)),
                  Arm_L=((150 * k, 0, 35 * k)), Arm_R=((150 * k, 0, -35 * k)),
                  Forearm_L=((-20 * k, 0, 0)), Forearm_R=((-20 * k, 0, 0)), Robe_Tail=((10 * k * wave(t, 0.5), 0, 0)))
    return Clip('raise_dead', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        return _p(Root=((-10 * k, 0, 0), (0, 0, 2 * k)), Chest=((-10 * k, 0, 8 * k)), Head=((16 * k, 0, 0)),
                  Robe_Tail=((25 * k, 0, 0)))
    return Clip('hit', length, pose)


def death(length=1.6):
    """The figure sinks and spreads out as it comes apart."""
    def pose(t):
        k = keys(t, [(0, 0), (length * 0.85, 1), (length, 1)], ease_in)
        return _p(Root=((20 * k, 0, 0), (0, -12 * k, 0)), Chest=((30 * k, 0, 0)), Head=((30 * k, 0, 0)),
                  Arm_L=((60 * k, 0, -50 * k)), Arm_R=((60 * k, 0, 50 * k)), Robe_Tail=((60 * k, 0, 0)))
    return Clip('death', length, pose)


def clips():
    return [idle(), drift('walk', 2.0, 8.0), drift('run', 1.0, 20.0), claw(), cast(), raise_dead(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
