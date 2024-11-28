/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.RangeWithValues;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * State to input W/Q data for fixings
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class WQFixing extends WQAdapted {

    /** The log used in this state.*/
    private static Logger log = LogManager.getLogger(WQFixing.class);

    /** Simple sanity check if values are positive numbers **/
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        log.debug("WQFixing.validate");

        RangeWithValues[] rwvs = extractInput(
            getData((D4EArtifact) artifact, "wq_values"));

        if (rwvs == null) {
            throw new IllegalArgumentException("error_missing_wq_data");
        }

        for (RangeWithValues rwv: rwvs) {
            double[] values = rwv.getValues();
            for (double val: values) {
                if (val <= 0) {
                    throw new IllegalArgumentException(
                        "error_validate_positive");
                }
            }
        }

        return true;
    }

    @Override
    protected String getUIProvider() {
        return "wq_panel_adapted_fixing";
    }
}
