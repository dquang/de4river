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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.commons.lang.StringEscapeUtils;

import org.dive4elements.river.client.client.services.ReportService;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

public class ReportServiceImpl
extends      RemoteServiceServlet
implements   ReportService
{
    private static final Logger log =
        LogManager.getLogger(ReportServiceImpl.class);


    @Override
    public String report(
        String collectionId,
        String locale,
        String out
    ) {
        log.info("report: " + collectionId + " " + out);

        String url = getServletContext().getInitParameter("server-url");

        Document request = ClientProtocolUtils.newOutCollectionDocument(
            collectionId,
            out,
            "report");

        InputStream in = null;
        try {
            HttpClient client = new HttpClientImpl(url, locale);
            in = client.collectionOut(request, collectionId, out);

            if (in == null) {
                log.debug("report: no report");
                return null;
            }

            Document report = XMLUtils.parseDocument(in);

            return buildReport(report);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }

        return "error processing error report";
    }


    /** Returns String containing markup that shows the report message. */
    protected static String buildReport(Document document) {

        NodeList problems = document.getElementsByTagName("problem");

        StringBuilder global = new StringBuilder();
        StringBuilder kms    = new StringBuilder();

        for (int i = 0, N = problems.getLength(); i < N; ++i) {

            Element element = (Element)problems.item(i);

            String km  = element.getAttribute("km");
            String msg = element.getTextContent();

            if (km.length() > 0) {
                kms.append("<li><strong>KM ")
                   .append(StringEscapeUtils.escapeHtml(km))
                   .append("</strong>: ")
                   .append(StringEscapeUtils.escapeHtml(msg))
                   .append("</li>");
            }
            else {
                global.append("<li>")
                      .append(StringEscapeUtils.escapeHtml(msg))
                      .append("</li>");
            }
        }

        StringBuilder sb = new StringBuilder("<ul>")
            .append(global)
            .append(kms)
            .append("</ul>");

        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
