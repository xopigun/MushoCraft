"""
MushoCraft's places, drawn after the anime and manga (references stay outside the repository):
- the dojo of a sword style, after the Sword God Style Dojo of the Holy Land of Swords (anime S3, manga): a hall of dark
  timber frames and white walls under a steep grey gabled roof, a shrine shelf at the back, the style's banners;
- the church of Milis, after the anime (S3 ep. 5): white stone, blue-green roofs, a rose window, a bell tower with a
  spire carrying the Milis cross; inside, columns, pews either side of a blue carpet, an altar.
"""
from nbt import Byte, Float, Int
from template import Template

STYLES = {
    'sword_god': 'red',
    'water_god': 'blue',
    'north_god': 'black',
}


def dojo(style):
    t = Template((19, 14, 15))
    # stone platform and steps
    t.fill(0, 0, 0, 18, 0, 14, 'stone_bricks')
    t.fill(1, 1, 1, 17, 1, 13, 'spruce_slab', type='bottom')
    for x in range(8, 11):
        t.set(x, 0, 14, 'stone_brick_stairs', facing='north', half='bottom')
    # floor and walls
    t.fill(2, 1, 2, 16, 1, 12, 'spruce_planks')
    t.walls(2, 1, 2, 16, 6, 12, 'white_concrete')
    t.walls(2, 1, 2, 16, 1, 12, 'dark_oak_planks')
    t.walls(2, 5, 2, 16, 5, 12, 'dark_oak_planks')
    for x in (2, 6, 9, 12, 16):
        for z in (2, 12):
            t.fill(x, 1, z, x, 6, z, 'dark_oak_log', axis='y')
    for z in (2, 7, 12):
        for x in (2, 16):
            t.fill(x, 1, z, x, 6, z, 'dark_oak_log', axis='y')
    t.walls(2, 6, 2, 16, 6, 12, 'dark_oak_planks')
    # paper windows in the side and back panels
    for (x0, x1) in ((3, 5), (13, 15)):
        for z in (2, 12):
            t.fill(x0, 3, z, x1, 3, z, 'white_stained_glass_pane')
    for z0, z1 in ((3, 6), (8, 11)):
        for x in (2, 16):
            t.fill(x, 3, z0 + 1, x, 3, z1 - 1, 'white_stained_glass_pane')
    # the wide entrance, open on the courtyard, lanterns either side
    t.clear(7, 2, 12, 11, 4, 12)
    t.set(6, 5, 13, 'lantern', hanging='true')
    t.set(12, 5, 13, 'lantern', hanging='true')
    # ceiling and hanging lanterns
    t.fill(3, 6, 3, 15, 6, 11, 'spruce_planks')
    for x, z in ((5, 5), (13, 5), (5, 9), (13, 9), (9, 7)):
        t.set(x, 5, z, 'lantern', hanging='true')
    # steep gabled roof of grey tiles, overhanging eaves, ridge along the hall
    for z in range(0, 15):
        rise = z if z <= 7 else 14 - z
        y = 6 + rise
        for x in range(0, 19):
            if z == 7:
                t.set(x, y, z, 'deepslate_tile_slab', type='bottom')
                t.set(x, y - 1, z, 'deepslate_tiles')
            else:
                t.set(x, y, z, 'deepslate_tile_stairs', facing='south' if z < 7 else 'north', half='bottom')
    # gable ends: white plaster framed in dark timber
    for z in range(3, 12):
        top = 6 + (z if z <= 7 else 14 - z) - 1
        for x in (2, 16):
            t.fill(x, 7, z, x, top, z, 'white_concrete')
    for x in (2, 16):
        t.fill(x, 7, 7, x, 12, 7, 'dark_oak_log', axis='y')
    # shrine shelf at the back with offerings, the style's banners
    banner = STYLES[style]
    t.fill(7, 4, 3, 11, 4, 3, 'dark_oak_slab', type='top')
    t.set(8, 5, 3, 'lantern')
    t.set(10, 5, 3, 'lantern')
    t.set(9, 5, 3, 'flower_pot')
    for x in (5, 13):
        t.set(x, 4, 3, banner + '_wall_banner', facing='south')
    # practice floor marks and benches by the walls
    for x in range(4, 15):
        t.set(x, 1, 4, 'stripped_spruce_wood', axis='x')
    for z in (5, 8):
        t.set(3, 2, z, 'spruce_stairs', facing='west', half='bottom')
        t.set(15, 2, z, 'spruce_stairs', facing='east', half='bottom')
    # weapon racks: armour stands with practice swords, the master at the shrine
    for x, z, item in ((4, 10, 'minecraft:wooden_sword'), (14, 10, 'minecraft:stone_sword')):
        t.entity(x, 2, z, {
            'id': 'minecraft:armor_stand',
            'ShowArms': Byte(1),
            'NoBasePlate': Byte(1),
            'Rotation': [Float(180.0), Float(0.0)],
            'HandItems': [{'id': item, 'count': Int(1)}, {}],
        })
    t.entity(9, 2, 5, {
        'id': 'mushocraft:sword_master',
        'style': style,
        'resident': Byte(1),
        'PersistenceRequired': Byte(1),
        'Rotation': [Float(0.0), Float(0.0)],
    })
    return t


def church():
    t = Template((20, 32, 28))
    W0, W1 = 1, 13          # nave side walls
    Z0, Z1 = 2, 24          # back (apse) and front walls
    WALL_TOP = 10
    t.fill(0, 0, 0, 19, 0, 27, 'smooth_quartz')
    for z in range(25, 28):
        t.fill(5, 0, z, 9, 0, z, 'quartz_bricks')
    t.fill(W0, 1, Z0, W1, 1, Z1, 'calcite')
    t.walls(W0, 1, Z0, W1, WALL_TOP, Z1, 'quartz_bricks')
    # buttresses along the sides
    for z in range(Z0, Z1 + 1, 4):
        for x in (W0 - 1, W1 + 1):
            t.fill(x, 1, z, x, WALL_TOP - 2, z, 'quartz_pillar', axis='y')
    # tall pointed windows of pale blue glass
    for z in range(Z0 + 3, Z1 - 1, 4):
        for x in (W0, W1):
            t.fill(x, 3, z, x, 7, z, 'light_blue_stained_glass_pane')
            t.set(x, 8, z, 'cyan_stained_glass_pane')
    # the Milis cross window in the apse
    cx = 7
    for y in range(4, 9):
        t.set(cx, y, Z0, 'light_blue_stained_glass')
    for x in (cx - 1, cx + 1):
        t.set(x, 7, Z0, 'light_blue_stained_glass')
    for x, y in ((cx - 1, 8), (cx + 1, 8), (cx - 1, 6), (cx + 1, 6)):
        t.set(x, y, Z0, 'cyan_stained_glass')
    # front: an arched doorway and a rose window
    t.clear(6, 2, Z1, 8, 5, Z1)
    t.set(6, 5, Z1, 'quartz_stairs', facing='east', half='top')
    t.set(8, 5, Z1, 'quartz_stairs', facing='west', half='top')
    t.set(7, 2, Z1, 'air')
    for dx in range(-2, 3):
        for dy in range(-2, 3):
            if dx * dx + dy * dy <= 5:
                t.set(cx + dx, 13 + dy, Z1, 'cyan_stained_glass' if dx * dx + dy * dy <= 1 else 'light_blue_stained_glass')
    # steep roof of weathered copper, gable ends of white stone
    for x in range(0, 15):
        rise = x if x <= 7 else 14 - x
        y = WALL_TOP + 1 + rise
        for z in range(Z0 - 1, Z1 + 2):
            if x == 7:
                t.set(x, y, z, 'oxidized_cut_copper_slab', type='bottom')
                t.set(x, y - 1, z, 'oxidized_cut_copper')
            else:
                t.set(x, y, z, 'oxidized_cut_copper_stairs', facing='east' if x < 7 else 'west', half='bottom')
    for x in range(W0 + 1, W1):
        top = WALL_TOP + (x if x <= 7 else 14 - x)
        for z in (Z0, Z1):
            for y in range(WALL_TOP + 1, top + 1):
                if t.get(x, y, z) is None or 'glass' not in t.get(x, y, z):
                    t.set(x, y, z, 'quartz_bricks')
    for dx in range(-2, 3):
        for dy in range(-2, 3):
            if dx * dx + dy * dy <= 5:
                t.set(cx + dx, 13 + dy, Z1, 'cyan_stained_glass' if dx * dx + dy * dy <= 1 else 'light_blue_stained_glass')
    # bell tower at the front corner, rising above the roof, its spire carrying the Milis cross
    TX0, TX1, TZ0, TZ1 = 14, 18, 18, 22
    TOP = 21
    t.fill(TX0, 1, TZ0, TX1, TOP, TZ1, 'quartz_bricks')
    t.fill(TX0 + 1, 1, TZ0 + 1, TX1 - 1, TOP - 1, TZ1 - 1, 'air')
    for y in (17, 18, 19):
        for x in range(TX0 + 1, TX1):
            t.set(x, y, TZ1, 'light_blue_stained_glass_pane')
            t.set(x, y, TZ0, 'light_blue_stained_glass_pane')
        for z in range(TZ0 + 1, TZ1):
            t.set(TX1, y, z, 'light_blue_stained_glass_pane')
    t.set(16, 19, 20, 'bell', attachment='ceiling', facing='north')
    t.set(16, 20, 20, 'chain', axis='y')
    for level in range(0, 3):
        y = TOP + 1 + level
        t.walls(TX0 + level, y, TZ0 + level, TX1 - level, y, TZ1 - level, 'oxidized_cut_copper')
    t.fill(16, TOP + 1, 20, 16, TOP + 6, 20, 'oxidized_cut_copper')
    t.set(16, TOP + 7, 20, 'end_rod', facing='up')
    t.set(16, TOP + 8, 20, 'end_rod', facing='up')
    t.set(15, TOP + 8, 20, 'end_rod', facing='west')
    t.set(17, TOP + 8, 20, 'end_rod', facing='east')
    t.set(16, TOP + 9, 20, 'end_rod', facing='up')
    # a door from the nave into the tower, and its own floor
    t.fill(TX0 + 1, 1, TZ0 + 1, TX1 - 1, 1, TZ1 - 1, 'calcite')
    t.clear(W1, 2, 20, W1 + 1, 3, 20)
    t.clear(W1 + 1, 2, 20, TX0, 3, 20)
    # inside: columns, pews, the blue carpet, the altar, chandeliers
    for z in range(Z0 + 4, Z1 - 2, 4):
        for x in (3, 11):
            t.fill(x, 2, z, x, WALL_TOP - 1, z, 'quartz_pillar', axis='y')
    for z in range(Z0 + 3, Z1):
        t.set(7, 2, z, 'blue_carpet')
    for z in range(Z0 + 6, Z1 - 2, 2):
        for x in list(range(4, 7)) + list(range(8, 11)):
            t.set(x, 2, z, 'spruce_stairs', facing='south', half='bottom')
    t.fill(5, 2, Z0 + 2, 9, 2, Z0 + 2, 'quartz_slab', type='bottom')
    t.fill(6, 2, Z0 + 1, 8, 2, Z0 + 1, 'quartz_block')
    t.set(7, 3, Z0 + 1, 'white_carpet')
    t.set(6, 3, Z0 + 1, 'candle', candles='3', lit='true')
    t.set(8, 3, Z0 + 1, 'candle', candles='3', lit='true')
    t.set(7, 3, Z0 + 3, 'lectern', facing='south')
    for z in (8, 14, 20):
        t.set(7, WALL_TOP + 3, z, 'chain', axis='y')
        t.set(7, WALL_TOP + 2, z, 'chain', axis='y')
        t.set(7, WALL_TOP + 1, z, 'lantern', hanging='true')
    # the two tutors of the church: healing and divine strike, by the altar
    for x, school in ((5, 'healing'), (9, 'divine_strike')):
        t.entity(x, 2, Z0 + 4, {
            'id': 'mushocraft:magic_tutor',
            'school': school,
            'resident': Byte(1),
            'PersistenceRequired': Byte(1),
            'Rotation': [Float(0.0), Float(0.0)],
        })
    return t


COLOURS = {
    'stone_bricks': (122, 122, 122), 'spruce_slab': (110, 80, 50), 'stone_brick_stairs': (122, 122, 122),
    'spruce_planks': (114, 84, 52), 'white_concrete': (226, 228, 228), 'dark_oak_planks': (66, 43, 20),
    'dark_oak_log': (60, 46, 30), 'white_stained_glass_pane': (240, 240, 240), 'lantern': (230, 180, 90),
    'deepslate_tile_slab': (54, 54, 58), 'deepslate_tiles': (54, 54, 58), 'deepslate_tile_stairs': (64, 64, 70),
    'dark_oak_slab': (66, 43, 20), 'stripped_spruce_wood': (120, 92, 60), 'spruce_stairs': (114, 84, 52),
    'smooth_quartz': (236, 230, 222), 'quartz_bricks': (234, 228, 220), 'calcite': (222, 224, 220),
    'quartz_pillar': (238, 232, 226), 'light_blue_stained_glass_pane': (110, 170, 220), 'cyan_stained_glass_pane': (60, 140, 160),
    'light_blue_stained_glass': (110, 170, 220), 'cyan_stained_glass': (60, 140, 160), 'quartz_stairs': (234, 228, 220),
    'oxidized_cut_copper_slab': (82, 162, 132), 'oxidized_cut_copper': (82, 162, 132), 'oxidized_cut_copper_stairs': (90, 172, 140),
    'end_rod': (250, 245, 230), 'blue_carpet': (50, 60, 160), 'quartz_slab': (234, 228, 220), 'quartz_block': (236, 230, 222),
    'bell': (220, 180, 60), 'chain': (60, 64, 76),
}


def colour(block):
    name = block.split(':')[1]
    if name.endswith('_wall_banner'):
        return {'red': (170, 40, 40), 'blue': (50, 60, 160), 'black': (30, 30, 34)}[name.split('_')[0]]
    return COLOURS.get(name, (180, 180, 180))
