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
import org.dive4elements.river.client.shared.model.Collection;


/**
 * This interface describes the service to remove an existing artifact to an
 * existing collection.
 *
 */
@RemoteServiceRelativePath("remove-artifact")
public interface RemoveArtifactService extends RemoteService {

    /**
     * Removes an artifact from a collection.
     *
     * @param collection The Collection that should be modified.
     * @param artifactId The artifact that should be removed.
     * @param url        The url of the artifact server.
     * @param locale     locae to use (for localized responses).
     *
     * @return the Collection after the operation.
     */
    Collection remove(
        Collection collection,
        String     artifactId,
        String     locale)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
