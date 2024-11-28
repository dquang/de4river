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

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;


public class DistanceOnlySelect extends DistanceSelect {

    private static Logger log = LogManager.getLogger(DistanceOnlySelect.class);

    @Override
    protected String getUIProvider() {
        return "distance_only_panel";
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        // TODO think about better hierarchy wrt RangeState#validate.
        D4EArtifact flys = (D4EArtifact) artifact;

        try {
            RangeAccess rangeAccess = new RangeAccess(flys);
            double from = rangeAccess.getFrom();
            double to   = rangeAccess.getTo();

            double[] minmax = getMinMax(flys);

            return validateBounds(minmax[0], minmax[1], from, to);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("error_invalid_double_value");
        }
        catch (NullPointerException npe) {
            throw new IllegalArgumentException("error_empty_state");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
