/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.extreme;

import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.artifacts.math.NaNFunction;
import org.dive4elements.river.artifacts.math.UnivariateRealFunctionFunction;

import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import java.io.Serializable;

import java.lang.ref.SoftReference;

import org.apache.commons.math.analysis.interpolation.SplineInterpolator;

import org.apache.commons.math.exception.MathIllegalArgumentException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** An extrapolating W/Q function/curve. */
public class Curve
implements   Serializable, Function
{
    private static Logger log = LogManager.getLogger(Curve.class);

    protected double [] qs;
    protected double [] ws;
    protected String    function;
    protected double [] coeffs;
    protected double    chiSqr;

    /** Suggested maximum value for q to input. */
    protected double    suggestedMaxQ;

    // The spline is pretty heavy weight so cache it with a soft ref only.
    protected transient SoftReference<Function> spline;
    protected transient Function                extrapolation;

    public Curve() {
    }

    public Curve(
        double [] qs,
        double [] ws,
        String    function,
        double [] coeffs,
        double    chiSqr
    ) {
        this.qs       = qs;
        this.ws       = ws;
        this.function = function;
        this.coeffs   = coeffs;
        this.suggestedMaxQ = Double.MAX_VALUE;
    }

    public double [] getQs() {
        return qs;
    }

    public double [] getWs() {
        return ws;
    }

    public String getFunction() {
        return function;
    }

    public double [] getCoeffs() {
        return coeffs;
    }


    public void setSuggestedMaxQ(double newMaxQ) {
        this.suggestedMaxQ = newMaxQ;
    }


    public double getSuggestedMaxQ() {
        return this.suggestedMaxQ;
    }


    /** Calculate value at given x. */
    @Override
    public double value(double x) {
        if (qs == null || x < qs[0]) return Double.NaN;
        return (x <= qs[qs.length-1]
            ? getSpline()
            : getExtrapolation()).value(x);
    }

    protected synchronized Function getExtrapolation() {
        if (extrapolation == null) {
            org.dive4elements.river.artifacts.math.fitting.Function
                f = FunctionFactory.getInstance().getFunction(function);

            extrapolation = f != null
                ? f.instantiate(coeffs)
                : NaNFunction.INSTANCE;
        }
        return extrapolation;
    }

    /**
     * Gets the chiSqr for this instance.
     *
     * @return The chiSqr.
     */
    public double getChiSqr() {
        return this.chiSqr;
    }

    /**
     * Sets the chiSqr for this instance.
     *
     * @param chiSqr The chiSqr.
     */
    public void setChiSqr(double chiSqr) {
        this.chiSqr = chiSqr;
    }

    protected synchronized Function getSpline() {
        Function sp;
        if (spline != null) {
            if ((sp = spline.get()) != null) {
                return sp;
            }
        }
        spline = new SoftReference<Function>(sp = createSpline());
        return sp;
    }

    protected Function createSpline() {
        SplineInterpolator interpolator = new SplineInterpolator();
        try {
            return new UnivariateRealFunctionFunction(
                interpolator.interpolate(qs, ws));
        }
        catch (MathIllegalArgumentException miae) {
            log.debug("creation on spline failed", miae);
            return NaNFunction.INSTANCE;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
