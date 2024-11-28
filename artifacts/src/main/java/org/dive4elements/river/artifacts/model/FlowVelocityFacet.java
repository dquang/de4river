/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Facet of a FlowVelocity curve.
 */
public class FlowVelocityFacet extends DataFacet {

    private static Logger log = LogManager.getLogger(FlowVelocityFacet.class);

    public FlowVelocityFacet() {
        // required for clone operation deepCopy()
    }


    public FlowVelocityFacet(
        int         idx,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash
    ) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.flow_velocity.section.yaxis.label");
    }


    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for flow velocity at index: " + index);

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult)
            flys.compute(context, hash, stateId, type, false);

        FlowVelocityData[] data = (FlowVelocityData[]) res.getData();

        return data[index];
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        FlowVelocityFacet copy = new FlowVelocityFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
