/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

import org.apache.commons.math.FunctionEvaluationException;

import org.apache.commons.math.analysis.UnivariateRealFunction;

public final class UnivariateRealFunctionFunction
implements         Function
{
    private UnivariateRealFunction function;

    public UnivariateRealFunctionFunction(UnivariateRealFunction function) {
        this.function = function;
    }

    @Override
    public double value(double x) {
        try {
            return function.value(x);
        }
        catch (FunctionEvaluationException fee) {
            return Double.NaN;
        }
    }

    public UnivariateRealFunction getFunction() {
        return function;
    }

    public void setFunction(UnivariateRealFunction function) {
        this.function = function;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
