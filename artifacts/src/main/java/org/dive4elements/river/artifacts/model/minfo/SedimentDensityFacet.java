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

import org.dive4elements.river.artifacts.model.DataFacet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Facet to access sediment density values measured in one year. */
public class SedimentDensityFacet
extends DataFacet
{
    /** Very own log. */
    private static Logger log = LogManager.getLogger(SedimentDensityFacet.class);

    /** Used as tolerance value when fetching measurement stations. */
    private static double EPSILON = 1e-5;


    public SedimentDensityFacet() {
    }

    public SedimentDensityFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.yaxis.label.sedimentdensity");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for sediment density at index: " + index);

        D4EArtifact flys = (D4EArtifact) artifact;

        SedimentDensity res = (SedimentDensity) flys.compute(context, hash,
            stateId, type, false);

        if (res == null) {
            log.error("No SedimentDensity");
        }

        return res;
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        SedimentDensityFacet copy = new SedimentDensityFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
