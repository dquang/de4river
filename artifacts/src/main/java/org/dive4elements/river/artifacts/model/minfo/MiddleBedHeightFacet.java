/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Facet of a MiddleBedHeight curve.
 */
public class MiddleBedHeightFacet extends DataFacet {

    private static Logger log = LogManager.getLogger(MiddleBedHeightFacet.class);


    public MiddleBedHeightFacet() {
        // required for clone operation deepCopy()
    }


    public MiddleBedHeightFacet(
        int         idx,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash
    ) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedheight_middle.section.yaxis.label");
    }


    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for middle bed height at index: " + index);

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult)
            flys.compute(context, hash, stateId, type, false);

        MiddleBedHeightData[] resultData =
            (MiddleBedHeightData[]) res.getData();
        MiddleBedHeightData data = resultData[index];

        return data.getMiddleHeightsPoints();
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        MiddleBedHeightFacet copy = new MiddleBedHeightFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
