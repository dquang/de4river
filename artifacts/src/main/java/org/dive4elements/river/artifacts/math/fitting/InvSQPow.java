/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvSQPow
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("Q'(S) = (S/a)^(1/b)/(b*S)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double _1a = 1d/parameters[0];
            final double   b = parameters[1];
            final double _1b = 1d/b;

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double S) {
                    return Math.pow(S*_1a, _1b)/(b*S);
                }
            };
        }
    };
    public static final Function INSTANCE = new InvSQPow();

    public InvSQPow() {
        super(
            "inv-sq-pow",
            "Q(S) = Q=(S/a)^(1/b)",
            new String [] { "a", "b" });
    }

    @Override
    public double value(double S, double [] parameters) {
        double a = parameters[0];
        double b = parameters[1];
        return Math.pow(S/a, 1d/b);
    }

    @Override
    public double [] gradient(double S, double [] parameters) {
        double a     = parameters[0];
        double b     = parameters[1];
        double Sa    = S/a;
        double _1b   = 1d/b;
        double eSa1b = Math.pow(Sa, _1b);
        return new double [] {
            -eSa1b/(a*b),
            -(eSa1b*Math.log(Sa))/(b*b)
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return SQPow.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
