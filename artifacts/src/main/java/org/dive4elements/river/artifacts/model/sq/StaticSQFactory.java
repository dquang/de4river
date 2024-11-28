/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Query;
import org.hibernate.Session;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.MeasurementStation;


public class StaticSQFactory
{
    private static final Logger log =
        LogManager.getLogger(StaticSQFactory.class);

    public static final String SQL_SQ =
        "SELECT " +
            "sq.description AS description,"+
            "ti.start_time  AS start_time," +
            "ti.stop_time    AS stop_time, " +
            "ms.name AS station_name, " +
            "CASE WHEN r.km_up = 1 AND ra.b IS NOT NULL " +
                "THEN ra.b " +
                "ELSE ra.a " +
            "END AS station_km, " +
            "ms.measurement_type AS measurement_type, " +
            "sqv.parameter AS parameter, " +
            "sqv.a AS a, " +
            "sqv.b AS b, " +
            "sqv.qmax AS qmax " +
        "FROM sq_relation sq " +
            "JOIN time_intervals ti ON ti.id   = sq.time_interval_id " +
            "JOIN sq_relation_value sqv ON sqv.sq_relation_id = sq.id " +
            "JOIN measurement_station ms " +
                "ON sqv.measurement_station_id = ms.id " +
            "JOIN ranges ra ON ra.id = ms.range_id " +
            "JOIN rivers r ON r.id = ra.river_id ";

    public static final String STATION_CLAUSE =
        "WHERE " +
            "r.name = :river " +
            "AND ms.id = :ms_id ";

    public static final String ID_CLAUSE =
        "WHERE " +
            "sqv.id = :dis_id ";

    private StaticSQFactory() {
    }

    /** Get SQ relations for a measurement station's location.
     * Returns all SQRelations for the location of the station and
     * not just for the station. E.g. for a "Geschiebemessstelle"
     * and a "Schwebstoffmesstelle" at the same place.*/
    public static StaticSQContainer getSQRelationsForLocation(
        String river,
        int mStationId
    ) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from MeasurementStation " +
            "where id=:ms_id")
            .setParameter("ms_id", mStationId);
        MeasurementStation mStation = (MeasurementStation)query.list().get(0);

        /* Take the first container for the station requested. */
        StaticSQContainer retval = getSQRelations(river, mStationId);

        /* And it's companion station */
        MeasurementStation companion = mStation.findCompanionStation();
        if (companion != null) {
            int stationId = companion.getId();
            StaticSQContainer additional = getSQRelations(river, stationId);
            if (additional == null || additional.getSQRelations() == null) {
                /* New one is empty, just take the old one. */
                return retval;
            }
            if (retval == null ||
                retval.getSQRelations() == null ||
                retval.getSQRelations().isEmpty()) {
                /* Old one is empty, just take the new one. */
                return additional;
            }
            for (StaticSQRelation add: additional.getSQRelations()) {
                /* Add SQ relations from new one to old one */
                retval.addSQRelation(add);
            }
        }
        return retval;
    }


    public static StaticSQContainer getSQRelations(
        String river,
        int mStationId
    ) {
        Cache cache = CacheFactory.getCache(StaticSQCacheKey.CACHE_NAME);

        StaticSQCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticSQCacheKey(river, mStationId);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static sq relations from cache");
                return (StaticSQContainer)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        StaticSQContainer values = getUncached(river, mStationId);

        if (values != null && cacheKey != null) {
            log.debug("Store static sq relations in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    public static StaticSQContainer getDistinctRelation(int id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createSQLQuery(SQL_SQ + ID_CLAUSE)
            .addScalar("description")
            .addScalar("start_time")
            .addScalar("stop_time")
            .addScalar("station_name")
            .addScalar("station_km")
            .addScalar("measurement_type")
            .addScalar("parameter")
            .addScalar("a")
            .addScalar("b")
            .addScalar("qmax");

        query.setParameter("dis_id", id);

        /* This could be done nicer with hibernate */
        List<Object []> list = query.list();
        if (list.isEmpty()) {
            log.debug("Query returened nothing");
            return null;
        }
        Object [] row = list.get(0);

        StaticSQContainer sq = new StaticSQContainer();
        sq.setDescription((String)list.get(0)[0]);
        sq.setStationName((String)list.get(0)[3]);
        sq.setKm(((BigDecimal)list.get(0)[4]).doubleValue());

        StaticSQRelation relation = new StaticSQRelation();
        relation.setStartTime((Date)row[1]);
        relation.setStopTime((Date)row[2]);
        relation.setType((String)row[5]);
        relation.setParameter((String)row[6]);
        relation.setA(((BigDecimal)row[7]).doubleValue());
        relation.setB(((BigDecimal)row[8]).doubleValue());
        relation.setQmax(((BigDecimal)row[9]).doubleValue());
        sq.addSQRelation(relation);

        return sq;
    }

    private static StaticSQContainer getUncached(
        String river,
        int mStationId
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createSQLQuery(SQL_SQ + STATION_CLAUSE)
            .addScalar("description")
            .addScalar("start_time")
            .addScalar("stop_time")
            .addScalar("station_name")
            .addScalar("station_km")
            .addScalar("measurement_type")
            .addScalar("parameter")
            .addScalar("a")
            .addScalar("b")
            .addScalar("qmax");

        query.setParameter("river", river);
        query.setParameter("ms_id", mStationId);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.debug("Query returned empty");
            return new StaticSQContainer();
        }

        StaticSQContainer sq = new StaticSQContainer();
        sq.setDescription((String)list.get(0)[0]);
        sq.setStationName((String)list.get(0)[3]);
        sq.setKm(((BigDecimal)list.get(0)[4]).doubleValue());

        for (Object[] row : list) {
            StaticSQRelation relation = new StaticSQRelation();
            relation.setStartTime((Date)row[1]);
            relation.setStopTime((Date)row[2]);
            relation.setType((String)row[5]);
            relation.setParameter((String)row[6]);
            relation.setA(((BigDecimal)row[7]).doubleValue());
            relation.setB(((BigDecimal)row[8]).doubleValue());
            relation.setQmax(((BigDecimal)row[9]).doubleValue());
            sq.addSQRelation(relation);
        }
        return sq;
    }
}
