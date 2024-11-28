/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.river.client.client.services.ServerInfoService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServerInfoServiceImpl extends RemoteServiceServlet implements
        ServerInfoService {

    // This works only because currently there is only one info transmitted
    private static final String XPATH_INFO = "/art:server/art:info";

    private final Logger log = LogManager.getLogger(ServerInfoServiceImpl.class);

    @Override
    public Map<String, String> getConfig(String locale) {
        Map<String, String> infoMap = new HashMap<String, String>();
        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        doc.appendChild(ec.create("action"));

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document res = client.callService(url, "server-info", doc);

            NodeList info = (NodeList) XMLUtils.xpath(res,
                    XPATH_INFO,
                    XPathConstants.NODESET,
                    ArtifactNamespaceContext.INSTANCE);

            for (int n = 0; n < info.getLength(); n++) {
                Element el = (Element)info.item(n);
                String key = el.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "key");
                String val = el.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "value");
                infoMap.put(key, val);

                log.debug("ServerInfoServiceImpl: " + key + "=" + val);
            }
        }
        catch (Exception ex) {
            log.error(ex, ex);
        }

        return infoMap;
    }


}
