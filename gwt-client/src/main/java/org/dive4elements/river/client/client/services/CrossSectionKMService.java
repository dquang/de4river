/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dive4elements.river.client.shared.exceptions.ServerException;

/**
 * This interface provides access to CrossSectionKMService .
 */
@RemoteServiceRelativePath("cross-section-km")
public interface CrossSectionKMService extends RemoteService {

    /**
     * @param serverUrl The url of the artifact server.
     * @param locale The locale used for the request.
     * @param artifact The artifact.
     * @param data The data that should be inserted.
     *
     * @return the artifact which description might have been changed.
     */
    public Map<Integer,Double[]> getCrossSectionKMs(
        String               locale,
        Map<Integer, Double> data,
        int                  nNeightbours)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
