/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.InputStream;
import java.io.IOException;

import java.util.Map;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.MapConfig;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.client.services.MapOutputService;


public class MapOutputServiceImpl
extends      RemoteServiceServlet
implements   MapOutputService
{

    private static final Logger log =
        LogManager.getLogger(MapOutputServiceImpl.class);


    public static final String ERROR_NO_MAP_CONFIG = "error_no_map_config";

    public static final String ERROR_NO_MAP_OUTPUT_TYPE =
        "error_no_map_output_type";

    public MapConfig doOut(Collection collection)
    throws ServerException
    {
        log.info("MapOutputServiceImpl.doOut");

        String url  = getServletContext().getInitParameter("server-url");
        String uuid = collection.identifier();

        Map<String, OutputMode> modes = collection.getOutputModes();
        String requestMode = "";
        if (modes.containsKey("floodmap")) {
            requestMode = "floodmap";
        }
        else if (modes.containsKey("map")) {
            requestMode = "map";
        }
        else {
           throw new ServerException(ERROR_NO_MAP_OUTPUT_TYPE);
        }

        try {
            Document request = ClientProtocolUtils.newOutCollectionDocument(
                uuid, requestMode, requestMode);

            HttpClient client = new HttpClientImpl(url);
            InputStream is = client.collectionOut(request, uuid, requestMode);

            Document response = XMLUtils.parseDocument(is);

            return MapHelper.parseConfig(response);
        }
        catch (ConnectionException e) {
            log.error(e, e);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }

        throw new ServerException(ERROR_NO_MAP_CONFIG);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
