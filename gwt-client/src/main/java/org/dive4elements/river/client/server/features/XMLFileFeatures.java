/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server.features;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.common.utils.XMLUtils;

public class XMLFileFeatures implements Features {

    private static final Logger log =
        LogManager.getLogger(XMLFileFeatures.class);

    private Map<String, List<String>> featuremap =
        new HashMap<String, List<String>>();

    private final static String XPATH_FEATURES = "ftr:feature/child::text()";
    private final static String XPATH_ROLES    = "/ftr:features/ftr:role";

    public XMLFileFeatures(String filename) throws IOException {
        FileInputStream finput = new FileInputStream(filename);
        log.debug("XMLFileFeatures: " + filename);
        try {
            Document doc = XMLUtils.parseDocument(finput);

            NodeList roles = (NodeList) XMLUtils.xpath(
                doc,
                XPATH_ROLES,
                XPathConstants.NODESET,
                FeaturesNamespaceContext.INSTANCE);

            for(int i = 0, m = roles.getLength(); i < m; i++) {
                Element rolenode = (Element)roles.item(i);

                String name = rolenode.getAttribute("name");

                log.debug("Found role: " + name);

                NodeList features = (NodeList) XMLUtils.xpath(
                    rolenode,
                    XPATH_FEATURES,
                    XPathConstants.NODESET,
                    FeaturesNamespaceContext.INSTANCE);

                if (features == null) {
                    continue;
                }

                int N = features.getLength();

                if (N > 0) {
                    List<String> allowed = new ArrayList<String>(N);
                    for (int j = 0; j < N; j++) {
                        Node featurenode = features.item(j);
                        String featurename = featurenode.getNodeValue();

                        log.debug("Found feature: " + featurename);

                        allowed.add(featurename);
                    }
                    featuremap.put(name, allowed);
                }
            }
            log.debug("Loaded all features");
        }
        finally {
            finput.close();
        }
    }

    @Override
    public List<String> getFeatures(List<String> roles) {
        List<String> features = new ArrayList<String>();

        for (String role: roles) {
            List<String> allowed = this.featuremap.get(role);
            if (allowed != null) {
                features.addAll(allowed);
            }
        }
        return features;
    }
}
