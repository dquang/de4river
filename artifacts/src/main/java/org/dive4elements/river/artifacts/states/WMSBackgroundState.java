/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.map.WMSLayerFacet;
import org.dive4elements.river.artifacts.resources.Resources;


public class WMSBackgroundState extends OutputState {

    public static final String I18N_DESCRIPTION = "floodmap.wmsbackground";

    public static final String XPATH_SRID =
        "/artifact-database/floodmap/river[@name=$name]/srid/@value";

    public static final String XPATH_WMS_URL =
        "/artifact-database/floodmap/river[@name=$name]/background-wms/@url";

    public static final String XPATH_WMS_LAYER =
        "/artifact-database/floodmap/river[@name=$name]/background-wms/@layers";


    protected String url;
    protected String layer;
    protected String srid;

    protected Document cfg;

    protected Map<String, String> variables;


    private static final Logger log = LogManager.getLogger(
        WMSBackgroundState.class);


    @Override
    public void setup(Node config) {
        super.setup(config);

        log.debug("WMSBackgroundState.setup()");
    }


    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        log.debug("WMSBackgroundState.computeInit()");

        initVariables(artifact);

        if (url == null || layer == null) {
            // XXX I don't remember why 'srid', 'url' and 'layer' are member
            // variables. I think the reason was buffering those values.
            srid  = getSrid();
            url   = getUrl();
            layer = getLayer();
        }

        if (url == null || layer == null) {
            log.warn("No background layers currently configured:");
            log.warn("... add config for WMS url: " + XPATH_WMS_URL);
            log.warn("... add config for WMS layer: " + XPATH_WMS_LAYER);
            return null;
        }

        WMSLayerFacet facet = new WMSLayerFacet(
            0,
            getFacetType(),
            getTitle(meta),
            ComputeType.INIT,
            getID(), hash,
            url);

        facet.addLayer(layer);
        facet.setSrid(srid);

        facets.add(facet);

        return null;
    }


    protected Document getConfig() {
        if (cfg == null) {
            cfg = Config.getConfig();
        }

        return cfg;
    }


    protected void initVariables(D4EArtifact artifact) {
        String river = artifact.getDataAsString("river");

        variables = new HashMap<String, String>();
        variables.put("name", river);
    }


    protected String getFacetType() {
        return FLOODMAP_WMSBACKGROUND;
    }


    protected String getSrid() {
        return (String) XMLUtils.xpath(
            getConfig(),
            XPATH_SRID,
            XPathConstants.STRING,
            null,
            variables);
    }


    protected String getUrl() {
        return (String) XMLUtils.xpath(
            getConfig(),
            XPATH_WMS_URL,
            XPathConstants.STRING,
            null,
            variables);
    }


    protected String getLayer() {
        return (String) XMLUtils.xpath(
            getConfig(),
            XPATH_WMS_LAYER,
            XPathConstants.STRING,
            null,
            variables);
    }


    protected String getTitle(CallMeta meta) {
        return Resources.getMsg(meta, I18N_DESCRIPTION, I18N_DESCRIPTION);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
