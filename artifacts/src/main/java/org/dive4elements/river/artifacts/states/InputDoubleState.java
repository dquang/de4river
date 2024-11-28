/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;


/**
 * State to keep a double value and validate it against a range.
 */
public class InputDoubleState extends MinMaxState {

    private static final Logger log = LogManager.getLogger(InputDoubleState.class);


    @Override
    protected String getUIProvider() {
        return "location_panel";
    }


    @Override
    protected Object getLower(D4EArtifact flys) {
        return 0;
    }


    @Override
    protected Object getUpper(D4EArtifact flys) {
        return 0;
    }


    @Override
    protected String getType() {
        return "double";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
