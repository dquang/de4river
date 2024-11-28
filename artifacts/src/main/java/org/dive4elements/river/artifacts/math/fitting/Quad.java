/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class Quad
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = 2*n*Q+m") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double n2 = 2d*parameters[0];
            final double m  = parameters[1];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return n2*Q+m;
                }
            };
        }
    };

    public static final Function INSTANCE = new Quad();

    public Quad() {
        super(
            "quad",
            "W(Q) = n*Q^2 + m*Q + b",
            new String [] { "n", "m", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        // n*Q^2 + m*Q + b <=> Q*(n*Q + m) + b
        return x*(parameters[0]*x + parameters[1]) + parameters[2];
    }

    @Override
    public double [] gradient(double x, double [] parameters) {
        return new double [] { x*x, x, 1d };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvQuad.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
