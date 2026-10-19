"""
Animations for the rigs of rig.py, authored as functions of time and sampled into Epic Fight clips.

A clip is a function `pose(t)` returning {bone: (rotation_deg, offset_px)} for t in [0, length] seconds. Rotations use
rig.rotation's order and signs (positive X lifts the front). The root's offset moves the whole body; in an attack, its
z offset (negative = forwards) is the lunge Epic Fight turns into real movement.
"""
import math

import numpy as np

from rig import BLENDER_FROM_MC, _row_major, _write_json

FPS = 20


class Clip:
    def __init__(self, name, length, pose, loop=False):
        self.name = name
        self.length = length
        self.pose = pose
        self.loop = loop

    def times(self):
        n = max(2, int(round(self.length * FPS)) + 1)
        return [self.length * i / (n - 1) for i in range(n)]

    def export(self, model, path):
        times = self.times()
        poses = [self.pose(t) for t in times]
        root = model.root
        tracks = []
        for bone in model.joint_order():
            mats = []
            for pose in poses:
                local = model.animated_local(bone, pose)
                if bone is root:
                    local = BLENDER_FROM_MC @ local
                mats.append(_row_major(local))
            tracks.append({'name': bone.name, 'time': [round(t, 4) for t in times], 'transform': mats})
        _write_json(path, {'animation': tracks})


# ---------------------------------------------------------------------- easing helpers

def smooth(x):
    x = min(1.0, max(0.0, x))
    return x * x * (3 - 2 * x)


def ease_out(x):
    x = min(1.0, max(0.0, x))
    return 1 - (1 - x) ** 3


def ease_in(x):
    x = min(1.0, max(0.0, x))
    return x ** 3


def keys(t, points, ease=smooth):
    """Value at time t along [(time, value), ...], eased between keys; values are numbers or tuples."""
    if t <= points[0][0]:
        return points[0][1]
    for (t0, v0), (t1, v1) in zip(points, points[1:]):
        if t <= t1:
            k = ease((t - t0) / (t1 - t0)) if t1 > t0 else 1.0
            if isinstance(v0, (tuple, list)):
                return tuple(a + (b - a) * k for a, b in zip(v0, v1))
            return v0 + (v1 - v0) * k
    return points[-1][1]


def wave(t, period, phase=0.0):
    return math.sin(2 * math.pi * (t / period + phase))


def add(*rots):
    return tuple(float(np.sum([r[i] for r in rots])) for i in range(3))
