/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.FlowVelocityMeasurementArtifact;
import org.dive4elements.river.artifacts.model.BlackboardDataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;


/** Facet to show measured flow velocity. */
public class FlowVelocityMeasurementFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    public FlowVelocityMeasurementFacet(String description) {
        this(FLOW_VELOCITY_MEASUREMENT, description);
    }


    public FlowVelocityMeasurementFacet(String name, String description) {
        this.name = name;
        this.description = description;
        this.index = 0;
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.flow_velocity.section.yaxis.label");
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        FlowVelocityMeasurementArtifact staticData =
            (FlowVelocityMeasurementArtifact) artifact;
        return staticData.getFlowVelocityMeasurementValue();
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FlowVelocityMeasurementFacet deepCopy() {
        FlowVelocityMeasurementFacet copy =
            new FlowVelocityMeasurementFacet(description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
