/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvLogLinear
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("Q'(W) = e^(W/a)/(a*m)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double _1a = 1d/parameters[0];
            final double _1am = 1d/(parameters[0] * parameters[1]);

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double W) {
                    return Math.exp(W*_1a)*_1am;
                }
            };
        }
    };
    public static final Function INSTANCE = new InvLogLinear();

    public InvLogLinear() {
        super(
            "inv-log-linear",
            "Q(W)=(e^(W/a)-b)/m",
            new String [] { "a", "m", "b" });
    }

    @Override
    public double value(double W, double [] parameters) {
        double a = parameters[0];
        double m = parameters[1];
        double b = parameters[2];
        return (Math.exp(W/a) - b)/m;
    }

    @Override
    public double [] gradient(double W, double [] parameters) {
        double a   = parameters[0];
        double m   = parameters[1];
        double b   = parameters[2];
        double eWa = Math.exp(W/a);
        return new double [] {
            -(W*eWa)/(a*a*m)
            -(eWa-b)/(m*m),
            -1/m
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return LogLinear.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
