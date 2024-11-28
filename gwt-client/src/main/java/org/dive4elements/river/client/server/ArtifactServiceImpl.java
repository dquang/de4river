/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.client.services.ArtifactService;

import org.dive4elements.river.client.shared.model.Recommendation;

/**
 * This interface provides artifact specific services as CREATE, DESCRIBE, FEED,
 * ADVANCE and OUT.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ArtifactServiceImpl
extends      RemoteServiceServlet
implements   ArtifactService
{
    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(ArtifactServiceImpl.class);


    /**
     * Creates new Artifacts based on a given Recommendation and factory.
     * <b>Note, that all the work is done in ArtifactHelper!</b>
     *
     * @param locale The locale used for HTTP request.
     * @param factory The factory that is used to create the new Artifact.
     * @param recom Recommendation with details of the artifact to create.
     *
     * @return a new Artifact.
     */
    public Artifact create(
        String         locale,
        String         factory,
        Recommendation recom
    )
    throws ServerException
    {
        log.info("ArtifactServiceImpl.create");

        String url  = getServletContext().getInitParameter("server-url");

        return ArtifactHelper.createArtifact(url, locale, factory, recom);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
