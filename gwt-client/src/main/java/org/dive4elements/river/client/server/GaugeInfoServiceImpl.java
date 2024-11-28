/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.client.services.GaugeInfoService;
import org.dive4elements.river.client.shared.model.Gauge;
import org.dive4elements.river.client.shared.model.GaugeImpl;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GaugeInfoServiceImpl
extends      RemoteServiceServlet
implements   GaugeInfoService
{
    private static final Logger log =
        LogManager.getLogger(GaugeInfoServiceImpl.class);


    public static final String ERROR_NO_GAUGES_FOUND =
        "error_no_gaugeinfo_found";

    public static final String XPATH_GAUGES = "art:service/art:gauge";


    public List<Gauge> getGaugeInfo(String rivername, String refnumber)
    throws ServerException
    {
        log.info("GaugeInfoServiceImpl.getGaugeInfo");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element river = ec.create("river");
        ec.addAttr(river, "name", rivername);

        if (refnumber != null && refnumber.length() > 0) {
            Element filter = ec.create("filter");
            Element gauge  = ec.create("gauge");
            gauge.setTextContent(refnumber);

            filter.appendChild(gauge);
            river.appendChild(filter);
        }

        doc.appendChild(river);

        HttpClient client = new HttpClientImpl(url);

        try {
            Document result = client.callService(url, "gaugeinfo", doc);

            log.debug("Extract gauge info now.");
            List<Gauge> gauges = extractGauges(result);

            if (gauges != null && gauges.size() > 0) {
                return gauges;
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_GAUGES_FOUND);
    }


    /**
     * Extracts all wq info objects from <i>result</i> document.
     *
     * @param result The document retrieved by the server.
     *
     * @return a list of WQInfoObjects.
     */
    protected List<Gauge> extractGauges(Document result)
    throws    ServerException
    {
        List<Gauge> gauges = new ArrayList<Gauge>();

        NodeList list = (NodeList) XMLUtils.xpath(
            result,
            XPATH_GAUGES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (list == null || list.getLength() == 0) {
            log.warn("No gauges found.");

            throw new ServerException(ERROR_NO_GAUGES_FOUND);
        }

        int num = list.getLength();
        log.debug("Response contains " + num + " objects.");

        for (int i = 0; i < num; i++) {
            Gauge obj = buildGauge((Element) list.item(i));

            if (obj != null) {
                gauges.add(obj);
            }
        }

        log.debug("Retrieved " + gauges.size() + " gauges.");

        return gauges;
    }


    protected Gauge buildGauge(Element ele) {
        String name     = ele.getAttribute("name");
        String lowerStr = ele.getAttribute("lower");
        String upperStr = ele.getAttribute("upper");

        if (lowerStr != null && upperStr != null) {
            try {
                return new GaugeImpl(
                    name,
                    Double.valueOf(lowerStr),
                    Double.valueOf(upperStr));
            }
            catch (NumberFormatException nfe) {
                log.warn("Error while Gauge creation: " + nfe.getMessage());
            }
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
