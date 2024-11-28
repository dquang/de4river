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
import org.dive4elements.river.client.client.services.RiverService;
import org.dive4elements.river.client.server.auth.User;
import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.DefaultRiver;
import org.dive4elements.river.client.shared.model.River;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This interface provides a method to list the supported rivers of the artifact
 * server.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverServiceImpl
extends      RemoteServiceServlet
implements   RiverService
{
    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(RiverServiceImpl.class);

    /** The XPath string that points to the rivers in the resulting document.*/
    public static final String XPATH_RIVERS = "/art:rivers/art:river";

    /** The error message key that is thrown if an error occured while reading
     * the supported rivers from server.*/
    public static final String ERROR_NO_RIVERS_FOUND = "error_no_rivers_found";


    /** Get river list. */
    @Override
    public River[] list(String locale)
    throws ServerException
    {
        String url = getServletContext().getInitParameter("server-url");

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        doc.appendChild(ec.create("action"));

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document res = client.callService(url, "rivers", doc);

            NodeList rivers = (NodeList) XMLUtils.xpath(
                res,
                XPATH_RIVERS,
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

            if (rivers == null || rivers.getLength() == 0) {
                throw new ServerException(ERROR_NO_RIVERS_FOUND);
            }

            int count = rivers.getLength();

            List<River> theRivers = new ArrayList<River>(count);
            User user = this.getUser();

            for (int i = 0; i < count; i++) {
                Element tmp = (Element)rivers.item(i);

                String name = tmp.getAttributeNS(
                    ArtifactNamespaceContext.NAMESPACE_URI, "name");
                String mUuid = tmp.getAttributeNS(
                    ArtifactNamespaceContext.NAMESPACE_URI, "modeluuid");

                if (name.length() > 0
                && (user == null || user.canUseFeature("river:" + name))) {
                    theRivers.add(new DefaultRiver(name, mUuid));
                }
            }

            return theRivers.toArray(new River[theRivers.size()]);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_NO_RIVERS_FOUND);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
