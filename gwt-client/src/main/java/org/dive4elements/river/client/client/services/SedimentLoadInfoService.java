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
import org.dive4elements.river.client.shared.model.SedimentLoadInfoObject;

/**
 * This service is used to fetch a list of SedimentLoadInfoObjects from artifact
 * server for a specific river.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
@RemoteServiceRelativePath("sedimentloadinfo")
public interface SedimentLoadInfoService extends RemoteService {

    /**
     * Returns a list of SedimentLoadInfoObjects for a specific river.
     */
    SedimentLoadInfoObject[] getSedimentLoadInfo(
        String locale,
        String river,
        String type,
        double startKm,
        double endKm,
        String sq_ti_id)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
