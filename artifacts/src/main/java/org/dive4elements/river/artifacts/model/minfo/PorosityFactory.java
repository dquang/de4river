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
import org.dive4elements.river.artifacts.model.StaticPorosityCacheKey;
import org.dive4elements.river.backend.SessionHolder;


public class PorosityFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(PorosityFactory.class);

    public static final String SQL_SELECT =
        "SELECT pv.station AS station, pv.porosity AS porosity " +
        "   FROM porosity p" +
        "       JOIN porosity_values pv on pv.porosity_id = p.id" +
        "   WHERE p.id = :porosity_id" +
        "   ORDER BY station";

    private PorosityFactory() {
    }


    /**
     * Get WKms for given column and wst_id, caring about the cache.
     */
    public static Porosity getPorosity(int porosity_id) {
        log.debug("PorosityFactory.getPorosity");
        Cache cache = CacheFactory.getCache(StaticPorosityCacheKey.CACHE_NAME);

        StaticPorosityCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticPorosityCacheKey(porosity_id);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static porosity values from cache");
                return (Porosity)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        Porosity values = getPorosityUncached(porosity_id);

        if (values != null && cacheKey != null) {
            log.debug("Store static porosity values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    private static Porosity getPorosityUncached(int porosity_id) {
        if (log.isDebugEnabled()) {
            log.debug("PorosityFactory.getPorosityUncached");
        }

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT)
                .addScalar("station", StandardBasicTypes.DOUBLE)
                .addScalar("porosity", StandardBasicTypes.DOUBLE);
        sqlQuery.setInteger("porosity_id", porosity_id);
        List<Object []> results = sqlQuery.list();

        Porosity porosities = new Porosity();
        for (Object [] row: results) {
            log.debug("got station: " + (Double)row[0]);
            porosities.add(
                (Double) row[0],
                (Double) row[1]);
        }
        return porosities;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
