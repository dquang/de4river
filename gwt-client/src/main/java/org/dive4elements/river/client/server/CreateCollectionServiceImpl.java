/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.DefaultCollection;
import org.dive4elements.river.client.client.services.CreateCollectionService;


/**
 * This interface provides the createCollection service to create new
 * collections in the artifact server.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CreateCollectionServiceImpl
extends      RemoteServiceServlet
implements   CreateCollectionService
{
    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(CreateCollectionServiceImpl.class);

    /** XPath to figure out the uuid of the created collection.*/
    public static final String XPATH_COLLECTION_UUID =
        "/art:result/art:artifact-collection/@art:uuid";

    /** XPath to figure out the ttl of the created collection.*/
    public static final String XPATH_COLLECTION_TTL =
        "/art:result/art:artifact-collection/@art:ttl";

    /** Error message key that is thrown if an error occured while creating
     *  a new collection.*/
    public static final String ERROR_CREATE_COLLECTION =
        "error_create_collection";


    /** Attempt creation of Collection. */
    public Collection create(String locale, String ownerId)
    throws ServerException
    {
        log.info("Start creating a new collection.");

        String url  = getServletContext().getInitParameter("server-url");

        Document create  =
            ClientProtocolUtils.newCreateCollectionDocument(null);
        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document doc = (Document) client.createCollection(
                create, ownerId, new DocumentResponseHandler());

            String uuid = XMLUtils.xpathString(
                doc, XPATH_COLLECTION_UUID, ArtifactNamespaceContext.INSTANCE);

            String ttlStr = XMLUtils.xpathString(
                doc, XPATH_COLLECTION_TTL, ArtifactNamespaceContext.INSTANCE);

            if (uuid.trim().length() == 0 || ttlStr.length() == 0) {
                throw new ServerException(ERROR_CREATE_COLLECTION);
            }

            return new DefaultCollection(uuid, Long.valueOf(ttlStr), uuid);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_CREATE_COLLECTION);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
