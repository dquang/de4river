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
import org.dive4elements.river.artifacts.model.StaticMorphoWidthCacheKey;
import org.dive4elements.river.backend.SessionHolder;


public class MorphologicWidthFactory
{
    /** Private log to use here. */
    private static Logger log = LogManager.getLogger(
        MorphologicWidthFactory.class);

    public static final String SQL_SELECT =
        "SELECT mwv.station AS station, mwv.width AS width " +
        "   FROM morphologic_width mw" +
        "       JOIN morphologic_width_values mwv " +
        "          ON mwv.morphologic_width_id = mw.id" +
        "   WHERE mw.id = :width_id";

    private MorphologicWidthFactory() {
    }


    /**
     * Get WKms for given column and wst_id, caring about the cache.
     */
    public static MorphologicWidth getWidth(int width_id) {
        log.debug("MorphologicWidthFactory.getWidth");
        Cache cache = CacheFactory.getCache(
            StaticMorphoWidthCacheKey.CACHE_NAME);

        StaticMorphoWidthCacheKey cacheKey;

        if (cache != null) {
            cacheKey = new StaticMorphoWidthCacheKey(width_id);
            Element element = cache.get(cacheKey);
            if (element != null) {
                log.debug("Got static bedheight values from cache");
                return (MorphologicWidth)element.getValue();
            }
        }
        else {
            cacheKey = null;
        }

        MorphologicWidth values = getWidthUncached(width_id);

        if (values != null && cacheKey != null) {
            log.debug("Store static morphologic width values in cache.");
            Element element = new Element(cacheKey, values);
            cache.put(element);
        }
        return values;
    }

    private static MorphologicWidth getWidthUncached(int width_id) {
        if (log.isDebugEnabled()) {
            log.debug("MorphologicWidthFactory.getWidthUncached");
        }

        Session session = SessionHolder.HOLDER.get();
        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT)
                .addScalar("station", StandardBasicTypes.DOUBLE)
                .addScalar("width", StandardBasicTypes.DOUBLE);
        sqlQuery.setInteger("width_id", width_id);
        List<Object []> results = sqlQuery.list();

        MorphologicWidth widths = new MorphologicWidth();
        for (Object [] row: results) {
            log.debug("got station: " + (Double)row[0]);
            widths.add(
                (Double) row[0],
                (Double) row[1]);
        }
        return widths;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
