/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvExp
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("W'(Q) = 1/(log(a)*(Q-b))") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double a = parameters[1];
            final double b = parameters[2];
            final double loga = Math.log(a);

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double Q) {
                    return 1d/(loga*(Q-a));
                }
            };
        }
    };

    public static final Function INSTANCE = new InvExp();

    public InvExp() {
        super(
            "inv-exp",
            "Q(W) = log((W-b)/m)/log(a)",
            new String [] { "m", "a", "b" });
    }

    @Override
    public double value(double W, double [] parameters) {
        double m = parameters[0];
        double a = parameters[1];
        double b = parameters[2];
        return Math.log((W-b)/m)/Math.log(a);
    }

    @Override
    public double [] gradient(double Q, double [] parameters) {
        double m = parameters[0];
        double a = parameters[1];
        double b = parameters[2];
        double loga = Math.log(a);
        return new double [] {
            -1d/(loga*m),
            -Math.log((Q-b)/m)/(a*loga*loga),
            -1d/(loga*(Q-b))
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return Exp.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
