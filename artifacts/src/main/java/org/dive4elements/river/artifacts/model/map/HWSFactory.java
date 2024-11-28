/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.hibernatespatial.GeometryUserType;

import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SessionHolder;


public class HWSFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(HWSFactory.class);

    private static final int HWS_LINES = 0;
    private static final int HWS_POINTS = 1;

    public static final String SQL_SELECT_LINES =
        "SELECT hl.name, hl.geom, hl.id, hl.kind_id, hl.official, " +
        "       fs.name AS fed, hl.description " +
        "   FROM hws_lines hl" +
        "       JOIN rivers r ON hl.river_id = r.id" +
        "       LEFT JOIN fed_states fs ON hl.fed_state_id = fs.id" +
        "   WHERE r.name = :river";

    public static final String SQL_SELECT_POINTS =
        "SELECT hp.name, hp.geom, hp.id, hp.kind_id, hp.official, " +
        "       fs.name AS fed, hp.description " +
        "   FROM hws_points hp" +
        "       JOIN rivers r ON hp.river_id = r.id" +
        "       LEFT JOIN fed_states fs ON hp.fed_state_id = fs.id" +
        "   WHERE r.name = :river";


    private HWSFactory() {
    }


    public static HWSContainer getHWSLines(String river) {
        log.debug("HWSFactory.getHWS");
        Cache cache = CacheFactory.getCache(StaticHWSCacheKey.CACHE_NAME);

        StaticHWSCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticHWSCacheKey(river, HWS_LINES);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static hws values from cache");
                return (HWSContainer)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        HWSContainer values = getHWSLinesUncached(river);

        if (values != null && cacheKey != null) {
            log.debug("Store static hws values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    public static HWSContainer getHWSPoints(String river) {
        log.debug("HWSFactory.getHWS");
        Cache cache = CacheFactory.getCache(StaticHWSCacheKey.CACHE_NAME);

        StaticHWSCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticHWSCacheKey(river, HWS_LINES);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static hws values from cache");
                return (HWSContainer)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        HWSContainer values = getHWSPointsUncached(river);

        if (values != null && cacheKey != null) {
            log.debug("Store static hws values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    private static HWSContainer getHWSLinesUncached(String river) {
        if (log.isDebugEnabled()) {
            log.debug("HWSFactory.getHWSLinesUncached");
        }

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = null;
        HWSContainer container = new HWSContainer();
        container.setRiver(river);
        container.setType(HWS.TYPE.LINE);
        sqlQuery = session.createSQLQuery(SQL_SELECT_LINES)
            .addScalar("name", StandardBasicTypes.STRING)
            .addScalar("geom", GeometryUserType.TYPE)
            .addScalar("id", StandardBasicTypes.STRING)
            .addScalar("kind_id", StandardBasicTypes.INTEGER)
            .addScalar("official", StandardBasicTypes.INTEGER)
            .addScalar("fed", StandardBasicTypes.STRING)
            .addScalar("description", StandardBasicTypes.STRING);

        sqlQuery.setString("river", river);
        List<Object []> resultsLines = sqlQuery.list();

        for (Object [] row: resultsLines) {
            container.addHws(
                new HWS(
                    (String) row[0],
                    (Geometry) row[1],
                    (String) row[2],
                    (Integer) row[3],
                    (Integer) row[4],
                    (String) row[5],
                    (String) row[6],
                    HWS.TYPE.LINE));
        }

        return container;
    }

    private static HWSContainer getHWSPointsUncached(String river) {
        if (log.isDebugEnabled()) {
            log.debug("HWSFactory.getHWSLinesUncached");
        }

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = null;
        HWSContainer container = new HWSContainer();
        container.setRiver(river);
        container.setType(HWS.TYPE.LINE);
        sqlQuery = session.createSQLQuery(SQL_SELECT_POINTS)
            .addScalar("name", StandardBasicTypes.STRING)
            .addScalar("geom", GeometryUserType.TYPE)
            .addScalar("id", StandardBasicTypes.STRING)
            .addScalar("kind_id", StandardBasicTypes.INTEGER)
            .addScalar("official", StandardBasicTypes.INTEGER)
            .addScalar("fed", StandardBasicTypes.STRING)
            .addScalar("description", StandardBasicTypes.STRING);

        sqlQuery.setString("river", river);
        List<Object []> resultsPoints = sqlQuery.list();

        for (Object [] row: resultsPoints) {
            container.addHws(
                new HWS(
                    (String) row[0],
                    (Geometry) row[1],
                    (String) row[2],
                    (Integer) row[3],
                    (Integer) row[4],
                    (String) row[5],
                    (String) row[6],
                    HWS.TYPE.POINT));
        }

        return container;
    }
}
