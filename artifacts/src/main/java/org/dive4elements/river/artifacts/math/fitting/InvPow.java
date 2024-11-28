/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvPow
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("Q'(W) = ((W-d)/a)^(1/c)/(c*(W-d))") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
                  double  a = parameters[0];
            final double  c = parameters[1];
            final double  d = parameters[2];
            final double _1a = 1d/a;
            final double _1c = 1d/c;

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double W) {
                    double Wd = W-d;
                    return Math.pow(Wd*_1a, _1c)/(c*Wd);
                }
            };
        }
    };

    public static final Function INSTANCE = new InvPow();

    public InvPow() {
        super(
            "pow",
            "Q(W) = ((W-d)/a)^(1/c)",
            new String [] { "a", "c", "d" });
    }

    @Override
    public double value(double W, double [] parameters) {
        double a = parameters[0];
        double c = parameters[1];
        double d = parameters[2];
        return Math.pow((W-d)/a, 1d/c);
    }

    @Override
    public double [] gradient(double W, double [] parameters) {
        double a = parameters[0];
        double c = parameters[1];
        double d = parameters[2];
        double _1c = 1d/c;
        double Wdac = Math.pow((W-d)/a, 1d/c);
        double Wd = W-d;
        return new double [] {
            -Wdac/(a*c),
            (Wdac*Math.log(Wd/a))/(c*c),
            -Wdac/(c*Wd)
        };
    }

    @Override
    public Derivative getDerivative() {
        // TODO: Implement me!
        return null;
    }

    @Override
    public Function getInverse() {
        return Pow.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
