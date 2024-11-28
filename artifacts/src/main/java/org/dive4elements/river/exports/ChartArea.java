/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.LogarithmicAxis;

/** Two Ranges that span a rectangular area. */
public class ChartArea {
    protected double xLower;
    protected double xUpper;
    protected double xLength;
    protected double yLower;
    protected double yUpper;
    protected double yLength;
    protected boolean xIsLog;
    protected boolean yIsLog;

    public ChartArea(ValueAxis axisX, ValueAxis axisY) {
        this.xLower = axisX.getRange().getLowerBound();
        this.xUpper = axisX.getRange().getUpperBound();
        this.xLength= axisX.getRange().getLength();
        this.yLower = axisY.getRange().getLowerBound();
        this.yUpper = axisY.getRange().getUpperBound();
        this.yLength= axisY.getRange().getLength();
        this.xIsLog = axisX instanceof LogarithmicAxis;
        this.yIsLog = axisY instanceof LogarithmicAxis;
    }

    public double ofLeft(double percent) {
        if (xIsLog) {
            return Math.pow(10,
                Math.log10(xLower)
                + Math.log10(xUpper / xLower) * percent
            );
        }
        return xLower + xLength * percent;
    }

    public double ofRight(double percent) {
        if (xIsLog) {
            return Math.pow(10,
                Math.log10(xUpper)
                - Math.log10(xUpper / xLower) * percent
            );
        }
        return xUpper - xLength * percent;
    }

    public double ofGround(double percent) {
        if (yIsLog) {
            return Math.pow(10,
                Math.log10(yLower)
                + Math.log10(yUpper / yLower) * percent
            );
        }
        return yLower + yLength * percent;
    }

    public double atTop() {
        return yUpper;
    }

    public double atGround() {
        return yLower;
    }

    public double atRight() {
        return xUpper;
    }

    public double atLeft() {
        return xLower;
    }

    public double above(double percent, double base) {
        if (yIsLog) {
            return Math.pow(10,
                Math.log10(base)
                + Math.log10(yUpper / yLower) * percent
            );
        }
        return base + yLength * percent;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
