/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.OutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;


/**
 * This service is used to request a chart from the artifact server. The
 * response is directed directly to the output stream. This can be used
 * to stream an image that is displayed in the UI afterwards.
 *
 * Note that a chart output can also be a csv file.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartOutputServiceImpl
extends      HttpServlet
{
    private static final Logger log =
        LogManager.getLogger(ChartOutputServiceImpl.class);


    /** Handle a get, collectionOut. */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        log.info("ChartOutputServiceImpl.doGet");

        try {
            OutputStream out = resp.getOutputStream();

            String url = getServletContext().getInitParameter("server-url");

            String uuid     = req.getParameter("uuid");
            String type     = req.getParameter("type");
            String locale   = req.getParameter("locale");

            prepareHeader(req, resp);

            Document request = ClientProtocolUtils.newOutCollectionDocument(
                uuid, type, type,
                ChartServiceHelper.getChartAttributes(
                    prepareChartAttributes(req)));

            HttpClient client = new HttpClientImpl(url, locale);

            client.collectionOut(request, uuid, "chart", out);

            out.close();
            out.flush();
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
        catch (Exception e) {
            log.error(e, e);
        }
    }


    protected Map<String, String> prepareChartAttributes(
        HttpServletRequest req
    ) {
        Map<String, String> attr = new HashMap<String, String>();

        Map params = req.getParameterMap();

        attr.put("width", req.getParameter("width"));
        attr.put("height", req.getParameter("height"));
        attr.put("minx", req.getParameter("minx"));
        attr.put("maxx", req.getParameter("maxx"));
        attr.put("miny", req.getParameter("miny"));
        attr.put("maxy", req.getParameter("maxy"));
        attr.put("format", req.getParameter("format"));
        attr.put("km", req.getParameter("currentKm"));

        if (log.isDebugEnabled()) {
            log.debug("====== ZOOM VALUES =======");
            log.debug("  min x: " + req.getParameter("minx"));
            log.debug("  max x: " + req.getParameter("maxx"));
            log.debug("  min y: " + req.getParameter("miny"));
            log.debug("  max y: " + req.getParameter("maxy"));
        }

        return attr;
    }


    protected void prepareHeader(
        HttpServletRequest  req,
        HttpServletResponse resp
    ) {
        String export = req.getParameter("export");

        if (export != null && export.equals("true")) {
            String format = req.getParameter("format");

            if (format == null || format.length() == 0) {
                format = "png";
            }

            String fn = "chart_export" + getFileExtension(format);

            resp.setHeader("Content-Disposition", "attachment;filename=" + fn);
            resp.setHeader("Content-Type", getMimeType(format));
        }
    }


    protected String getFileExtension(String format) {
        if (format.equals("png")) {
            return ".png";
        }
        else if (format.equals("pdf")) {
            return ".pdf";
        }
        else if (format.equals("svg")) {
            return ".svg";
        }
        else if (format.equals("csv")) {
            return ".csv";
        }

        return ".png";
    }


    protected String getMimeType(String format) {
        if (format.equals("png")) {
            return "image/png";
        }
        else if (format.equals("pdf")) {
            return "application/pdf";
        }
        else if (format.equals("svg")) {
            return "svg+xml";
        }
        else if (format.equals("csv")) {
            return "text/plain";
        }

        return "image/png";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
