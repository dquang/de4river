/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixationSelect extends DefaultState {

    /** Constant value for the default fixation calculation. */
    public static final String CALCULATION_DEFAULT = "calculation.analysis";

    /** Constant value for the vollmer fixation analysis. */
    public static final String CALCULATION_VOLLMER = "calculation.vollmer";

    /** An Array that holds all available calculation modes.*/
    public static final String[] CALCULATIONS = {
        CALCULATION_DEFAULT,
        CALCULATION_VOLLMER
    };

    /** Error message that is thrown if no mode has been chosen. */
    public static final String ERROR_NO_CALCULATION_MODE =
        "error_feed_no_calculation_mode";

    /** Error message that is thrown if an invalid calculation mode has been
     * chosen. */
    public static  final String ERROR_INVALID_CALCULATION_MODE =
        "error_feed_invalid_calculation_mode";


    /**
     * The default constructor that initializes an empty State object.
     */
    public FixationSelect() {
    }


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator ec,
        Artifact                artifact,
        String                  name,
        CallContext             context)
    {
        CallMeta meta = context.getMeta();
        Element[] calculations = new Element[CALCULATIONS.length];

        for (int i = 0; i < CALCULATIONS.length; i++) {
            String calc = CALCULATIONS[i];
            calculations[i] = createItem(
                ec,
                new String[] {Resources.getMsg(meta, calc, calc), calc});
        }
        return calculations;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
