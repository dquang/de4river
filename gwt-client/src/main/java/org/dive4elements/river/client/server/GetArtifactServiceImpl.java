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
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.client.services.GetArtifactService;


/**
 * This service provides a method that returns an artifact based on its
 * identifier.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GetArtifactServiceImpl
extends      RemoteServiceServlet
implements   GetArtifactService
{
    private static final Logger log =
        LogManager.getLogger(GetArtifactServiceImpl.class);


    public static final String ERROR_DESCRIBE_ARTIFACT =
        "error_describe_artifact";

    public static final String XPATH_RESULT = "/art:result/text()";

    public static final String OPERATION_FAILURE = "FAILED";


    public Artifact getArtifact(
        String locale,
        String uuid,
        String hash)
    throws ServerException
    {
        log.info("GetArtifactServiceImpl.getArtifact");

        String url  = getServletContext().getInitParameter("server-url");

        Document describe = ClientProtocolUtils.newDescribeDocument(
            uuid, hash, true);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document description = (Document) client.describe(
                new org.dive4elements.artifacts.httpclient.objects.Artifact(
                    uuid, hash),
                describe,
                new DocumentResponseHandler());

            if (description == null) {
                throw new ServerException(ERROR_DESCRIBE_ARTIFACT);
            }

            String result = XMLUtils.xpathString(
                description,
                XPATH_RESULT,
                ArtifactNamespaceContext.INSTANCE);

            if (result == null || !result.equals(OPERATION_FAILURE)) {
                return (Artifact) new FLYSArtifactCreator().create(description);
            }
        }
        catch (ConnectionException ce) {
            log.error(ce, ce);
        }

        throw new ServerException(ERROR_DESCRIBE_ARTIFACT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
