"""
Pax Coyote (wiki, page Monsters: rank C, Biegoya Region of the Demon Continent): canine monsters about 2 m long that
come by the dozen, under the complete control of one leader. Drawn after the anime (S1 ep. 11, Dead End against a
pack): gaunt, long-legged, slate-violet coat bristling into ragged tufts along the neck and back, red eyes, a long
shaggy tail.
"""
import numpy as np

import canine
from paint import fbm, fur, mix, rgba, scale, top_light

NAME = 'pax_coyote'

SHAPE = canine.CanineShape(hip_y=19.0, chest_drop=12.5, withers=23.0, half_width=3.8, body_front=-11.0, body_back=9.0,
                           leg_width=2.4, paw_length=3.6, neck_length=7.5, neck_height=6.0,
                           head=(6.0, 5.5, 7.0), muzzle=(3.4, 2.8, 9.0), ear=(2.2, 4.4), ear_spread=2.0,
                           tail=(3.0, 8.0, 8.0), tail_lift=30.0, spikes=11, spike_size=1.8, fangs=1.7, claw=1.2,
                           mane=1.6)

DARK = (66, 62, 94)
MID = (116, 110, 148)
LIGHT = (178, 172, 204)
IVORY = (226, 220, 204)
MOUTH = (92, 30, 44)
EYE = (226, 36, 52)


def painter(model):
    meta = model.meta
    height = meta['height']

    def paint(p, n, cube, face, uv):
        tag = cube.tag
        if tag == 'tooth':
            return rgba(IVORY)
        if tag == 'claw':
            return rgba((40, 38, 46))
        if tag == 'nose':
            return rgba((24, 22, 30))
        for eye in meta['eyes']:
            d = np.linalg.norm(p - np.array(eye))
            if tag == 'skull' and d < 0.85:
                return rgba(mix((255, 150, 150), EYE, d / 0.85))
        if (tag == 'jaw' and face.name == 'up') or (tag == 'muzzle' and face.name == 'down'):
            return rgba(MOUTH)
        if tag == 'ear' and face.name == 'north' and 0.25 < uv[0] < 0.75 and uv[1] > 0.3:
            return rgba((70, 52, 72))
        strand = fur(p, density=1.3, seed=7)
        if tag in ('mane', 'spike'):
            # ragged edge: the tufts thin out into transparent points
            edge = min(uv[0], 1 - uv[0], uv[1], 1 - uv[1])
            if tag == 'mane' and face.name not in ('up',) and edge < 0.3 and fur(p * 1.7, density=1.0, seed=9) > 0.25 + edge * 2.2:
                return (0, 0, 0, 0)
            if tag == 'spike' and edge < 0.25 and strand > 0.55:
                return (0, 0, 0, 0)
        light = top_light(n, p[1], height)
        coat = mix(DARK, MID, 0.25 + 0.75 * light)
        # the back and the ragged tufts darker, the belly and inner legs paler
        if n[1] < -0.3 or (tag in ('leg', 'shin') and abs(n[0]) > 0.5 and n[0] * p[0] < 0):
            coat = mix(coat, LIGHT, 0.35)
        if tag in ('spike', 'mane'):
            coat = mix(DARK, MID, 0.3 + 0.5 * strand)
        # strong strands: a coat bristling in streaks, as the anime draws it
        coat = scale(coat, 0.72 + 0.5 * strand)
        if strand > 0.78:
            coat = mix(coat, LIGHT, 0.5)
        if fbm(np.array(p) * 0.5, 2, 5) < 0.3:
            coat = scale(coat, 0.85)
        return rgba(coat)

    return paint


def build():
    model = canine.build(NAME, SHAPE)
    model.pack()
    return model, painter(model), canine.all_clips(SHAPE)
