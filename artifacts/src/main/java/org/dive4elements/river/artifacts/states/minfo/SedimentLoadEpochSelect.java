/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.states.DefaultState;


public class SedimentLoadEpochSelect
extends DefaultState
{
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(SedimentLoadEpochSelect.class);


    /**
     * The default constructor that initializes an empty State object.
     */
    public SedimentLoadEpochSelect() {
    }

    @Override
    protected String getUIProvider() {
        return "minfo.sedimentload_epoch_select";
    }
}
