/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.model.Parameters;

public final class FixFacetUtils {

    public static final String [] MAX_Q_COLUMN = { "max_q" };

    public static double getMaxQ(Parameters params, double km) {
        double [] maxQ = params.interpolate("km", km, MAX_Q_COLUMN);
        if (maxQ == null) {
            return 1000d;
        }
        double mQ = Math.min(10000d, Math.abs(maxQ[0]));
        return mQ + 0.05*mQ;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
