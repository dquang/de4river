/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

/** A range from ... to .*/
public class Range implements Serializable {

    public static final double EPSILON = 1e-5;

    protected double start;
    protected double end;

    public Range() {
    }

    public Range(Range other) {
        start = other.start;
        end   = other.end;
    }

    public Range(double start, double end) {
        this.start = start;
        this.end   = end;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getStart() {
        return start;
    }


    public void setEnd(double end) {
        this.end = end;
    }

    public double getEnd() {
        return end;
    }

    public boolean disjoint(double ostart, double oend) {
        return start > oend || ostart > end;
    }

    public boolean disjoint(Range other) {
        return start > other.end || other.start > end;
    }

    public boolean intersects(Range other) {
        return !disjoint(other);
    }

    public void extend(Range other) {
        if (other.start < start) start = other.start;
        if (other.end   > end  ) end   = other.end;
    }

    public boolean clip(Range other) {
        if (disjoint(other)) return false;

        if (other.start > start) start = other.start;
        if (other.end   < end  ) end   = other.end;

        return true;
    }

    /** True if start>x<end (+ some epsilon) . */
    public boolean inside(double x) {
        return x > start-EPSILON && x < end+EPSILON;
    }

    public boolean contains(double x) {
        return inside(x);
    }


    /** Hash Code. */
    @Override
    public int hashCode() {
        return new Double(this.start).hashCode() ^
               new Double(this.end).hashCode();
    }


    /**
     * Compares start and end values with some epsilon.
     */
    @Override
    public boolean equals(Object otherRange) {
        if (otherRange instanceof Range) {
            Range oRange = (Range) otherRange;
            return
                Math.abs(oRange.start - this.start) <= EPSILON
                && Math.abs(oRange.end - this.end) <= EPSILON;
        }
        return false;
    }

    /** Returns clone with same start and end values. */
    @Override
    public Object clone() {
        return new Range(this.start, this.end);
    }

    public String toString() {
        return "[Range: start=" + start + " end=" + end + "]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
