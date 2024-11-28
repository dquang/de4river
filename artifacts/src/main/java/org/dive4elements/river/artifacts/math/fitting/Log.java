/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class Log
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = m/(Q+b)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double m = parameters[0];
            final double b = parameters[1];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return m/(Q+b);
                }
            };
        }
    };

    public static final Function INSTANCE = new Log();

    public Log() {
        super("log",  "W(Q) = m*ln(Q + b)", new String [] { "m", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return parameters[0]*Math.log(x + parameters[1]);
    }

    @Override
    public double [] gradient(double x, double [] parameters) {
        double m = parameters[0];
        double b = parameters[1];
        double b_x = b + x;
        return new double [] {
            Math.log(b_x),
            m/b_x
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvLog.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
