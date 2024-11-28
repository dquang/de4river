/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;
import java.util.Date;

public class DateRange
implements   Serializable
{
    private static final long serialVersionUID = -2553914795388094818L;

    protected Date from;
    protected Date to;

    public DateRange(Date from, Date to) {
        this.from = from;
        this.to   = to;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    private static final boolean equalDates(Date a, Date b) {
        if (a == null && b != null) return false;
        if (a != null && b == null) return false;
        if (a == null) return true;
        return a.equals(b);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DateRange)) {
            return false;
        }
        DateRange o = (DateRange)other;
        return equalDates(from, o.from) && equalDates(to, o.to);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
