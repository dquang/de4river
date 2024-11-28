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
public class WQDataItem extends DefaultDataItem {

    protected double[] qRange;
    protected double[] wRange;

    public WQDataItem() {
    }


    public WQDataItem(
        String   label,
        String   description,
        String   value,
        double[] qRange,
        double[] wRange)
    {
        super(label, description, value);

        this.qRange = qRange;
        this.wRange = wRange;
    }


    public double[] getQRange() {
        return qRange;
    }


    public double[] getWRange() {
        return wRange;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
