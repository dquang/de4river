/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.model.QW;

import java.util.Date;

public class QWI
extends      QW
{
    protected String  description;
    protected Date    date;
    protected boolean interpolated;
    protected int     index;

    public QWI() {
    }

    public QWI(double q, double w) {
        super(q, w);
    }

    public QWI(
        double  q,
        double  w,
        String  description,
        Date    date,
        boolean interpolated,
        int     index
    ) {
        super(q, w);
        this.description  = description;
        this.date         = date;
        this.interpolated = interpolated;
        this.index        = index;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getInterpolated() {
        return interpolated;
    }

    public void setInterpolated(boolean interpolated) {
        this.interpolated = interpolated;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
