/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.shared.model.MapConfig;


public class MapHelper {

    private static final Logger log = LogManager.getLogger(MapHelper.class);


    public static final String XPATH_SRID =
        "/art:floodmap/art:srid/text()";

    public static final String XPATH_MAX_EXTENT =
        "/art:floodmap/art:maxExtent/text()";

    public static final String XPATH_INITIAL_EXTENT =
        "/art:floodmap/art:initialExtent/text()";


    private MapHelper() {
    }


    public static MapConfig parseConfig(Document raw) {
        log.debug("MapHelper.parseConfig");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(raw));
        }

        MapConfig config = new MapConfig();

        setSrid(config, raw);
        setMaxExtent(config, raw);
        setInitialExtent(config, raw);

        return config;
    }


    protected static void setSrid(MapConfig config, Document raw) {
        String srid = (String) XMLUtils.xpathString(
            raw,
            XPATH_SRID,
            ArtifactNamespaceContext.INSTANCE);

        log.debug("Found srid: '" + srid + "'");

        if (srid != null && srid.length() > 0) {
            log.debug("Set srid: '" + srid + "'");
            config.setSrid(srid);
        }
    }


    protected static void setMaxExtent(MapConfig config, Document raw) {
        String maxExtent = (String) XMLUtils.xpathString(
            raw,
            XPATH_MAX_EXTENT,
            ArtifactNamespaceContext.INSTANCE);

        log.debug("Found max extent: '" + maxExtent + "'");

        if (maxExtent != null && maxExtent.length() > 0) {
            log.debug("Set max extent: '" + maxExtent + "'");
            config.setMaxExtent(maxExtent);
        }
    }


    protected static void setInitialExtent(MapConfig config, Document raw) {
        String initialExtent = (String) XMLUtils.xpathString(
            raw,
            XPATH_INITIAL_EXTENT,
            ArtifactNamespaceContext.INSTANCE);

        if (initialExtent != null && initialExtent.length() > 0) {
            log.debug("Set initial extent: '" + initialExtent + "'");
            config.setInitialExtent(initialExtent);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
