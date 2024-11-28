/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.StreamResponseHandler;


public class DischargeTablesServiceImpl extends HttpServlet {

    private static final Logger log = LogManager
        .getLogger(DischargeInfoServiceImpl.class);

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(
        DateFormat.MEDIUM, Locale.GERMANY);

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        log.info("ChartOutputServiceImpl.doGet");

        String url = getServletContext().getInitParameter("server-url");
        String locale = req.getParameter("locale");

        prepareHeader(req, resp);

        Document requestDoc = createRequestDoc(req);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            OutputStream out = resp.getOutputStream();
            InputStream stream = (InputStream) client.callService(url,
                "dischargetablesoverview", requestDoc,
                new StreamResponseHandler());

            byte[] b = new byte[4096];
            try {
                int i;
                while ((i = stream.read(b)) >= 0) {
                    out.write(b, 0, i);
                }
            }
            finally {
                stream.close();
            }
        }
        catch (IOException ioe) {
            log.error("Error while fetching discharge tables chart!", ioe);
        }
    }

    protected void prepareHeader(HttpServletRequest req,
        HttpServletResponse resp) {
        resp.setHeader("Content-Type", "image/png");
    }

    protected Document createRequestDoc(HttpServletRequest req) {
        Document request = XMLUtils.newDocument();
        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(request,
            null, null);

        Element service = creator.create("service");
        Element gauge = creator.create("gauge");
        Element extent = creator.create("extent");
        Element format = creator.create("format");
        Element timerange = creator.create("timerange");

        creator.addAttr(gauge, "name", extractRequestGauge(req));
        creator.addAttr(extent, "width", extractRequestWidth(req));
        creator.addAttr(extent, "height", extractRequestHeight(req));
        creator.addAttr(format, "type", extractRequestFormat(req));
        creator.addAttr(timerange, "lower", extractRequestLowerTime(req));
        creator.addAttr(timerange, "upper", extractRequestUpperTime(req));

        request.appendChild(service);
        service.appendChild(gauge);
        service.appendChild(extent);
        service.appendChild(format);
        service.appendChild(timerange);

        return request;
    }

    protected String extractRequestGauge(HttpServletRequest req) {
        return req.getParameter("gauge");
    }

    protected String extractRequestWidth(HttpServletRequest req) {
        return req.getParameter("width");
    }

    protected String extractRequestHeight(HttpServletRequest req) {
        return req.getParameter("height");
    }

    protected String extractRequestFormat(HttpServletRequest req) {
        return req.getParameter("format");
    }

    protected String extractRequestLowerTime(HttpServletRequest req) {
        String lowerStr = req.getParameter("lower");
        try {
            long lowerMillis = Long.parseLong(lowerStr);
            Date lower = new Date(lowerMillis);

            return DATE_FORMAT.format(lower);
        }
        catch (NumberFormatException nfe) {
            log.warn("Cannot parse time millies.", nfe);
        }

        return null;
    }

    protected String extractRequestUpperTime(HttpServletRequest req) {
        String upperStr = req.getParameter("upper");
        try {
            long upperMillis = Long.parseLong(upperStr);
            Date upper = new Date(upperMillis);

            return DATE_FORMAT.format(upper);
        }
        catch (NumberFormatException nfe) {
            log.warn("Cannot parse time millies.", nfe);
        }

        return null;
    }
}
