/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.Comparator;

public class Sieve
{
    public static final double EPSILON = 1e-6;

    public static final Comparator<Double> DIAMETER_CMP =
        new Comparator<Double>() {
            @Override
            public int compare(Double a, Double b) {
                double diff = a - b;
                if (diff < -EPSILON) return -1;
                if (diff >  EPSILON) return +1;
                return 0;
            }
        };

    protected double diameter;
    protected double load;

    /**
     * Constructs a new instance.
     */
    public Sieve() {
        this(Double.NaN, Double.NaN);
    }

    public Sieve(double diameter, double load) {
        this.diameter = diameter;
        this.load = load;
    }

    /**
     * Gets the diameter for this instance.
     *
     * @return The diameter.
     */
    public double getDiameter() {
        return this.diameter;
    }

    /**
     * Sets the diameter for this instance.
     *
     * @param diameter The diameter.
     */
    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    /**
     * Gets the load for this instance.
     *
     * @return The load.
     */
    public double getLoad() {
        return this.load;
    }

    /**
     * Sets the load for this instance.
     *
     * @param load The load.
     */
    public void setLoad(double load) {
        this.load = load;
    }

    public boolean matchesDiameter(double diameter) {
        return Math.abs(diameter - this.diameter) < EPSILON;
    }

    public boolean hasDiameter() {
        return !Double.isNaN(diameter);
    }

    public boolean hasLoad() {
        return !Double.isNaN(load);
    }

    public boolean isValid() {
        return hasDiameter() && hasLoad();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
