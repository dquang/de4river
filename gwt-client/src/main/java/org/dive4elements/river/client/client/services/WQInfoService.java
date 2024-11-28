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
import org.dive4elements.river.client.shared.model.WQInfoObject;

/**
 * This service is used to fetch a list of DistanceInfoObjects from artifact
 * server for a specific river.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@RemoteServiceRelativePath("mainvalues")
public interface WQInfoService extends RemoteService {

    /**
     * This method returns a list of DistanceInfoObjects for a specific river.
     */
    WQInfoObject[] getWQInfo(
        String locale,
        String river,
        double start,
        double end)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
