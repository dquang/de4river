/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

public final class Sub
implements         Function
{
    private double s;

    public Sub(double s) {
        this.s = s;
    }

    @Override
    public double value(double x) {
        return x - s;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
