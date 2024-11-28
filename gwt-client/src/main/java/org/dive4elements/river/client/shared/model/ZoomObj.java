/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


public class ZoomObj implements Serializable {

    protected Number a;
    protected Number b;
    protected Number c;
    protected Number d;


    public ZoomObj() {
    }


    public ZoomObj(Number a, Number b, Number c, Number d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }


    public Number[] getZoom() {
        return new Number[] { a, b, c, d };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
