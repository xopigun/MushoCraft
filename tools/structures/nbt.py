"""
A minimal NBT writer (Minecraft's binary format), enough to write structure templates. Values are written by type:
Byte, Short, Int, Long, Float, Double wrap numbers; str is a String tag, dict a Compound, list a List (of one type).
"""
import gzip
import struct


class Byte(int):
    TAG = 1


class Short(int):
    TAG = 2


class Int(int):
    TAG = 3


class Long(int):
    TAG = 4


class Float(float):
    TAG = 5


class Double(float):
    TAG = 6


def _tag(value):
    if isinstance(value, (Byte, Short, Int, Long, Float, Double)):
        return value.TAG
    if isinstance(value, bool):
        return 1
    if isinstance(value, int):
        return 3
    if isinstance(value, float):
        return 6
    if isinstance(value, str):
        return 8
    if isinstance(value, list):
        return 9
    if isinstance(value, dict):
        return 10
    raise TypeError(type(value))


def _payload(out, value):
    tag = _tag(value)
    if tag == 1:
        out += struct.pack('>b', int(value))
    elif tag == 2:
        out += struct.pack('>h', value)
    elif tag == 3:
        out += struct.pack('>i', value)
    elif tag == 4:
        out += struct.pack('>q', value)
    elif tag == 5:
        out += struct.pack('>f', value)
    elif tag == 6:
        out += struct.pack('>d', value)
    elif tag == 8:
        data = value.encode('utf-8')
        out += struct.pack('>H', len(data)) + data
    elif tag == 9:
        inner = _tag(value[0]) if value else 0
        out += struct.pack('>bi', inner, len(value))
        for item in value:
            _payload(out, item)
    elif tag == 10:
        for key, item in value.items():
            name = key.encode('utf-8')
            out += struct.pack('>bH', _tag(item), len(name)) + name
            _payload(out, item)
        out += b'\x00'


def write(path, root, name=''):
    out = bytearray()
    data = name.encode('utf-8')
    out += struct.pack('>bH', 10, len(data)) + data
    _payload(out, root)
    with gzip.open(path, 'wb') as f:
        f.write(bytes(out))
