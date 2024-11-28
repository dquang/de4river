/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class LogLinear
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = a*m/(m*Q + b)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double a = parameters[0];
            final double m = parameters[1];
            final double b = parameters[2];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return a*m/(m*Q + b);
                }
            };
        }
    };

    public static final Function INSTANCE = new LogLinear();

    public LogLinear() {
        super(
            "log-linear",
            "W(Q) = a*ln(m*Q + b)",
            new String [] { "a", "m", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return parameters[0]*Math.log(parameters[1]*x + parameters[2]);
    }

    @Override
    public double [] gradient(double x, double [] parameters) {
        double a = parameters[0];
        double m = parameters[1];
        double b = parameters[2];

        double lin = m*x + b;

        return new double [] {
            Math.log(lin),
            a*x / lin,
            a / lin
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvLogLinear.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
