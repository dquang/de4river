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
import org.dive4elements.river.client.shared.model.CollectionItemAttribute;

/**
 * This interface provides a method to retrieve an artifact based on its uuid.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@RemoteServiceRelativePath("collection-item-attribute")
public interface CollectionItemAttributeService extends RemoteService {

    CollectionItemAttribute getCollectionItemAttribute(
        Collection collection,
        String artifact,
        String locale)
    throws ServerException;

    void setCollectionItemAttribute(
        Collection collection,
        String artifact,
        String locale,
        CollectionItemAttribute attribute)
    throws ServerException;

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
