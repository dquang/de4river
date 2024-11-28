#!/usr/bin/env python

""" Add unique numbers in front of properties values
    to identfy the key without knowing the real key.
"""

import sys
import re
import os

BLACK_LISTED_KEYS = [
    re.compile(r".*\.file$")
]

BLACK_LISTED_VALUES = [
    re.compile(r"^http.*$")
]

NUMBERED  = re.compile(r"^\s*([^\s]+)\s*=\s*\[([0-9a-zA-Z]+)\]\s*(.+)$")
UNUMBERED = re.compile(r"^\s*([^\s]+)\s*=\s*(.+)$")

ALPHA = "0123456789" \
        "abcdefghijklmnopqrstuvwxyz" \
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

def decode_ibase62(s):
    t, c = 0, 1
    for x in s[::-1]:
        i = ALPHA.find(x)
        t += i*c
        c *= len(ALPHA)
    return t

def ibase62(i):
    if i == 0:
        return "0"
    out = []
    if i < 0:
        out.append("-")
        i = -1
    while i > 0:
        out.append(ALPHA[i % len(ALPHA)])
        i //= len(ALPHA)
    out.reverse()
    return ''.join(out)

def is_blacklisted(key, value):

    for bl in BLACK_LISTED_KEYS:
        if bl.match(key):
            return True

    for bl in BLACK_LISTED_VALUES:
        if bl.match(value):
            return True

    return False

def find_key(already_numbered, value):
    for k, v in already_numbered.iteritems():
        if v == value:
            return k
    return None

def decorated_content(infile, outfile, already_numbered):

    for line in infile:
        line = line.strip()
        m = NUMBERED.match(line)
        if m:
            key, num, value = m.groups()
            decoded_num = decode_ibase62(num)
            last = find_key(already_numbered, decoded_num)
            if last is None:
                already_numbered[key] = decoded_num
            elif last != key:
                print >> sys.stderr,  "WARN: Number clash: " \
                    "%s leeds to '%s' and '%s'" % (num, key, last)
            print >> outfile, line
            continue

        m = UNUMBERED.match(line)
        if m:
            key, value = m.groups(1)
            if is_blacklisted(key, value):
                print >> outfile, line
            else:
                num = already_numbered.setdefault(key, len(already_numbered))
                print >> outfile, "%s=[%s] %s" % (key, ibase62(num), m.group(2))
            continue
        print >> outfile, line

def tmp_fname(fname):
    name = fname + ".tmp"
    i = 0
    while os.path.exists(name):
        name = "%s.tmp%d" % (fname, i)
        i += 1
    return name

def decorate_file(fname, already_numbered):

    tmp = tmp_fname(fname)

    with open(fname, "r") as infile:
        with open(tmp, "w") as outfile:
            decorated_content(infile, outfile, already_numbered)

    os.rename(tmp, fname)

def main():
    already_numbered = {}
    for fname in sys.argv[1:]:
        print >> sys.stderr, "checking %s" % fname
        decorate_file(fname, already_numbered)

if __name__ == "__main__":
    main()
