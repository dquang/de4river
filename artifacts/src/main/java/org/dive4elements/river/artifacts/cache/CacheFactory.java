/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.cache;

import org.dive4elements.artifacts.common.utils.Config;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class CacheFactory
{
    private static Logger log = LogManager.getLogger(CacheFactory.class);

    public static final String CACHE_CONFIG_FILE_PROPERTY =
        "flys.artifacts.cache.config.file";

    public static final String XPATH_CACHE_CONFIG_FILE =
        "/artifact-database/cache/config-file/text()";

    private CacheFactory() {
    }

    private static boolean      initialized;

    private static CacheManager cacheManager;

    public static final Cache getCache() {
        return getCache(Cache.DEFAULT_CACHE_NAME);
    }

    public static final String getConfigFile() {
        String configFile = System.getProperty(CACHE_CONFIG_FILE_PROPERTY);

        if (configFile != null) {
            return configFile;
        }

        configFile = Config.getStringXPath(XPATH_CACHE_CONFIG_FILE);

        if (configFile != null) {
            configFile = Config.replaceConfigDir(configFile);
        }

        return configFile;
    }

    public static final synchronized Cache getCache(String cacheName) {
        if (!initialized) {
            initialized = true; // try only once
            String configFile = getConfigFile();
            if (configFile != null) {
                try {
                    cacheManager = CacheManager.create(configFile);
                    //System.setProperty(
                    //  "net.sf.ehcache.enableShutdownHook", "true");
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            log.info("shutting down caches");
                            for (String name: cacheManager.getCacheNames()) {
                                log.info("\tflushing '" + name + "'");
                                cacheManager.getCache(name).flush();
                            }
                            cacheManager.shutdown();
                        }
                    });
                }
                catch (CacheException ce) {
                    log.error("cannot configure cache", ce);
                }
            }
        }

        return cacheManager != null
            ? cacheManager.getCache(cacheName)
            : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
