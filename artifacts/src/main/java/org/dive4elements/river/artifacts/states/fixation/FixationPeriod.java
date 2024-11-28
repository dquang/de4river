/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.states.DefaultState;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixationPeriod extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(FixationPeriod.class);


    /**
     * The default constructor that initializes an empty State object.
     */
    public FixationPeriod() {
    }

    @Override
    protected String getUIProvider() {
        return "fix.period_panel";
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
