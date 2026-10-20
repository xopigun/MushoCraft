"""
Writes MushoCraft's structure templates into the mod's data, and isometric previews.

    python build.py [--previews DIR]
"""
import argparse
import os

import buildings

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, '..', '..', 'src', 'main', 'resources', 'data', 'mushocraft', 'structure')


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--previews')
    a = p.parse_args()
    os.makedirs(DATA, exist_ok=True)
    built = {'%s_dojo' % style: buildings.dojo(style) for style in buildings.STYLES}
    built['milis_church'] = buildings.church()
    for name, template in built.items():
        template.write(os.path.join(DATA, name + '.nbt'))
        if a.previews:
            os.makedirs(a.previews, exist_ok=True)
            template.preview(os.path.join(a.previews, name + '.png'), buildings.colour)
        print(name, template.size, len(template.blocks), 'blocks', len(template.entities), 'entities')


if __name__ == '__main__':
    main()
