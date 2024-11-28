/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.cache.CacheFactory;

import org.dive4elements.river.model.CrossSection;

import org.dive4elements.river.model.FastCrossSectionLine;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class FastCrossSectionLineFactory
{
    private static Logger log =
        LogManager.getLogger(FastCrossSectionLineFactory.class);

    public static final String CACHE_NAME = "fast-cross-section-lines";

    private FastCrossSectionLineFactory() {
    }

    public static FastCrossSectionLine getCrossSectionLine(
        CrossSection cs,
        double       km
    ) {
        Cache cache = CacheFactory.getCache(CACHE_NAME);

        boolean debug = log.isDebugEnabled();

        if (cache == null) {
            if (debug) {
                log.debug("No cross section chunk cache configured.");
            }
            List<FastCrossSectionLine> lines = cs.getFastLines(km, km);
            return lines.isEmpty() ? null : lines.get(0);
        }

        String cacheKey = FastCrossSectionChunk.createHashKey(cs, km);

        Element element = cache.get(cacheKey);

        FastCrossSectionChunk fcsc;

        if (element != null) {
            if (debug) {
                log.debug("Found cross section chunk in cache id: " +
                    cs.getId() + " km: " + km);
            }

            fcsc = (FastCrossSectionChunk)element.getValue();
        }
        else {
            if (debug) {
                log.debug("Not found cross section chunk in cache id: " +
                    cs.getId() + " km: " + km + " -> loading");
            }
            fcsc = new FastCrossSectionChunk(cs, km);
            element = new Element(cacheKey, fcsc);
            cache.put(element);
        }

        return fcsc.getCrossSectionLine(km);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
