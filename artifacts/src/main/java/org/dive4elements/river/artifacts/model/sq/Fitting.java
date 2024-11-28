/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.artifacts.common.utils.StringUtils;
import org.dive4elements.river.artifacts.math.fitting.Function;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;

import org.apache.commons.math.optimization.fitting.CurveFitter;

import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.stat.regression.SimpleRegression;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Fitting
implements   Outlier.Callback
{
    // XXX: Hack to force linear fitting!
    private static final boolean USE_NON_LINEAR_FITTING =
        Boolean.getBoolean("minfo.sq.fitting.nonlinear");

    private static Logger log = LogManager.getLogger(Fitting.class);

    public interface Callback {

        void afterIteration(
            double [] parameters,
            SQ []     measurements,
            SQ []     outliers,
            double    standardDeviation,
            double    chiSqr);
    } // interfacte

    protected Function function;

    protected double [] coeffs;

    protected org.dive4elements.river.artifacts.math.Function instance;

    protected double stdDevFactor;
    protected double chiSqr;

    protected Callback callback;

    protected SQ.View sqView;

    public Fitting() {
    }

    public Fitting(Function function, double stdDevFactor, SQ.View sqView) {
        this.function     = function;
        this.stdDevFactor = stdDevFactor;
        this.sqView       = sqView;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public double getStdDevFactor() {
        return stdDevFactor;
    }

    public void setStdDevFactor(double stdDevFactor) {
        this.stdDevFactor = stdDevFactor;
    }

    @Override
    public void initialize(List<SQ> sqs) throws MathException {

        if (USE_NON_LINEAR_FITTING
        || function.getParameterNames().length != 2) {
            nonLinearFitting(sqs);
        }
        else {
            linearFitting(sqs);
        }
    }

    protected void linearFitting(List<SQ> sqs) {
        coeffs   = linearRegression(sqs);
        instance = function.instantiate(coeffs);
    }

    protected double [] linearRegression(List<SQ> sqs) {

        String [] pns = function.getParameterNames();
        double [] result = new double[pns.length];

        if (sqs.size() < 2) {
            log.debug("not enough points");
            return result;
        }

        SimpleRegression reg = new SimpleRegression();

        for (SQ sq: sqs) {
            double s = sqView.getS(sq);
            double q = sqView.getQ(sq);
            reg.addData(q, s);
        }

        double m = reg.getIntercept();
        double b = reg.getSlope();

        if (log.isDebugEnabled()) {
            log.debug("m: " + m);
            log.debug("b: " + b);
        }

        int mIdx = StringUtils.indexOf("m", pns);
        int bIdx = StringUtils.indexOf("b", pns);

        if (mIdx == -1 || bIdx == -1) {
            log.error("index not found: " + mIdx + " " + bIdx);
            return result;
        }

        result[bIdx] = m;
        result[mIdx] = b;

        return result;
    }


    protected void nonLinearFitting(List<SQ> sqs) throws MathException {

        LevenbergMarquardtOptimizer optimizer =
            new LevenbergMarquardtOptimizer();

        CurveFitter cf = new CurveFitter(optimizer);

        for (SQ sq: sqs) {
            cf.addObservedPoint(sqView.getQ(sq), sqView.getS(sq));
        }

        coeffs = cf.fit(
            function, function.getInitialGuess());

        instance = function.instantiate(coeffs);

        chiSqr = optimizer.getChiSquare();
    }

    @Override
    public double eval(SQ sq) {
        double s = instance.value(sqView.getQ(sq));
        return sqView.getS(sq) - s;
    }

    @Override
    public void iterationFinished(
        double   standardDeviation,
        SQ       outlier,
        List<SQ> remainings
    ) {
        if (log.isDebugEnabled()) {
            log.debug("iterationFinished ----");
            log.debug(" num remainings: " + remainings.size());
            log.debug(" has outlier: " + outlier != null);
            log.debug(" standardDeviation: " + standardDeviation);
            log.debug(" Chi^2: " + chiSqr);
            log.debug("---- iterationFinished");
        }
        callback.afterIteration(
            coeffs,
            remainings.toArray(new SQ[remainings.size()]),
            outlier != null ? new SQ [] { outlier} : new SQ [] {},
            standardDeviation,
            chiSqr);
    }

    public boolean fit(List<SQ> sqs, String  method, Callback callback) {

        if (sqs.size() < 2) {
            log.warn("Too less points for fitting.");
            return false;
        }

        sqs = new ArrayList<SQ>(sqs);

        this.callback = callback;

        try {
            Outlier.detectOutliers(this, sqs, stdDevFactor, method);
        }
        catch (MathException me) {
            log.warn(me);
            return false;
        }

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
