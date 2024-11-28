/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.artifacts.httpclient.http.response.StreamResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FixingsKMChartServiceImpl
extends      HttpServlet
{
    private static final Logger log =
        LogManager.getLogger(FixingsKMChartServiceImpl.class);

    public static final String SERVICE_NAME = "fixings-km-chart";

    public FixingsKMChartServiceImpl() {
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {

        log.info("FixingsKMChartServiceImpl.doGet");

        String url    = getServletContext().getInitParameter("server-url");
        String locale = req.getParameter("locale");
        String filter = req.getParameter("filter");

        if (filter == null || filter.length() == 0) {
            log.warn("Missing 'filter' parameter.");
            return;
        }

        if (locale == null || locale.length() == 0) {
            locale = "de";
        }

        Document filterDoc = XMLUtils.jsonToXML(filter);

        if (filterDoc == null) {
            log.warn("Creating filter document failed.");
            return;
        }

        InputStream in;

        try {
            HttpClient client = new HttpClientImpl(url, locale);
            in = (InputStream)client.callService(
                url, // XXX: Why? The URL is passed by construction already.
                SERVICE_NAME,
                filterDoc,
                new StreamResponseHandler());
        }
        catch (ConnectionException ce) {
            log.error(ce);
            return;
        }

        resp.setHeader("Content-Type", guessMIMEType(filterDoc));

        try {
            OutputStream out = resp.getOutputStream();

            byte [] buf = new byte[4096];
            int i = -1;
            while ((i = in.read(buf)) >= 0) {
                out.write(buf, 0, i);
            }
            out.flush();
        }
        catch (IOException ioe) {
            log.error(ioe);
        }
        finally {
            try { in.close(); }
            catch (IOException ioe) { /* ignored */ }
        }
    }

    protected static String guessMIMEType(Document document) {

        NodeList formats = document.getElementsByTagName("format");

        String format = "png";

        if (formats.getLength() > 0) {
            String type = ((Element)formats.item(0)).getAttribute("type");
            if (type.length() > 0) {
                format = type;
            }
        }

        return "image/" + format;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
