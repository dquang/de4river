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
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DischargeInfoXML
extends      HttpServlet
{
    private static final Logger log = LogManager.getLogger(DischargeInfoXML.class);


    public static final String ERROR_NO_DISTANCEINFO_FOUND =
        "error_no_dischargeinfo_found";


    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        log.info("DischargeInfoXML.doGet");

        String url  = getServletContext().getInitParameter("server-url");

        String gauge = req.getParameter("gauge");

        String river = req.getParameter("river");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element gaugeEl = ec.create("gauge");
        gaugeEl.setTextContent(gauge);

        if (river != null && !river.isEmpty()) {
            Element riverEl = ec.create("river");
            riverEl.setTextContent(river);
            gaugeEl.appendChild(riverEl);
        }

        doc.appendChild(gaugeEl);

        HttpClient client = new HttpClientImpl(url);

        try {
            InputStream in = (InputStream) client.callService(
                url, "dischargeinfo", doc, new StreamResponseHandler());

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
