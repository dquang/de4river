/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class Linear
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = m") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double m = parameters[0];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return m;
                }
            };
        }
    };

    public static final Function INSTANCE = new Linear();

    public Linear() {
        super("linear", "W(Q) = m*Q + b", new String [] { "m", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return x*parameters[0] + parameters[1];
    }

    @Override
    public double [] gradient(double x, double [] parameters) {
        return new double [] { x, 1d };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvLinear.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
