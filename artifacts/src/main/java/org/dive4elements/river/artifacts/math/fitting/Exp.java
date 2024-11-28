/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class Exp
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = a^Q*log(a)*m") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
                  double m     = parameters[0];
            final double a     = parameters[1];
            final double logam = Math.log(a)*m;

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return Math.pow(a, Q)*logam;
                }
            };
        }
    };

    public static final Function INSTANCE = new Exp();

    public Exp() {
        super(
            "exp",
            "W(Q) = m * a^Q + b",
            new String [] { "m", "a", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return parameters[0]*Math.pow(parameters[1], x) + parameters[2];
    }

    @Override
    public double [] gradient(double Q, double [] parameters) {
        double m = parameters[0];
        double a = parameters[1];
        double b = parameters[2];
        return new double [] {
            Math.pow(a, Q),
            Math.pow(a, Q-1d)*m*Q,
            1d
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvExp.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
