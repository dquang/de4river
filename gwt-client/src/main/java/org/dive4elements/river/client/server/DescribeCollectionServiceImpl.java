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

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;

import org.dive4elements.river.client.client.services.DescribeCollectionService;


/**
 * This service implements a method that queries the DESCRIBE document of a
 * specific collection and returns a Collection object with the information of
 * the document.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DescribeCollectionServiceImpl
extends      RemoteServiceServlet
implements   DescribeCollectionService
{
    private static final Logger log =
        LogManager.getLogger(DescribeCollectionServiceImpl.class);


    /** The error message key that is thrown if an error occured while
     * describe() a Collection.*/
    public static final String ERROR_DESCRIBE_COLLECTION =
        "error_describe_collection";


    public Collection describe(String uuid, String locale)
    throws ServerException
    {
        log.info("DescribeCollectionServiceImpl.describe");

        String url  = getServletContext().getInitParameter("server-url");

        Document describe = ClientProtocolUtils.newDescribeCollectionDocument(
            uuid);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document response = (Document) client.doCollectionAction(
                describe, uuid, new DocumentResponseHandler());

            Collection c = CollectionHelper.parseCollection(response);

            if (c == null) {
                throw new ServerException(ERROR_DESCRIBE_COLLECTION);
            }

            log.debug("Collection successfully parsed.");

            return c;
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_DESCRIBE_COLLECTION);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
