/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.util.Date;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TimeBounds implements Bounds {

    protected long lower;
    protected long upper;


    public TimeBounds(long lower, long upper) {
        this.lower = lower;
        this.upper = upper;
    }


    @Override
    public Number getLower() {
        return Long.valueOf(lower);
    }


    public Date getLowerAsDate() {
        return new Date(lower);
    }


    @Override
    public Number getUpper() {
        return Long.valueOf(upper);
    }


    public Date getUpperAsDate() {
        return new Date(upper);
    }


    @Override
    public void applyBounds(ValueAxis axis) {
        DateAxis dateAxis = (DateAxis) axis;

        dateAxis.setMinimumDate(new Date(lower));
        dateAxis.setMaximumDate(new Date(upper));
    }


    @Override
    public void applyBounds(ValueAxis axis, int percent) {
        DateAxis dateAxis = (DateAxis) axis;

        long space = (upper - lower) / 100 * percent;

        dateAxis.setMinimumDate(new Date(lower-space));
        dateAxis.setMaximumDate(new Date(upper+space));
    }


    @Override
    public Bounds combine(Bounds bounds) {
        if (bounds == null) {
            return this;
        }

        TimeBounds other = (TimeBounds) bounds;

        long otherLower = other.getLower().longValue();
        long otherUpper = other.getUpper().longValue();

        return new TimeBounds(
            otherLower < lower ? otherLower : lower,
            otherUpper > upper ? otherUpper : upper);
    }


    @Override
    public String toString() {
        return "TimeBounds=["+ getLowerAsDate() + " ; " + getUpperAsDate() +"]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
