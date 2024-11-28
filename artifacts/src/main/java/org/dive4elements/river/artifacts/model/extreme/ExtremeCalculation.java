/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.extreme;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.math.MathException;

import org.apache.commons.math.optimization.fitting.CurveFitter;

import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

import org.dive4elements.river.artifacts.access.ExtremeAccess;

import org.dive4elements.river.artifacts.math.Linear;
//import org.dive4elements.river.artifacts.math.Utils;

import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.RangeWithValues;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WstValueTable;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;

import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.DoubleUtil;
import org.dive4elements.river.utils.KMIndex;

import gnu.trove.TDoubleArrayList;

import java.util.List;

import java.awt.geom.Line2D;

/** Calculate extrapolated W. */
public class ExtremeCalculation
extends      Calculation
{
    private static final Log log =
        LogFactory.getLog(ExtremeCalculation.class);

    protected String                river;
    protected String                function;
    protected double                from;
    protected double                to;
    protected double                step;
    protected double                percent;
    protected List<RangeWithValues> ranges;

    public ExtremeCalculation() {
    }

    public ExtremeCalculation(ExtremeAccess access) {
        String                river    = access.getRiverName();
        String                function = access.getFunction();
        Double                from     = access.getFrom();
        Double                to       = access.getTo();
        Double                step     = access.getStep();
        Double                percent  = access.getPercent();
        List<RangeWithValues> ranges   = access.getRanges();

        if (river == null) {
            // TODO: i18n
            addProblem("extreme.no.river");
        }

        if (function == null) {
            // TODO: i18n
            addProblem("extreme.no.function");
        }

        if (from == null) {
            // TODO: i18n
            addProblem("extreme.no.from");
        }

        if (to == null) {
            // TODO: i18n
            addProblem("extreme.no.to");
        }

        if (step == null) {
            // TODO: i18n
            addProblem("extreme.no.step");
        }

        if (percent == null) {
            // TODO: i18n
            addProblem("extreme.no.percent");
        }

        if (ranges == null) {
            // TODO: i18n
            addProblem("extreme.no.ranges");
        }

        if (!hasProblems()) {
            this.river    = river;
            this.function = function;
            this.from     = Math.min(from, to);
            this.to       = Math.max(from, to);
            this.step     = Math.max(0.001d, Math.abs(step)/1000d);
            this.percent  = Math.max(0d, Math.min(100d, percent));
            this.ranges   = ranges;
        }
    }


    /** Calculate an extreme curve (extrapolate). */
    public CalculationResult calculate() {

        WstValueTable wst = null;

        River river = RiverFactory.getRiver(this.river);
        if (river == null) {
            // TODO: i18n
            addProblem("extreme.no.such.river", this.river);
        }
        else {
            wst = WstValueTableFactory.getTable(river);
            if (wst == null) {
                // TODO: i18n
                addProblem("extreme.no.wst.table");
            }
        }

        Function function =
            FunctionFactory.getInstance().getFunction(this.function);
        if (function == null) {
            // TODO: i18n
            addProblem("extreme.no.such.function", this.function);
        }

        return hasProblems()
            ? new CalculationResult(this)
            : innerCalculate(wst, function);
    }


    /** Name of wqkms like W(5000,6000) */
    protected String wqkmsName(int i) {
        StringBuilder sb = new StringBuilder("W(");
        boolean already = false;
        for (RangeWithValues r: ranges) {
            double [] values = r.getValues();
            if (i < values.length) {
                if (already) {
                    sb.append(", ");
                }
                else {
                    already = true;
                }
                // TODO: i18n
                sb.append(values[i]);
            }
        }
        return sb.append(')').toString();
    }

    protected WQKms [] allocWQKms() {
        int max = 0;
        for (RangeWithValues r: ranges) {
            double [] values = r.getValues();
            if (values.length > max) {
                max = values.length;
            }
        }
        WQKms [] wqkms = new WQKms[max];
        for (int i = 0; i < max; ++i) {
            wqkms[i] = new WQKms(wqkmsName(i));
        }
        return wqkms;
    }


    /** Calculate an extreme curve (extrapolate). */
    protected CalculationResult innerCalculate(
        WstValueTable wst,
        Function      function
    ) {
        RangeWithValues range = null;

        double [] chiSqr = { 0d };

        KMIndex<Curve> curves = new KMIndex<Curve>();
        WQKms [] wqkms = allocWQKms();

        boolean debug = log.isDebugEnabled();

        from = DoubleUtil.round(from);
        to = DoubleUtil.round(to);

        for (double km = from; km <= to; km = DoubleUtil.round(km+step)) {

            if (debug) {
                log.debug("km: " + km);
            }

            boolean foundRange = false;

            if (range == null || !range.inside(km)) {
                for (RangeWithValues r: ranges) {
                    if (r.inside(km)) {
                        range = r;
                        foundRange = true;
                        break;
                    }
                }
                // TODO: i18n
                if (!foundRange) {
                    addProblem(km, "extreme.no.range.inner");
                    continue;
                }
            }

            double [][] wqs = wst.interpolateTabulated(km);
            if (wqs == null) {
                // TODO: i18n
                addProblem(km, "extreme.no.raw.data");
                continue;
            }

            // XXX: This should not be necessary for model data.
            if (!DoubleUtil.isValid(wqs)) {
                // TODO: i18n
                addProblem(km, "extreme.invalid.data");
                continue;
            }

            double [][] fitWQs = extractPointsToFit(wqs);
            if (fitWQs == null) {
                // TODO: i18n
                addProblem(km, "extreme.too.less.points");
                continue;
            }

            double [] coeffs = doFitting(function, fitWQs, chiSqr);
            if (coeffs == null) {
                // TODO: i18n
                addProblem(km, "extreme.fitting.failed");
                continue;
            }

            Curve curve = new Curve(
                wqs[1], wqs[0],
                function.getName(),
                coeffs,
                chiSqr[0]);

            curves.add(km, curve);

            double [] values = range.getValues();

            int V = Math.min(values.length, wqkms.length);
            for (int i = 0; i < V; ++i) {
                double q = values[i];
                double w = curve.value(q);
                if (Double.isNaN(w)) {
                    // TODO: i18n
                    addProblem(km, "extreme.evaluate.failed", values[i]);
                }
                else {
                    wqkms[i].add(w, q, km);
                }
            }
        }

        ExtremeResult result = new ExtremeResult(curves, wqkms);
        return new CalculationResult(result, this);
    }

    protected double [] doFitting(
        Function    function,
        double [][] wqs,
        double []   chiSqr
    ) {
        LevenbergMarquardtOptimizer lmo = null;

        double [] coeffs = null;

        double [] ws = wqs[0];
        double [] qs = wqs[1];

        for (double tolerance = 1e-10; tolerance < 1e-3; tolerance *= 10d) {
            lmo = new LevenbergMarquardtOptimizer();
            lmo.setCostRelativeTolerance(tolerance);
            lmo.setOrthoTolerance(tolerance);
            lmo.setParRelativeTolerance(tolerance);

            CurveFitter cf = new CurveFitter(lmo);

            for (int i = 0; i < ws.length; ++i) {
                cf.addObservedPoint(qs[i], ws[i]);
            }

            try {
                coeffs = cf.fit(function, function.getInitialGuess());
                break;
            }
            catch (MathException me) {
                if (log.isDebugEnabled()) {
                    log.debug("tolerance " + tolerance + " + failed.");
                }
            }
        }
        if (coeffs != null) {
            chiSqr[0] = lmo.getChiSquare();
        }
        return coeffs;
    }

    protected double [][] extractPointsToFit(double [][] wqs) {

        double [] ws = wqs[0];
        double [] qs = wqs[1];

        int N = Math.min(ws.length, qs.length);

        if (N < 2) {
            log.warn("Too less points for fitting");
            return null;
        }

        double q2 = qs[N-1];
        double w2 = ws[N-1];
        double q1 = qs[N-2];
        double w1 = ws[N-2];

        boolean ascending = w2 > w1;

        TDoubleArrayList ows = new TDoubleArrayList();
        TDoubleArrayList oqs = new TDoubleArrayList();

        oqs.add(q2); oqs.add(q1);
        ows.add(w2); ows.add(w1);

        int lastDir = -2;

        for (int i = N-3; i >= 0; --i) {
            double q = qs[i];
            double w = ws[i];

            if ((ascending && w > w1) || (!ascending && w < w1)) {
                break;
            }

            int dir = Line2D.relativeCCW(q2, w2, q1, w1, q, w);
            //int dir = Utils.relativeCCW(q2, w2, q1, w1, q, w);
            if (lastDir == -2) {
                lastDir = dir;
            }
            else if (lastDir != dir) {
                break;
            }

            oqs.add(q);
            ows.add(w);
            w2 = w1;
            q2 = q1;
            w1 = w;
            q1 = q;
        }

        oqs.reverse();
        ows.reverse();

        boolean debug = log.isDebugEnabled();
        if (debug) {
            log.debug("from table: " + N);
            log.debug("after trim: " + oqs.size());
        }

        cutPercent(ows, oqs);

        if (debug) {
            log.debug("after percent cut: " + oqs.size());
        }

        return new double [][] {
            ows.toNativeArray(),
            oqs.toNativeArray()
        };
    }


    protected void cutPercent(TDoubleArrayList ws, TDoubleArrayList qs) {
        int N = qs.size();
        if (percent <= 0d || N == 0) {
            return;
        }

        double minQ = qs.getQuick(0);
        double maxQ = qs.getQuick(N-1);
        double factor = Math.min(Math.max(0d, percent/100d), 1d);
        double cutQ = Linear.weight(factor, minQ, maxQ);
        int cutIndex = 0;
        for (; cutIndex < N; ++cutIndex) {
            double q = qs.getQuick(cutIndex);
            if (minQ < maxQ) {
                if (q > cutQ) {
                    break;
                }
            }
            else {
                if (q < cutQ) {
                    break;
                }
            }
        }
        ws.remove(0, cutIndex);
        qs.remove(0, cutIndex);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
