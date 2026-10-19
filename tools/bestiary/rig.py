"""
Creature rigs for Epic Fight, written from scratch for MushoCraft (GPLv3).

A creature is a tree of bones, each carrying axis-aligned cubes (optionally tilted), authored in Minecraft model
pixels: 16 px = 1 block, origin at the feet, front towards -Z, right towards +X, up +Y. From it this module writes
Epic Fight's skinned mesh + armature JSON, packs a texture atlas painted texel by texel from the 3D point each texel
covers, and renders previews with the same maths as the export, to check a model and its animations before the game.

Epic Fight conventions (decompiled from 21.17, see JsonAssetLoader): positions are in Blender space and converted with
a -90 degree turn about X; only the root joint's matrix gets that correction; matrices are written row-major with the
translation in the last column.
"""
import json
import math

import numpy as np
from PIL import Image

PX = 1.0 / 16.0
# Minecraft model space from Blender space, and back: mc = (x, z, -y), blender = (x, -z, y)
MC_FROM_BLENDER = np.array([[1, 0, 0, 0], [0, 0, 1, 0], [0, -1, 0, 0], [0, 0, 0, 1]], dtype=float)
BLENDER_FROM_MC = np.linalg.inv(MC_FROM_BLENDER)


def translation(v):
    m = np.eye(4)
    m[:3, 3] = v
    return m


def rotation(deg):
    """Rotation from Euler angles in degrees, applied yaw (Y), then pitch (X), then roll (Z): R = Ry . Rx . Rz.
    A positive X turn lifts the front (-Z) upwards; a positive Y turn swings the front towards -X (left)."""
    x, y, z = (math.radians(a) for a in deg)
    cx, sx, cy, sy, cz, sz = math.cos(x), math.sin(x), math.cos(y), math.sin(y), math.cos(z), math.sin(z)
    rx = np.array([[1, 0, 0, 0], [0, cx, -sx, 0], [0, sx, cx, 0], [0, 0, 0, 1]])
    ry = np.array([[cy, 0, sy, 0], [0, 1, 0, 0], [-sy, 0, cy, 0], [0, 0, 0, 1]])
    rz = np.array([[cz, -sz, 0, 0], [sz, cz, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]])
    return ry @ rx @ rz


class Bone:
    def __init__(self, name, parent, pivot):
        self.name = name
        self.parent = parent
        self.pivot = np.array(pivot, dtype=float)
        self.children = []
        if parent is not None:
            parent.children.append(self)

    def rest_local(self):
        """Rest transform relative to the parent, in blocks: bones are never turned at rest, only placed."""
        offset = self.pivot - (self.parent.pivot if self.parent is not None else np.zeros(3))
        return translation(offset * PX)


class Face:
    """One textured side of a cube: its corners (model px), its normal and its rectangle in the atlas."""

    def __init__(self, cube, name, origin, u_axis, v_axis, u_len, v_len, normal):
        self.cube = cube
        self.name = name
        self.origin = np.array(origin, dtype=float)
        self.u_axis = np.array(u_axis, dtype=float)
        self.v_axis = np.array(v_axis, dtype=float)
        self.u_len = u_len
        self.v_len = v_len
        self.normal = np.array(normal, dtype=float)
        self.rect = None  # (x, y, w, h) in texels

    def corners(self):
        o, u, v = self.origin, self.u_axis * self.u_len, self.v_axis * self.v_len
        return [o, o + u, o + u + v, o + v]


class Cube:
    """An axis-aligned box from `start` (min corner) of `size`, all in model px; `tilt` (degrees, same order as bones)
    turns it about `tilt_pivot` (defaults to the box centre); `tag` tells the painter what the box is."""

    def __init__(self, bone, start, size, tag, tilt=(0, 0, 0), tilt_pivot=None, inflate=0.0):
        self.bone = bone
        self.start = np.array(start, dtype=float) - inflate
        self.size = np.array(size, dtype=float) + 2 * inflate
        self.tag = tag
        self.tilt = tilt
        self.tilt_pivot = np.array(tilt_pivot if tilt_pivot is not None else self.start + self.size / 2, dtype=float)
        self.faces = self._faces()

    def _faces(self):
        x0, y0, z0 = self.start
        w, h, d = self.size
        x1, y1, z1 = self.start + self.size
        # each face seen from outside: u to the viewer's right, v downwards
        return [
            Face(self, 'north', (x1, y1, z0), (-1, 0, 0), (0, -1, 0), w, h, (0, 0, -1)),
            Face(self, 'south', (x0, y1, z1), (1, 0, 0), (0, -1, 0), w, h, (0, 0, 1)),
            Face(self, 'east', (x1, y1, z1), (0, 0, -1), (0, -1, 0), d, h, (1, 0, 0)),
            Face(self, 'west', (x0, y1, z0), (0, 0, 1), (0, -1, 0), d, h, (-1, 0, 0)),
            Face(self, 'up', (x0, y1, z0), (1, 0, 0), (0, 0, 1), w, d, (0, 1, 0)),
            Face(self, 'down', (x0, y0, z1), (1, 0, 0), (0, 0, -1), w, d, (0, -1, 0)),
        ]

    def tilt_matrix(self):
        p = self.tilt_pivot
        return translation(p) @ rotation(self.tilt) @ translation(-p)

    def world_face(self, face):
        """Corners and normal of a face once the cube's tilt is applied (model px)."""
        m = self.tilt_matrix()
        corners = [(m @ np.append(c, 1.0))[:3] for c in face.corners()]
        normal = (m[:3, :3] @ face.normal)
        return corners, normal


class Model:
    def __init__(self, name, density=2):
        """`density`: texels per model pixel."""
        self.name = name
        self.density = density
        self.bones = []
        self.cubes = []
        self.texture_size = None

    def bone(self, name, parent, pivot):
        b = Bone(name, parent, pivot)
        self.bones.append(b)
        return b

    def cube(self, bone, start, size, tag, **kw):
        c = Cube(bone, start, size, tag, **kw)
        self.cubes.append(c)
        return c

    def mirrored(self, bone, start, size, tag, mirror_bone, **kw):
        """A cube and its mirror across x = 0, the mirror on `mirror_bone`."""
        a = self.cube(bone, start, size, tag, **kw)
        s = np.array(start, dtype=float)
        sz = np.array(size, dtype=float)
        tilt = kw.pop('tilt', (0, 0, 0))
        pivot = kw.pop('tilt_pivot', None)
        mirrored_pivot = None if pivot is None else (-pivot[0], pivot[1], pivot[2])
        b = self.cube(mirror_bone, (-s[0] - sz[0], s[1], s[2]), size, tag, tilt=(tilt[0], -tilt[1], -tilt[2]),
                      tilt_pivot=mirrored_pivot, **kw)
        return a, b

    @property
    def root(self):
        return self.bones[0]

    # ------------------------------------------------------------------ atlas

    def pack(self, max_width=None):
        """Places every face in the atlas, cube by cube, each cube's faces side by side on one shelf."""
        dn = self.density
        blocks = []
        for cube in self.cubes:
            w, h, d = (int(math.ceil(v * dn)) for v in cube.size)
            # layout:  [up][down] on top row; [west][north][east][south] below
            block_w = max(2 * w + 2 * d, d + 2 * w)
            block_h = d + h
            blocks.append((cube, block_w + 2, block_h + 2))
        total = sum(bw * bh for _, bw, bh in blocks)
        width = max_width or 1 << max(6, int(math.ceil(math.log2(math.sqrt(total) * 1.15))))
        x = y = shelf = 0
        for cube, bw, bh in sorted(blocks, key=lambda b: -b[2]):
            if x + bw > width:
                x, y, shelf = 0, y + shelf, 0
            self._place(cube, x + 1, y + 1)
            x += bw
            shelf = max(shelf, bh)
        height = 1 << int(math.ceil(math.log2(max(y + shelf, 1))))
        self.texture_size = (width, max(height, width // 2))
        return self.texture_size

    def _place(self, cube, x, y):
        dn = self.density
        w, h, d = (int(math.ceil(v * dn)) for v in cube.size)
        f = {face.name: face for face in cube.faces}
        f['up'].rect = (x + d, y, w, d)
        f['down'].rect = (x + d + w, y, w, d)
        f['west'].rect = (x, y + d, d, h)
        f['north'].rect = (x + d, y + d, w, h)
        f['east'].rect = (x + d + w, y + d, d, h)
        f['south'].rect = (x + 2 * d + w, y + d, w, h)

    # ------------------------------------------------------------------ painting

    def paint(self, painter, out_path):
        """Fills the atlas: `painter(point_px, normal, cube, face, texel_uv)` returns RGBA (0-255) for each texel,
        given the 3D point of the model the texel covers (rest pose, tilt applied)."""
        tw, th = self.texture_size
        img = np.zeros((th, tw, 4), dtype=np.uint8)
        for cube in self.cubes:
            m = cube.tilt_matrix()
            for face in cube.faces:
                fx, fy, fw, fh = face.rect
                normal = m[:3, :3] @ face.normal
                for j in range(fh):
                    for i in range(fw):
                        a = (i + 0.5) / fw
                        b = (j + 0.5) / fh
                        p = face.origin + face.u_axis * face.u_len * a + face.v_axis * face.v_len * b
                        p = (m @ np.append(p, 1.0))[:3]
                        img[fy + j, fx + i] = painter(p, normal, cube, face, (a, b))
        Image.fromarray(img, 'RGBA').save(out_path)
        return img

    # ------------------------------------------------------------------ Epic Fight export

    def joint_order(self):
        order = []

        def walk(b):
            order.append(b)
            for c in b.children:
                walk(c)
        walk(self.root)
        return order

    def export_mesh(self, path):
        tw, th = self.texture_size
        joints = self.joint_order()
        index = {b.name: i for i, b in enumerate(joints)}
        positions, normals, uvs, vcounts, vindices, tris = [], [], [], [], [], []
        for cube in self.cubes:
            for face in cube.faces:
                corners, normal = cube.world_face(face)
                fx, fy, fw, fh = face.rect
                face_uv = [(fx, fy), (fx + fw, fy), (fx + fw, fy + fh), (fx, fy + fh)]
                ni = len(normals) // 3
                normals.extend(BLENDER_FROM_MC[:3, :3] @ (normal / np.linalg.norm(normal)))
                base_p = len(positions) // 3
                base_t = len(uvs) // 2
                for c, (u, v) in zip(corners, face_uv):
                    positions.extend(BLENDER_FROM_MC[:3, :3] @ (c * PX))
                    uvs.extend((u / tw, v / th))
                    vcounts.append(1)
                    vindices.extend((index[cube.bone.name], 0))
                for k in (0, 1, 2, 0, 2, 3):
                    tris.extend((base_p + k, base_t + k, ni))
        root = joints[0]

        def hierarchy(b):
            local = b.rest_local()
            if b is root:
                local = BLENDER_FROM_MC @ local
            return {'name': b.name, 'transform': _row_major(local), 'children': [hierarchy(c) for c in b.children]}

        data = {
            'vertices': {
                'positions': {'stride': 3, 'count': len(positions) // 3, 'array': _round(positions)},
                'uvs': {'stride': 2, 'count': len(uvs) // 2, 'array': _round(uvs)},
                'normals': {'stride': 3, 'count': len(normals) // 3, 'array': _round(normals)},
                'vcounts': {'stride': 1, 'count': len(vcounts), 'array': vcounts},
                'weights': {'stride': 1, 'count': 1, 'array': [1.0]},
                'vindices': {'stride': 1, 'count': len(vindices), 'array': vindices},
                'parts': {'body': {'stride': 3, 'count': len(tris) // 3, 'array': tris}},
            },
            'armature': {
                'joints': [b.name for b in joints],
                'hierarchy': [hierarchy(root)],
            },
        }
        _write_json(path, data)

    # ------------------------------------------------------------------ posing (shared by export and previews)

    def pose_matrices(self, pose):
        """Model-space matrix of every bone (blocks) for a pose {bone: (rotation_deg, offset_px)}."""
        world = {}

        def walk(b, parent):
            world[b.name] = parent @ self.animated_local(b, pose)
            for c in b.children:
                walk(c, world[b.name])
        walk(self.root, np.eye(4))
        return world

    def animated_local(self, bone, pose):
        rot, off = pose.get(bone.name, ((0, 0, 0), (0, 0, 0)))
        return bone.rest_local() @ translation(np.array(off, dtype=float) * PX) @ rotation(rot)

    def bind_inverse(self):
        return {b: np.linalg.inv(m) for b, m in self.pose_matrices({}).items()}

    # ------------------------------------------------------------------ previews

    def render(self, texture, pose=None, yaw=35.0, pitch=20.0, size=420, scale=None, background=(40, 44, 52)):
        """Software render (orthographic, z-buffered, lit) of the model in a pose, for checking by eye."""
        tex = np.asarray(texture)
        th, tw = tex.shape[:2]
        mats = self.pose_matrices(pose or {})
        inv = self.bind_inverse()
        view = rotation((pitch, 0, 0)) @ rotation((0, yaw, 0))
        quads = []
        for cube in self.cubes:
            skin = mats[cube.bone.name] @ inv[cube.bone.name]
            for face in cube.faces:
                corners, normal = cube.world_face(face)
                pts = [(view @ skin @ np.append(c * PX, 1.0))[:3] for c in corners]
                n = (view @ skin)[:3, :3] @ normal
                quads.append((pts, n / np.linalg.norm(n), face))
        allpts = np.array([p for q in quads for p in q[0]])
        lo, hi = allpts.min(0), allpts.max(0)
        span = scale or max(hi[0] - lo[0], hi[1] - lo[1]) * 1.15
        cx, cy = (lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2
        img = np.zeros((size, size, 3), dtype=float)
        img[:] = background
        zbuf = np.full((size, size), -1e9)
        light = np.array([-0.4, 0.8, -0.45])
        light /= np.linalg.norm(light)
        for pts, n, face in quads:
            fx, fy, fw, fh = face.rect
            uv = [(fx, fy), (fx + fw, fy), (fx + fw, fy + fh), (fx, fy + fh)]
            scr = [((p[0] - cx) / span * size + size / 2, size / 2 - (p[1] - cy) / span * size, -p[2]) for p in pts]
            shade = 0.55 + 0.45 * max(0.0, float(n @ light))
            for tri in ((0, 1, 2), (0, 2, 3)):
                _raster(img, zbuf, [scr[k] for k in tri], [uv[k] for k in tri], tex, tw, th, shade)
        return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), 'RGB')


def _raster(img, zbuf, s, uv, tex, tw, th, shade):
    (x0, y0, z0), (x1, y1, z1), (x2, y2, z2) = s
    minx, maxx = int(max(0, math.floor(min(x0, x1, x2)))), int(min(img.shape[1] - 1, math.ceil(max(x0, x1, x2))))
    miny, maxy = int(max(0, math.floor(min(y0, y1, y2)))), int(min(img.shape[0] - 1, math.ceil(max(y0, y1, y2))))
    if minx > maxx or miny > maxy:
        return
    area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
    if abs(area) < 1e-9:
        return
    ys, xs = np.mgrid[miny:maxy + 1, minx:maxx + 1] + 0.5
    w0 = ((x1 - xs) * (y2 - ys) - (x2 - xs) * (y1 - ys)) / area
    w1 = ((x2 - xs) * (y0 - ys) - (x0 - xs) * (y2 - ys)) / area
    w2 = 1 - w0 - w1
    inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
    if not inside.any():
        return
    z = w0 * z0 + w1 * z1 + w2 * z2
    u = w0 * uv[0][0] + w1 * uv[1][0] + w2 * uv[2][0]
    v = w0 * uv[0][1] + w1 * uv[1][1] + w2 * uv[2][1]
    ui = np.clip(u.astype(int), 0, tw - 1)
    vi = np.clip(v.astype(int), 0, th - 1)
    texel = tex[vi, ui]
    region = zbuf[miny:maxy + 1, minx:maxx + 1]
    draw = inside & (z > region) & (texel[..., 3] > 127)
    region[draw] = z[draw]
    img[miny:maxy + 1, minx:maxx + 1][draw] = texel[..., :3][draw] * shade


def _row_major(m):
    return [round(float(v), 6) for v in np.asarray(m).reshape(-1)]


def _round(values):
    return [round(float(v), 6) for v in values]


def _write_json(path, data):
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(data, f, separators=(',', ':'))
