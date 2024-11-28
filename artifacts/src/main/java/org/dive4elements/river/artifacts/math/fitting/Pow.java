/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class Pow
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = a*c*Q^(c-1)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double a = parameters[0];
            final double c = parameters[1];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return a*c*Math.pow(Q, c-1);
                }
            };
        }
    };

    public static final Function INSTANCE = new Pow();

    public Pow() {
        super(
            "pow",
            "W(Q) = a*Q^c + d",
            new String [] { "a", "c", "d" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return parameters[0]*Math.pow(x, parameters[1]) + parameters[2];
    }

    @Override
    public double [] gradient(double x, double [] parameters) {
        double a   = parameters[0];
        double c   = parameters[1];
        double x_c = Math.pow(x, c);
        return new double [] {
            x_c,
            a*x_c*Math.log(x),
            1d
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvPow.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
