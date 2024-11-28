/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import java.util.List;

import org.apache.commons.math.MathException;

import org.apache.commons.math.distribution.TDistributionImpl;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class GrubbsOutlier
{
    public static final double EPSILON = 1e-5;

    public static final double DEFAULT_ALPHA = 0.05;

    private static Logger log = LogManager.getLogger(GrubbsOutlier.class);

    protected GrubbsOutlier() {
    }

    public static Integer findOutlier(List<Double> values) {
        return findOutlier(values, DEFAULT_ALPHA, null);
    }

    public static Integer findOutlier(
        List<Double> values,
        double alpha,
        double[] stdDevResult
    ) {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("outliers significance: " + alpha);
        }

        alpha = 1d - alpha;

        int N = values.size();

        if (debug) {
            log.debug("Values to check: " + N);
        }

        if (N < 3) {
            return null;
        }

        Mean mean = new Mean();
        StandardDeviation std = new StandardDeviation();

        for (Double value: values) {
            double v = value.doubleValue();
            mean.increment(v);
            std .increment(v);
        }

        double m = mean.getResult();
        double s = std.getResult();

        if (debug) {
            log.debug("mean: " + m);
            log.debug("std dev: " + s);
        }

        double maxZ = -Double.MAX_VALUE;
        int iv = -1;
        for (int i = N-1; i >= 0; --i) {
            double v = values.get(i).doubleValue();
            double z = Math.abs(v - m);
            if (z > maxZ) {
                maxZ = z;
                iv = i;
            }
        }

        if (Math.abs(s) < EPSILON) {
            return null;
        }

        maxZ /= s;

        TDistributionImpl tdist = new TDistributionImpl(N-2);

        double t;

        try {
            t = tdist.inverseCumulativeProbability(alpha/(N+N));
        }
        catch (MathException me) {
            log.error(me);
            return null;
        }

        t *= t;

        double za = ((N-1)/Math.sqrt(N))*Math.sqrt(t/(N-2d+t));

        if (debug) {
            log.debug("max: " + maxZ + " crit: " + za);
        }
        if (stdDevResult != null) {
            stdDevResult[0] = std.getResult();
        }
        return maxZ > za
            ? Integer.valueOf(iv)
            : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
