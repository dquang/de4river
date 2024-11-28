/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import com.vividsolutions.jts.geom.Envelope;

import org.dive4elements.artifactdatabase.XMLService;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.utils.GeometryUtils;
import org.dive4elements.river.utils.RiverUtils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This service provides information about the supported rivers by this
 * application.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapInfoService extends XMLService {

    private static final String MAPTYPE_FLOOD = "floodmap";
    private static final String MAPTYPE_RIVER = "rivermap";

    /** XPath that points to the river.*/
    public static final String XPATH_RIVER = "/mapinfo/river/text()";

    public static final String XPATH_MAPTYPE = "/mapinfo/maptype/text()";

    private static final String XPATH_RIVER_PROJECTION =
        "/artifact-database/*[local-name()=$maptype]/"
        + "river[@name=$river]/srid/@value";

    private static final String XPATH_RIVER_BACKGROUND =
        "/artifact-database/*[local-name()=$maptype]/"
        + "river[@name=$river]/background-wms";

    private static Logger log = LogManager.getLogger(MapInfoService.class);


    /**
     * The default constructor.
     */
    public MapInfoService() {
    }

    protected static String getStringXPath(
        String              query,
        Map<String, String> variables
    ) {
        return (String)XMLUtils.xpath(
            Config.getConfig(), query, XPathConstants.STRING,
            null, variables);
    }

    protected static Node getNodeXPath(
        String              query,
        Map<String, String> variables
    ) {
        return (Node)XMLUtils.xpath(
            Config.getConfig(), query, XPathConstants.NODE,
            null, variables);
    }

    @Override
    public Document processXML(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("MapInfoService.process");

        Document result   = XMLUtils.newDocument();
        ElementCreator cr = new ElementCreator(result, null, null);

        Element mapinfo = cr.create("mapinfo");
        result.appendChild(mapinfo);

        String river = extractRiver(data);
        if (river == null || river.length() == 0) {
            log.warn("Cannot generate information: river is empty!");
            return result;
        }

        String mapType = extractMaptype(data);
        if (mapType == null
        || !(mapType.equals(MAPTYPE_FLOOD) || mapType.equals(MAPTYPE_RIVER))) {
            mapType = MAPTYPE_FLOOD;
        }

        Element root = cr.create("river");
        cr.addAttr(root, "name", river);
        mapinfo.appendChild(root);

        Envelope env = GeometryUtils.getRiverBoundary(river);
        if (env != null) {
            String bounds = GeometryUtils.jtsBoundsToOLBounds(env);
            if (log.isDebugEnabled()) {
                log.debug("River '" + river + "' bounds: " + bounds);
            }

            Element bbox = cr.create("bbox");
            cr.addAttr(bbox, "value", bounds);
            root.appendChild(bbox);
        }

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("maptype", mapType);
        vars.put("river", river);

        String sridStr = getStringXPath(XPATH_RIVER_PROJECTION, vars);

        if (sridStr != null && sridStr.length() > 0) {
            Element srid = cr.create("srid");
            cr.addAttr(srid, "value", sridStr);
            root.appendChild(srid);
        }

        if (log.isDebugEnabled()) {
            log.debug("processXML: " + XMLUtils.toString(root));
        }

        Element bgWMS = (Element) getNodeXPath(XPATH_RIVER_BACKGROUND, vars);
        root.appendChild(
            createWMSElement("background-wms",
                bgWMS.getAttribute("url"),
                bgWMS.getAttribute("layers"),
                cr));

        root.appendChild(
            createWMSElement("river-wms",
                mapType == MAPTYPE_FLOOD
                    ? RiverUtils.getUserWMSUrl()
                    : RiverUtils.getRiverWMSUrl(),
                river,
                cr));

        return result;
    }


    protected Element createWMSElement(
        String elementName,
        String url,
        String layers,
        ElementCreator cr)
    {
        Element el = cr.create(elementName);

        cr.addAttr(el, "url", url);
        cr.addAttr(el, "layers", layers);

        return el;
    }


    private static String extractRiver(Document data) {
        return XMLUtils.xpathString(
            data, XPATH_RIVER, ArtifactNamespaceContext.INSTANCE);
    }

    private static String extractMaptype(Document data) {
        return XMLUtils.xpathString(
            data, XPATH_MAPTYPE, ArtifactNamespaceContext.INSTANCE);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
