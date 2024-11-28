/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;

import org.dive4elements.river.client.shared.model.Recommendation;

/**
 * This interface provides artifact specific services as CREATE, DESCRIBE, FEED,
 * ADVANCE and OUT.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
@RemoteServiceRelativePath("artifact")
public interface ArtifactService extends RemoteService {

    /**
     * This method creates a new artifact based on the given <i>factory</i>.
     *
     * @param serverUrl The url of the artifact server.
     * @param locale The locale used for the request.
     * @param factory The factory that should be used for the artifact creation.
     *
     * @return the new artifact.
     */
    public Artifact create(
        String         locale,
        String         factory,
        Recommendation recommendation
    ) throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
