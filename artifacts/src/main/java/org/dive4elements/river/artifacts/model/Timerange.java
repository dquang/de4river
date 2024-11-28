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


/**
 * This class represents time ranges specified by start and end time. Start and
 * end times are stored as long (number of milliseconds since january 1, 1970).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Timerange implements Serializable {

    private long start;
    private long end;


    public Timerange(long start, long end) {
        this.start = start;
        this.end   = end;
    }


    public Timerange(Date start, Date stop) {
        this.start = start.getTime();
        this.end   = stop != null ? stop.getTime() : System.currentTimeMillis();
    }


    public long getStart() {
        return start;
    }


    public long getEnd() {
        return end;
    }

    public void sort() {
        if (start > end) {
            long t = start; start = end; end = t;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
