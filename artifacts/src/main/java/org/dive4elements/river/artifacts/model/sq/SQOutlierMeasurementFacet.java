/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SQOutlierMeasurementFacet
extends      DataFacet
implements   FacetTypes
{
    private static final Logger log =
        LogManager.getLogger(SQOutlierMeasurementFacet.class);

    private int fractionIdx;

    public static final int BITMASK_ITERATION = (1 << 16) - 1;

    public SQOutlierMeasurementFacet() {
    }

    public SQOutlierMeasurementFacet(
        int    idx,
        int    fractionIdx,
        String name,
        String description,
        String hash,
        String stateId
    ) {
        super(idx, name, description, ComputeType.ADVANCE, hash, stateId);
        this.fractionIdx = fractionIdx;
        this.metaData.put("X", "chart.sq_relation.xaxis.label");
        this.metaData.put("Y", "chart.sq_relation.yaxis.label");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("SQOutlierMeasurementFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            return null;
        }

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(
            context, ComputeType.ADVANCE, false);

        int idx  = this.index >> 16;
        int iter = this.index & BITMASK_ITERATION;

        SQResult[]       result  = (SQResult[]) res.getData();
        SQFractionResult fResult = result[idx].getFraction(fractionIdx);

        return fResult.getMeasurements(iter);
    }

    @Override
    public Facet deepCopy() {
        SQOutlierMeasurementFacet copy = new SQOutlierMeasurementFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        copy.fractionIdx = fractionIdx;

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
