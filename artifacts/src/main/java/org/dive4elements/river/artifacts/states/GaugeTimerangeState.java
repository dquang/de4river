/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.utils.RiverUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GaugeTimerangeState extends IntRangeState {

    /** Private log. */
    private static final Logger log =
            LogManager.getLogger(GaugeTimerangeState.class);


    /** Get 'min' and 'max'times of gauge time intervals. */
    protected long[] getLowerUpper(D4EArtifact flys) {
        Gauge gauge = RiverUtils.getReferenceGauge(flys);

        if (gauge == null) {
            log.warn("No reference gauge specified!");
            return new long[] { 0, 0 };
        }

        Session session = SessionHolder.HOLDER.get();

        SQLQuery query = session.createSQLQuery(
                "SELECT min(start_time) as min, max(stop_time) as max " +
                        "FROM time_intervals WHERE id in " +
                        "(SELECT time_interval_id FROM discharge_tables " +
                "WHERE kind <> 0 and gauge_id =:gid)");

        query.addScalar("min", StandardBasicTypes.CALENDAR);
        query.addScalar("max", StandardBasicTypes.CALENDAR);

        query.setInteger("gid", gauge.getId());

        List<?> results = query.list();

        if (!results.isEmpty()) {
            Object[] res = (Object[]) results.get(0);

            Calendar lo = (Calendar) res[0];
            Calendar up = (Calendar) res[1];

            if (lo != null && up != null) {
                return new long[] { lo.getTimeInMillis(),
                                    up.getTimeInMillis() };
            }
        }

        log.warn("Could not determine time range for gauge: " + gauge.getName()
                + " id: " + gauge.getId());

        return null;
    }


    @Override
    protected Object getLower(D4EArtifact flys) {
        long[] lowerUpper = getLowerUpper(flys);

        return lowerUpper != null ? lowerUpper[0] : 0;
    }


    @Override
    protected Object getUpper(D4EArtifact flys) {
        long[] lowerUpper = getLowerUpper(flys);

        return lowerUpper != null ? lowerUpper[1] : 0;
    }


    @Override
    protected String getUIProvider() {
        return "gaugetimerange";
    }

    @Override
    protected String getType() {
        return "longrange";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
