/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SedDBSessionHolder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;

public class SQOverviewFactory {

    private static Logger log = LogManager.getLogger(SQOverviewFactory.class);

    public static final String CACHE_NAME = "sq-overviews";

    private SQOverviewFactory() {
    }


    public static SQOverview getOverview(String river) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "Looking for sq overview for river '" + river + "'");
        }

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            if (debug) {
                log.debug("Cache not configured.");
            }
            return getUncached(river);
        }

        String key = "sq-over-" + river;

        Element element = cache.get(key);

        if (element != null) {
            if (debug) {
                log.debug("Overview found in cache");
            }
            return (SQOverview)element.getValue();
        }

        SQOverview overview = getUncached(river);

        if (overview != null) {
            if (debug) {
                log.debug("Store overview in cache.");
            }
            cache.put(new Element(key, overview));
        }

        return overview;
    }

    public static SQOverview getUncached(String river) {
        SQOverview overview = new SQOverview(river);

        Session session = SedDBSessionHolder.HOLDER.get();

        return overview.load(session) ? overview : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
