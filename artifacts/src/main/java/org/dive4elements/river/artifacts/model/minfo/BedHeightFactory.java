/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;


import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SessionHolder;


/** Create BedHeights from database. */
public class BedHeightFactory {
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(BedHeightFactory.class);

    /** Query to get km and ws for wst_id and column_pos. */
    public static final String SQL_SELECT_SINGLE =
        "SELECT bhsv.height, bhsv.station, bhsv.data_gap," +
        "       bhsv.sounding_width, bhs.year" +
        "   FROM bed_height bhs" +
        "       JOIN bed_height_values bhsv on bhsv.bed_height_id = bhs.id";

    public static final String ID_CLAUSE =
        "   WHERE bhs.id = :height_id" +
        "       ORDER BY bhsv.station";

    public static final String ID_STATION_CLAUSE =
        "   WHERE bhs.id = :height_id AND" +
        "         bhsv.station BETWEEN :fromkm AND :tokm" +
        "       ORDER BY bhsv.station";

    /** Query to get name (description) for wst_id. */
    public static final String SQL_SELECT_DESCR_SINGLE =
        "SELECT description FROM bed_height "+
        "WHERE id = :height_id";

    private BedHeightFactory() {
    }

    /**
     * Get BedHeightData for given type and height_id, caring about the cache.
     * If from or to are NaN all values are returned. Otherwise only get
     * values with stations between from and to.
     */
    public static BedHeightData getHeight(
        String type,
        int height_id,
        double from,
        double to
    ) {
        log.debug("BedHeightFactory.getHeight");
        Cache cache = CacheFactory.getCache("bedheight-value-table-static");

        String cacheKey = Integer.toString(height_id) + ":" +
            Double.toString(from) + ":" + Double.toString(to);

        if (cache != null) {
            /* We could be more intelligent here and reuse cached values for
             * a complete river and filter the other stations out afterwards.
             * It might even be better to cache all values first and filter
             * later. */
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static bedheight values from cache");
                return (BedHeightData)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        BedHeightData values = getBedHeightUncached(type, height_id, from, to);

        if (values != null && cacheKey != null) {
            log.debug("Store static bed height values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    /**
     * Get BedHeightData for given type and height_id, caring about the cache.
     */
    public static BedHeightData getHeight(String type, int height_id) {
        return getHeight(type, height_id, Double.NaN, Double.NaN);
    }

    /** Get name for a BedHeight. */
    public static String getHeightName(String type, int height_id) {
        log.debug("BedHeightFactory.getHeightName height_id/" + height_id);

        String name = null;
        Session session = SessionHolder.HOLDER.get();

        SQLQuery nameQuery = null;
        if (type.equals("single")) {
            nameQuery = session.createSQLQuery(SQL_SELECT_DESCR_SINGLE)
                .addScalar("description", StandardBasicTypes.STRING);
            nameQuery.setInteger("height_id", height_id);
        }
        else {
            return "none";
        }
        List<String> names = nameQuery.list();
        if (!names.isEmpty()) {
            name = names.get(0);
        }

        return name;
    }


    /**
     * Get BedHeightData from db.
     *
     * If from or to are negative all stations are returned. Otherwise
     * only the values with a station betweend from and to.
     * @param height_id database id of the bed_height
     * @param from minimum station value or NaN
     * @param to maximum station value or NaN
     * @return according BedHeight.
     */
    public static BedHeightData getBedHeightUncached(
        String type,
        int height_id,
        double from,
        double to)
    {
        if (log.isDebugEnabled()) {
            log.debug("BedHeightFactory.getBedHeightUncached");
        }

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = null;
        if (type.equals("single")) {
            BedHeightData height =
                new BedHeightData(getHeightName(type, height_id));
            String queryString = SQL_SELECT_SINGLE;
            if (Double.isNaN(from) || Double.isNaN(to)) {
                queryString += ID_CLAUSE;
            } else {
                queryString += ID_STATION_CLAUSE;
            }
            sqlQuery = session.createSQLQuery(queryString)
                .addScalar("height", StandardBasicTypes.DOUBLE)
                .addScalar("station", StandardBasicTypes.DOUBLE)
                .addScalar("data_gap", StandardBasicTypes.DOUBLE)
                .addScalar("sounding_width", StandardBasicTypes.DOUBLE)
                .addScalar("year", StandardBasicTypes.INTEGER);
            sqlQuery.setInteger("height_id", height_id);
            if (!Double.isNaN(from) && !Double.isNaN(to)) {
                sqlQuery.setDouble("fromkm", from);
                sqlQuery.setDouble("tokm", to);
            }
            List<Object []> results = sqlQuery.list();

            for (Object [] row: results) {
                log.debug("got station: " + (Double)row[1]);
                Double row0 = row[0] != null ? (Double)row[0] : Double.NaN;
                Double row1 = row[1] != null ? (Double)row[1] : Double.NaN;
                Double row2 = row[2] != null ? (Double)row[2] : Double.NaN;
                Double row3 = row[3] != null ? (Double)row[3] : Double.NaN;
                height.add(row0, row1, row2, row3, (Integer) row[4]);
            }
            return height;
        }
        return new BedHeightData();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
