#!/usr/bin/env python

import os
import sys

def main():
    dirs = ['.'] if len(sys.argv) < 2 else sys.argv[1:]

    cnames = []
    for dir in dirs:
        for root, _, files in os.walk(dir):
            for f in files:
                if not (f.endswith(".java") or f.endswith('.xml')):
                    continue
                p = os.path.join(root, f)
                with open(p, "rb") as jf:
                    content = jf.read()
                if f.endswith('.xml'):
                    cnames.append(('', content, p))
                else:
                    cname = f[0:-5]
                    cnames.append((cname, content, p))

    for i in range(len(cnames)):
        x = cnames[i]
        cname = x[0]
        if cname == '':
            continue
        found = False
        for j in range(len(cnames)):
            if i == j:
                continue
            if cnames[j][1].find(cname) >= 0:
                found = True
                break
        if not found:
            print cname, x[2]


if __name__ == "__main__":
    main()
