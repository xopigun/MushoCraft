"""
Builds every creature: mesh and armature, texture, animations into the mod's resources, and preview sheets.

    python build.py [creature ...] [--previews DIR]
"""
import argparse
import importlib
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'assets', 'mushocraft')
CREATURES = ['assault_dog', 'pax_coyote', 'terminate_boar', 'luster_grizzly', 'goblin', 'wraith', 'armored_warrior', 'mad_skull']


def build(name, previews):
    module = importlib.import_module(name)
    model, painter, clips = module.build()
    mesh_dir = os.path.join(ASSETS, 'animmodels', 'entity')
    anim_dir = os.path.join(ASSETS, 'animmodels', 'animations', name)
    tex_dir = os.path.join(ASSETS, 'textures', 'entity', 'monster')
    for d in (mesh_dir, anim_dir, tex_dir):
        os.makedirs(d, exist_ok=True)
    texture = model.paint(painter, os.path.join(tex_dir, name + '.png'))
    model.export_mesh(os.path.join(mesh_dir, name + '.json'))
    for clip in clips:
        clip.export(model, os.path.join(anim_dir, clip.name + '.json'))
    if previews:
        os.makedirs(previews, exist_ok=True)
        sheet(model, texture, clips, os.path.join(previews, name))
    print(name, 'texture', model.texture_size, 'cubes', len(model.cubes), 'clips', [c.name for c in clips])


def sheet(model, texture, clips, prefix):
    tex = Image.fromarray(texture, 'RGBA')
    views = [model.render(tex, yaw=y, pitch=p, size=360) for y, p in ((35, 15), (-35, 15), (90, 0), (180, 10), (0, 0), (0, 70))]
    out = Image.new('RGB', (360 * 3, 360 * 2))
    for i, v in enumerate(views):
        out.paste(v, ((i % 3) * 360, (i // 3) * 360))
    out.save(prefix + '_views.png')
    tex.resize((tex.width * 2, tex.height * 2), Image.NEAREST).save(prefix + '_texture.png')
    for clip in clips:
        frames = 8
        strip = Image.new('RGB', (300 * frames, 300))
        span = None
        for i in range(frames):
            t = clip.length * i / (frames - 1 if not clip.loop else frames)
            img = model.render(tex, pose=clip.pose(t), yaw=60, pitch=12, size=300, scale=3.2)
            strip.paste(img, (i * 300, 0))
        strip.save('%s_anim_%s.png' % (prefix, clip.name))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('creatures', nargs='*', default=CREATURES)
    parser.add_argument('--previews')
    args = parser.parse_args()
    for creature in args.creatures:
        build(creature, args.previews)
