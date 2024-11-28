/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Through this service the client can retrieve basic information about or
 * configuration of the artifact server.
 * Currently it only returns the help-url (wiki) to the client.
 *
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class ServerInfoService extends D4EService {

    /** The log used in this service.*/
    private static Logger log = LogManager.getLogger(ServerInfoService.class);

    private static final String XPATH_HELP_URL =
        "/artifact-database/help-url/text()";

    @Override
    protected Document doProcess(Document data, GlobalContext globalContext,
            CallMeta callMeta) {
        log.debug("ServerInfoService.process");

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element serverInfo = ec.create("server");

        String helpUrl = (String) XMLUtils.xpath(
                Config.getConfig(),
                XPATH_HELP_URL,
                XPathConstants.STRING);

        Element info = ec.create("info");
        ec.addAttr(info, "key", "help-url", true);
        ec.addAttr(info, "value", helpUrl, true);
        serverInfo.appendChild(info);

        result.appendChild(serverInfo);

        return result;
    }

}
