/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.data.Range;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DoubleBounds implements Bounds {

    protected double lower;
    protected double upper;


    /**
     * Default constructor. <b>A DoubleBounds has always set lower &lt;
     * upper!</b>
     */
    public DoubleBounds(double lower, double upper) {
        this.lower = Math.min(lower, upper);
        this.upper = Math.max(lower, upper);
    }

    public DoubleBounds(Range range) {
        this.lower = range.getLowerBound();
        this.upper = range.getUpperBound();
    }

    @Override
    public Number getLower() {
        return Double.valueOf(lower);
    }


    @Override
    public Number getUpper() {
        return Double.valueOf(upper);
    }


    @Override
    public void applyBounds(ValueAxis axis) {
        axis.setRange(new Range(lower, upper));
    }


    /**
     * Set extended range to ValueAxis.
     * @param percent how many percent to extend (in each direction,
     *        thus 10 percent on [0,100] -> [-10,110].
     */
    @Override
    public void applyBounds(ValueAxis axis, int percent) {
        double space = (upper - lower) / 100 * percent;
        if (axis instanceof LogarithmicAxis) {
            axis.setRange(new Range(Math.max(lower-space, 0.0001),
                        Math.max(upper+space, 0.0002)));
        } else {
            axis.setRange(new Range(lower-space, upper+space));
        }
    }


    @Override
    public Bounds combine(Bounds bounds) {
        if (bounds == null) {
            return this;
        }

        DoubleBounds other = (DoubleBounds) bounds;

        double otherLower = other.getLower().doubleValue();
        double otherUpper = other.getUpper().doubleValue();

        return new DoubleBounds(
            otherLower < lower ? otherLower : lower,
            otherUpper > upper ? otherUpper : upper);
    }


    @Override
    public String toString() {
        return "DoubleBounds=[" + lower + " ; " + upper + "]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
