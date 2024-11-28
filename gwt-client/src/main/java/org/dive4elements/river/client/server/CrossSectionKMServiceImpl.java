/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.dive4elements.river.client.client.services.CrossSectionKMService;

/**
 * Interact with not documented service.
 */
public class CrossSectionKMServiceImpl
extends      RemoteServiceServlet
implements   CrossSectionKMService
{
    private static final Logger log =
        LogManager.getLogger(CrossSectionKMServiceImpl.class);

    /** XPath that points to the found cross section measurements. */
    public static final String XPATH_CROSS_SECTIONS
        = "/cross-sections/cross-section";

    /** The error message key that is thrown if an error occured while getting
     * new data. */
    public static final String ERROR_GET_CROSS_SECTION
        = "error_get_cross_section";


    /**
     * Fetches positions (kms) at which measurements for given cross-sections
     * exists.
     *
     * @param data Map of Integer (cross-section-id) to km.
     *
     */
    public Map<Integer,Double[]> getCrossSectionKMs(
        String               locale,
        Map<Integer, Double> data,
        int                  nNeighbours)
    throws ServerException
    {
        log.info("CrossSectionKMService.getCrossSectionKMs");

        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
             doc,
             ArtifactNamespaceContext.NAMESPACE_URI,
             ArtifactNamespaceContext.NAMESPACE_PREFIX);
        Element crossSection = ec.create("cross-sections");

        doc.appendChild(crossSection);

        for(Map.Entry<Integer, Double> oneCrossSection : data.entrySet()) {
            Element cs = ec.create("cross-section");
            cs.setAttribute("id", oneCrossSection.getKey().toString());
            cs.setAttribute("km", oneCrossSection.getValue().toString());
            cs.setAttribute("n", Integer.valueOf(nNeighbours).toString());
            crossSection.appendChild(cs);
        }

        HttpClient client = new HttpClientImpl(url, locale);
            log.debug("Created httpclient");

        try {
            // Document should contain:
            //   crosse-sections:
            //     attribute(id), attribute(km) attribute(n)
            Document response = client.callService(
                url, "cross-section-km", doc);
            //<cross-sections><cross-section id="1">
            //<line km="19.5" line-id="189"/>...

            NodeList nodeList = (NodeList) XMLUtils.xpath(response,
                XPATH_CROSS_SECTIONS,
                XPathConstants.NODESET);

            int num = nodeList.getLength();

            Map<Integer, Double[]> result = new HashMap<Integer, Double[]>();

            try{
                for (int i = 0; i < num; i++) {
                    Element csElement = (Element) nodeList.item(i);

                    int idx = Integer.parseInt(csElement.getAttribute("id"));
                    ArrayList<Double> kms = new ArrayList<Double>();

                    NodeList lineNodes = csElement.getElementsByTagName("line");
                    int numLines       = lineNodes.getLength();
                    for (int k = 0; k < numLines; k++) {
                        Element line = (Element) lineNodes.item(k);
                        double d = Double.parseDouble(line.getAttribute("km"));
                        kms.add(d);
                    }

                    Double[] doubles = new Double[kms.size()];
                    kms.toArray(doubles);
                    result.put(Integer.valueOf(idx), doubles);
                }
            }
            catch(NumberFormatException nfe) {
                log.error("Response was not parsable");
            }

            return result;
        }
        catch (ConnectionException ce) {
            log.error("ConnectionExsp", ce);
        }

        log.warn("CrossSectionKMService.getCrossSectionKMS() - FAILED");
        throw new ServerException(ERROR_GET_CROSS_SECTION);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
