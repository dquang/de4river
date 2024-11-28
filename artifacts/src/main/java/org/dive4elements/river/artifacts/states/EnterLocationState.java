/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.RiverAccess;


/**
 * Get me a double (km).
 */
public class EnterLocationState extends InputDoubleState {

    /** Provoke this kind of provider in the UI. */
    @Override
    protected String getUIProvider() {
        return "location_panel";
    }


    /** Allow from min km of river. */
    @Override
    protected Object getLower(D4EArtifact flys) {
        double[] lowerUpper = new RiverAccess(flys).getRiver()
            .determineMinMaxDistance();

        return lowerUpper != null
            ? lowerUpper[0]
            : 0;
    }


    /** Allow to max km of river. */
    @Override
    protected Object getUpper(D4EArtifact flys) {
        double[] lowerUpper = new RiverAccess(flys).getRiver()
            .determineMinMaxDistance();

        return lowerUpper != null
            ? lowerUpper[1]
            : 0;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
