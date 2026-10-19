"""
Painting helpers: procedural noise in model space, so fur, markings and shading run seamlessly across the faces of
different cubes (a stripe down the spine stays one stripe). Colours are RGB tuples 0-255.
"""
import math

import numpy as np


def _hash(ix, iy, iz, seed):
    h = (ix * 374761393 + iy * 668265263 + iz * 2147483647 + seed * 144665) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65535.0


def value_noise(p, seed=0):
    """Smooth value noise in [0, 1] at a 3D point."""
    x, y, z = p
    ix, iy, iz = math.floor(x), math.floor(y), math.floor(z)
    fx, fy, fz = x - ix, y - iy, z - iz
    sx, sy, sz = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy), fz * fz * (3 - 2 * fz)
    total = 0.0
    for dx in (0, 1):
        for dy in (0, 1):
            for dz in (0, 1):
                w = (sx if dx else 1 - sx) * (sy if dy else 1 - sy) * (sz if dz else 1 - sz)
                total += w * _hash(ix + dx, iy + dy, iz + dz, seed)
    return total


def fbm(p, octaves=3, seed=0):
    total, amp, norm = 0.0, 1.0, 0.0
    q = np.array(p, dtype=float)
    for o in range(octaves):
        total += amp * value_noise(q, seed + o * 17)
        norm += amp
        amp *= 0.5
        q = q * 2.03
    return total / norm


def fur(p, flow=(0.0, -1.0, 0.6), density=1.0, seed=0):
    """Fur strands: noise stretched along the direction the hair lies (default: down and backwards)."""
    f = np.array(flow, dtype=float)
    f /= np.linalg.norm(f)
    q = np.array(p, dtype=float) * density
    along = q @ f
    across = q - along * f
    return value_noise(across * 2.2 + f * along * 0.35, seed)


def mix(a, b, k):
    k = min(1.0, max(0.0, k))
    return tuple(x + (y - x) * k for x, y in zip(a, b))


def scale(c, k):
    return tuple(x * k for x in c)


def rgba(c, a=255):
    return tuple(int(min(255, max(0, round(x)))) for x in c) + (a,)


def band(x, edge, softness=0.08):
    """0 below `edge`, 1 above, with a short soft step."""
    return min(1.0, max(0.0, (x - edge) / softness + 0.5))


def cel(c, light, levels=(0.72, 1.0, 1.12), cuts=(0.35, 0.75)):
    """Anime-like flat shading: three tones picked from a light value in [0, 1]."""
    if light < cuts[0]:
        return scale(c, levels[0])
    if light < cuts[1]:
        return scale(c, levels[1])
    return scale(c, levels[2])


def top_light(normal, p_y, height):
    """Light baked into the texture: tops brighter, undersides and lower body darker (ambient occlusion)."""
    up = 0.5 + 0.5 * float(normal[1])
    return 0.55 * up + 0.45 * min(1.0, max(0.0, p_y / height))
