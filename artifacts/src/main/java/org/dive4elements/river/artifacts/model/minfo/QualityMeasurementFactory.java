/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.backend.SedDBSessionHolder;
import org.dive4elements.river.artifacts.model.RiverFactory;


public class QualityMeasurementFactory {

    private static Logger log = LogManager.getLogger(
        QualityMeasurementFactory.class);

    private static final String SQL_BED_MEASUREMENT =
        "SELECT dat.km      as km," +
        "       dat.datum   as datum," +
        "       sp.tiefevon as depth1," +
        "       sp.tiefebis as depth2," +
        "       sa.d10      as d10," +
        "       sa.dm       as dm," +
        "       sa.d16      as d16," +
        "       sa.d20      as d20," +
        "       sa.d25      as d25," +
        "       sa.d30      as d30," +
        "       sa.d40      as d40," +
        "       sa.d50      as d50," +
        "       sa.d60      as d60," +
        "       sa.d70      as d70," +
        "       sa.d75      as d75," +
        "       sa.d80      as d80," +
        "       sa.d84      as d84," +
        "       sa.d90      as d90," +
        "       sa.dmin     as dmin," +
        "       sa.dmax     as dmax " +
        "FROM sohltest dat " +
        "    JOIN station sn ON sn.stationid = dat.stationid " +
        "    JOIN gewaesser gw ON gw.gewaesserid = sn.gewaesserid " +
        "    JOIN sohlprobe sp ON sp.sohltestid = dat.sohltestid " +
        "    JOIN siebanalyse sa ON sa.sohlprobeid = sp.sohlprobeid " +
        "WHERE gw.name = :name AND " +
        "      dat.km IS NOT NULL AND " +
        "      sp.tiefevon IS NOT NULL AND " +
        "      sp.tiefebis IS NOT NULL AND " +
        "      dat.km BETWEEN :from - 0.001 AND :to + 0.001 ";
        // TODO: Test if char diameter ist null.

    private static final String SQL_BEDLOAD_MEASUREMENT =
        "SELECT dat.km    as km," +
        "       dat.datum as datum," +
        "       dat.dm    as dm," +
        "       dat.d10   as d10," +
        "       dat.d16   as d16," +
        "       dat.d20   as d20," +
        "       dat.d25   as d25," +
        "       dat.d30   as d30," +
        "       dat.d40   as d40," +
        "       dat.d50   as d50," +
        "       dat.d60   as d60," +
        "       dat.d70   as d70," +
        "       dat.d75   as d75," +
        "       dat.d80   as d80," +
        "       dat.d84   as d84," +
        "       dat.d90   as d90," +
        "       dat.dmin  as dmin," +
        "       dat.dmax  as dmax " +
        "FROM messung dat" +
        "    JOIN station sn ON sn.stationid = dat.stationid" +
        "    JOIN gewaesser gw ON gw.gewaesserid = sn.gewaesserid " +
        "WHERE gw.name = :name AND " +
        "      dat.km IS NOT NULL AND " +
        "      dat.d10 IS NOT NULL AND" + //TODO: Add all other char. diameter.
        "      dat.km BETWEEN :from - 0.001 AND :to + 0.001 ";

    private static final String SQL_WHERE_DATE =
        "AND dat.datum BETWEEN :start AND :end ";

    private static final String SQL_ORDER_BY = "ORDER BY dat.km";


    /** Transform query result into objects, use INSTANCE singleton. */
    public static final class QualityMeasurementResultTransformer
    extends BasicTransformerAdapter {

        // Make a singleton
        public static QualityMeasurementResultTransformer INSTANCE =
            new QualityMeasurementResultTransformer();

        private QualityMeasurementResultTransformer() {
        }

        /** tuples is a row. */
        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            Map<String, Double> map = new HashMap<String, Double>();
            double km = 0;
            Date d = null;
            double depth1 = Double.NaN;
            double depth2 = Double.NaN;
            for (int i = 0; i < tuple.length; ++i) {
                if (tuple[i] != null) {
                    if (aliases[i].equals("km")) {
                        km = ((Number) tuple[i]).doubleValue();
                    }
                    else if (aliases[i].equals("datum")) {
                        d = (Date) tuple[i];
                    }
                    else if (aliases[i].equals("depth1")) {
                        depth1 = ((Number) tuple[i]).doubleValue();
                    }
                    else if (aliases[i].equals("depth2")) {
                        depth2 = ((Number) tuple[i]).doubleValue();
                    }
                    else {
                        map.put(aliases[i], ((Double) tuple[i])/1000);
                    }
                }
            }
            return new QualityMeasurement(km, d, depth1, depth2, map);
        }
    } // class BasicTransformerAdapter

    private QualityMeasurementFactory() {
    }

    private static SQLQuery baseQuery(
        Session session,
        String river,
        double from,
        double to,
        String statement
    ) {
        SQLQuery query = session.createSQLQuery(statement)
            .addScalar("km", StandardBasicTypes.DOUBLE)
            .addScalar("datum", StandardBasicTypes.DATE)
            .addScalar("dm", StandardBasicTypes.DOUBLE)
            .addScalar("d10", StandardBasicTypes.DOUBLE)
            .addScalar("d16", StandardBasicTypes.DOUBLE)
            .addScalar("d20", StandardBasicTypes.DOUBLE)
            .addScalar("d25", StandardBasicTypes.DOUBLE)
            .addScalar("d30", StandardBasicTypes.DOUBLE)
            .addScalar("d40", StandardBasicTypes.DOUBLE)
            .addScalar("d50", StandardBasicTypes.DOUBLE)
            .addScalar("d60", StandardBasicTypes.DOUBLE)
            .addScalar("d70", StandardBasicTypes.DOUBLE)
            .addScalar("d75", StandardBasicTypes.DOUBLE)
            .addScalar("d80", StandardBasicTypes.DOUBLE)
            .addScalar("d84", StandardBasicTypes.DOUBLE)
            .addScalar("d90", StandardBasicTypes.DOUBLE)
            .addScalar("dmin", StandardBasicTypes.DOUBLE)
            .addScalar("dmax", StandardBasicTypes.DOUBLE);

        if (statement.startsWith(SQL_BED_MEASUREMENT)) {
            query.addScalar("depth1", StandardBasicTypes.DOUBLE);
            query.addScalar("depth2", StandardBasicTypes.DOUBLE);
        }

        String seddbRiver = RiverFactory.getRiver(river).nameForSeddb();

        query.setString("name", seddbRiver);
        query.setDouble("from", from);
        query.setDouble("to", to);

        query.setResultTransformer(
            QualityMeasurementResultTransformer.INSTANCE);

        return query;
    }

    protected static QualityMeasurements load(
        Session session,
        String river,
        double from,
        double to,
        String statement
    ) {
        SQLQuery query = baseQuery(session, river, from, to, statement);

        return new QualityMeasurements(query.list());
    }

    protected static QualityMeasurements load(
        Session session,
        String river,
        double from,
        double to,
        Date start,
        Date end,
        String statement
    ) {
        SQLQuery query = baseQuery(session, river, from, to, statement);

        query.setDate("start", start);
        query.setDate("end", end);

        return new QualityMeasurements(query.list());
    }


    public static QualityMeasurements getBedMeasurements(
        String river,
        double from,
        double to
    ) {
        Session session = SedDBSessionHolder.HOLDER.get();
        return load(session, river, from, to,
            SQL_BED_MEASUREMENT + SQL_ORDER_BY);
    }

    public static QualityMeasurements getBedloadMeasurements(
        String river,
        double from,
        double to
    ) {
        Session session = SedDBSessionHolder.HOLDER.get();
        return load(session, river, from, to,
            SQL_BEDLOAD_MEASUREMENT + SQL_ORDER_BY);
    }

    public static QualityMeasurements getBedMeasurements(
        String river,
        double from,
        double to,
        Date start,
        Date end
    ) {
        Session session = SedDBSessionHolder.HOLDER.get();
        return load(session, river, from, to, start, end,
            SQL_BED_MEASUREMENT + SQL_WHERE_DATE + SQL_ORDER_BY);
    }

    public static QualityMeasurements getBedloadMeasurements(
        String river,
        double from,
        double to,
        Date start,
        Date end
    ) {
        Session session = SedDBSessionHolder.HOLDER.get();
        return load(
            session,
            river,
            from,
            to,
            start,
            end,
            SQL_BEDLOAD_MEASUREMENT + SQL_WHERE_DATE + SQL_ORDER_BY);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
