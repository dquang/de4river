/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.event;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class RangeFilterEvent {

    protected Float from;
    protected Float to;

    private static Float asFloat(NumberFormat nf, String x) {
        try {
            return Float.valueOf((float)nf.parse(x));
        }
        catch (NumberFormatException nfe) {
            return Float.NaN;
        }
    }


    public RangeFilterEvent(String from, String to) {
        NumberFormat nf = NumberFormat.getDecimalFormat();

        this.from = asFloat(nf, from);
        this.to = asFloat(nf, to);
    }


    public Float getFrom() {
        return this.from;
    }


    public Float getTo() {
        return this.to;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
