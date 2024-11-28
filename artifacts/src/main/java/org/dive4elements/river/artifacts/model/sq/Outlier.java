/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.river.artifacts.math.GrubbsOutlier;
import org.dive4elements.river.artifacts.math.StdDevOutlier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Outlier
{
    private static Logger log = LogManager.getLogger(Outlier.class);

    private static final String GRUBBS = "outlier.method.grubbs";

    //private static final String STD_DEV = "std-dev";

    public interface Callback {

        void initialize(List<SQ> sqs) throws MathException;

        double eval(SQ sq);

        void iterationFinished(
            double   stdDev,
            SQ       outlier,
            List<SQ> remaining);

    } // interface Callback

    public static void detectOutliers(
        Callback callback,
        List<SQ> sqs,
        double   stdDevFactor,
        String   method
    )
    throws MathException
    {
        boolean debug = log.isDebugEnabled();

        if (method == null) {
            method = "std-dev";
        }

        if (debug) {
            log.debug("stdDevFactor: " + stdDevFactor);
            log.debug("method: " + method);
        }

        List<SQ> data = new ArrayList<SQ>(sqs);

        double [] stdDev = new double[1];

        boolean useGrubbs = method.equals(GRUBBS);

        if (useGrubbs) {
            stdDevFactor = Math.max(0d, Math.min(stdDevFactor/100d, 1d));
        }

        List<Double> values = new ArrayList<Double>(data.size());

        while (data.size() > 2) {

            callback.initialize(data);

            for (SQ sq: data) {
                values.add(callback.eval(sq));
            }

            Integer ndx = useGrubbs
                ? GrubbsOutlier.findOutlier(values, stdDevFactor, stdDev)
                : StdDevOutlier.findOutlier(values, stdDevFactor, stdDev);

            if (ndx == null) {
                callback.iterationFinished(stdDev[0], null, data);
                break;
            }

            SQ outlier = data.remove(ndx.intValue());
            if (debug) {
                log.debug("stdDev: " + stdDev[0]);
                log.debug("removed " + ndx +
                    "; S: " + outlier.getS() + " Q: " + outlier.getQ());
            }
            callback.iterationFinished(stdDev[0], outlier, data);
            values.clear();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
