/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.backend.SessionHolder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;

public class FixingsOverviewFactory
{
    private static Logger log = LogManager.getLogger(FixingsOverviewFactory.class);

    public static final String CACHE_NAME = "fixings-overviews";

    private FixingsOverviewFactory() {
    }


    public static FixingsOverview getOverview(String river) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "Looking for fixings overview for river '" + river + "'");
        }

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            if (debug) {
                log.debug("Cache not configured.");
            }
            return getUncached(river);
        }

        String key = "fix-over-" + river;

        Element element = cache.get(key);

        if (element != null) {
            if (debug) {
                log.debug("Overview found in cache");
            }
            return (FixingsOverview)element.getValue();
        }

        FixingsOverview overview = getUncached(river);

        if (overview != null) {
            if (debug) {
                log.debug("Store overview in cache.");
            }
            cache.put(new Element(key, overview));
        }

        return overview;
    }

    public static FixingsOverview getUncached(String river) {
        FixingsOverview overview = new FixingsOverview(river);

        Session session = SessionHolder.HOLDER.get();

        return overview.load(session) ? overview : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
