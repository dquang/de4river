/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import gnu.trove.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A subclass of WQTimerange that stores besides W, Q and Timerange values
 * another double value (difference to something).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalWQTimerange extends WQTimerange {

    public static class HistoricalTimerangeItem extends TimerangeItem {
        public double diff;

        public HistoricalTimerangeItem(
            Timerange timerange,
            double q,
            double w,
            double diff
        ) {
            super(timerange, q, w);
            this.diff = diff;
        }

        public double[] get(double[] wq) {
            if (wq.length >= 3) {
                wq[0] = w;
                wq[1] = q;
                wq[2] = diff;
            }
            else if (wq.length >= 2) {
                return super.get(wq);
            }

            return wq;
        }
    }

    protected TDoubleArrayList diffs;


    public HistoricalWQTimerange(String name) {
        super(name);

        diffs = new TDoubleArrayList();
    }


    public void add(double w, double q, double diff, Timerange t) {
        ws.add(w);
        qs.add(q);
        timeranges.add(t);
        diffs.add(diff);
    }


    /**
     * This method requires a 3dim double array for <i>res</i>!
     */
    @Override
    public double[] get(int idx, double[] res) {
        res[0] = ws.getQuick(idx);
        res[1] = qs.getQuick(idx);
        res[2] = diffs.getQuick(idx);

        return res;
    }


    public double[] getDiffs() {
        return diffs.toNativeArray();
    }

    @Override
    public List<TimerangeItem> sort() {
        ArrayList<TimerangeItem> items =
            new ArrayList<TimerangeItem>(timeranges.size());
        for (int i = 0, n = size(); i < n; i++) {
            items.add(new HistoricalTimerangeItem(
                    getTimerange(i), getQ(i), getW(i), diffs.get(i)));
        }

        Collections.sort(items);
        return items;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
