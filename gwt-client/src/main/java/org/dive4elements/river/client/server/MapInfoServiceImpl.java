/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.river.client.client.services.MapInfoService;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.BBox;
import org.dive4elements.river.client.shared.model.MapInfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This service fetches a document that contains meta information for a specific
 * chart.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapInfoServiceImpl
extends      RemoteServiceServlet
implements   MapInfoService
{
    private static final Logger log =
        LogManager.getLogger(MapInfoServiceImpl.class);


    public static final String XPATH_RIVER =
        "/mapinfo/river/@name";

    public static final String XPATH_SRID =
        "/mapinfo/river/srid/@value";

    public static final String XPATH_BBOX  =
        "/mapinfo/river/bbox/@value";

    public static final String XPATH_RIVER_WMS =
        "/mapinfo/river/river-wms/@url";

    public static final String XPATH_RIVER_LAYERS =
        "/mapinfo/river/river-wms/@layers";

    public static final String XPATH_WMS_URL =
        "/mapinfo/river/background-wms/@url";

    public static final String XPATH_WMS_LAYERS =
        "/mapinfo/river/background-wms/@layers";

    public static final String ERROR_NO_MAPINFO_FOUND =
        "mapinfo_service_no_result";


    @Override
    public MapInfo getMapInfo(String locale, String river)
    throws ServerException
    {
        log.info("MapInfoServiceImpl.getMapInfo");

        String url  = getServletContext().getInitParameter("server-url");

        Document request = getRequestDocument(river, "rivermap");

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            log.debug("MapInfoServiceImpl.callService");
            Document result = client.callService(url, "mapinfo", request);

            if (result == null) {
                log.warn("MapInfo service returned no result.");
                throw new ServerException(ERROR_NO_MAPINFO_FOUND);
            }

            return getMapInfo(result);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_MAPINFO_FOUND);
    }


    public static Document getRequestDocument(
        String rivername,
        String maptypeStr
    ) {
        log.debug("MapInfoServiceImpl.getRequestDocument");

        Document  request = XMLUtils.newDocument();
        ElementCreator cr = new ElementCreator(request, null, null);

        Element root    = cr.create("mapinfo");
        Element river   = cr.create("river");
        Element maptype = cr.create("maptype");

        river.setTextContent(rivername);
        maptype.setTextContent(maptypeStr);

        request.appendChild(root);
        root.appendChild(river);
        root.appendChild(maptype);

        return request;
    }


    public static MapInfo getMapInfo(Document result) {
        log.debug("MapInfoServiceImpl.getMapInfo");

        String river   = XMLUtils.xpathString(result, XPATH_RIVER, null);
        String sridStr = XMLUtils.xpathString(result, XPATH_SRID, null);
        String bboxS   = XMLUtils.xpathString(result, XPATH_BBOX,  null);
        BBox   bbox    = BBox.getBBoxFromString(bboxS);

        String riverWMS    = XMLUtils.xpathString(
            result, XPATH_RIVER_WMS, null);
        String riverLayers = XMLUtils.xpathString(
            result, XPATH_RIVER_LAYERS, null);
        String wmsURL      = XMLUtils.xpathString(
            result, XPATH_WMS_URL, null);
        String wmsLayers   = XMLUtils.xpathString(
            result, XPATH_WMS_LAYERS, null);

        int srid = 4326;

        try {
            srid = Integer.parseInt(sridStr);
        }
        catch (NumberFormatException nfe) {
            GWT.log("Could not parse SRID String: " + sridStr);
        }

        return new MapInfo(
                river, srid, bbox, riverWMS, riverLayers, wmsURL, wmsLayers);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
