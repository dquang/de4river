/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public interface MeasurementStation extends Serializable {

    /**
     * Returns the name of the measurement station
     */
    String getName();

    /**
     * Returns the start KM of the measurement station or null if not available
     */
    Double getKmStart();

    /**
     * Returns the end KM of the measurement station or null if not available
     */
    Double getKmEnd();

    /**
     * Returns the river to which this measurement station belongs
     */
    String getRiverName();

    /**
     * Returns the side of the river which this measurement station belongs
     */
    String getRiverSide();

    /**
     * Returns the type of the measurement station
     */
    String getMeasurementType();

    /**
     * Returns the ID of the measurement station
     */
    Integer getID();

    /**
     * Returns the operator of the measurement station
     */
    String getOperator();

    /**
     * Returns the start time of the observation at this measurement station
     */
    Date getStartTime();

    /**
     * Returns the end time of the observation at this measurement station
     */
    Date getStopTime();

    /**
     * Returns the name of the gauge in reference to this measurement station
     */
    String getGaugeName();

    /**
     * Returns the comment to this measurement station
     */
    String getComment();
}
