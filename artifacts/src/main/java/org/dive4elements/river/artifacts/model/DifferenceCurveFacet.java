/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Facet with the curve of a subtraction of two waterlevel-lines.
 * TODO inherit directly from DataFacet? Check whether this Facet is obsolete.
 */
public class DifferenceCurveFacet extends WaterlevelFacet {

    private static Logger log = LogManager.getLogger(DifferenceCurveFacet.class);


    public DifferenceCurveFacet() {
    }

    public DifferenceCurveFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateID,
        String      hash

    ) {
        super(index, name, description, type, stateID, hash);
    }

    /**
     * Get difference curve data.
     * @return a WKms at given index.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        WINFOArtifact winfo = (WINFOArtifact)artifact;

        CalculationResult res = (CalculationResult)
            winfo.compute(context, hash, stateId, type, false);

        WKms [] wkms = (WKms [])res.getData();

        if (wkms.length > 0) {
            WKms result = wkms[index];
            log.debug("Got difference curve data (" + result.getName()
                + ") at index: " + index);
            return result;
        } else {
            log.debug("Empty difference facet.");
            return new WKmsImpl();
        }
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        WaterlevelFacet copy = new DifferenceCurveFacet();
        copy.set(this);
        copy.type    = type;
        copy.stateId = stateId;
        copy.hash    = hash;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
