/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.math;

public class AddScaleFunction
implements   Function
{
    protected double b;
    protected double m;

    public AddScaleFunction(double b, double m) {
        this.b = b;
        this.m = m;
    }

    @Override
    public double value(double x) {
        return (x + b)*m;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
