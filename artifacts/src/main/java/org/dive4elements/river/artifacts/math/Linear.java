/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

public final class Linear
implements         Function
{
    private double m;
    private double b;

    public Linear(
        double x1, double x2,
        double y1, double y2
    ) {
        // y1 = m*x1 + b
        // y2 = m*x2 + b
        // y2 - y1 = m*(x2 - x1)
        // m = (y2 - y1)/(x2 - x1) # x2 != x1
        // b = y1 - m*x1

        if (x1 == x2) {
            m = 0;
            b = 0.5*(y1 + y2);
        }
        else {
            m = (y2 - y1)/(x2 - x1);
            b = y1 - m*x1;
        }
    }

    public static final double linear(
        double x,
        double x1, double x2,
        double y1, double y2
    ) {
        // y1 = m*x1 + b
        // y2 = m*x2 + b
        // y2 - y1 = m*(x2 - x1)
        // m = (y2 - y1)/(x2 - x1) # x2 != x1
        // b = y1 - m*x1

        if (x1 == x2) {
            return 0.5*(y1 + y2);
        }
        double m = (y2 - y1)/(x2 - x1);
        double b = y1 - m*x1;
        return x*m + b;
    }

    @Override
    public double value(double x) {
        return m*x + b;
    }

    public static final double factor(double x, double p1, double p2) {
        // 0 = m*p1 + b <=> b = -m*p1
        // 1 = m*p2 + b
        // 1 = m*(p2 - p1)
        // m = 1/(p2 - p1) # p1 != p2
        // f(x) = x/(p2-p1) - p1/(p2-p1) <=> (x-p1)/(p2-p1)

        return p1 == p2 ? 0.0 : (x-p1)/(p2-p1);
    }

    public static final double weight(double factor, double a, double b) {
        //return (1.0-factor)*a + factor*b;
        return a + factor*(b-a);
    }

    public static final void weight(
        double factor,
        double [] a, double [] b, double [] c
    ) {
        int N = Math.min(Math.min(a.length, b.length), c.length);
        for (int i = 0; i < N; ++i) {
            c[i] = weight(factor, a[i], b[i]);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
