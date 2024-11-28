/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A collection of triples W,Q,Timerange.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQTimerange extends WQ {

    /** Used to sort &lt;w,q,timerange&gt; triples. */
    public static class TimerangeItem implements Comparable<TimerangeItem> {
        public double q;
        public double w;
        public Timerange timerange;

        public TimerangeItem (Timerange timerange, double q, double w) {
            this.timerange = timerange;
            this.q = q;
            this.w = w;
        }

        /** Sets [w,q] in wq. */
        public double[] get(double[] wq) {
            if (wq.length >= 2) {
                wq[0] = w;
                wq[1] = q;
            }

            return wq;
        }

        @Override
        public int compareTo(TimerangeItem other) {
            if (other.timerange.getStart() < timerange.getStart()) {
                return 1;
            }
            else if (other.timerange.getStart() > timerange.getStart()) {
                return -1;
            }
            else if (other.timerange.getEnd() < timerange.getEnd()) {
                return 1;
            }
            else if (other.timerange.getEnd() > timerange.getEnd()){
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    protected List<Timerange> timeranges;


    public WQTimerange() {
        super("");
    }


    public WQTimerange(String name) {
        super(name);
        timeranges = new ArrayList<Timerange>();
    }


    public void add(double w, double q, Timerange t) {
        ws.add(w);
        qs.add(q);
        timeranges.add(t);
    }


    public Timerange getTimerange(int idx) {
        return timeranges.get(idx);
    }


    public Timerange[] getTimeranges() {
        return timeranges.toArray(new Timerange[timeranges.size()]);
    }

    public List<TimerangeItem> sort() {
        ArrayList<TimerangeItem> items =
            new ArrayList<TimerangeItem>(timeranges.size());
        for (int i = 0, n = size(); i < n; i++) {
            items.add(new TimerangeItem(getTimerange(i), getQ(i), getW(i)));
        }

        Collections.sort(items);
        return items;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
