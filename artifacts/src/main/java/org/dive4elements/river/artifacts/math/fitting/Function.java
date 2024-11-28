/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

import org.apache.commons.math.FunctionEvaluationException;

import org.apache.commons.math.optimization.fitting.ParametricRealFunction;

import org.dive4elements.river.utils.DoubleUtil;

public abstract class Function
implements            ParametricRealFunction
{
    protected String    name;
    protected String    description;
    protected String [] parameterNames;
    protected double [] initialGuess;

    public static abstract class Derivative {

        protected String description;

        public Derivative() {
        }

        public Derivative(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public abstract org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters);

    } // interface Derivative

    public Function() {
    }

    public Function(
        String    name,
        String    description,
        String [] parameterNames
    ) {
        this(name,
            description,
            parameterNames,
            DoubleUtil.fill(parameterNames.length, 1d));
    }

    public Function(
        String    name,
        String    description,
        String [] parameterNames,
        double [] initialGuess
    ) {
        this.name           = name;
        this.description    = description;
        this.parameterNames = parameterNames;
        this.initialGuess   = initialGuess;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String [] getParameterNames() {
        return parameterNames;
    }

    public double [] getInitialGuess() {
        return initialGuess;
    }

    public org.dive4elements.river.artifacts.math.Function instantiate(
        final double [] parameters
    ) {
        return new org.dive4elements.river.artifacts.math.Function() {

            @Override
            public double value(double x) {
                try {
                    return Function.this.value(x, parameters);
                }
                catch (FunctionEvaluationException fee) {
                    return Double.NaN;
                }
            }
        };
    }

    public abstract Derivative getDerivative();

    public abstract Function getInverse();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
