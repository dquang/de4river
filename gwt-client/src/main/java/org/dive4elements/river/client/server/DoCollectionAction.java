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
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DoCollectionAction extends RemoteServiceServlet {

    private static final Logger log =
        LogManager.getLogger(DoCollectionAction.class);


    public static final String XPATH_RESULT      = "/art:result/text()";
    public static final String OPERATION_FAILURE = "FAILED";
    public static final String FAILURE_EXCEPTION = "collection_action_failed";


    protected void doAction(Collection c, Document action, String url)
    throws    ServerException
    {
        log.info("DoCollectionAction.doAction");

        HttpClient client = new HttpClientImpl(url);

        try {
            Document res  = (Document) client.doCollectionAction(
                action, c.identifier(),
                new DocumentResponseHandler());

            String result = XMLUtils.xpathString(
                res,
                XPATH_RESULT,
                ArtifactNamespaceContext.INSTANCE);

            if (result == null || result.equals(OPERATION_FAILURE)) {
                log.error("Operation failed.");
                throw new ServerException(FAILURE_EXCEPTION);
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
            throw new ServerException(FAILURE_EXCEPTION);
        }
    }
}
