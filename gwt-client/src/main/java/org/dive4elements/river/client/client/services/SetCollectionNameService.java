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
 * This interface describes the service to add an existing artifact to an
 * existing collection.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
@RemoteServiceRelativePath("set-collectionname")
public interface SetCollectionNameService extends RemoteService {

    /**
     * Set the name of a collection.
     *
     * @param collection The Collection that should be extended.
     */
    void setName(Collection collection)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
