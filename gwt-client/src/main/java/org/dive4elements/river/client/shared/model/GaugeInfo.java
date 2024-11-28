/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public interface GaugeInfo extends Serializable {

    /**
     * Returns the name of the gauge.
     */
    String getName();

    /**
     * Returns the start KM of the gauge or null if not available.
     */
    Double getKmStart();

    /**
     * Returns the end KM of the gauge or null if not available.
     */
    Double getKmEnd();

    /**
     * Returns the mimimum Q value at this gauge or null if not available.
     */
    Double getMinQ();

    /**
     * Returns the maximum Q value at this gauge or null if not available.
     */
    Double getMaxQ();

    /**
     * Returns the mimimum W value at this gauge or null if not available.
     */
    Double getMinW();

    /**
     * Returns the maximim W value at this gauge or null if not available.
     */
    Double getMaxW();

    /**
     * Returns the datum value or null if not available.
     */
    Double getDatum();

    /**
     * Returns the aeo value or null if not available.
     */
    Double getAeo();

    boolean isKmUp();

    /**
     * Returns the station km of the gauge or null if not available.
     */
    Double getStation();

    /**
     * Returns the wst unit as a String.
     */
    String getWstUnit();

    /**
     * Returns the official number of this gauge.
     */
    Long getOfficialNumber();

    /**
     * Returns the river to which this gauge belongs.
     */
    String getRiverName();
}
