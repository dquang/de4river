/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.model.Annotation;
import org.dive4elements.river.model.FastAnnotations;

import org.dive4elements.river.artifacts.cache.CacheFactory;


/** Make FastAnnotations (db unbound) available. */
public class LocationProvider {

    private static final Logger log =
        LogManager.getLogger(LocationProvider.class);


    public static final String CACHE_KEY = "location-provider";

    public static final String PREFIX = "lp-";


    private LocationProvider() {
    }

    public static String getLocation(String river, double km) {

        FastAnnotations fas = getAnnotations(river, km);

        FastAnnotations.Annotation an = fas.findByKm(km);

        return an != null ? an.getPosition() : null;
    }

    public static FastAnnotations getAnnotations(String river) {
        return getAnnotations(river, Double.MAX_VALUE);
    }

    protected static FastAnnotations getAnnotations(String river, double km) {
        // TODO issue880: Make annotations available _per type_

        Cache cache = CacheFactory.getCache(CACHE_KEY);

        if (cache == null) {
            return uncachedAnnotations(river, km);
        }

        String key = PREFIX + river;

        Element element = cache.get(key);

        if (element != null) {
            return (FastAnnotations)element.getValue();
        }

        FastAnnotations fas = uncachedAnnotations(river, Double.MAX_VALUE);

        cache.put(new Element(key, fas));

        return fas;
    }

    protected static FastAnnotations uncachedAnnotations(
        String river,
        double km
    ) {
        if (km != Double.MAX_VALUE) {
            // XXX Fake it by using a standard Annotation.

            Annotation annotation =
                AnnotationsFactory.getAnnotation(river, km);

            if (annotation != null) {
                FastAnnotations.Annotation fa =
                    new FastAnnotations.Annotation(
                        km, Double.NaN,
                        annotation.getPosition().getValue(), null, null,
                        Double.NaN, Double.NaN);
                return new FastAnnotations(
                    new FastAnnotations.Annotation [] { fa });
            }

            return new FastAnnotations(new FastAnnotations.Annotation[0]);
        }

        long startTime = System.currentTimeMillis();

        FastAnnotations fas = new FastAnnotations(river);

        long stopTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Loading locations took " +
                (stopTime-startTime)/1000f + " secs.");
        }

        return fas;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
