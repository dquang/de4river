/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.Arrays;

/**
 * A range (from -> to) with associated double array.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RangeWithValues extends Range {

    protected double[] values;

    public RangeWithValues() {
    }

    public RangeWithValues(double lower, double upper, double[] values) {
        super(lower, upper);
        this.values = values;
    }


    /** Returns the values. */
    public double[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return new StringBuilder("start=").append(start)
            .append(" end=" ).append(end)
            .append(" values=[").append(Arrays.toString(values)).append(']')
            .toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
