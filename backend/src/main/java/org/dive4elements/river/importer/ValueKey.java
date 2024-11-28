/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.math.BigDecimal;

import java.util.Comparator;

public class ValueKey
{
    public static final double EPSILON = 1e-6;

    public static final Comparator<ValueKey> EPSILON_COMPARATOR =
        new Comparator<ValueKey>()
    {
        public int compare(ValueKey x, ValueKey y) {
            int cmp = ValueKey.compare(x.a, y.a);
            if (cmp != 0) return cmp;
            return ValueKey.compare(x.b, y.b);
        }
    };

    public static int compare(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return  0;
        if (a != null && b == null) return +1;
        if (a == null && b != null) return -1;

        double diff = a.doubleValue() - b.doubleValue();
        if (diff < -EPSILON) return -1;
        return diff > EPSILON ? +1 : 0;
    }

    protected BigDecimal a;
    protected BigDecimal b;

    public ValueKey() {
    }

    public ValueKey(BigDecimal a, BigDecimal b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        return ((a != null ? a.hashCode() : 0) << 16)
              | (b != null ? b.hashCode() : 0);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValueKey)) {
            return false;
        }
        ValueKey o = (ValueKey)other;
        return !(
               (a == null && o.a != null)
            || (a != null && o.a == null)
            || (a != null && !a.equals(o.a))
            || (b == null && o.b != null)
            || (b != null && o.b == null)
            || (b != null && !b.equals(o.b)));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
