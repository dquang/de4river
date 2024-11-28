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
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.client.services.RemoveArtifactService;


/**
 * Implementation of RemoveArtifactService .
 */
public class RemoveArtifactServiceImpl
extends      DescribeCollectionServiceImpl
implements   RemoveArtifactService
{
    private static final Logger log =
        LogManager.getLogger(RemoveArtifactServiceImpl.class);


    public Collection remove(
        Collection collection,
        String     artifactId,
        String     locale)
    throws ServerException
    {
        log.info("RemoveArtifactServiceImpl.remove");

        String url = getServletContext().getInitParameter("server-url");

        return CollectionHelper.removeArtifact(
            collection, artifactId, url, locale);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
