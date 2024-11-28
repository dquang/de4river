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
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.StreamResponseHandler;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DistanceInfoXML
extends      HttpServlet
{
    private static final Logger log = LogManager.getLogger(DistanceInfoXML.class);


    public static final String ERROR_NO_DISTANCEINFO_FOUND =
        "error_no_distanceinfo_found";


    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        log.info("DistanceInfoXML.doGet");

        String url  = getServletContext().getInitParameter("server-url");

        String river  = req.getParameter("river");
        String filter = req.getParameter("filter");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element riverEl = ec.create("river");
        riverEl.setTextContent(river);

        doc.appendChild(riverEl);

        if (filter != null && filter.length() > 0) {
            Element typeEl = ec.create("filter");
            typeEl.setTextContent(filter);

            riverEl.appendChild(typeEl);
        }

        HttpClient client = new HttpClientImpl(url);

        try {
            InputStream in = (InputStream) client.callService(
                url, "distanceinfo", doc, new StreamResponseHandler());

            OutputStream out = resp.getOutputStream();

            byte[] b = new byte[4096];
            int i;
            while ((i = in.read(b)) >= 0) {
                out.write(b, 0, i);
            }

            out.flush();
            out.close();
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
