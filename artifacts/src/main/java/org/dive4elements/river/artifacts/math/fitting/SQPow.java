/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class SQPow
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("S'(Q) = a*b*Q^(b-1)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double a = parameters[0];
            final double b = parameters[1];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return a*b*Math.pow(Q, b-1);
                }
            };
        }
    };

    public static final Function INSTANCE = new SQPow();

    public SQPow() {
        super(
            "sq-pow",
            "S(Q) = a*Q^b",
            new String [] { "a", "b" });
    }

    @Override
    public double value(double x, double [] parameters) {
        return parameters[0]*Math.pow(x, parameters[1]);
    }

    @Override
    public double [] gradient(double q, double [] parameters) {
        double a   = parameters[0];
        double b   = parameters[1];
        double q_b = Math.pow(q, b);
        return new double [] {
            q_b,
            a*q_b*Math.log(q),
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return InvSQPow.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
