/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.utils;

import gnu.trove.TLongArrayList;

import java.util.Date;

public class DateAverager
{
   protected TLongArrayList dates;

    public DateAverager() {
        dates = new TLongArrayList();
    }

    public void add(Date date) {
        dates.add(date.getTime());
    }

    public Date getAverage() {
        int N = dates.size();
        if (N == 0) {
            return null;
        }
        long min = dates.min();
        long sum = 0L;
        for (int i = 0; i < N; ++i) {
            sum += dates.getQuick(i) - min;
        }
        return new Date(min + (long)Math.round(sum/(double)N));
    }

    public void clear() {
        dates.resetQuick();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
