/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend.utils;

import org.dive4elements.river.importer.XY; // TODO: Move to a more common package.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DouglasPeuker
{
    public static final double EPSILON = 1e-4;

    private DouglasPeuker() {
    }

    public static List<XY> simplify(List<XY> input) {
        return simplify(input, EPSILON);
    }

    public static List<XY> simplify(List<XY> input, double epsilon) {

        int N = input.size();

        if (N < 3) {
            return new ArrayList<XY>(input);
        }

        List<XY> simplified = recursiveSimplify(input, 0, N-1, epsilon);

        List<XY> output = new ArrayList<XY>(simplified.size()+2);
        output.add(input.get(0));
        output.addAll(simplified);
        output.add(input.get(N-1));

        return output;
    }

    private static List recursiveSimplify(
        List<XY> input,
        int      start,
        int      end,
        double   epsilon
    ) {
        XY a = input.get(start);
        XY b = input.get(end);

        // Normal of hesse normal form.
        XY n = new XY(b).sub(a).ortho().normalize();

        // distance offset of the hesse normal form.
        double d = n.lineOffset(a);

        double maxDist = -Double.MAX_VALUE;
        int maxIdx = -1;

        for (int i = start+1; i < end; ++i) {
            double dist = Math.abs(n.dot(input.get(i)) + d);
            if (dist > maxDist) {
                maxDist = dist;
                maxIdx  = i;
            }
        }

        if (maxDist < epsilon) {
            // All points between a and b can be ignored.
            return Collections.<XY>emptyList();
        }

        // Split by input[maxIdx].
        List<XY> before = recursiveSimplify(input, start, maxIdx, epsilon);
        List<XY> after  = recursiveSimplify(input, maxIdx, end, epsilon);

        List<XY> output = new ArrayList<XY>(before.size()+1+after.size());
        output.addAll(before);
        output.add(input.get(maxIdx));
        output.addAll(after);

        return output;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
