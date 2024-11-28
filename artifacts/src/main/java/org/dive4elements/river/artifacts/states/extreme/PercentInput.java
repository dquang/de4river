/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.extreme;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.states.DefaultState;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class PercentInput extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(PercentInput.class);

    public static final String PARAMETER_NAME = "percent";


    /**
     * The default constructor that initializes an empty State object.
     */
    public PercentInput () {
    }


    /** Tell UI what to display for input. */
    @Override
    protected String getUIProvider() {
        return "percent_input";
    }


    @Override
    protected String[] getDefaultsFor(CallContext context, StateData data) {
        if (data != null && data.getName().equals(PARAMETER_NAME)) {
            return new String[] {"5", "5"};
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
