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

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.client.services.AddArtifactService;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AddArtifactServiceImpl
extends      DescribeCollectionServiceImpl
implements   AddArtifactService
{
    private static final Logger log =
        LogManager.getLogger(AddArtifactService.class);


    public Collection add(
        Collection collection,
        Artifact   artifact,
        String     locale)
    throws ServerException
    {
        log.info("AddArtifactServiceImpl.add");
        String url  = getServletContext().getInitParameter("server-url");

        return CollectionHelper.addArtifact(collection, artifact, url, locale);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
