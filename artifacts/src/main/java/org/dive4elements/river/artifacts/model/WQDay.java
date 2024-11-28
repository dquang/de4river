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
import java.util.Comparator;

import org.dive4elements.river.artifacts.math.Linear;

import gnu.trove.TIntArrayList;

/**
 * This class represents a pool of data triples that consists of 'W', 'Q' and
 * 'Day' data.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQDay
extends      WQ
{
    public static final Comparator<double []> FIRST_CMP =
        new Comparator<double []>() {
        @Override
        public int compare(double [] a, double [] b) {
            double diff = a[0] - b[0];
            if (diff < 0d) return -1;
            if (diff > 0d) return +1;
            return 0;
        }
    };

    public static final double EPSILON = 1e-4;


    protected TIntArrayList days;

    public WQDay() {
        super("");
        days = new TIntArrayList();
    }

    public WQDay(int capacity) {
        super(capacity);
        days = new TIntArrayList(capacity);
    }

    public WQDay(int [] days, double [] ws, double [] qs) {
        super(qs, ws, "");
        this.days = new TIntArrayList(days);
    }


    public void add(int day, double w, double q) {
        super.add(w, q);
        days.add(day);
    }


    public boolean isIncreasing() {
        return size() == 0 || getDay(0) < getDay(size()-1);
    }


    public int getDay(int idx) {
        return days.getQuick(idx);
    }

    private static final Double interpolateX(
        ArrayList<double []> dxs,
        double x
    ) {
        Collections.sort(dxs, FIRST_CMP);

        if (Math.abs(x - dxs.get(0)[1]) < EPSILON) {
            return dxs.get(0)[0];
        }

        for (int i = 1, S = dxs.size(); i < S; ++i) {
            double [] curr = dxs.get(i);
            if (Math.abs(x - curr[1]) < EPSILON) {
                return curr[0];
            }

            double [] prev = dxs.get(i-1);
            double x1 = Math.min(prev[1], curr[1]);
            double x2 = Math.max(prev[1], curr[1]);
            if (x > x1 && x < x2) {
                return Linear.linear(
                    x,
                    prev[1], curr[1],
                    prev[0], curr[0]);
            }
        }

        return null;
    }

    public Double interpolateDayByW(double w) {

        int S = days.size();

        if (S == 0) {
            return null;
        }

        ArrayList<double[]> dws = new ArrayList<double[]>(S);

        for (int i = 0; i < S; ++i) {
            dws.add(new double[] { getDay(i), getW(i) });
        }

        return interpolateX(dws, w);
    }

    public Double interpolateDayByQ(double q) {

        int S = days.size();

        if (S == 0) {
            return null;
        }

        ArrayList<double[]> dqs = new ArrayList<double[]>(S);

        for (int i = 0; i < S; ++i) {
            dqs.add(new double[] { getDay(i), getQ(i) });
        }

        return interpolateX(dqs, q);
    }

    @Override
    public void removeNaNs() {

        int dest = 0;
        int N = ws.size();

        for (int i = 0; i < N; ++i) {
            double wi = ws.getQuick(i);
            double qi = qs.getQuick(i);

            if (Double.isNaN(wi) || Double.isNaN(qi)) {
                continue;
            }

            days.setQuick(dest, days.getQuick(i));
            ws.setQuick(dest, wi);
            qs.setQuick(dest, qi);
            ++dest;
        }

        if (dest < N) {
            days.remove(dest, N-dest);
            ws  .remove(dest, N-dest);
            qs  .remove(dest, N-dest);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
