"""
Canine rig shared by the dog-like monsters (Assault Dog, Pax Coyote): body in two parts so the spine flexes in the
gallop, neck, head with a hinged jaw, ears, two-part tail and legs in two segments. Proportions are parameters, so
each species gets its own silhouette from the same bones and the same animations.
"""
import math

from anim import Clip, ease_in, ease_out, keys, smooth, wave
from rig import Model


class CanineShape:
    """Proportions in model pixels (16 px = 1 block)."""

    def __init__(self, **kw):
        self.hip_y = 14.0          # height of the hips' pivot
        self.chest_drop = 8.0      # bottom of the deep chest
        self.withers = 18.5        # top of the shoulders
        self.half_width = 4.5      # half width of the chest
        self.body_front = -11.0    # front of the chest
        self.body_back = 9.0       # rump
        self.leg_width = 3.0
        self.paw_length = 4.0
        self.neck_length = 7.0
        self.head = (6.5, 6.0, 7.0)  # skull width, height, depth
        self.muzzle = (4.0, 3.0, 7.0)
        self.ear = (2.0, 5.5)
        self.ear_spread = 2.2
        self.tail = (2.0, 7.0, 5.0)  # thickness, base length, tip length
        self.tail_lift = -25.0
        self.spikes = 0              # bony spikes along the spine
        self.fangs = 1.6
        self.neck_height = 5.5
        self.claw = 0.8
        for k, v in kw.items():
            setattr(self, k, v)


def build(name, s, density=2):
    m = Model(name, density)
    hw = s.half_width
    root = m.bone('Root', None, (0, s.hip_y, 3))
    chest = m.bone('Chest', root, (0, s.hip_y + 0.5, -1))
    neck_y = s.withers - 2.0
    neck = m.bone('Neck', chest, (0, neck_y, s.body_front + 1.5))
    head_z = s.body_front - s.neck_length * 0.5 - 1.5
    head_y = neck_y + s.neck_length * 0.62
    head = m.bone('Head', neck, (0, head_y, head_z))
    hw_s, hh_s, hd_s = s.head
    skull_front = head_z - hd_s * 0.78
    mw, mh, md = s.muzzle
    jaw = m.bone('Jaw', head, (0, head_y - hh_s * 0.42, skull_front + 0.5))
    ear_l = m.bone('Ear_L', head, (-s.ear_spread, head_y + hh_s * 0.5, head_z - hd_s * 0.25))
    ear_r = m.bone('Ear_R', head, (s.ear_spread, head_y + hh_s * 0.5, head_z - hd_s * 0.25))
    tail = m.bone('Tail', root, (0, s.hip_y + 2.5, s.body_back - 0.5))
    tl = s.tail[1]
    tip_y = s.hip_y + 2.5 + tl * math.sin(math.radians(-s.tail_lift))
    tip_z = s.body_back - 0.5 + tl * math.cos(math.radians(-s.tail_lift))
    tail_tip = m.bone('Tail_Tip', tail, (0, tip_y, tip_z))

    # body: rump on the root, deep chest on the chest bone
    m.cube(root, (-hw + 0.5, s.hip_y - 3.5, -1.5), (2 * hw - 1, 7.5, s.body_back + 1.5), 'body')
    m.cube(chest, (-hw, s.chest_drop, s.body_front), (2 * hw, s.withers - s.chest_drop, -s.body_front - 0.5), 'body')
    # neck slanting up and forwards
    m.cube(neck, (-hw * 0.6, neck_y - s.neck_height / 2, s.body_front - s.neck_length + 2), (hw * 1.2, s.neck_height, s.neck_length),
           'neck', tilt=(38, 0, 0), tilt_pivot=(0, neck_y, s.body_front + 1.5))
    # head
    m.cube(head, (-hw_s / 2, head_y - hh_s * 0.42, skull_front), (hw_s, hh_s, hd_s), 'skull')
    m.cube(head, (-hw_s / 2 - 0.25, head_y + hh_s * 0.33, skull_front - 0.5), (hw_s + 0.5, 1.5, 2.5), 'brow')
    m.cube(head, (-mw / 2, head_y - hh_s * 0.42 + 0.9, skull_front - md + 0.5), (mw, mh, md - 0.5), 'muzzle')
    m.cube(head, (-1.25, head_y - hh_s * 0.42 + 0.9 + mh - 1.4, skull_front - md), (2.5, 1.5, 1.5), 'nose')
    m.cube(jaw, (-mw / 2 + 0.25, head_y - hh_s * 0.42 - 1.6, skull_front - md + 1.0), (mw - 0.5, 1.6, md), 'jaw')
    # fangs: upper on the muzzle, lower on the jaw
    upper_y = head_y - hh_s * 0.42 + 0.9 - s.fangs + 0.2
    m.mirrored(head, (mw / 2 - 1.0, upper_y, skull_front - md + 1.2), (0.6, s.fangs, 0.6), 'tooth', head)
    m.mirrored(jaw, (mw / 2 - 1.1, head_y - hh_s * 0.42 - 0.2, skull_front - md + 1.6), (0.5, 1.0, 0.5), 'tooth', jaw)
    # ears: tall and pointed, leaning outwards
    ew, eh = s.ear
    for bone, side in ((ear_l, -1), (ear_r, 1)):
        x = s.ear_spread * side
        m.cube(bone, (x - ew / 2, bone.pivot[1] - 0.5, bone.pivot[2] - 0.5), (ew, eh * 0.6 + 0.5, 1.0), 'ear',
               tilt=(-8, 0, -12 * side), tilt_pivot=tuple(bone.pivot))
        m.cube(bone, (x - ew * 0.3 + side * 0.2, bone.pivot[1] + eh * 0.6, bone.pivot[2] - 0.4), (ew * 0.55, eh * 0.45, 0.8),
               'ear', tilt=(-8, 0, -12 * side), tilt_pivot=tuple(bone.pivot))
    # tail
    tt = s.tail[0]
    m.cube(tail, (-tt / 2, tail.pivot[1] - tt / 2, tail.pivot[2]), (tt, tt, tl), 'tail',
           tilt=(s.tail_lift, 0, 0), tilt_pivot=tuple(tail.pivot))
    m.cube(tail_tip, (-tt * 0.4, tip_y - tt * 0.4, tip_z), (tt * 0.8, tt * 0.8, s.tail[2]), 'tail',
           tilt=(s.tail_lift - 15, 0, 0), tilt_pivot=(0, tip_y, tip_z))
    # bony ridge along the spine
    for i in range(s.spikes):
        k = i / max(1, s.spikes - 1)
        z = s.body_front + 2 + k * (s.body_back - s.body_front - 4)
        bone = chest if z < -1 else root
        top = s.withers if z < -1 else s.hip_y + 4
        h = 2.6 - 1.2 * abs(k - 0.3)
        m.cube(bone, (-0.5, top - 0.3, z), (1.0, h, 1.4), 'spike', tilt=(40, 0, 0), tilt_pivot=(0, top, z + 0.7))
    # legs
    lw = s.leg_width
    for side, suffix in ((-1, 'L'), (1, 'R')):
        x = (hw - lw / 2 - 0.2) * side
        f_thigh = m.bone('Thigh_F' + suffix, chest, (x, s.hip_y + 1, s.body_front + 4.5))
        f_shin = m.bone('Shin_F' + suffix, f_thigh, (x, s.hip_y - 6.5, s.body_front + 4.5))
        b_thigh = m.bone('Thigh_B' + suffix, root, (x, s.hip_y + 1, s.body_back - 4))
        b_shin = m.bone('Shin_B' + suffix, b_thigh, (x, s.hip_y - 5.5, s.body_back - 2))
        fz = s.body_front + 4.5
        m.cube(f_thigh, (x - lw / 2, s.hip_y - 7, fz - 2), (lw, 9, 4), 'leg')
        m.cube(f_shin, (x - lw * 0.42, 1.5, fz - 1.25), (lw * 0.84, s.hip_y - 7.5, 2.5), 'shin')
        m.cube(f_shin, (x - lw / 2, 0, fz - s.paw_length + 1.0), (lw, 1.8, s.paw_length), 'paw')
        bz = s.body_back - 4
        m.cube(b_thigh, (x - lw * 0.58, s.hip_y - 6, bz - 2.5), (lw * 1.16, 9, 6), 'leg')
        m.cube(b_shin, (x - lw * 0.42, 1.5, bz + 1.0), (lw * 0.84, s.hip_y - 6.5, 2.5), 'shin')
        m.cube(b_shin, (x - lw / 2, 0, bz + 3.5 - s.paw_length), (lw, 1.8, s.paw_length), 'paw')
        for paw_z, shin in ((fz - s.paw_length + 1.0, f_shin), (bz + 3.5 - s.paw_length, b_shin)):
            for k in (-1, 0, 1):
                m.cube(shin, (x + k * lw * 0.3 - 0.3, 0, paw_z - s.claw + 0.1), (0.6, 0.8, s.claw), 'claw')
    m.meta = {
        'eyes': [(side * (hw_s / 2 - 0.9), head_y + 0.9, skull_front) for side in (-1, 1)],
        'skull_front': skull_front, 'head_y': head_y, 'mouth_y': head_y - hh_s * 0.42 + 0.9,
        'height': s.withers + 6,
    }
    return m


# ---------------------------------------------------------------------------------------------------- animations

LEGS = ('BL', 'FL', 'BR', 'FR')


def _gait(t, period, phases, swing, fold, extra=None):
    pose = {}
    for leg, phase in phases.items():
        a = wave(t, period, phase)
        lift = max(0.0, wave(t, period, phase + 0.25))
        front = leg[0] == 'F'
        pose['Thigh_' + leg] = ((swing * a, 0, 0), (0, 0, 0))
        pose['Shin_' + leg] = ((-fold * lift if front else fold * 0.6 * lift - fold * 0.25 * a, 0, 0), (0, 0, 0))
    if extra:
        pose.update(extra)
    return pose


def idle(length=2.4):
    def pose(t):
        breath = wave(t, length)
        look = keys(t, [(0, 0), (0.6, 0), (1.0, 14), (1.6, 14), (2.0, 0), (length, 0)])
        twitch = 18 * max(0.0, wave(t, length / 2, 0.1)) ** 8
        return {
            'Chest': ((breath * 1.2, 0, 0), (0, breath * 0.25, 0)),
            'Neck': ((-4 + breath * 1.5, look * 0.4, 0), (0, 0, 0)),
            'Head': ((breath * 2, look * 0.6, 0), (0, 0, 0)),
            'Jaw': ((-4 - 3 * max(0.0, breath), 0, 0), (0, 0, 0)),
            'Ear_L': ((-twitch, 0, 0), (0, 0, 0)),
            'Ear_R': ((0, 0, 0), (0, 0, 0)),
            'Tail': ((-6, 8 * wave(t, length / 2), 0), (0, 0, 0)),
            'Tail_Tip': ((0, 10 * wave(t, length / 2, 0.15), 0), (0, 0, 0)),
        }
    return Clip('idle', length, pose, loop=True)


def walk(length=0.9):
    def pose(t):
        bob = abs(wave(t, length / 2))
        return _gait(t, length, {'BL': 0.0, 'FL': 0.25, 'BR': 0.5, 'FR': 0.75}, 28, 45, {
            'Root': ((0, 0, 2 * wave(t, length)), (0, bob * 0.6, 0)),
            'Chest': ((1.5 * wave(t, length / 2), 0, -2 * wave(t, length)), (0, 0, 0)),
            'Neck': ((-6 + 3 * wave(t, length / 2, 0.2), 0, 0), (0, 0, 0)),
            'Head': ((2 * wave(t, length / 2, 0.3), 0, 0), (0, 0, 0)),
            'Tail': ((-4, 12 * wave(t, length), 0), (0, 0, 0)),
            'Tail_Tip': ((0, 14 * wave(t, length, 0.2), 0), (0, 0, 0)),
        })
    return Clip('walk', length, pose, loop=True)


def run(length=0.48):
    def pose(t):
        flex = wave(t, length)
        return _gait(t, length, {'BL': 0.0, 'BR': 0.07, 'FL': 0.5, 'FR': 0.57}, 48, 70, {
            'Root': ((6 * flex, 0, 0), (0, 1.6 * wave(t, length, 0.25), 0)),
            'Chest': ((-9 * flex, 0, 0), (0, 0, 0)),
            'Neck': ((-14 + 8 * flex, 0, 0), (0, 0, 0)),
            'Head': ((-4 - 6 * flex, 0, 0), (0, 0, 0)),
            'Jaw': ((-10, 0, 0), (0, 0, 0)),
            'Ear_L': ((-35, 0, 0), (0, 0, 0)),
            'Ear_R': ((-35, 0, 0), (0, 0, 0)),
            'Tail': ((8 - 10 * flex, 0, 0), (0, 0, 0)),
            'Tail_Tip': ((-10 * flex, 0, 0), (0, 0, 0)),
        })
    return Clip('run', length, pose, loop=True)


def bite(length=0.8, lunge=14.0):
    """Crouch, lunge with the jaws wide open, snap, shake. Hit window ~0.33-0.48 s."""
    def pose(t):
        crouch = keys(t, [(0, 0), (0.28, 1), (0.36, 0), (length, 0)])
        reach = keys(t, [(0, 0), (0.28, 0), (0.42, 1), (0.55, 1), (length, 0)])
        jaw = keys(t, [(0, 0), (0.25, 0.4), (0.34, 1), (0.44, 0), (0.5, 0.15), (0.56, 0), (length, 0)])
        shake = keys(t, [(0, 0), (0.44, 0), (0.48, 1), (0.53, -1), (0.58, 0.6), (0.64, 0), (length, 0)])
        root_z = keys(t, [(0, 0), (0.3, 0), (0.45, -lunge), (length, -lunge)], ease_out)
        return {
            'Root': ((-6 * crouch + 4 * reach, 0, 0), (0, -2 * crouch + 0.8 * reach, root_z)),
            'Chest': ((-8 * crouch + 6 * reach, 0, 0), (0, 0, 0)),
            'Neck': ((-18 * crouch - 4 * reach, 0, 0), (0, 0, 0)),
            'Head': ((-6 * crouch + 2 * reach, 0, 14 * shake), (0, 0, 0)),
            'Jaw': ((-38 * jaw, 0, 0), (0, 0, 0)),
            'Ear_L': ((-30 * max(crouch, reach), 0, 0), (0, 0, 0)),
            'Ear_R': ((-30 * max(crouch, reach), 0, 0), (0, 0, 0)),
            'Thigh_FL': ((-12 * crouch + 35 * reach, 0, 0), (0, 0, 0)),
            'Thigh_FR': ((-12 * crouch + 30 * reach, 0, 0), (0, 0, 0)),
            'Shin_FL': ((-20 * reach, 0, 0), (0, 0, 0)),
            'Shin_FR': ((-25 * reach, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((22 * crouch - 25 * reach, 0, 0), (0, 0, 0)),
            'Thigh_BR': ((22 * crouch - 25 * reach, 0, 0), (0, 0, 0)),
            'Shin_BL': ((-20 * crouch + 15 * reach, 0, 0), (0, 0, 0)),
            'Shin_BR': ((-20 * crouch + 15 * reach, 0, 0), (0, 0, 0)),
            'Tail': ((10 * reach, 0, 0), (0, 0, 0)),
        }
    return Clip('bite', length, pose)


def pounce(length=1.15, lunge=30.0, height=9.0):
    """Leap at the target: crouch, spring, fly with the forepaws out, land. Hit window ~0.42-0.66 s."""
    def pose(t):
        crouch = keys(t, [(0, 0), (0.32, 1), (0.4, 0), (0.7, 0), (0.8, 0.6), (length, 0)])
        flight = keys(t, [(0, 0), (0.36, 0), (0.48, 1), (0.66, 1), (0.74, 0), (length, 0)])
        y = keys(t, [(0, 0), (0.36, 0), (0.52, height), (0.72, 0), (length, 0)])
        z = keys(t, [(0, 0), (0.36, 0), (0.72, -lunge), (length, -lunge)], smooth)
        jaw = keys(t, [(0, 0), (0.38, 0.2), (0.5, 1), (0.66, 1), (0.72, 0), (length, 0)])
        return {
            'Root': ((-8 * crouch + 14 * flight, 0, 0), (0, -2.5 * crouch + y, z)),
            'Chest': ((-6 * crouch + 8 * flight, 0, 0), (0, 0, 0)),
            'Neck': ((-15 * crouch + 6 * flight, 0, 0), (0, 0, 0)),
            'Head': ((-8 * crouch, 0, 0), (0, 0, 0)),
            'Jaw': ((-40 * jaw, 0, 0), (0, 0, 0)),
            'Ear_L': ((-40 * flight, 0, 0), (0, 0, 0)),
            'Ear_R': ((-40 * flight, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((-15 * crouch + 70 * flight, 0, -8 * flight), (0, 0, 0)),
            'Thigh_FR': ((-15 * crouch + 70 * flight, 0, 8 * flight), (0, 0, 0)),
            'Shin_FL': ((-30 * flight, 0, 0), (0, 0, 0)),
            'Shin_FR': ((-30 * flight, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((30 * crouch - 55 * flight, 0, 0), (0, 0, 0)),
            'Thigh_BR': ((30 * crouch - 55 * flight, 0, 0), (0, 0, 0)),
            'Shin_BL': ((-30 * crouch + 25 * flight, 0, 0), (0, 0, 0)),
            'Shin_BR': ((-30 * crouch + 25 * flight, 0, 0), (0, 0, 0)),
            'Tail': ((-20 * flight, 0, 0), (0, 0, 0)),
        }
    return Clip('pounce', length, pose)


def hit(length=0.4):
    def pose(t):
        k = keys(t, [(0, 0), (0.08, 1), (length, 0)], ease_out)
        return {
            'Root': ((4 * k, 0, 5 * k), (0, 0, 1.5 * k)),
            'Chest': ((6 * k, 0, -4 * k), (0, 0, 0)),
            'Neck': ((14 * k, -10 * k, 0), (0, 0, 0)),
            'Head': ((10 * k, 0, 8 * k), (0, 0, 0)),
            'Jaw': ((-25 * k, 0, 0), (0, 0, 0)),
            'Ear_L': ((-40 * k, 0, 0), (0, 0, 0)),
            'Ear_R': ((-40 * k, 0, 0), (0, 0, 0)),
        }
    return Clip('hit', length, pose)


def death(length=1.3, rest_height=4.5):
    def pose(t):
        k = keys(t, [(0, 0), (0.15, 0.1), (0.7, 1), (length, 1)], ease_in)
        sag = keys(t, [(0, 0), (0.2, 1), (length, 1)])
        return {
            'Root': ((0, 0, 88 * k), (0, -(14 - rest_height) * k - 1.5 * sag * (1 - k), 0)),
            'Chest': ((-6 * sag, 0, 0), (0, 0, 0)),
            'Neck': ((-20 * k, 0, -15 * k), (0, 0, 0)),
            'Head': ((-15 * k, 0, 0), (0, 0, 0)),
            'Jaw': ((-15 * k, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((25 * k, 0, 0), (0, 0, 0)),
            'Thigh_FR': ((35 * k, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((-25 * k, 0, 0), (0, 0, 0)),
            'Thigh_BR': ((-15 * k, 0, 0), (0, 0, 0)),
            'Shin_FL': ((-30 * k, 0, 0), (0, 0, 0)),
            'Shin_BR': ((20 * k, 0, 0), (0, 0, 0)),
            'Tail': ((25 * k, 0, 0), (0, 0, 0)),
        }
    return Clip('death', length, pose)


def howl(length=2.2):
    """Sits back and howls: the pack's rallying call."""
    def pose(t):
        sit = keys(t, [(0, 0), (0.35, 1), (1.85, 1), (length, 0)])
        call = keys(t, [(0, 0), (0.45, 0), (0.7, 1), (1.6, 1), (1.85, 0), (length, 0)])
        quiver = 2 * wave(t, 0.12) * call
        return {
            'Root': ((26 * sit, 0, 0), (0, -4.5 * sit, 1.5 * sit)),
            'Chest': ((10 * sit, 0, 0), (0, 0, 0)),
            'Neck': ((14 * sit + 22 * call, 0, 0), (0, 0, 0)),
            'Head': ((26 * call + quiver, 0, 0), (0, 0, 0)),
            'Jaw': ((-22 * call, 0, 0), (0, 0, 0)),
            'Ear_L': ((-25 * call, 0, 0), (0, 0, 0)),
            'Ear_R': ((-25 * call, 0, 0), (0, 0, 0)),
            'Thigh_FL': ((-36 * sit, 0, 0), (0, 0, 0)),
            'Thigh_FR': ((-36 * sit, 0, 0), (0, 0, 0)),
            'Thigh_BL': ((-60 * sit, 0, 0), (0, 0, 0)),
            'Thigh_BR': ((-60 * sit, 0, 0), (0, 0, 0)),
            'Shin_BL': ((80 * sit, 0, 0), (0, 0, 0)),
            'Shin_BR': ((80 * sit, 0, 0), (0, 0, 0)),
            'Tail': ((30 * sit, 0, 0), (0, 0, 0)),
        }
    return Clip('howl', length, pose)


def all_clips():
    return [idle(), walk(), run(), bite(), pounce(), hit(), death(), howl()]
