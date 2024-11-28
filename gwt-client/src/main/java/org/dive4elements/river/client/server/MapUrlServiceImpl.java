/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.File;

import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.client.services.MapUrlService;


public class MapUrlServiceImpl
extends      RemoteServiceServlet
implements   MapUrlService
{

    private static final Logger log =
        LogManager.getLogger(MapUrlServiceImpl.class);


    public Map<String, String> getUrls()
    throws ServerException
    {
        log.info("MapUrlServiceImpl.getUrls");
        Map<String, String> urls = new HashMap<String, String>();

        File file = new File(
            getServletContext().getInitParameter("wms-services-file"));

        Document doc = XMLUtils.parseDocument(file);

        NodeList list = doc.getElementsByTagName("wms");
        for (int i = 0; i < list.getLength(); i++) {
            Element e = (Element) list.item(i);
            urls.put(e.getAttribute("url"), e.getAttribute("name"));
        }

        return urls;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
