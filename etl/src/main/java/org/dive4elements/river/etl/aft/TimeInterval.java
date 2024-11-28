/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import java.util.Date;

public class TimeInterval
implements   Comparable<TimeInterval>
{
    protected int  id;
    protected Date start;
    protected Date stop;

    public TimeInterval() {
    }

    public TimeInterval(Date start, Date stop) {
        this.start = start;
        this.stop  = stop;
    }

    public TimeInterval(int id, Date start, Date stop) {
        this(start, stop);
        this.id    = id;
    }

    protected static int compare(Date d1, Date d2) {
        long s1 = d1 != null ? d1.getTime()/1000L : 0L;
        long s2 = d2 != null ? d2.getTime()/1000L : 0L;
        long diff = s1 - s2;
        return diff < 0L
            ? -1
            : diff > 0L ? 1 : 0;
    }

    @Override
    public int compareTo(TimeInterval other) {
        int cmp = compare(start, other.start);
        return cmp != 0
            ? cmp
            : compare(stop, other.stop);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStop() {
        return stop;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return "[TimeInterval: start=" + start + ", stop=" + stop + "]";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
