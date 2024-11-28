/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;


public final class Utils {

    public static final double EPSILON = 1e-3;

    private Utils() {
    }

    public static final boolean epsilonEquals(double a, double b) {
        return epsilonEquals(a, b, EPSILON);
    }

    public static final boolean epsilonEquals(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public static int relativeCCW(
        double x1, double y1,
        double x2, double y2,
        double px, double py
    ) {
        if ((epsilonEquals(x1, x2) && epsilonEquals(y1, y2))
        || ((epsilonEquals(x1, px) && epsilonEquals(y1, py)))) {
            return 0; // Coincident points.
        }
        // Translate to the origin.
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double slope2 = y2 / x2;
        double slopep = py / px;
        if (epsilonEquals(slope2, slopep)
        || (epsilonEquals(x2, 0.0) && epsilonEquals(px, 0.0))) {
            return y2 > EPSILON // Colinear.
                ? (py < -EPSILON ? -1 : py > y2 ? 1 : 0)
                : (py > -EPSILON ? -1 : py < y2 ? 1 : 0);
        }
        if (x2 >= EPSILON && slope2 >= EPSILON) {
            return px >= EPSILON // Quadrant 1.
                ? (slope2 > slopep ? 1 : -1)
                : (slope2 < slopep ? 1 : -1);
        }

        if (y2 > EPSILON) {
            return px < -EPSILON // Quadrant 2.
                ? (slope2 > slopep ? 1 : -1)
                : (slope2 < slopep ? 1 : -1);
        }
        if (slope2 >= EPSILON) {
            return px >= EPSILON // Quadrant 3.
                ? (slope2 < slopep ? 1 : -1)
                : (slope2 > slopep ? 1 : -1);
        }
        return px < -EPSILON // Quadrant 4.
            ? (slope2 < slopep ? 1 : -1)
            : (slope2 > slopep ? 1 : -1);
    }
}
