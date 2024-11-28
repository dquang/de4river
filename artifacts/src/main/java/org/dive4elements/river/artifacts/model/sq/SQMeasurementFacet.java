/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Facet to show the measured values in a sq relation.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SQMeasurementFacet extends DataFacet implements FacetTypes {

    private static final Logger log = LogManager.getLogger(
        SQMeasurementFacet.class);


    private int fractionIdx;


    public SQMeasurementFacet() {
    }


    public SQMeasurementFacet(
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
        log.debug("SQMeasurementFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            return null;
        }

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(
            context, ComputeType.ADVANCE, false);

        SQResult[]       result  = (SQResult[]) res.getData();
        SQFractionResult fResult = result[index].getFraction(fractionIdx);

        return fResult.getMeasurements();
    }


    @Override
    public Facet deepCopy() {
        SQMeasurementFacet copy = new SQMeasurementFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        copy.fractionIdx = fractionIdx;

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
