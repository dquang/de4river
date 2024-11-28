/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;


public class StaticSQRelationAccess
extends RiverAccess
{
    /** The log that is used in this state. */
    private static final Logger log =
        LogManager.getLogger(StaticSQRelationAccess.class);

    private String measurementStation;

    public StaticSQRelationAccess(D4EArtifact artifact) {
        super(artifact);
    }

    /** Get measurement station */
    public String getMeasurementStation() {
        if (measurementStation == null) {
            measurementStation = getString("station");
        }
        if (log.isDebugEnabled()) {
            log.debug("measurement station: '" + measurementStation + "'");
        }
        return measurementStation;
    }
}
