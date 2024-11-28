/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/* XXX:
 * Warning: This class is called StdDevOutlier because it caculates the
 * Standard Deviation method for outlier removal as the BFG calls it.
 * But the actual calculation used to remove the outliers calculates
 * the Standard Error and not the Standard Deviation! */

public class StdDevOutlier
{
    public static final double DEFAULT_FACTOR = 3;

    private static Logger log = LogManager.getLogger(StdDevOutlier.class);

    protected StdDevOutlier() {
    }

    public static Integer findOutlier(List<Double> values) {
        return findOutlier(values, DEFAULT_FACTOR, null);
    }

    public static Integer findOutlier(
        List<Double> values,
        double       factor,
        double []    stdErrResult
    ) {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("factor for std dev test (that calculates std err): "
                + factor);
        }

        int N = values.size();

        if (debug) {
            log.debug("Values to check: " + N);
        }

        if (N < 3) {
            return null;
        }

        double maxValue = -Double.MAX_VALUE;
        int    maxIndex = -1;

        double squareSumResiduals = 0;
        for (Double db: values) {
            squareSumResiduals += Math.pow(db, 2);
        }

        double stdErr = Math.sqrt(squareSumResiduals / (N - 2));

        double accepted = factor * stdErr;

        for (int i = N-1; i >= 0; --i) {
            double value = Math.abs(values.get(i));
            if (value > maxValue) {
                maxValue = value;
                maxIndex = i;
            }
        }

        if (debug) {
            log.debug("std err: " + stdErr);
            log.debug("accepted: " + accepted);
            log.debug("max value: " + maxValue);
        }

        if (stdErrResult != null) {
            stdErrResult[0] = stdErr;
        }

        return maxValue > accepted ? maxIndex : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
