/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvLinear
extends      Function
{
    public static Function INSTANCE = new InvLinear();

    public static final Derivative DERIVATIVE =
        new Derivative("Q'(W) = 1/m") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double _1m = 1d/parameters[0];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return _1m;
                }
            };
        }
    };

    public InvLinear() {
        super("inv-linear", "W(Q) = (Q-b)/m", new String [] { "m", "b" });
    }

    @Override
    public double value(double Q, double [] parameters) {
        double m = parameters[0];
        double b = parameters[1];
        return (Q-b)/m;
    }

    @Override
    public double [] gradient(double Q, double [] parameters) {
        double m = parameters[0];
        double b = parameters[1];
        return new double [] {
            -(Q-b)/(m*m),
            -1d/m
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return Linear.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
