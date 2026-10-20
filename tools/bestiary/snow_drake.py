"""
Snow Drake (wiki, page Monsters: Northern Territories): a lesser, wingless dragon with pure white scales, three or four
metres long, nesting in large groups deep inside caves; it hates bright light and is docile unless its nest is
threatened; its scales resist ice magic. Drawn after the anime (S2 ep. 2): a low white lizard with sprawled limbs and
five-fingered clawed hands, a long tapering tail, a back of hexagonal scales spotted with dark blue, a crest of short
dark spikes at the back of the skull, a broad flat head with a pink mouth full of teeth, a lavender belly.
"""
import math

import numpy as np

from anim import Clip, ease_in, ease_out, keys, wave
from paint import fbm, mix, rgba, scale
from rig import Model

NAME = 'snow_drake'
HEIGHT = 18.0

WHITE = (236, 236, 242)
SHADE = (188, 190, 206)
BLUE = (64, 72, 150)
BELLY = (214, 196, 214)
MOUTH = (204, 120, 146)
CLAW = (70, 66, 80)
SPIKE = (78, 78, 92)

TAIL = [('Tail1', 12, 10.0, 7.0, 10), ('Tail2', 22, 7.0, 5.0, 10), ('Tail3', 32, 4.4, 3.6, 10), ('Tail4', 42, 2.2, 2.2, 9)]


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
    root = m.bone('Root', None, (0, 10, 2))
    chest = m.bone('Chest', root, (0, 11, 0))
    neck = m.bone('Neck', chest, (0, 12, -14))
    head = m.bone('Head', neck, (0, 12, -19))
    jaw = m.bone('Jaw', head, (0, 9.5, -20))
    m.cube(root, (-7, 5, -1), (14, 9, 13), 'body')
    m.cube(chest, (-7.5, 5, -14), (15, 10, 14), 'body')
    m.cube(neck, (-5, 7, -20), (10, 7, 7), 'body')
    m.cube(head, (-5, 9.5, -31), (10, 5, 12), 'head')
    m.cube(jaw, (-4.5, 7, -30.5), (9, 2.5, 11), 'jaw')
    for i, x in enumerate((-3.6, -1.2, 1.2, 3.6)):
        _segment(m, head, (x, 13.8, -21.5 + abs(x) * 0.3), (x * 1.3, 16.5 - abs(x) * 0.35, -18.5), 1.3, 'spike')
    for side, s in (('L', -1), ('R', 1)):
        # sprawled forelimbs: upper arm out sideways, forearm down, a broad clawed hand
        shoulder = np.array([s * 7.0, 9.0, -9.0])
        elbow = shoulder + np.array([s * 6.5, -2.5, -1.0])
        wrist = elbow + np.array([s * 1.0, -6.3, -2.0])
        arm = m.bone('Arm_' + side, chest, tuple(shoulder))
        fore = m.bone('Forearm_' + side, arm, tuple(elbow))
        _segment(m, arm, shoulder, elbow, 4.2, 'limb')
        _segment(m, fore, elbow, wrist, 3.6, 'limb')
        m.cube(fore, (wrist[0] - 3.2, 0, wrist[2] - 4.5), (6.4, 1.4, 6.5), 'hand')
        _segment(m, fore, elbow + np.array([0, 0.8, 1.0]), elbow + np.array([s * 1.4, 1.2, 3.6]), 1.0, 'spike')
        hip = np.array([s * 6.5, 9.0, 6.0])
        knee = hip + np.array([s * 6.5, -1.5, 2.0])
        ankle = knee + np.array([s * 1.0, -7.5, 1.0])
        thigh = m.bone('Thigh_' + side, root, tuple(hip))
        shin = m.bone('Shin_' + side, thigh, tuple(knee))
        _segment(m, thigh, hip, knee, 5.2, 'limb')
        _segment(m, shin, knee, ankle, 4.2, 'limb')
        m.cube(shin, (ankle[0] - 3.4, 0, ankle[2] - 5.0), (6.8, 1.6, 7.0), 'hand')
    parent = root
    for name, z, w, h, length in TAIL:
        bone = m.bone(name, parent, (0, 6.5 + h / 2 + (12 - z) * 0.03, z))
        m.cube(bone, (-w / 2, 6.5 + (12 - z) * 0.03, z), (w, h, length), 'tail')
        parent = bone
    return m


def painter(model):
    def scales(p, n):
        """White hexagonal scales on the back, a few of them dark blue."""
        u, v = p[0] * 0.34, p[2] * 0.3
        row = math.floor(v)
        u += 0.5 * (row % 2)
        fu, fv = u - math.floor(u), v - row
        edge = min(fu, 1 - fu, fv, 1 - fv) < 0.07
        cell = fbm(np.array([math.floor(u), row, 3.0]), 1, 91)
        if edge:
            return SHADE
        if cell > 0.8 and abs(p[0]) < 5.0 and p[2] > -16:
            return BLUE
        return WHITE

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'spike':
            return rgba(scale(SPIKE, 0.9 + 0.3 * max(0.0, n[1])))
        if tag == 'hand':
            if face.name == 'north' or (face.name == 'up' and int((p[0] + 20) * 0.9) % 2 == 0 and abs(p[2] % 6.5) < 1.5):
                return rgba(CLAW if face.name == 'north' and uv[1] > 0.5 else WHITE)
            return rgba(mix(SHADE, WHITE, 0.6 + 0.4 * max(0.0, n[1])))
        if tag == 'jaw':
            if face.name == 'up':
                if abs(abs(p[0]) - 3.8) < 0.6 or p[2] < -29.5:
                    return rgba((246, 244, 236))  # teeth along the rim
                return rgba(MOUTH)
            if face.name in ('east', 'west', 'north') and uv[1] < 0.35:
                return rgba(MOUTH)
            return rgba(mix(BELLY, WHITE, 0.4))
        if tag == 'head':
            if face.name == 'down':
                return rgba(MOUTH if abs(p[0]) < 4.0 else (246, 244, 236))
            if face.name in ('east', 'west'):
                if math.hypot(p[1] - 12.6, p[2] + 24.5) < 0.9:
                    return rgba((30, 26, 40))  # a narrow eye
                if p[1] < 10.6:
                    return rgba(MOUTH)  # the lips of a wide mouth
            if face.name == 'up':
                return rgba(scales(p, n))
            return rgba(mix(SHADE, WHITE, 0.7))
        if face.name == 'down' or (face.name in ('east', 'west') and p[1] < 7.5 and tag in ('body', 'tail')):
            return rgba(BELLY)
        if face.name == 'up' or (face.name in ('east', 'west') and tag in ('body', 'tail') and p[1] > 10):
            return rgba(scales(p, n))
        return rgba(mix(SHADE, WHITE, 0.55 + 0.45 * max(0.0, n[1])))

    return paint


# ---------------------------------------------------------------------------------------------------- animations

def _gait(t, period, swing, lift, sway):
    """Lizard gait: diagonal pairs move together; the spine bends in an S, the tail follows."""
    a = wave(t, period)
    up_l = max(0.0, wave(t, period, 0.25))
    up_r = max(0.0, wave(t, period, 0.75))
    p = {
        'Arm_L': ((0, -swing * a, -lift * up_l), (0, 0, 0)), 'Thigh_R': ((0, swing * a, lift * up_l), (0, 0, 0)),
        'Arm_R': ((0, -swing * a, lift * up_r), (0, 0, 0)), 'Thigh_L': ((0, swing * a, -lift * up_r), (0, 0, 0)),
        'Chest': ((0, sway * a, 0), (0, 0, 0)), 'Root': ((0, -sway * 0.5 * a, 0), (0, 0, 0)),
        'Neck': ((0, -sway * 0.8 * a, 0), (0, 0, 0)),
    }
    for i, (name, *_) in enumerate(TAIL):
        p[name] = ((0, sway * 0.9 * wave(t, period, 0.12 * (i + 1)), 0), (0, 0, 0))
    return p


def idle(length=3.0):
    def pose(t):
        b = wave(t, length)
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Chest': ((1.5 * b, 0, 0), (0, 0.2 * b, 0)), 'Neck': ((2 * b, 6 * wave(t, length, 0.3), 0), (0, 0, 0)),
                  'Jaw': ((-3 * max(0.0, wave(t, length, 0.6)), 0, 0), (0, 0, 0))})
        for i, (name, *_) in enumerate(TAIL):
            p[name] = ((0, 6 * wave(t, length, 0.15 * i), 0), (0, 0, 0))
        return p
    return Clip('idle', length, pose, loop=True)


def walk(length=1.2, name='walk', swing=26.0, lift=22.0, sway=10.0):
    def pose(t):
        return _gait(t, length, swing, lift, sway)
    return Clip(name, length, pose, loop=True)


def bite(length=0.9, lunge=10.0):
    """Head drawn back with the jaws open, a lunge, the jaws snap shut. Hit window ~0.3-0.45 s."""
    def pose(t):
        back = keys(t, [(0, 0), (0.25, 1), (0.34, 0), (length, 0)])
        snap = keys(t, [(0, 0), (0.28, 0), (0.4, 1), (0.6, 1), (length, 0)], ease_out)
        jaw = keys(t, [(0, 0), (0.22, 1), (0.36, 1), (0.42, 0), (length, 0)])
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((0, 0, 0), (0, 0, keys(t, [(0, 0), (0.28, 0), (0.42, -lunge), (length, -lunge)], ease_out))),
                  'Chest': ((8 * back - 4 * snap, 0, 0), (0, 0, 0)), 'Neck': ((14 * back - 10 * snap, 0, 0), (0, 0, 2 * back - 3 * snap)),
                  'Head': ((8 * back, 0, 0), (0, 0, 0)), 'Jaw': ((-38 * jaw, 0, 0), (0, 0, 0)),
                  'Arm_L': ((0, -15 * snap, 0), (0, 0, 0)), 'Arm_R': ((0, 15 * snap, 0), (0, 0, 0))})
        return p
    return Clip('bite', length, pose)


def tail_sweep(length=1.2):
    """The body coils, then the tail lashes round from one side to the other. Hit window ~0.4-0.7 s."""
    def pose(t):
        coil = keys(t, [(0, 0), (0.3, 1), (0.42, 0.6), (0.75, -1), (1.0, -0.3), (length, 0)])
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Chest': ((0, -14 * coil, 0), (0, 0, 0)), 'Root': ((0, 18 * coil, 0), (0, 0, 0)),
                  'Neck': ((0, -18 * coil, 0), (0, 0, 0))})
        for i, (name, *_) in enumerate(TAIL):
            lag = keys(t - 0.04 * (i + 1), [(0, 0), (0.3, 1), (0.42, 0.6), (0.75, -1), (1.0, -0.3), (length, 0)])
            p[name] = ((-4 * abs(lag), 22 * lag, 0), (0, 0, 0))
        return p
    return Clip('tail_sweep', length, pose)


def rear(length=1.6):
    """Rises on its hind legs, the forelimbs spread, and roars: the pack hears it."""
    def pose(t):
        k = keys(t, [(0, 0), (0.45, 1), (1.2, 1), (length, 0)])
        roar = keys(t, [(0, 0), (0.4, 0), (0.55, 1), (1.1, 1), (1.3, 0), (length, 0)])
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((40 * k, 0, 0), (0, 4 * k, 2 * k)), 'Chest': ((8 * k, 0, 0), (0, 0, 0)),
                  'Neck': ((-25 * k, 0, 0), (0, 0, 0)), 'Head': ((-10 * k + 18 * roar, 0, 0), (0, 0, 0)),
                  'Jaw': ((-40 * roar, 0, 0), (0, 0, 0)),
                  'Arm_L': ((0, -20 * k, 35 * k), (0, 0, 0)), 'Arm_R': ((0, 20 * k, -35 * k), (0, 0, 0)),
                  'Thigh_L': ((-40 * k, 0, 0), (0, 0, 0)), 'Thigh_R': ((-40 * k, 0, 0), (0, 0, 0)),
                  'Tail1': ((-30 * k, 0, 0), (0, 0, 0)), 'Tail2': ((-6 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('rear', length, pose)


def hit(length=0.45):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((0, 0, 0), (0, 0, 2 * k)), 'Chest': ((-6 * k, 8 * k, 0), (0, 0, 0)), 'Neck': ((-12 * k, 0, 0), (0, 0, 0)),
                  'Jaw': ((-15 * k, 0, 0), (0, 0, 0))})
        return p
    return Clip('hit', length, pose)


def death(length=1.8):
    """Rolls over onto its side and lies still, the tail falling last."""
    def pose(t):
        k = keys(t, [(0, 0), (1.1, 1), (length, 1)], ease_in)
        p = _gait(0.0, 1.0, 0, 0, 0)
        p.update({'Root': ((0, 0, 80 * k), (0, -3 * k, 0)), 'Neck': ((-10 * k, 0, 15 * k), (0, 0, 0)),
                  'Jaw': ((-25 * k, 0, 0), (0, 0, 0)), 'Arm_R': ((0, 0, -40 * k), (0, 0, 0)), 'Thigh_R': ((0, 0, -35 * k), (0, 0, 0))})
        for i, (name, *_) in enumerate(TAIL):
            p[name] = ((0, 10 * k, 0), (0, 0, 0))
        return p
    return Clip('death', length, pose)


def clips():
    return [idle(), walk(), walk(0.7, 'run', 32.0, 26.0, 14.0), bite(), tail_sweep(), rear(), hit(), death()]


def build():
    model = build_model()
    model.pack()
    return model, painter(model), clips()
