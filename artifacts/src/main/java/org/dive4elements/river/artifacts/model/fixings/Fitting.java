/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import gnu.trove.TDoubleArrayList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.optimization.fitting.CurveFitter;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.math.GrubbsOutlier;
import org.dive4elements.river.artifacts.math.fitting.Function;

public class Fitting
{
    private static Logger log = LogManager.getLogger(Fitting.class);

    /** Use instance of this factory to find meta infos for outliers. */
    public interface QWDFactory {

        QWD create(double q, double w);

    } // interface QWFactory

    public static final QWDFactory QWD_FACTORY = new QWDFactory() {
        @Override
        public QWD create(double q, double w) {
            return new QWD(q, w);
        }
    };

    protected boolean        checkOutliers;
    protected Function       function;
    protected QWDFactory     qwdFactory;
    protected double         chiSqr;
    protected double []      parameters;
    protected ArrayList<QWI> removed;
    protected QWD []         referenced;
    protected double         standardDeviation;


    public Fitting() {
        removed = new ArrayList<QWI>();
    }

    public Fitting(Function function) {
        this(function, QWD_FACTORY);
    }

    public Fitting(Function function, QWDFactory qwdFactory) {
        this(function, qwdFactory, false);
    }

    public Fitting(
        Function   function,
        QWDFactory qwdFactory,
        boolean    checkOutliers
    ) {
        this();
        this.function      = function;
        this.qwdFactory    = qwdFactory;
        this.checkOutliers = checkOutliers;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public boolean getCheckOutliers() {
        return checkOutliers;
    }

    public void setCheckOutliers(boolean checkOutliers) {
        this.checkOutliers = checkOutliers;
    }

    public double getChiSquare() {
        return chiSqr;
    }

    public void reset() {
        chiSqr     = 0.0;
        parameters = null;
        removed.clear();
        referenced = null;
        standardDeviation = 0.0;
    }

    public boolean hasOutliers() {
        return !removed.isEmpty();
    }

    public List<QWI> getOutliers() {
        return removed;
    }

    public QWI [] outliersToArray() {
        return removed.toArray(new QWI[removed.size()]);
    }

    public QWD [] referencedToArray() {
        return referenced != null ? (QWD [])referenced.clone() : null;
    }

    public double getMaxQ() {
        double maxQ = -Double.MAX_VALUE;
        if (referenced != null) {
            for (QWI qw: referenced) {
                double q = qw.getQ();
                if (q > maxQ) {
                    maxQ = q;
                }
            }
        }
        return maxQ;
    }

    public double [] getParameters() {
        return parameters;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public boolean fit(double [] qs, double [] ws) {

        TDoubleArrayList xs = new TDoubleArrayList(qs.length);
        TDoubleArrayList ys = new TDoubleArrayList(ws.length);

        for (int i = 0; i < qs.length; ++i) {
            if (!Double.isNaN(qs[i]) && !Double.isNaN(ws[i])) {
                xs.add(qs[i]);
                ys.add(ws[i]);
            }
        }

        if (xs.size() < 2) {
            log.warn("Too less points.");
            return false;
        }

        List<Double> inputs = new ArrayList<Double>(xs.size());

        org.dive4elements.river.artifacts.math.Function instance = null;

        LevenbergMarquardtOptimizer lmo = null;

        for (;;) {
            parameters = null;
            for (double tolerance = 1e-10; tolerance < 1e-1; tolerance *= 10d) {

                lmo = new LevenbergMarquardtOptimizer();
                lmo.setCostRelativeTolerance(tolerance);
                lmo.setOrthoTolerance(tolerance);
                lmo.setParRelativeTolerance(tolerance);

                CurveFitter cf = new CurveFitter(lmo);

                for (int i = 0, N = xs.size(); i < N; ++i) {
                    cf.addObservedPoint(xs.getQuick(i), ys.getQuick(i));
                }

                try {
                    parameters = cf.fit(function, function.getInitialGuess());
                    break;
                }
                catch (MathException me) {
                    if (log.isDebugEnabled()) {
                        log.debug("tolerance " + tolerance + " + failed.");
                    }
                }
            }
            if (parameters == null) {
                /*
                log.debug("Parameters is null");
                for (int i = 0, N = xs.size(); i < N; ++i) {
                    log.debug("DATA: " + xs.getQuick(i) + " " + ys.getQuick(i));
                }*/
                return false;
            }

            // This is the paraterized function for a given km.
            instance = function.instantiate(parameters);

            if (!checkOutliers) {
                break;
            }

            inputs.clear();

            for (int i = 0, N = xs.size(); i < N; ++i) {
                double y = instance.value(xs.getQuick(i));
                if (Double.isNaN(y)) {
                    y = Double.MAX_VALUE;
                }
                inputs.add(Double.valueOf(ys.getQuick(i) - y));
            }

            Integer outlier = GrubbsOutlier.findOutlier(inputs);

            if (outlier == null) {
                break;
            }

            int idx = outlier.intValue();
            removed.add(
                qwdFactory.create(
                    xs.getQuick(idx), ys.getQuick(idx)));
            xs.remove(idx);
            ys.remove(idx);
        }

        StandardDeviation stdDev = new StandardDeviation();

        referenced = new QWD[xs.size()];
        for (int i = 0; i < referenced.length; ++i) {
            QWD qwd = qwdFactory.create(xs.getQuick(i), ys.getQuick(i));

            if (qwd == null) {
                log.warn("QW creation failed!");
            }
            else {
                referenced[i] = qwd;
                double dw = (qwd.getW() - instance.value(qwd.getQ()))*100.0;
                qwd.setDeltaW(dw);
                stdDev.increment(dw);
            }
        }

        standardDeviation = stdDev.getResult();

        chiSqr = lmo.getChiSquare();

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
