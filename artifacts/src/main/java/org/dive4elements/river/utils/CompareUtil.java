/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

/** Utils to deal with Comparisons. */
public class CompareUtil
{
    /** Singleton. */
    private CompareUtil() {
    }

    /** Return true if a and b are either both null or equal(). */
    public static <T> boolean areSame(T a, T b) {
       if (a == null) return b == null;
       if (b == null) return false;
       return a.equals(b);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
