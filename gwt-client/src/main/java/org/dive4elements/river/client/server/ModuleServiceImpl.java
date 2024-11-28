/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.river.client.client.services.ModuleService;
import org.dive4elements.river.client.server.auth.User;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.DefaultModule;
import org.dive4elements.river.client.shared.model.Module;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ModuleServiceImpl
extends      RemoteServiceServlet
implements   ModuleService
{
    private static final Logger log =
        LogManager.getLogger(ModuleServiceImpl.class);

    public static final String XPATH_MODULES = "/art:modules/art:module";

    public static final String ERROR_NO_MODULES_FOUND =
        "error_no_module_found";

    @Override
    public Module[] list(String locale) throws ServerException {
        User user = this.getUser();

        log.info("ModuleService.list");

        String url = getServletContext().getInitParameter("server-url");

        // create dummy xml
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element dummy = ec.create("modules");
        doc.appendChild(dummy);

        HttpClient client = new HttpClientImpl(url, locale);
        try {
            Document result = client.callService(url, "modules", doc);

            NodeList list = (NodeList) XMLUtils.xpath(
                result,
                XPATH_MODULES,
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

            if (list == null) {
                log.warn("No modules found.");

                throw new ServerException(ERROR_NO_MODULES_FOUND);
            }

            int num = list.getLength();

            List<Module> modules = new ArrayList<Module>(list.getLength());
            for(int i =0; i < num; i++) {
                Element em = (Element)list.item(i);
                String name = em.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "name");
                String localname = em.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "localname");
                String strselected = em.getAttributeNS(
                        ArtifactNamespaceContext.NAMESPACE_URI, "selected");
                boolean selected = strselected == null ? false :
                        strselected.equalsIgnoreCase("true");
                NodeList rivers = em.getChildNodes();
                List<String> riverUuids = new ArrayList<String>();
                for (int j = 0; j < rivers.getLength(); j++) {
                    Element re = (Element)rivers.item(j);
                    riverUuids.add(re.getAttribute("uuid"));
                }
                log.debug("Found module " + name + " " + localname);
                if (user == null || user.canUseFeature("module:" + name)) {
                    modules.add(new DefaultModule(
                            name, localname, selected, riverUuids));
                }
            }
            return modules.toArray(new Module[modules.size()]);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_MODULES_FOUND);
    }
}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 tw=80 :
