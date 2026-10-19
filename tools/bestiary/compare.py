"""
A creature's model beside its reference images, to judge it at a glance: references on the left, the model from three
angles in the middle, in-game screenshots on the right (cropped around the creature when a box is given).

    python compare.py creature OUT.png --refs a.png b.png [--game shot.png[@x0,y0,x1,y1] ...]

References are anime or manga images: they stay outside the repository and are never shipped.
"""
import argparse
import importlib

from PIL import Image, ImageDraw

H = 420


def fit(img, height=H):
    img = img.convert('RGB')
    return img.resize((max(1, int(img.width * height / img.height)), height), Image.LANCZOS)


def labelled(img, text):
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 8 * len(text) + 12, 20), fill=(20, 20, 24))
    d.text((6, 4), text, fill=(240, 240, 240))
    return img


def main():
    p = argparse.ArgumentParser()
    p.add_argument('creature')
    p.add_argument('out')
    p.add_argument('--refs', nargs='*', default=[])
    p.add_argument('--game', nargs='*', default=[])
    a = p.parse_args()
    model, painter, _ = importlib.import_module(a.creature).build()
    import tempfile, os
    tex_path = os.path.join(tempfile.gettempdir(), a.creature + '_compare_tex.png')
    texture = Image.fromarray(model.paint(painter, tex_path), 'RGBA')
    tiles = [labelled(fit(Image.open(r)), 'reference') for r in a.refs]
    for yaw, pitch, name in ((35, 12, 'model 3/4'), (90, 0, 'model side'), (-20, 8, 'model front')):
        tiles.append(labelled(model.render(texture, yaw=yaw, pitch=pitch, size=H, background=(232, 232, 236)), name))
    for g in a.game:
        path, _, box = g.partition('@')
        img = Image.open(path)
        if box:
            img = img.crop(tuple(int(v) for v in box.split(',')))
        tiles.append(labelled(fit(img), 'in game'))
    width = sum(t.width for t in tiles) + 8 * (len(tiles) - 1)
    sheet = Image.new('RGB', (width, H), (255, 255, 255))
    x = 0
    for t in tiles:
        sheet.paste(t, (x, 0))
        x += t.width + 8
    if sheet.width > 3000:
        sheet = sheet.resize((3000, int(H * 3000 / sheet.width)), Image.LANCZOS)
    sheet.save(a.out)


if __name__ == '__main__':
    main()
