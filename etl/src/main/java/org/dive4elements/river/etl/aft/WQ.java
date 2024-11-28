/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import java.util.Comparator;

public class WQ
{
    public static final double EPSILON = 1e-4;

    public static final Comparator<WQ> EPS_CMP = new Comparator<WQ>() {
        @Override
        public int compare(WQ a, WQ b) {
            int cmp = compareEpsilon(a.q, b.q);
            if (cmp != 0) return cmp;
            return compareEpsilon(a.w, b.w);
        }
    };

    protected int id;

    protected double w;
    protected double q;

    public WQ() {
    }

    public WQ(double w, double q) {
        this.w = w;
        this.q = q;
    }

    public WQ(int id, double w, double q) {
        this.id = id;
        this.w  = w;
        this.q  = q;
    }

    public static final int compareEpsilon(double a, double b) {
        double diff = a - b;
        if (diff < -EPSILON) return -1;
        return diff > EPSILON ? +1 : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
