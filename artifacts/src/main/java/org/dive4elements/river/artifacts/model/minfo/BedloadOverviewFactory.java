/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;

import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.backend.SedDBSessionHolder;

public class BedloadOverviewFactory {

    private static Logger log = LogManager.getLogger(BedloadOverviewFactory.class);

    public static final String CACHE_NAME = "sq-overviews";

    private BedloadOverviewFactory() {
    }


    public static BedloadOverview getOverview(String river) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "Looking for bedload overview for river '" + river + "'");
        }

        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            log.warn("Cache not configured.");
            return getUncached(river);
        }

        String key = "bedload-over-" + river;

        Element element = cache.get(key);

        if (element != null) {
            if (debug) {
                log.debug("Overview found in cache");
            }
            return (BedloadOverview)element.getValue();
        }

        BedloadOverview overview = getUncached(river);

        if (overview != null) {
            if (debug) {
                log.debug("Store overview in cache.");
            }
            cache.put(new Element(key, overview));
        }

        return overview;
    }

    public static BedloadOverview getUncached(String river) {
        BedloadOverview overview = new BedloadOverview(river);

        Session session = SedDBSessionHolder.HOLDER.get();

        return overview.load(session) ? overview : null;
    }
}
