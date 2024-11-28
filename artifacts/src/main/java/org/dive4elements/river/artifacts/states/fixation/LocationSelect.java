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

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.states.RangeState;
import org.dive4elements.river.artifacts.model.FixingsOverviewFactory;
import org.dive4elements.river.artifacts.model.FixingsOverview;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class LocationSelect extends RangeState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(LocationSelect.class);

    /**
     * The default constructor that initializes an empty State object.
     */
    public LocationSelect() {
    }

    @Override
    protected String getUIProvider() {
        return "fix.location_panel";
    }

    @Override
    protected double[] getMinMax(Artifact artifact) {
        D4EArtifact flysArtifact = (D4EArtifact) artifact;
        String riverName = flysArtifact.getDataAsString("river");
        FixingsOverview overview = FixingsOverviewFactory
            .getOverview(riverName);

        return new double[]{
            overview.getExtent().getStart(),
            overview.getExtent().getEnd()};
    }


    /** Misuse to set location mode. */
    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        ((D4EArtifact) artifact).addStringData("ld_mode", "distance");
        return super.validate(artifact);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
