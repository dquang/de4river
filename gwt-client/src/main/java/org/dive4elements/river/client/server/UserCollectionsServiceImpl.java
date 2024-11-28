/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import java.util.ArrayList;
import java.util.Date;
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

import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.DefaultCollection;
import org.dive4elements.river.client.client.services.UserCollectionsService;


/**
 * This service returns a list of collections owned by a specified user.
 * <b>NOTE:</b> The Collections returned by this service provide no information
 * about the CollectionItems or OutputModes of the Collection. You need to fetch
 * these information explicitly using another service.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class UserCollectionsServiceImpl
extends      RemoteServiceServlet
implements   UserCollectionsService
{
    private static final Logger log = LogManager.getLogger(
        UserCollectionsServiceImpl.class);


    public Collection[] getUserCollections(String locale, String userid) {
        log.info("UserCollectionsServiceImpl.getUserCollections");

        String serverUrl  = getServletContext().getInitParameter("server-url");
        HttpClient client = new HttpClientImpl(serverUrl, locale);

        try {
            Document result = client.listUserCollections(userid);

            NodeList list = (NodeList) XMLUtils.xpath(
                result,
                "/art:artifact-collections/art:artifact-collection",
                XPathConstants.NODESET,
                ArtifactNamespaceContext.INSTANCE);

            if (list == null || list.getLength() == 0) {
                log.debug("No collection found for user: " + userid);
                return null;
            }

            int num = list.getLength();

            List<Collection> all = new ArrayList<Collection>(num);

            for (int i = 0; i < num; i++) {
                Collection c = createCollection((Element) list.item(i));

                if (c != null) {
                    all.add(c);
                }
            }

            log.debug("User has " + all.size() + " collections.");

            return all.toArray(new Collection[all.size()]);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        log.debug("No user collections found.");
        return null;
    }


    /**
     * Extracts a SimpleCollection from <i>node</i>.
     *
     * @param node Contains information about a collection.
     *
     * @return a list of Simplecollections.
     */
    protected Collection createCollection(Element node) {
        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        String creationStr = node.getAttributeNS(uri, "creation");
        String name        = node.getAttributeNS(uri, "name");
        String uuid        = node.getAttributeNS(uri, "uuid");
        String ttlStr      = node.getAttributeNS(uri, "ttl");

        if (!uuid.isEmpty() && !ttlStr.isEmpty() && !creationStr.isEmpty()) {
            try {
                long time = Long.parseLong(creationStr);
                long ttl  = Long.parseLong(ttlStr);
                return new DefaultCollection(uuid, ttl, name, new Date(time));
            }
            catch (NumberFormatException nfe) {
                log.warn("Error while parsing collection attributes.");
                return null;
            }
        }

        log.warn("Found an invalid Collection.");
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
