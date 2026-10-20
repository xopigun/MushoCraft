"""
A structure template under construction: blocks with their states, block entities and entities, written as a Minecraft
structure file (data/<ns>/structure/<name>.nbt). Coordinates are relative to the template's corner; x east, y up, z south.
Glass panes, fences and bars get their side connections from their neighbours when the template is written, since a
template is placed without updating shapes.
"""
import nbt
from nbt import Double, Int

DATA_VERSION = 3955  # Minecraft 1.21.1

CONNECTING = ('_pane', '_fence', 'iron_bars')
SOLID_HINTS = ('planks', 'log', 'concrete', 'terracotta', 'brick', 'stone', 'quartz', 'calcite', 'glass', 'wood', 'copper',
               'deepslate', 'pillar', 'wool')


class Template:
    def __init__(self, size):
        self.size = size
        self.blocks = {}
        self.block_nbt = {}
        self.entities = []

    def set(self, x, y, z, block, **props):
        if 0 <= x < self.size[0] and 0 <= y < self.size[1] and 0 <= z < self.size[2]:
            self.blocks[(x, y, z)] = (block if ':' in block else 'minecraft:' + block, props)
            self.block_nbt.pop((x, y, z), None)

    def get(self, x, y, z):
        return self.blocks.get((x, y, z), (None, {}))[0]

    def fill(self, x0, y0, z0, x1, y1, z1, block, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, block, **props)

    def clear(self, x0, y0, z0, x1, y1, z1):
        self.fill(x0, y0, z0, x1, y1, z1, 'air')

    def walls(self, x0, y0, z0, x1, y1, z1, block, **props):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    if x in (x0, x1) or z in (z0, z1):
                        self.set(x, y, z, block, **props)

    def block_entity(self, x, y, z, data):
        self.block_nbt[(x, y, z)] = data

    def entity(self, x, y, z, data):
        """An entity standing at block (x, y, z) (feet at the block's bottom centre)."""
        self.entities.append({
            'pos': [Double(x + 0.5), Double(y), Double(z + 0.5)],
            'blockPos': [Int(x), Int(y), Int(z)],
            'nbt': data,
        })

    def _connect(self):
        for (x, y, z), (block, props) in list(self.blocks.items()):
            if not any(block.endswith(c) for c in CONNECTING):
                continue
            for side, (dx, dz) in (('north', (0, -1)), ('south', (0, 1)), ('east', (1, 0)), ('west', (-1, 0))):
                other = self.get(x + dx, y, z + dz)
                linked = other is not None and other != 'minecraft:air' and (
                    any(other.endswith(c) for c in CONNECTING) or any(h in other for h in SOLID_HINTS))
                props.setdefault(side, 'true' if linked else 'false')

    def write(self, path):
        self._connect()
        palette, index, blocks = [], {}, []
        for pos, (block, props) in sorted(self.blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
            key = (block, tuple(sorted(props.items())))
            if key not in index:
                index[key] = len(palette)
                entry = {'Name': block}
                if props:
                    entry['Properties'] = {k: str(v) for k, v in props.items()}
                palette.append(entry)
            item = {'pos': [Int(pos[0]), Int(pos[1]), Int(pos[2])], 'state': Int(index[key])}
            if pos in self.block_nbt:
                item['nbt'] = self.block_nbt[pos]
            blocks.append(item)
        root = {
            'DataVersion': Int(DATA_VERSION),
            'size': [Int(v) for v in self.size],
            'palette': palette,
            'blocks': blocks,
            'entities': self.entities,
        }
        nbt.write(path, root)

    # ---- previews: a quick isometric look at the build, from its block colours

    def preview(self, path, colours, scale=8):
        from PIL import Image, ImageDraw
        sx, sy, sz = self.size
        w = (sx + sz) * scale + 20
        h = (sx + sz) * scale // 2 + sy * scale + 20
        img = Image.new('RGB', (w, h), (235, 238, 242))
        d = ImageDraw.Draw(img)
        ox, oy = sz * scale + 10, sy * scale + 10
        for (x, y, z), (block, _) in sorted(self.blocks.items(), key=lambda kv: (kv[0][0] + kv[0][2], kv[0][1])):
            if block == 'minecraft:air':
                continue
            c = colours(block)
            if c is None:
                continue
            px = ox + (x - z) * scale
            py = oy + (x + z) * scale // 2 - y * scale
            top = [(px, py), (px + scale, py + scale // 2), (px, py + scale), (px - scale, py + scale // 2)]
            left = [(px - scale, py + scale // 2), (px, py + scale), (px, py + 2 * scale), (px - scale, py + scale + scale // 2)]
            right = [(px + scale, py + scale // 2), (px, py + scale), (px, py + 2 * scale), (px + scale, py + scale + scale // 2)]
            d.polygon(left, fill=tuple(int(v * 0.72) for v in c))
            d.polygon(right, fill=tuple(int(v * 0.86) for v in c))
            d.polygon(top, fill=c)
        img.save(path)


def text(value):
    return '{"text":"%s"}' % value
