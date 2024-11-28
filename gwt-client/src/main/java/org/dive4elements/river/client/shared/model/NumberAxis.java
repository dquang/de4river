/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class NumberAxis implements Axis {

    protected int pos;

    protected double from;
    protected double to;

    protected double min;
    protected double max;


    public NumberAxis() {
    }


    public NumberAxis(int pos, double from, double to, double min, double max) {
        this.pos  = pos;
        this.from = from;
        this.to   = to;
        this.min  = min;
        this.max  = max;
    }


    @Override
    public int getPos() {
        return pos;
    }


    @Override
    public Number getFrom() {
        return from;
    }


    @Override
    public Number getTo() {
        return to;
    }


    @Override
    public Number getMin() {
        return min;
    }


    @Override
    public Number getMax() {
        return max;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
