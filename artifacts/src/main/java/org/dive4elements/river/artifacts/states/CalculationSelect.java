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

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CalculationSelect extends DefaultState {

    /** The log that is used in this class. */
    private static Logger log = LogManager.getLogger(CalculationSelect.class);

    /** Name of data item. */
    public static final String FIELD_MODE = "calculation_mode";

    /** Constant value for the reference line calculation. */
    public static final String CALCULATION_SURFACE_CURVE =
        "calc.surface.curve";

    /** Constant value for the differences calculation. */
    public static final String CALCULATION_DURATION_CURVE =
        "calc.duration.curve";

    /** Constant value for the flood map calculation. */
    public static final String CALCULATION_FLOOD_MAP =
        "calc.flood.map";

    /** Constant value for the profile calculation. */
    public static final String CALCULATION_DISCHARGE_LONGITUDINAL_CURVE =
        "calc.discharge.longitudinal.section";

    /** Constant value for the state discharge curve calculation. */
    public static final String CALCULATION_DISCHARGE_CURVE =
        "calc.discharge.curve";

    /** Constant value for the state w differences calculation. */
    public static final String CALCULATION_W_DIFFERENCES =
        "calc.w.differences";

    /** Constant value for the state reference curve calculation. */
    public static final String CALCULATION_REFERENCE_CURVE =
        "calc.reference.curve";

    /** Constant value for the historical discharge curve calculation. */
    public static final String CALCULATION_HISTORICAL_DISCHARGE_CURVE =
        "calc.historical.discharge.curve";

    /** Constant value for the extreme W curve calculation. */
    public static final String CALCULATION_EXTREME =
        "calc.extreme.curve";

    /** An array that holds all available calculation modes. */
    public static final String[] CALCULATIONS = {
        CALCULATION_SURFACE_CURVE,
        CALCULATION_FLOOD_MAP,
        CALCULATION_DISCHARGE_CURVE,
        CALCULATION_HISTORICAL_DISCHARGE_CURVE,
        CALCULATION_DURATION_CURVE,
        CALCULATION_DISCHARGE_LONGITUDINAL_CURVE,
        CALCULATION_W_DIFFERENCES,
        CALCULATION_REFERENCE_CURVE //,
//        CALCULATION_EXTREME
    };


    /** Error message that is thrown if no mode has been chosen. */
    public static final String ERROR_NO_CALCULATION_MODE =
        "error_feed_no_calculation_mode";

    /** Error message that is thrown if an invalid calculation mode has been
     * chosen. */
    public static  final String ERROR_INVALID_CALCULATION_MODE =
        "error_feed_invalid_calculation_mode";


    public CalculationSelect() {
    }


    /** Create choices (i18ned). */
    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        CallMeta meta   = context.getMeta();
        Element[] calcs = new Element[CALCULATIONS.length];

        for (int i = 0; i < CALCULATIONS.length; ++i) {
            String calc = CALCULATIONS[i];
            calcs[i] = createItem(
                cr, new String[] {
                    Resources.getMsg(meta, calc, calc),
                    calc
                });
        }

        return calcs;
    }


    /** Validate the chosen calculation. */
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("CalculationSelect.validate");
        D4EArtifact flys = (D4EArtifact) artifact;

        StateData data = getData(flys, FIELD_MODE);
        String    calc = (data != null) ? (String) data.getValue() : null;

        if (calc == null) {
            throw new IllegalArgumentException(ERROR_NO_CALCULATION_MODE);
        }

        calc = calc.trim().toLowerCase();

        for (String mode: CALCULATIONS) {
            if (mode.equals(calc)) {
                return true;
            }
        }

        throw new IllegalArgumentException(ERROR_INVALID_CALCULATION_MODE);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
