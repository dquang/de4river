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
import org.dive4elements.river.client.shared.model.RiverInfo;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
@RemoteServiceRelativePath("riverinfo")
public interface RiverInfoService extends RemoteService {

    /**
     * Returns a RiverInfo object with GaugeInfos
     */
    public RiverInfo getGauges(String river)
        throws ServerException;

    /**
     * Returns a RiverInfo object with MeasurementStations
     */
    public RiverInfo getMeasurementStations(String river)
        throws ServerException;

}
