#!/usr/bin/env python

import os
import re
import sys

KEY_RE = re.compile(r"^\s*([^\s=]+)\s*=.*$")

def main():
    content = []
    for root, dirs, files in os.walk('.'):
        for f in files:
            if not (f.endswith(".java") or f.endswith(".xml")):
                continue
            p = os.path.join(root, f)
            with open(p, "rb") as jf:
                content.append(jf.read())

    content = ''.join(content)

    for arg in sys.argv[1:]:
        with open(arg, "rb") as prop:
            for line in prop:
                m = KEY_RE.match(line)
                if not m:
                    continue
                key = m.group(1)
                if content.find(key) == -1:
                    print key

if __name__ == "__main__":
    main()
