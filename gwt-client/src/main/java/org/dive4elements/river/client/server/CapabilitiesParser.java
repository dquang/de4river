/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Capabilities;
import org.dive4elements.river.client.shared.model.ContactInformation;
import org.dive4elements.river.client.shared.model.WMSLayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser for GetCapabilities of a WMS.
 */
public class CapabilitiesParser {

    private static final Logger log =
        LogManager.getLogger(CapabilitiesParser.class);


    public static final String ERR_GC_REQUEST_FAILED =
        "error_gc_req_failed";

    public static final String ERR_GC_DOC_NOT_VALID =
        "error_gc_doc_not_valid";

    public static final String ERR_MALFORMED_URL =
        "error_malformed_url";


    public static final String XPATH_WMS_CAPS =
        "/WMS_Capabilities";

    public static final String XPATH_WMT_CAPS =
        "/WMT_MS_Capabilities";

    public static final String XPATH_TITLE =
        "Service/Title/text()";

    public static final String XPATH_ONLINE_RESOURCE =
        "Service/OnlineResource/@href";

    public static final String XPATH_CONTACT_INFORMATION =
        "Service/ContactInformation";

    public static final String XPATH_CI_PERSON =
        "ContactPersonPrimary/ContactPerson/text()";

    public static final String XPATH_CI_ORGANIZATION =
        "ContactPersonPrimary/ContactOrganization/text()";

    public static final String XPATH_CI_ADDRESS =
        "ContactAddress/Address/text()";

    public static final String XPATH_CI_CITY =
        "ContactAddress/City/text()";

    public static final String XPATH_CI_POSTCODE =
        "ContactAddress/PostCode/text()";

    public static final String XPATH_CI_PHONE =
        "ContactVoiceTelephone/text()";

    public static final String XPATH_CI_EMAIL =
        "ContactElectronicMailAddress/text()";

    public static final String XPATH_FEES =
        "Service/Fees/text()";

    public static final String XPATH_ACCESS_CONSTRAINTS =
        "Service/AccessConstraints/text()";

    public static final String XPATH_LAYERS =
        "Capability/Layer";

    public static final String XPATH_MAP_FORMAT =
        "Capability/Request/GetMap/Format";

    public static final String XPATH_GETMAP_ONLINERESOURCE =
        "Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource/@href";

    public static final Pattern SRS_PATTERN = Pattern.compile("(EPSG:\\d+)*");


    private CapabilitiesParser() {
    }


    public static void main(String[] args) {
        log.info("Do static Capabilities request/parsing.");

        try {
            String wmsURL = System.getProperty("test.wms");
            if (wmsURL == null || args.length > 0) {
                wmsURL = args[0];
            }
            Capabilities caps = getCapabilities(wmsURL);

            log.debug(caps.toString());
            System.out.println(caps.toString());
        }
        catch (ServerException se) {
            se.printStackTrace();
        }

        log.info("Finished fetching capabiltiies.");
    }


    public static Capabilities getCapabilities(String urlStr)
    throws ServerException
    {
        try {
            URL url = new URL(urlStr);

            log.debug("Open connection to url: " + urlStr);

            URLConnection conn = url.openConnection();
            conn.connect();

            InputStream is = conn.getInputStream();

            return parse(is);
        }
        catch (MalformedURLException mue) {
            log.warn(mue, mue);
            throw new ServerException(ERR_MALFORMED_URL);
        }
        catch (IOException ioe) {
            log.warn(ioe, ioe);
        }

        throw new ServerException(ERR_GC_REQUEST_FAILED);
    }


    protected static Capabilities parse(InputStream is)
    throws ServerException
    {
        Document doc = XMLUtils.parseDocument(is, false, null);

        if (doc == null) {
            throw new ServerException(ERR_GC_DOC_NOT_VALID);
        }

        return CapabilitiesParser.parse(doc);
    }


    public static Capabilities parse(Document doc)
    throws ServerException
    {
        Node capabilities = getCapabilitiesNode(doc);

        String title = (String) XMLUtils.xpath(
            capabilities,
            XPATH_TITLE,
            XPathConstants.STRING);

        String onlineResource = (String) XMLUtils.xpath(
            capabilities,
            XPATH_ONLINE_RESOURCE,
            XPathConstants.STRING);

        String fees = (String) XMLUtils.xpath(
            capabilities,
            XPATH_FEES,
            XPathConstants.STRING);

        String accessConstraints = (String) XMLUtils.xpath(
            capabilities,
            XPATH_ACCESS_CONSTRAINTS,
            XPathConstants.STRING);

        Node contactInformation = (Node) XMLUtils.xpath(
            capabilities,
            XPATH_CONTACT_INFORMATION,
            XPathConstants.NODE);

        ContactInformation ci = parseContactInformation(contactInformation);

        log.debug("Found fees: " + fees);
        log.debug("Found access constraints: " + accessConstraints);

        NodeList layerNodes = (NodeList) XMLUtils.xpath(
            capabilities,
            XPATH_LAYERS,
            XPathConstants.NODESET);

        String getMapOnlineResource = (String) XMLUtils.xpath(
            capabilities,
            XPATH_GETMAP_ONLINERESOURCE,
            XPathConstants.STRING);

        List<WMSLayer> layers = parseLayers(layerNodes, getMapOnlineResource);

        // Parse MIME types of supported return types, e.g. image/jpeg
        NodeList mapFormatNodes = (NodeList)
                XMLUtils.xpath(
                    capabilities, XPATH_MAP_FORMAT, XPathConstants.NODESET);
        List<String> mapFormats = new ArrayList<String>();
        for (int n = 0; n < mapFormatNodes.getLength(); n++) {
            mapFormats.add(mapFormatNodes.item(n).getTextContent());
        }

        return new Capabilities(
            title,
            onlineResource,
            ci,
            fees,
            accessConstraints,
            layers,
            mapFormats);
    }


    protected static Node getCapabilitiesNode(Document doc)
    throws ServerException {
        Node capabilities = (Node) XMLUtils.xpath(
            doc,
            XPATH_WMS_CAPS,
            XPathConstants.NODE);

        if (capabilities == null) {
            log.info("No '/WMS_Capabilities' node found.");
            log.info("Try to find a '/WMT_MS_Capabilities' node.");

            capabilities = (Node) XMLUtils.xpath(
                doc,
                XPATH_WMT_CAPS,
                XPathConstants.NODE);
        }

        if (capabilities == null) {
            throw new ServerException(ERR_GC_DOC_NOT_VALID);
        }

        return capabilities;
    }


    protected static ContactInformation parseContactInformation(Node node) {
        String person = (String) XMLUtils.xpath(
            node,
            XPATH_CI_PERSON,
            XPathConstants.STRING);

        String organization = (String) XMLUtils.xpath(
            node,
            XPATH_CI_ORGANIZATION,
            XPathConstants.STRING);

        String address = (String) XMLUtils.xpath(
            node,
            XPATH_CI_ADDRESS,
            XPathConstants.STRING);

        String postcode = (String) XMLUtils.xpath(
            node,
            XPATH_CI_POSTCODE,
            XPathConstants.STRING);

        String city = (String) XMLUtils.xpath(
            node,
            XPATH_CI_CITY,
            XPathConstants.STRING);

        String phone = (String) XMLUtils.xpath(
            node,
            XPATH_CI_PHONE,
            XPathConstants.STRING);

        String email = (String) XMLUtils.xpath(
            node,
            XPATH_CI_EMAIL,
            XPathConstants.STRING);

        ContactInformation ci = new ContactInformation();
        ci.setPerson(person);
        ci.setOrganization(organization);
        ci.setAddress(address);
        ci.setPostcode(postcode);
        ci.setCity(city);
        ci.setPhone(phone);
        ci.setEmail(email);

        return ci;
    }


    /**
     * @param layersNode
     * @param onlineResource
     *
     * @return
     */
    protected static List<WMSLayer> parseLayers(
        NodeList layersNode,
        String   onlineResource
    ) {
        int len = layersNode != null ? layersNode.getLength() : 0;

        log.debug("Node has " + len + " layers.");

        List<WMSLayer> layers = new ArrayList<WMSLayer>(len);

        for (int i = 0; i < len; i++) {
            layers.add(parseLayer(layersNode.item(i), onlineResource));
        }

        return layers;
    }


    protected static WMSLayer parseLayer(
        Node layerNode,
        String onlineResource
    ) {
        String title = (String) XMLUtils.xpath(
            layerNode,
            "Title/text()",
            XPathConstants.STRING);

        String name = (String) XMLUtils.xpath(
            layerNode,
            "Name/text()",
            XPathConstants.STRING);

        log.debug("Found layer: " + title + "(" + name + ")");

        boolean queryable = true;
        Node queryableAttr = layerNode.getAttributes()
            .getNamedItem("queryable");
        if (queryableAttr != null
            && queryableAttr.getNodeValue().equals("0")
        ) {
            queryable = false;
        }

        List<String> srs = parseSRS(layerNode);

        NodeList layersNodes = (NodeList) XMLUtils.xpath(
            layerNode,
            "Layer",
            XPathConstants.NODESET);

        List<WMSLayer> layers = parseLayers(layersNodes, onlineResource);

        return new WMSLayer(
            onlineResource, title, name, srs, layers, queryable);
    }


    protected static List<String> parseSRS(Node layerNode) {
        NodeList srsNodes = ((Element) layerNode).getElementsByTagName("SRS");

        if (srsNodes.getLength() == 0) {
            srsNodes = ((Element) layerNode).getElementsByTagName("CRS");

            if (srsNodes.getLength() == 0) {
                log.debug("No explicit SRS for this layer specified.");
                return null;
            }
        }

        List<String> allSRS = new ArrayList<String>();

        for (int i = 0, n = srsNodes.getLength(); i < n; i++) {
            List<String> srs = parseSRSItem(srsNodes.item(i).getTextContent());

            if (srs != null && srs.size() > 0) {
                allSRS.addAll(srs);
            }
        }

        return allSRS;
    }


    protected static List<String> parseSRSItem(String srsStr) {
        if (srsStr == null || srsStr.length() == 0) {
            return null;
        }

        List<String> allSRS = new ArrayList<String>();

        if (srsStr.indexOf(" ") <= 0) {
            String srs = getSRSFromString(srsStr);
            if (srs != null && srs.length() > 0) {
                allSRS.add(srs);
            }

            return allSRS;
        }

        String[] splittedSrs = srsStr.split(" ");

        for (String singleSrs: splittedSrs) {
            String srs = getSRSFromString(singleSrs);
            if (srs != null && srs.length() > 0) {
                allSRS.add(srs);
            }
        }

        return allSRS;
    }


    protected static String getSRSFromString(String singleSrs) {
        Matcher m = SRS_PATTERN.matcher(singleSrs);

        if (m.matches()) {
            log.debug("Found SRS '" + m.group(1) + "'");
            return m.group(1);
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
