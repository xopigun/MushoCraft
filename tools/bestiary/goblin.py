"""
Goblin (wiki, page Monsters: rank E, forest boundaries): humanoid monsters with crude weapons that cannot understand
human speech; harmless in small numbers, but they breed fast and raid villages when left alone. Drawn after the anime
(S1 OVA 1): short and hunched, bald, olive-tan skin, huge pointed ears, green eyes, an underbite with fangs, a ragged
violet-grey fur over one shoulder, a loincloth tied with rope, a wooden club.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, fur, mix, rgba, scale, top_light
from rig import Model

NAME = 'goblin'
HEIGHT = 30.0

SKIN = (150, 136, 102)
SKIN_DARK = (106, 96, 74)
FUR = (96, 76, 104)
FUR_DARK = (62, 48, 70)
ROPE = (126, 96, 70)
CLOTH = (88, 70, 60)
WOOD = (110, 78, 50)
WOOD_DARK = (72, 50, 34)
EYE = (120, 196, 70)
TOOTH = (232, 226, 206)


def build_model():
    m = Model(NAME, density=3)
    root = m.bone('Root', None, (0, 13, 0))
    chest = m.bone('Chest', root, (0, 15, 0))
    head = m.bone('Head', chest, (0, 22, -1))
    m.cube(root, (-3.5, 10.5, -2), (7, 5, 4), 'cloth')
    m.cube(root, (-3.7, 13.5, -2.2), (7.4, 1.2, 4.4), 'rope')
    m.cube(chest, (-4, 14.5, -2.5), (8, 8, 4.5), 'skin')
    m.cube(chest, (-4.0, 15.5, -2.5), (5.5, 7.0, 4.5), 'fur', tilt=(0, 0, 18), tilt_pivot=(-2, 20, 0), inflate=0.6)
    m.cube(chest, (-5.6, 19.5, -2.2), (3.2, 3.2, 3.8), 'fur')
    # head: big bald skull, nose, underbite with fangs, huge ears
    m.cube(head, (-3.5, 22, -5), (7, 7, 6.5), 'skull')
    m.cube(head, (-1.1, 23.6, -6.6), (2.2, 3.0, 1.8), 'nose')
    m.cube(head, (-3.6, 26.6, -5.5), (7.2, 1.0, 1.2), 'brow')
    m.cube(head, (-3, 21, -5.2), (6, 2, 4), 'chin')
    for side in (-1, 1):
        m.cube(head, (side * 1.6 - 0.35, 22.6, -5.6), (0.7, 1.3, 0.6), 'tooth')
        m.cube(head, (side * 3.5 + (0 if side > 0 else -4.5), 24.2, -1.5), (4.5, 2.6, 0.8), 'ear',
               tilt=(0, -12 * side, 10 * side), tilt_pivot=(side * 3.5, 25.5, -1.1))
        m.cube(head, (side * 3.5 + (3.5 if side > 0 else -6.5), 25.4, -1.4), (3, 1.4, 0.6), 'ear',
               tilt=(0, -12 * side, 10 * side), tilt_pivot=(side * 3.5, 25.5, -1.1))
    for name, side in (('L', -1), ('R', 1)):
        arm = m.bone('Arm_' + name, chest, (side * 5.2, 21.5, -0.5))
        fore = m.bone('Forearm_' + name, arm, (side * 5.4, 15.5, -0.5))
        m.cube(arm, (side * 5.2 - 1.3, 15, -1.8), (2.6, 7, 2.6), 'skin')
        m.cube(fore, (side * 5.4 - 1.2, 9.5, -1.7), (2.4, 6.3, 2.4), 'skin')
        m.cube(fore, (side * 5.4 - 1.5, 8, -2), (3, 2, 3), 'skin')
        thigh = m.bone('Thigh_' + name, root, (side * 2, 11, 0))
        shin = m.bone('Shin_' + name, thigh, (side * 2, 6, 0))
        m.cube(thigh, (side * 2 - 1.5, 6, -1.5), (3, 6, 3), 'skin')
        m.cube(shin, (side * 2 - 1.3, 1, -1.3), (2.6, 5.5, 2.6), 'skin')
        m.cube(shin, (side * 2 - 1.6, 0, -3.2), (3.2, 1.5, 4.6), 'foot')
        if name == 'R':
            # the club, held forwards in the right fist
            m.cube(fore, (side * 5.4 - 0.7, 8.3, -6), (1.4, 1.4, 6.5), 'handle')
            m.cube(fore, (side * 5.4 - 1.5, 7.5, -12.5), (3, 3, 6.8), 'club')
    m.meta = {'eyes': [(-1.6, 26.0, -5.0), (1.6, 26.0, -5.0)]}
    return m


def painter(model):
    eyes = [np.array(e) for e in model.meta['eyes']]

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        grain = fur(p, density=1.4, seed=31)
        light = top_light(n, p[1], HEIGHT)
        if tag == 'tooth':
            return rgba(TOOTH)
        if tag in ('club', 'handle'):
            g = fur(p * np.array([3.0, 3.0, 0.4]), density=1.0, seed=33)
            c = mix(WOOD_DARK, WOOD, 0.3 + 0.7 * g)
            if tag == 'club' and fbm(np.array(p) * 0.9, 2, 35) < 0.3:
                c = scale(c, 0.7)  # knots
            return rgba(scale(c, 0.85 + 0.25 * light))
        if tag == 'rope':
            return rgba(scale(ROPE, 0.8 + 0.35 * (0.5 + 0.5 * math.sin(p[0] * 3 + p[2] * 3))))
        if tag == 'cloth':
            if p[1] < 11.2 and fbm(np.array(p) * 1.4, 2, 37) < 0.45:
                return (0, 0, 0, 0)  # ragged hem
            return rgba(scale(CLOTH, 0.75 + 0.35 * grain))
        if tag == 'fur':
            edge = min(uv[0], 1 - uv[0], uv[1], 1 - uv[1])
            if edge < 0.15 and fur(p * 1.6, density=1.0, seed=39) > 0.45 + edge * 3.0:
                return (0, 0, 0, 0)
            return rgba(mix(FUR_DARK, FUR, 0.3 + 0.7 * grain))
        if tag == 'skull' and face.name == 'north':
            for e in eyes:
                d = math.hypot(p[0] - e[0], p[1] - e[1])
                if d < 0.9:
                    return rgba((20, 24, 16) if d < 0.35 else EYE)
            if abs(p[1] - 22.9) < 0.35 and abs(p[0]) < 2.2:
                return rgba((70, 40, 36))  # the mouth line above the underbite
        if tag == 'brow':
            return rgba(scale(SKIN_DARK, 0.9 + 0.15 * grain))
        if tag == 'ear' and face.name in ('north', 'south'):
            return rgba(mix((150, 112, 92), SKIN, 0.4))
        base = mix(SKIN_DARK, SKIN, 0.25 + 0.75 * light)
        base = scale(base, 0.92 + 0.12 * grain)
        if tag in ('skull', 'chin') and fbm(np.array(p) * 1.2, 2, 41) < 0.28:
            base = scale(base, 0.88)  # wrinkles
        if tag == 'foot':
            base = scale(base, 0.85)
        return rgba(base)

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _pose(**bones):
    return {k: (v if isinstance(v[0], tuple) else (v, (0, 0, 0))) for k, v in bones.items()}


def idle(length=2.4):
    def pose(t):
        b = wave(t, length)
        look = keys(t, [(0, 0), (0.8, 0), (1.2, 18), (1.8, 18), (2.2, 0), (length, 0)])
        return _pose(Chest=((14 + 2 * b, 0, 0)), Head=((-10 - 2 * b, look, 0)),
                     Arm_L=((8 + 3 * b, 0, -8)), Arm_R=((20, 0, 6)), Forearm_L=((-15, 0, 0)), Forearm_R=((-30, 0, 0)),
                     Thigh_L=((10, 0, 0)), Thigh_R=((10, 0, 0)), Shin_L=((-12, 0, 0)), Shin_R=((-12, 0, 0)),
                     Root=((0, 0, 0), (0, -0.6, 0)))
    return Clip('idle', length, pose, loop=True)


def walk(length=0.8, amp=30.0, lean=16.0, name='walk', bob=0.6):
    def pose(t):
        a = wave(t, length)
        return _pose(Root=((0, 6 * a, 0), (0, bob * abs(wave(t, length / 2)) - 0.6, 0)),
                     Chest=((lean, -6 * a, 0)), Head=((-lean * 0.7, 6 * a, 0)),
                     Thigh_L=((amp * a + 6, 0, 0)), Thigh_R=((-amp * a + 6, 0, 0)),
                     Shin_L=((-35 * max(0.0, wave(t, length, 0.25)) - 6, 0, 0)),
                     Shin_R=((-35 * max(0.0, wave(t, length, 0.75)) - 6, 0, 0)),
                     Arm_L=((-amp * 0.8 * a, 0, -8)), Arm_R=((amp * 0.5 * a + 20, 0, 6)),
                     Forearm_L=((-20, 0, 0)), Forearm_R=((-30, 0, 0)))
    return Clip(name, length, pose, loop=True)


def swing(length=0.8):
    """A sideways blow of the club, right to left. Hit window ~0.28-0.44 s."""
    def pose(t):
        wind = keys(t, [(0, 0), (0.26, 1), (0.36, 0), (length, 0)])
        strike = keys(t, [(0, 0), (0.26, 0), (0.42, 1), (0.55, 1), (length, 0)], ease_out)
        return _pose(Root=((0, 0, 0), (0, -0.6, keys(t, [(0, 0), (0.28, 0), (0.44, -5), (length, -5)]))),
                     Chest=((18, -35 * wind + 45 * strike, 0)), Head=((-12, 20 * wind - 25 * strike, 0)),
                     Arm_R=((60 * wind + 70 * strike, 50 * wind - 70 * strike, 30 * wind)),
                     Forearm_R=((-40 * wind - 10 * strike, 0, 0)),
                     Arm_L=((10, 0, -25)), Forearm_L=((-30, 0, 0)),
                     Thigh_L=((-20 * strike + 10, 0, 0)), Thigh_R=((25 * strike, 0, 0)), Shin_R=((-20 * strike, 0, 0)))
    return Clip('swing', length, pose)


def overhead(length=1.0):
    """The club raised high and brought straight down. Hit window ~0.42-0.58 s."""
    def pose(t):
        up = keys(t, [(0, 0), (0.38, 1), (0.48, 0), (length, 0)])
        down = keys(t, [(0, 0), (0.38, 0), (0.5, 1), (0.7, 1), (length, 0)], ease_in)
        return _pose(Root=((0, 0, 0), (0, -0.6 - 1.2 * down, keys(t, [(0, 0), (0.4, 0), (0.55, -6), (length, -6)]))),
                     Chest=((-6 * up + 34 * down, 0, 0)), Head=((8 * up - 20 * down, 0, 0)),
                     Arm_R=((170 * up + 75 * down, 0, 10 * up)), Forearm_R=((-60 * up, 0, 0)),
                     Arm_L=((60 * up + 30 * down, 0, -20)), Forearm_L=((-30, 0, 0)),
                     Thigh_L=((-25 * down, 0, 0)), Shin_L=((-20 * down, 0, 0)), Thigh_R=((15 * down, 0, 0)))
    return Clip('overhead', length, pose)


def shout(length=1.4):
    """Brandishes the club and screeches: the gang's call."""
    def pose(t):
        k = keys(t, [(0, 0), (0.3, 1), (1.1, 1), (length, 0)])
        shake = 12 * wave(t, 0.2) * k
        return _pose(Chest=((-4 * k, 0, 0)), Head=((22 * k, 0, 0)),
                     Arm_R=((150 * k, 0, 20 * k + shake)), Forearm_R=((-40 * k, 0, 0)),
                     Arm_L=((20 * k, 0, -40 * k)), Forearm_L=((-50 * k, 0, 0)))
    return Clip('shout', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        return _pose(Root=((0, 0, 0), (0, -0.6, 1.5 * k)), Chest=((-12 * k, 10 * k, 0)), Head=((18 * k, -10 * k, 0)),
                     Arm_L=((-30 * k, 0, -30 * k)), Arm_R=((10 * k, 0, 30 * k)))
    return Clip('hit', length, pose)


def death(length=1.3):
    def pose(t):
        k = keys(t, [(0, 0), (0.25, 0.1), (0.9, 1), (length, 1)], ease_in)
        return _pose(Root=((-85 * k, 0, 0), (0, -10.5 * k, 3 * k)), Chest=((10 * (1 - k), 0, 0)), Head=((15 * k, 20 * k, 0)),
                     Arm_L=((30 * k, 0, -50 * k)), Arm_R=((40 * k, 0, 40 * k)),
                     Thigh_L=((40 * k, 0, 0)), Thigh_R=((25 * k, 0, 0)), Shin_L=((-40 * k, 0, 0)))
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.5, 45.0, 30.0, 'run', 1.4), swing(), overhead(), shout(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
