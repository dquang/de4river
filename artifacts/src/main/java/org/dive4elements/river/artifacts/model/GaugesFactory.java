/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;
import java.util.ArrayList;

import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.Range;

import org.hibernate.Session;
import org.hibernate.Query;

public class GaugesFactory
{
    public static List<Gauge> getGauges(River river) {
        return getGauges(river.getName());
    }


    public static Gauge getGauge(String gaugeName) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Gauge where name=:name");
        query.setParameter("name", gaugeName);

        List<Gauge> res = query.list();

        return res.isEmpty() ? null : res.get(0);
    }


    public static List<Gauge> getGauges(String river) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Gauge where river.name=:name");
        query.setParameter("name", river);
        return query.list();
    }

    public static List<Gauge> filterRanges(
        List<Gauge>     gauges,
        List<double []> ranges
    ) {
        // XXX: Inefficent!
        ArrayList<Range> rs = new ArrayList<Range>();
        for (double [] range: ranges) {
            double a = range[0];
            double b = range[1];
            rs.add(new Range(Math.min(a, b), Math.max(a, b), null));
        }
        return filter(gauges, rs);
    }

    public static List<Gauge> filter(List<Gauge> gauges, List<Range> ranges) {
        // TODO: Make it an HQL filter!
        ArrayList<Gauge> out = new ArrayList<Gauge>();
        for (Gauge gauge: gauges) {
            Range range = gauge.getRange();
            for (Range cmp: ranges) {
                if (range.intersects(cmp)) {
                    out.add(gauge);
                    break;
                }
            }
        }
        return out;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
