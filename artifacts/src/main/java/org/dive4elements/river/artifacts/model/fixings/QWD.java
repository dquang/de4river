/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import java.util.Date;

public class QWD
extends      QWI
{
    protected double deltaW;

    public QWD() {
    }

    public QWD(double q, double w) {
        super(q, w);
    }

    public QWD(
        double  q,
        double  w,
        String  description,
        Date    date,
        boolean interpolated,
        double  deltaW,
        int     index
    ) {
        super(q, w, description, date, interpolated, index);
        this.deltaW = deltaW;
    }

    public double getDeltaW() {
        return deltaW;
    }

    public void setDeltaW(double deltaW) {
        this.deltaW = deltaW;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
