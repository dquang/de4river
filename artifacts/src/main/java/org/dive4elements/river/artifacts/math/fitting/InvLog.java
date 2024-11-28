/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvLog
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("Q'(W) = exp(W/m)/m") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
            final double _1m = 1d / parameters[0];

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double W) {
                    return Math.exp(W*_1m)*_1m;
                }
            };
        }
    };

    public static final Function INSTANCE = new InvLog();

    public InvLog() {
        super("inv-log",  "Q(W) = e^(W/m) - b", new String [] { "m", "b" });
    }

    @Override
    public double value(double w, double [] parameters) {
        double m = parameters[0];
        double b = parameters[1];
        return Math.exp(w/m) - b;
    }

    @Override
    public double [] gradient(double w, double [] parameters) {
        double m   = parameters[0];
        double b   = parameters[1];
        double ewm = Math.exp(w/m);
        return new double [] { -w*ewm/(m*m), -1 };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return Log.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
