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
import org.dive4elements.river.client.client.services.AdvanceService;


/**
 * This interface provides artifact specific operation ADVANCE.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AdvanceServiceImpl
extends      RemoteServiceServlet
implements   AdvanceService
{
    private static final Logger log = LogManager.getLogger(AdvanceService.class);

    public static final String XPATH_RESULT = "/art:result/text()";

    public static final String OPERATION_FAILURE = "FAILED";

    public static final String ERROR_ADVANCE_ARTIFACT =
        "error_advance_artifact";


    public Artifact advance(
        String   locale,
        Artifact artifact,
        String   target)
    throws ServerException
    {
        log.info("AdvanceServiceImpl.advance");

        String url = getServletContext().getInitParameter("server-url");

        Document advance = ClientProtocolUtils.newAdvanceDocument(
            artifact.getUuid(),
            artifact.getHash(),
            target);

        HttpClient client = new HttpClientImpl(url, locale);

        try {
            Document description = (Document) client.advance(
                new org.dive4elements.artifacts.httpclient.objects.Artifact(
                    artifact.getUuid(),
                    artifact.getHash()),
                advance,
                new DocumentResponseHandler());

            if (description == null) {
                throw new ServerException(ERROR_ADVANCE_ARTIFACT);
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

        throw new ServerException(ERROR_ADVANCE_ARTIFACT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
