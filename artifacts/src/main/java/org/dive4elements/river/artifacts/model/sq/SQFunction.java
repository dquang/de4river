/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.river.artifacts.math.Function;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SQFunction {

    private Function function;

    private double minQ;
    private double maxQ;


    public SQFunction(Function function, double minQ, double maxQ) {
        this.function = function;
        this.minQ     = minQ;
        this.maxQ     = maxQ;
    }


    public Function getFunction() {
        return function;
    }

    public double getMinQ() {
        return minQ;
    }

    public double getMaxQ() {
        return maxQ;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
