/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SieveArray
{
    private static final Logger log =
        LogManager.getLogger(SieveArray.class);

    public static final double EPSILON = 1e-8;

    public static final double [] SIEVE_DIAMETERS = {
        100d,   63d,  31.5d,    16d,
          8d,    4d,     2d,     1d,
        0.5d, 0.25d, 0.125d, 0.063d
    };

    protected double [] loads;
    protected double [] normLoads;

    public SieveArray() {
        loads = new double[SIEVE_DIAMETERS.length+1];
        normLoads = new double[SIEVE_DIAMETERS.length+1];
    }

    public void doSieving(Sieve s) {

        double diameter = s.getDiameter();

        for (int i = 0; i < SIEVE_DIAMETERS.length; ++i) {
            if (diameter >= SIEVE_DIAMETERS[i]) {
                loads[i] += s.getLoad();
                return;
            }
        }
        loads[loads.length-1] += s.getLoad();
    }

    public double totalLoad() {
        double sum = 0d;
        for (double load: loads) {
            sum += load;
        }
        return sum;
    }

    public void calculateNormLoads() {
        double total = totalLoad();
        if (Math.abs(total) < EPSILON) {
            System.arraycopy(loads, 0, normLoads, 0, loads.length);
            return;
        }
        total = 1d/total;
        for (int i = 0; i < normLoads.length; ++i) {
            normLoads[i] = total*loads[i];
        }
        log.debug("calculated norm loads: " + Arrays.toString(normLoads));
    }

    /*
    public void adjust(double eight, double four) {
        this.normLoads[4] = eight;
        this.normLoads[5] = four;
    }
    */

    /**
     * Gets the loads for this instance.
     *
     * @return The loads.
     */
    public double[] getLoads() {
        return this.loads;
    }

    /**
     * Gets the loads for this instance.
     *
     * @param index The index to get.
     * @return The loads.
     */
    public double getLoads(int index) {
        return this.loads[index];
    }

    /**
     * Gets the normLoads for this instance.
     *
     * @return The normLoads.
     */
    public double[] getNormLoads() {
        return this.normLoads;
    }

    /**
     * Gets the normLoads for this instance.
     *
     * @param index The index to get.
     * @return The normLoads.
     */
    public double getNormLoads(int index) {
        return this.normLoads[index];
    }

    public double sandNormFraction() {
        double sum = 0d;
        for (int i = 7; i < normLoads.length; ++i) {
            sum += normLoads[i];
        }
        return sum;
    }

    public double coarseNormFraction() {
        double sum = 0d;
        for (int i = 0; i < 4; ++i) {
            sum += normLoads[i];
        }
        return sum;
    }

    public double gravelNormFraction() {
        double sum = 0d;
        for (int i = 4; i < 7; ++i) {
            sum += normLoads[i];
        }
        return sum;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
