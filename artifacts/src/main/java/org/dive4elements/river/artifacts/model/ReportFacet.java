/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.artifacts.D4EArtifact;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ReportFacet
extends      DefaultFacet
implements   FacetTypes
{
    private static Logger log = LogManager.getLogger(ReportFacet.class);

    protected ComputeType type;
    protected String      hash;
    protected String      stateId;

    public ReportFacet() {
        this(ComputeType.ADVANCE);
    }

    public ReportFacet(ComputeType type) {
        super(0, REPORT, "report");
        this.type = type;
    }


    public ReportFacet(ComputeType type, String hash, String stateId) {
        super(0, REPORT, "report");
        this.type    = type;
        this.hash    = hash;
        this.stateId = stateId;
    }

    public Object getData(Artifact artifact, CallContext context) {
        log.debug("get report data");

        D4EArtifact flys = (D4EArtifact)artifact;

        CalculationResult cr = (CalculationResult)flys.compute(
            context, hash, stateId, type, false);

        return cr.getReport();
    }

    @Override
    public Facet deepCopy() {
        ReportFacet copy = new ReportFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :