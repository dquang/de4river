/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.jfree.chart.axis.NumberAxis;

/** Axis of which label and key differs. */
public class IdentifiableNumberAxis extends NumberAxis {


    protected String key;


    protected IdentifiableNumberAxis(String key, String label) {
        super(label);
        this.key = key;
    }


    public String getId() {
        return key;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
