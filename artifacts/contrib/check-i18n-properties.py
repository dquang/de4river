#!/usr/bin/env python

import sys
import re

SPLIT_RE = re.compile(r"^\s*([^=]+)=\s*(.*)\s*")

def load_properties_file(filename):
    props = {}
    with open(filename, "r") as f:
        while True:
            line = f.readline()
            if not line: break
            m = SPLIT_RE.match(line)
            if not m: continue
            k = m.group(1).strip()
            v = m.group(2).strip()
            if k in props:
                print >> sys.stderr, "'%s' found more than once in '%s'." % (
                    k, filename)
            else:
                props[k] = v
    return props

def main():

    props = [(arg, load_properties_file(arg)) for arg in sys.argv[1:]]

    l = len(props)

    for i in range(0, l):
        a = props[i][1]
        for j in range(i+1, l):
            b = props[j][1]
            for k in a.iterkeys():
                if k not in b:
                    print >> sys.stderr, "'%s' found in '%s' but not in '%s'." % (
                        k, props[i][0], props[j][0])
            for k in b.iterkeys():
                if k not in a:
                    print >> sys.stderr, "'%s' found in '%s' but not in '%s'." % (
                        k, props[j][0], props[i][0])
        
if __name__ == '__main__':
    main()
