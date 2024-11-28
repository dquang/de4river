/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

import org.apache.commons.math.analysis.interpolation.SplineInterpolator;

import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;

import org.apache.commons.math.ArgumentOutsideDomainException;

import org.apache.commons.math.exception.MathIllegalArgumentException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.model.Calculation;

import org.dive4elements.river.utils.DoubleUtil;

public class BackJumpCorrector
implements   Serializable
{
    private static Logger log = LogManager.getLogger(BackJumpCorrector.class);

    protected ArrayList<Double> backjumps;

    protected double [] corrected;

    public BackJumpCorrector() {
        backjumps = new ArrayList<Double>();
    }

    public boolean hasBackJumps() {
        return !backjumps.isEmpty();
    }

    public List<Double> getBackJumps() {
        return backjumps;
    }

    public double [] getCorrected() {
        return corrected;
    }

    public boolean doCorrection(
        double []   km,
        double []   ws,
        Calculation errors
    ) {
        boolean wsUp = DoubleUtil.isIncreasing(ws);

        if (wsUp) {
            km = DoubleUtil.swapClone(km);
            ws = DoubleUtil.swapClone(ws);
        }

        boolean kmUp = DoubleUtil.isIncreasing(km);

        if (!kmUp) {
            km = DoubleUtil.sumDiffs(km);
        }

        if (log.isDebugEnabled()) {
            log.debug("BackJumpCorrector.doCorrection ------- enter");
            log.debug("  km increasing: " + DoubleUtil.isIncreasing(km));
            log.debug("  ws increasing: " + DoubleUtil.isIncreasing(ws));
            log.debug("BackJumpCorrector.doCorrection ------- leave");
        }

        boolean hasBackJumps = doCorrectionClean(km, ws, errors);

        if (hasBackJumps && wsUp) {
            // mirror back
            DoubleUtil.swap(corrected);
        }

        return hasBackJumps;
    }

    protected boolean doCorrectionClean(
        double []   km,
        double []   ws,
        Calculation errors
    ) {
        int N = km.length;

        if (N != ws.length) {
            throw new IllegalArgumentException("km.length != ws.length");
        }

        if (N < 2) {
            return false;
        }

        SplineInterpolator interpolator = null;

        for (int i = 1; i < N; ++i) {
            if (ws[i] <= ws[i-1]) {
                // no back jump
                continue;
            }
            backjumps.add(km[i]);

            if (corrected == null) {
                // lazy cloning
                ws = corrected = (double [])ws.clone();
            }

            double above = aboveWaterKM(km, ws, i);

            if (Double.isNaN(above)) { // run over start km
                // fill all previous
                for (int j = 0; j < i; ++j) {
                    ws[j] = ws[i];
                }
                continue;
            }

            double distance = Math.abs(km[i] - above);

            double quarterDistance = 0.25*distance;

            double start = above - quarterDistance;

            double startHeight = DoubleUtil.interpolateSorted(km, ws, start);

            if (Double.isNaN(startHeight)) {
                // run over start km
                startHeight = ws[0];
            }

            double between = above + quarterDistance;

            double aboveHeight = ws[i] + 0.25*(startHeight - ws[i]);

            double [] x = { start,  above,  between };
            double [] y = { startHeight, aboveHeight, ws[i] };

            if (log.isDebugEnabled()) {
                for (int j = 0; j < x.length; ++j) {
                    log.debug("   " + x[j] + " -> " + y[j]);
                }
            }

            if (interpolator == null) {
                interpolator = new SplineInterpolator();
            }

            PolynomialSplineFunction spline;

            try {
                spline = interpolator.interpolate(x, y);
            }
            catch (MathIllegalArgumentException miae) {
                errors.addProblem("spline.creation.failed");
                log.error(miae);
                continue;
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug("spline points:");
                    for (int j = 0; j < x.length; ++j) {
                        log.debug(x[j] + " " + y[j] + " " + spline.value(x[j]));
                    }
                }

                int j = i-1;

                for (; j >= 0 && km[j] >= between; --j) {
                    ws[j] = ws[i];
                }

                for (; j >= 0 && ws[j] < startHeight; --j) {
                    ws[j] = spline.value(km[j]);
                }
            }
            catch (ArgumentOutsideDomainException aode) {
                errors.addProblem("spline.interpolation.failed");
                log.error("spline interpolation failed", aode);
            }
        } // for all km

        return !backjumps.isEmpty();
    }


    protected static double aboveWaterKM(
        double [] km,
        double [] ws,
        int       wIndex
    ) {
        double w = ws[wIndex];

        while (--wIndex >= 0) {
            // still under water
            if (ws[wIndex] < w) continue;

            if (ws[wIndex] > w) {
                // f(ws[wIndex])   = km[wIndex]
                // f(ws[wIndex+1]) = km[wIndex+1]
                return Linear.linear(
                    w,
                    ws[wIndex], ws[wIndex+1],
                    km[wIndex], km[wIndex+1]);
            }
            else {
                return km[wIndex];
            }
        }

        return Double.NaN;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
