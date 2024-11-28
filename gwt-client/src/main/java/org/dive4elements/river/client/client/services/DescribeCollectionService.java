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
 * This service describes an operation the fetches the DESCRIBE document of a
 * specific collection and returns a Collection.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
@RemoteServiceRelativePath("describe-collection")
public interface DescribeCollectionService extends RemoteService {

    /**
     * Adds an artifact to a collection.
     *
     * @param uuid The uuid of the desired collection.
     * @param url  The url of the artifact server.
     * @param locale The name of the locale used for the request.
     *
     * @return the Collection after the operation.
     */
    Collection describe(String uuid, String locale)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
