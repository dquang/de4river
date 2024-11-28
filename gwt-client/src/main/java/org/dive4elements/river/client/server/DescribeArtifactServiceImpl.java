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
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.client.services.DescribeArtifactService;


/**
 * This interface provides artifact specific operation DESCRIBE.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DescribeArtifactServiceImpl
extends      RemoteServiceServlet
implements   DescribeArtifactService
{
    private static final Logger log =
        LogManager.getLogger(DescribeArtifactServiceImpl.class);


    public static final String ERROR_DESCRIBE_ARTIFACT =
        "error_describe_artifact";


    public Artifact describe(String locale, Artifact artifact)
    throws ServerException
    {
        log.info("DescribeArtifactServiceImpl.describe");

        String url  = getServletContext().getInitParameter("server-url");

        Document describe = ClientProtocolUtils.newDescribeDocument(
            artifact.getUuid(),
            artifact.getHash(),
            true);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            log.debug("Start Http request now.");

            Document description = (Document) client.describe(
                new org.dive4elements.artifacts.httpclient.objects.Artifact(
                    artifact.getUuid(),
                    artifact.getHash()),
                describe,
                new DocumentResponseHandler());

            if (description != null) {
                log.debug("Finished Http request sucessfully!");

                return (Artifact) new FLYSArtifactCreator().create(description);
            }
        }
        catch (ConnectionException ce) {
            ce.printStackTrace();
        }

        throw new ServerException(ERROR_DESCRIBE_ARTIFACT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
