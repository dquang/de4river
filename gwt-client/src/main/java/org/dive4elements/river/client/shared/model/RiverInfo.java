/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */

public interface RiverInfo extends Serializable {

    boolean isKmUp();

    /**
     * Start KM of the river
     */
    Double getKmStart();

    /**
     * End KM of the river
     */
    Double getKmEnd();

    /**
     * Returns the name of the river
     */
    String getName();

    /**
     * Returns the name of the WST unit
     */
    String getWstUnit();

    /**
     * Return all gauge info of the river
     */
    List<GaugeInfo> getGauges();

    /**
     * Returns the min q value of the river
     */
    Double getMinQ();

    /**
     * Returns the max q value of the river
     */
    Double getMaxQ();

    /**
     * Returns the official number of the river
     */
    Long getOfficialNumber();

    /**
     * Returns the MeasurementStations on this river or null if they aren't
     * available.
     */
    List<MeasurementStation> getMeasurementStations();
}


