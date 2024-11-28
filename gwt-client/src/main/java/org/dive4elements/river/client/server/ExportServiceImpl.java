/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;


/**
 * This service is used to request a data export from the artifact server. The
 * response is directed directly to the output stream, so that a file dialog is
 * opened.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ExportServiceImpl
extends      HttpServlet
{
    private static final Logger log =
        LogManager.getLogger(ExportServiceImpl.class);


    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        log.info("ExportServiceImpl.doGet");

        try {
            final OutputStream out = resp.getOutputStream();

            String url    = getServletContext().getInitParameter("server-url");
            String uuid   = req.getParameter("uuid");
            String name   = req.getParameter("name");
            String mode   = req.getParameter("mode");
            String type   = req.getParameter("type");
            String locale = req.getParameter("locale");
            String km     = req.getParameter("km");
            String fn     = name + "." + type;
            final String enc = req.getParameter("encoding");

            resp.setHeader("Content-Disposition", "attachment;filename=" + fn);

            if (log.isDebugEnabled()) {
                log.debug("Request " + type + " export.");
            }

            Document attr = null;
            if (km != null && km.length() > 0) {
                attr = XMLUtils.newDocument();
                XMLUtils.ElementCreator ec =
                        new XMLUtils.ElementCreator(attr, null, null);
                Element e = ec.create("km");
                e.setTextContent(km);
                attr.appendChild(e);
            }
            Document request = ClientProtocolUtils.newOutCollectionDocument(
                uuid, mode, type, attr);
            HttpClient client = new HttpClientImpl(url, locale);

            if (enc != null) {
                InputStreamReader in = new InputStreamReader(
                    client.collectionOut(request, uuid, mode),
                    "UTF-8");
                try {
                    OutputStreamWriter encOut = new OutputStreamWriter(
                        out, enc);
                    char buf [] = new char[4096];
                    int c;
                    while ((c = in.read(buf, 0, buf.length)) >= 0) {
                        encOut.write(buf, 0, c);
                    }
                    encOut.flush();
                }
                finally {
                    in.close();
                }
            }
            else { // Just copy thru.
                client.collectionOut(request, uuid, mode, out);
                out.flush();
            }
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
