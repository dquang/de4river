/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.math.Function;

public class FixFunction
{
    protected String   name;
    protected String   description;
    protected Function function;
    protected double   maxQ;

    public FixFunction (
        String   name,
        String   description,
        Function function,
        double   maxQ
    ) {
        this.name        = name;
        this.description = description;
        this.function    = function;
        this.maxQ        = maxQ;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Function getFunction() {
        return function;
    }

    public double getMaxQ() {
        return maxQ;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
