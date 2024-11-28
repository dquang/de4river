/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.artifacts.common.utils.StringUtils;
import org.dive4elements.river.artifacts.access.SQRelationAccess;

import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.backend.SedDBSessionHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SQRelationCalculation extends Calculation {

    private static final Logger log =
        LogManager.getLogger(SQRelationCalculation.class);

    public static final boolean NON_LINEAR_FITTING =
        Boolean.getBoolean("minfo.sq.calcution.non.linear.fitting");

    public static final String SQ_POW_FUNCTION_NAME = "sq-pow";
    public static final String SQ_LIN_FUNCTION_NAME = "linear";

    public static final String [] EXTRA_PARAMETERS = {
        "chi_sqr",
        "std_dev",
        "max_q",
        "c_ferguson",
        "c_duan",
        "r2"
    };

    protected String    river;
    protected double    location;
    protected DateRange period;
    protected double    outliers;
    private   String    method;

    public SQRelationCalculation() {
    }

    public SQRelationCalculation(SQRelationAccess access) {

        String    river    = access.getRiverName();
        Double    location = access.getLocation();
        DateRange period   = access.getPeriod();
        Double    outliers = access.getOutliers();
        String    method   = access.getOutlierMethod();

        if (river == null) {
            // TODO: i18n
            addProblem("sq.missing.river");
        }

        if (location == null) {
            // TODO: i18n
            addProblem("sq.missing.location");
        }

        if (period == null) {
            // TODO: i18n
            addProblem("sq.missing.periods");
        }

        if (outliers == null) {
            // TODO: i18n
            addProblem("sq.missing.outliers");
        }

        if (method == null) {
            //TODO: i18n
            addProblem("sq.missing.method");
        }

        if (!hasProblems()) {
            this.river    = river;
            this.location = location;
            this.period   = period;
            this.outliers = outliers;
            this.method   = method;
        }
    }


    public CalculationResult calculate() {
        log.debug("SQRelationCalculation.calculate");

        if (hasProblems()) {
            return new CalculationResult(this);
        }

        SedDBSessionHolder.acquire();
        try {
            return internalCalculate();
        }
        finally {
            SedDBSessionHolder.release();
        }
    }

    public interface TransformCoeffs {
        double [] transform(double [] coeffs);
    }

    public static final TransformCoeffs IDENTITY_TRANS =
        new TransformCoeffs() {
            @Override
            public double [] transform(double [] coeffs) {
                return coeffs;
            }
        };

    public static final TransformCoeffs LINEAR_TRANS =
        new TransformCoeffs() {
            @Override
            public double [] transform(double [] coeffs) {
                log.debug("before transform: " + Arrays.toString(coeffs));
                if (coeffs.length == 2) {
                    coeffs = new double [] { Math.exp(coeffs[1]), coeffs[0] };
                }
                log.debug("after transform: " + Arrays.toString(coeffs));
                return coeffs;
            }
        };

    protected CalculationResult internalCalculate() {

        Function powFunction = FunctionFactory
            .getInstance()
            .getFunction(SQ_POW_FUNCTION_NAME);

        if (powFunction == null) {
            log.error("No '" + SQ_POW_FUNCTION_NAME + "' function found.");
            // TODO: i18n
            addProblem("sq.missing.sq.function");
            return new CalculationResult(new SQResult[0], this);
        }

        Function         function;
        SQ.View          sqView;
        SQ.Factory       sqFactory;
        ParameterCreator pc;

        if (NON_LINEAR_FITTING) {
            log.debug("Use non linear fitting.");
            sqView    = SQ.SQ_VIEW;
            sqFactory = SQ.SQ_FACTORY;
            function  = powFunction;
            pc = new ParameterCreator(
                powFunction.getParameterNames(),
                powFunction.getParameterNames(),
                powFunction,
                sqView);
        }
        else {
            log.debug("Use linear fitting.");
            sqView    = LogSQ.LOG_SQ_VIEW;
            sqFactory = LogSQ.LOG_SQ_FACTORY;
            function  = FunctionFactory
                .getInstance()
                .getFunction(SQ_LIN_FUNCTION_NAME);
            if (function == null) {
                log.error("No '" + SQ_LIN_FUNCTION_NAME + "' function found.");
                // TODO: i18n
                addProblem("sq.missing.sq.function");
                return new CalculationResult(new SQResult[0], this);
            }
            pc = new LinearParameterCreator(
                powFunction.getParameterNames(),
                function.getParameterNames(),
                function,
                sqView);
        }

        Measurements measurements =
            MeasurementFactory.getMeasurements(
                river, location, period, sqFactory);

        SQFractionResult [] fractionResults =
            new SQFractionResult[SQResult.NUMBER_FRACTIONS];


        for (int i = 0; i < fractionResults.length; ++i) {
            List<SQ> sqs = measurements.getSQs(i);

            SQFractionResult fractionResult;

            List<SQFractionResult.Iteration> iterations =
                doFitting(function, sqs, sqView, pc);

            if (iterations == null) {
                // TODO: i18n
                addProblem("sq.fitting.failed." + i);
                fractionResult = new SQFractionResult();
            }
            else {
                fractionResult = new SQFractionResult(
                    sqs.toArray(new SQ[sqs.size()]),
                    iterations);
            }

            fractionResults[i] = fractionResult;
        }

        return new CalculationResult(
            new SQResult[] { new SQResult(location, fractionResults) },
            this);
    }

    protected List<SQFractionResult.Iteration> doFitting(
        final Function         function,
        List<SQ>               sqs,
        SQ.View                sqView,
        final ParameterCreator pc
    ) {
        final List<SQFractionResult.Iteration> iterations =
            new ArrayList<SQFractionResult.Iteration>();

        boolean success = new Fitting(function, outliers, sqView).fit(
            sqs,
            method,
            new Fitting.Callback() {
                @Override
                public void afterIteration(
                    double [] coeffs,
                    SQ []     measurements,
                    SQ []     outliers,
                    double    standardDeviation,
                    double    chiSqr
                ) {
                    Parameters parameters = pc.createParameters(
                        coeffs,
                        standardDeviation,
                        chiSqr,
                        measurements);
                    iterations.add(new SQFractionResult.Iteration(
                        parameters,
                        measurements,
                        outliers));
                }
            });

        return success ? iterations : null;
    }

    public static class ParameterCreator {

        protected String [] origNames;
        protected String [] proxyNames;

        protected Function  function;
        protected SQ.View   view;

        public ParameterCreator(
            String [] origNames,
            String [] proxyNames,
            Function  function,
            SQ.View   view
        ) {
            this.origNames  = origNames;
            this.proxyNames = proxyNames;
            this.function   = function;
            this.view       = view;
        }

        protected double [] transformCoeffs(double [] coeffs) {
            return coeffs;
        }

        private static double maxQ(SQ [] sqs) {
            double max = -Double.MAX_VALUE;
            for (SQ sq: sqs) {
                double q = sq.getQ(); // Don't use view here!
                if (q > max) {
                    max = q;
                }
            }
            return Math.max(0d, max);
        }

        private double cFerguson(
            org.dive4elements.river.artifacts.math.Function instance,
            SQ [] sqs
        ) {
            double sqrSum = 0d;

            for (SQ sq: sqs) {
                double s = view.getS(sq);
                double q = view.getQ(sq);
                double diffS = s - instance.value(q);
                sqrSum += diffS*diffS;
            }

            return Math.exp(0.5d * sqrSum/(sqs.length-2));
        }

        private double cDuan(
            org.dive4elements.river.artifacts.math.Function instance,
            SQ [] sqs
        ) {
            double sum = 0d;

            for (SQ sq: sqs) {
                double s = view.getS(sq);
                double q = view.getQ(sq);
                double diffS = s - instance.value(q);
                sum += Math.exp(diffS);
            }
            return sum / sqs.length;
        }

        private double r2(
            org.dive4elements.river.artifacts.math.Function instance,
            SQ [] sqs
        ) {
            double xm = 0;
            double ym = 0;
            for (SQ sq: sqs) {
                double s  = view.getS(sq);
                double q  = view.getQ(sq);
                double fs = instance.value(q);
                xm += s;
                ym += fs;
            }
            xm /= sqs.length;
            ym /= sqs.length;

            double mixXY = 0d;
            double sumX = 0d;
            double sumY = 0d;

            for (SQ sq: sqs) {
                double s  = view.getS(sq);
                double q  = view.getQ(sq);
                double fs = instance.value(q);

                double xDiff = xm - s;
                double yDiff = ym - fs;

                mixXY += xDiff*yDiff;

                sumX += xDiff*xDiff;
                sumY += yDiff*yDiff;
            }

            double r = mixXY/Math.sqrt(sumX*sumY);
            return r*r;
        }


        public Parameters createParameters(
            double [] coeffs,
            double    standardDeviation,
            double    chiSqr,
            SQ []     measurements
        ) {
            String [] columns = StringUtils.join(EXTRA_PARAMETERS, origNames);

            Parameters parameters = new Parameters(columns);
            int row = parameters.newRow();
            parameters.set(row, origNames, transformCoeffs(coeffs));
            parameters.set(row, "chi_sqr", chiSqr);
            parameters.set(row, "std_dev", standardDeviation);
            parameters.set(row, "max_q", maxQ(measurements));

            // We need to instantiate the function to calculate
            // the remaining values.
            org.dive4elements.river.artifacts.math.Function f =
                function.instantiate(coeffs);

            parameters.set(row, "c_ferguson", cFerguson(f, measurements));
            parameters.set(row, "c_duan", cDuan(f, measurements));
            parameters.set(row, "r2", r2(f, measurements));

            return parameters;
        }
    }

    /** We need to transform the coeffs back to the original function. */
    public static class LinearParameterCreator extends ParameterCreator {

        public LinearParameterCreator(
            String [] origNames,
            String [] proxyNames,
            Function  function,
            SQ.View   view
        ) {
            super(origNames, proxyNames, function, view);
        }

        @Override
        protected double [] transformCoeffs(double [] coeffs) {

            int bP = StringUtils.indexOf("m", proxyNames);
            int mP = StringUtils.indexOf("b", proxyNames);

            int aO = StringUtils.indexOf("a", origNames);
            int bO = StringUtils.indexOf("b", origNames);

            if (bP == -1 || mP == -1 || aO == -1 || bO == -1) {
                log.error("index not found: "
                    + bP + " " + mP + " "
                    + aO + " " + bO);
                return coeffs;
            }

            double [] ncoeffs = (double [])coeffs.clone();
            ncoeffs[aO] = Math.exp(coeffs[mP]);
            ncoeffs[bO] = coeffs[bP];

            if (log.isDebugEnabled()) {
                log.debug("before transform: " + Arrays.toString(coeffs));
                log.debug("after transform: " + Arrays.toString(ncoeffs));
            }

            return ncoeffs;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
