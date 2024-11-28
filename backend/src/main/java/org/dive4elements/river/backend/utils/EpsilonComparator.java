/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.backend.utils;

import java.util.Comparator;
import java.io.Serializable;

/** Comparator with some tolerance (epsilon). */
public class EpsilonComparator implements Comparator<Double>, Serializable
{
    public static final double EPSILON = 1e-4;

    /** Ready-made comparator with 1e-4 tolerance. */
    public static final EpsilonComparator CMP = new EpsilonComparator(EPSILON);

    private double epsilon;

    public EpsilonComparator(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public int compare(Double a, Double b) {
        double diff = a - b;
        if (diff < -epsilon) return -1;
        if (diff >  epsilon) return +1;
        return 0;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
