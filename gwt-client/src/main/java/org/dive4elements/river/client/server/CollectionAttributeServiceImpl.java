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

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;

import org.dive4elements.river.client.client.services.CollectionAttributeService;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionAttributeServiceImpl
extends      DescribeCollectionServiceImpl
implements   CollectionAttributeService
{
    private static final Logger log =
        LogManager.getLogger(CollectionAttributeServiceImpl.class);


    public static final String ERROR_UPDATING_COLLECTION_ATTRIBUTE =
        "error_update_collection_attribute";


    public Collection update(Collection collection, String locale)
    throws ServerException
    {
        log.info("CollectionAttributeServiceImpl.update");

        String url  = getServletContext().getInitParameter("server-url");

        Document attribute = CollectionHelper.createAttribute(collection);
        Document action    = ClientProtocolUtils.newSetAttributeDocument(
            collection.identifier(),
            attribute);

        try {
            HttpClient http = new HttpClientImpl(url, locale);
            Document   res  = (Document) http.doCollectionAction(
                action,
                collection.identifier(),
                new DocumentResponseHandler());

            log.debug("Collection attribute successfully set.");

            return describe(collection.identifier(), locale);
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_UPDATING_COLLECTION_ATTRIBUTE);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
