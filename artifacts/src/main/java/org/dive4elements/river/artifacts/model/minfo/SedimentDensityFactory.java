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


public class SedimentDensityFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(SedimentDensityFactory.class);

    private static final String DENSITY_CACHE_NAME = "sedimentdensity";

    /** Query to get sediment density values and kms. */
    private static final String SQL_SELECT_DENSITY =
        "SELECT sdv.station AS km, " +
        "       sdv.density AS density," +
        "       sdv.year AS year " +
        "   FROM sediment_density sd" +
        "       JOIN rivers r ON sd.river_id = r.id " +
        "       JOIN sediment_density_values sdv " +
        "          ON sd.id = sdv.sediment_density_id" +
        "   WHERE r.name = :name";

    /** Query to get sediment density values and kms by id. */
    private static final String SQL_SELECT_DENSITY_BY_ID =
        "SELECT sdv.station AS km, " +
        "       sdv.density AS density," +
        "       sdv.year AS year " +
        "   FROM sediment_density sd" +
        "       JOIN sediment_density_values sdv " +
        "          ON sd.id = sdv.sediment_density_id" +
        "   WHERE sd.id = :id";

    /** Query to get sediment density depth by sediment density id. */
    private static final String SQL_SELECT_DENSITY_DEPTH_BY_ID =
        "SELECT d.lower AS lower, " +
        "       d.upper AS upper" +
        "   FROM sediment_density sd " +
        "       JOIN depths d ON d.id = sd.depth_id " +
        "   WHERE sd.id = :id";

    /** Query to get sediment density description by id. */
    private static final String SQL_SELECT_DESCRIPTION_BY_ID =
        "SELECT sd.description " +
        "   FROM sediment_density sd" +
        "   WHERE sd.id = :id";

    private SedimentDensityFactory() {}

    public static SedimentDensity getSedimentDensity(
        String river,
        double startKm,
        double endKm
    ) {
        log.debug("getSedimentDensity");
        Cache cache = CacheFactory.getCache(DENSITY_CACHE_NAME);

        if (cache == null) {
            log.debug("Cache not configured.");
            return getSedimentDensityUncached(river, startKm, endKm);
        }

        String key = river + startKm + endKm;
        Element element = cache.get(key);
        if (element != null) {
            log.debug("SedimentDensity found in cache!");
            return (SedimentDensity)element.getValue();
        }
        SedimentDensity value =
            getSedimentDensityUncached(river, startKm, endKm);

        if (value != null && key != null) {
            log.debug("Store sediment density values in cache.");
            element = new Element(key, value);
            cache.put(element);
        }
        return value;
    }

    private static SedimentDensity getSedimentDensityUncached(
        String river,
        double startKm,
        double endKm
    ) {
        log.debug("getSedimentDensityUncached");
        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_DENSITY)
            .addScalar("km", StandardBasicTypes.DOUBLE)
            .addScalar("density", StandardBasicTypes.DOUBLE)
            .addScalar("year", StandardBasicTypes.INTEGER);
        sqlQuery.setString("name", river);
        List<Object[]> results = sqlQuery.list();
        SedimentDensity density = new SedimentDensity();
        for (Object[] row : results) {
            if (row[0] != null && row[1] != null && row[2] != null) {
                density.addDensity(
                    (Double)row[0], (Double)row[1], (Integer)row[2]);
            }
        }

        density.cleanUp();
        return density;
    }

    /** Query and return depth of a sediment density entry. */
    public static double[] getDepth(int id) {
        log.debug("getDepth");
        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(
            SQL_SELECT_DENSITY_DEPTH_BY_ID)
            .addScalar("lower", StandardBasicTypes.DOUBLE)
            .addScalar("upper", StandardBasicTypes.DOUBLE);
        sqlQuery.setInteger("id", id);
        List<Object[]> results = sqlQuery.list();
        Object[] row = results.get(0);

        return new double[] {(Double)row[0], (Double)row[1]};
    }

    /** Query and return description of a sediment density entry. */
    public static String getSedimentDensityDescription(int id) {
        log.debug("getSedimentDensityDescription");
        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(
            SQL_SELECT_DESCRIPTION_BY_ID)
            .addScalar("description", StandardBasicTypes.STRING);
        sqlQuery.setInteger("id", id);
        List<Object> results = sqlQuery.list();

        return (String) results.get(0);
    }

    public static SedimentDensity getSedimentDensityUncached(
        int id
    ) {
        log.debug("getSedimentDensityUncached id/year");
        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_DENSITY_BY_ID)
            .addScalar("km", StandardBasicTypes.DOUBLE)
            .addScalar("density", StandardBasicTypes.DOUBLE)
            .addScalar("year", StandardBasicTypes.INTEGER);
        sqlQuery.setInteger("id", id);
        List<Object[]> results = sqlQuery.list();
        SedimentDensity density = new SedimentDensity();
        for (Object[] row : results) {
            if (row[0] != null && row[1] != null && row[2] != null) {
                density.addDensity(
                    (Double)row[0], (Double)row[1], (Integer)row[2]);
            }
        }

        density.cleanUp();
        return density;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
