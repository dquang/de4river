/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math.fitting;

public class InvQuad
extends      Function
{
    public static final Derivative DERIVATIVE =
        new Derivative("Q'(W) = 1/sqrt(4*n*(W-b)+m^2)") {

        @Override
        public org.dive4elements.river.artifacts.math.Function
            instantiate(double [] parameters)
        {
                  double n  = parameters[0];
                  double m  = parameters[1];
            final double b  = parameters[2];
            final double n4 = 4d*n;
            final double mm = m*m;

            return new org.dive4elements.river.artifacts.math.Function() {
                @Override
                public double value(double W) {
                    return 1d/Math.sqrt(n4*(W-b)+mm);
                }
            };
        }
    };

    public static final Function INSTANCE = new InvQuad();

    public InvQuad() {
        super(
            "inv-quad",
            "(sqrt(4*n*W-4*b*n+m^2)-m)/(2*n)",
            new String [] { "n", "m", "b" });
    }

    @Override
    public double value(double W, double [] parameters) {
        double n = parameters[0];
        double m = parameters[1];
        double b = parameters[2];
        return (Math.sqrt(4d*n*(W - b) + m*m)-m)/(2d*n);
    }

    @Override
    public double [] gradient(double W, double [] parameters) {
        double n  = parameters[0];
        double m  = parameters[1];
        double b  = parameters[2];
        double Wb = W-b;
        double sn4Wb = Math.sqrt(4d*n*Wb + m*m);
        return new double [] {
            Wb/(n*sn4Wb)-(sn4Wb-m)/(2d*n*n),
            (m/sn4Wb-1d)/(2d*n),
            -1d/sn4Wb
        };
    }

    @Override
    public Derivative getDerivative() {
        return DERIVATIVE;
    }

    @Override
    public Function getInverse() {
        return Quad.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
