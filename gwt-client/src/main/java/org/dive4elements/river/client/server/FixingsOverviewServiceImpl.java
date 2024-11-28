/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XSLTransformer;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.client.services.FixingsOverviewService;

import org.dive4elements.river.client.shared.exceptions.ServerException;

import org.dive4elements.river.client.shared.model.FixingsOverviewInfo.FixEvent;
import org.dive4elements.river.client.shared.model.FixingsOverviewInfo.Sector;

import org.dive4elements.river.client.shared.model.FixingsOverviewInfo;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FixingsOverviewServiceImpl
extends      RemoteServiceServlet
implements   FixingsOverviewService
{
    private static final Logger log =
        LogManager.getLogger(FixingsOverviewServiceImpl.class);

    public static final String SERVICE_NAME = "fixings-overview";

    public static final String XSL_TRANSFORM =
        "/WEB-INF/stylesheets/fixoverview2html.xsl";

    protected static final String XPATH_RID = "/fixings/river/@rid";
    protected static final String XPATH_RIVER = "/fixings/river/@name";
    protected static final String XPATH_RFROM = "/fixings/river/@from";
    protected static final String XPATH_RTO = "/fixings/river/@to";

    @Override
    public FixingsOverviewInfo generateOverview(
        String  locale,
        String  uuid,
        String  filter,
        boolean checkboxes,
        String  callback
    )
    throws ServerException
    {
        log.info("FixingsOverviewServiceImpl.doGet");

        if (filter == null || filter.length() == 0) {
            log.warn("Missing 'filter' parameter.");
            return null;
        }

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("JSON filter: ------------------");
            log.debug(filter);
        }

        Document filterDoc = XMLUtils.jsonToXML(filter);

        if (filterDoc == null) {
            log.warn("Creating filter document failed.");
            return null;
        }

        if (debug) {
            log.debug("XML filter: ------------------");
            log.debug(XMLUtils.toString(filterDoc));
        }

        try {
            String url = getServletContext().getInitParameter("server-url");
            HttpClient client = new HttpClientImpl(url, locale);
            Document resultDoc =
                client.callService(url, SERVICE_NAME, filterDoc);

            if (debug) {
                log.debug("Result XML: -----------");
                log.debug(XMLUtils.toString(resultDoc));
            }

            FixingsOverviewInfo i = getInfo(
                locale, resultDoc, uuid, checkboxes, callback);
            return i;
        }
        catch (ConnectionException ce) {
            log.error(ce);
        }
        return null;
    }


    protected FixingsOverviewInfo getInfo(
        String   locale,
        Document doc,
        String   uuid,
        boolean  checkboxes,
        String   callback
    ) {
        // TODO: Find a more general solution.
        locale = locale == null || locale.toLowerCase().startsWith("de")
            ? "de"
            : "en";

        InputStream transform = getServletContext()
            .getResourceAsStream(XSL_TRANSFORM);

        if (transform == null) {
            log.warn("transform not found");
            return null;
        }

        String result = null;
        try {
            XSLTransformer xformer = new XSLTransformer();
            xformer.addParameter("locale", locale);
            xformer.addParameter("project-uuid", uuid);
            xformer.addParameter(
                "render-checkboxes",
                checkboxes ? Boolean.TRUE : Boolean.FALSE);
            xformer.addParameter("callback", callback);
            result = xformer.transform(doc, transform);
        }
        finally {
            try { transform.close(); }
            catch (IOException ioe) {}
        }

        if (log.isDebugEnabled()) {
            log.debug("--------------------------------------");
            log.debug(result);
            log.debug("--------------------------------------");
        }

        int    rid  = -1;
        double from = -1;
        double to   = -1;

        String rid_str  = XMLUtils.xpathString(doc, XPATH_RID, null);
        String river    = XMLUtils.xpathString(doc, XPATH_RIVER, null);
        String from_str = XMLUtils.xpathString(doc, XPATH_RFROM, null);
        String to_str   = XMLUtils.xpathString(doc, XPATH_RTO, null);

        try {
            rid = Integer.parseInt(rid_str);
            from = Double.parseDouble(from_str);
            to = Double.parseDouble(to_str);
        }
        catch(NumberFormatException nfe) {
            log.warn(nfe, nfe);
        }

        List<FixEvent> fixEvents = getFixEvents(doc);
        return new FixingsOverviewInfo(
            rid,
            river,
            from,
            to,
            fixEvents,
            result);
    }


    protected List<FixEvent> getFixEvents(Document doc) {
        List<FixEvent> list = new ArrayList<FixEvent>();

        NodeList events = doc.getElementsByTagName("event");

        int E = events.getLength();

        if (E == 0) {
            log.warn("No events in Overview!");
            return list;
        }

        for (int i = 0; i < E; i++) {
            Element n = (Element)events.item(i);
            List<Sector> sectors = getSectors(n);
            String cid  = n.getAttribute("cid");
            String date = n.getAttribute("date");;
            String name = n.getAttribute("description");
            list.add(new FixEvent( cid, date, name, sectors));
        }
        return list;
    }

    protected List<Sector> getSectors(Element event) {
        NodeList sectors = event.getElementsByTagName("sector");

        int S = sectors.getLength();

        if (S == 0) {
            log.warn("No Sectors in Event!");
            return null;
        }

        List<Sector> list = new ArrayList<Sector>(S);
        for (int i = 0; i < S; i++) {
            Element n = (Element)sectors.item(i);
            int    cls  = -1;
            double from = -1;
            double to   = -1;
            String cls_str  = n.getAttribute("class");
            String from_str = n.getAttribute("from");
            String to_str   = n.getAttribute("to");
            try {
                cls  = Integer.parseInt(cls_str);
                from = Double.parseDouble(from_str);
                to   = Double.parseDouble(to_str);
            }
            catch(NumberFormatException nfe) {
                log.warn(nfe, nfe);
            }
            list.add(new Sector(cls, from, to));
        }
        return list;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
