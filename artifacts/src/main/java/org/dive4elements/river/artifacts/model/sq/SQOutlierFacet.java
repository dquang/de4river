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

/**
 * Facet to show the outliers in a sq relation.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SQOutlierFacet extends DataFacet implements FacetTypes {

    private static final Logger log = LogManager.getLogger(SQOutlierFacet.class);

    public static final int BITMASK_ITERATION = (1 << 16) - 1;

    private int fractionIdx;

    public SQOutlierFacet() {
    }

    public SQOutlierFacet(
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
        log.debug("SQOutlierFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            return null;
        }

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(
            context, ComputeType.ADVANCE, false);

        int idx  = this.index >> 16;
        int iter = this.index & BITMASK_ITERATION;

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Fetch data for index : " + this.index);
            log.debug("  > index:       " + idx);
            log.debug("  > fraction:    " + fractionIdx);
            log.debug("  > iteration:   " + iter);
        }

        SQResult[]       result  = (SQResult[]) res.getData();
        SQFractionResult fResult = result[idx].getFraction(fractionIdx);

        if (fResult == null) {
            log.warn("No SQFractionResult at " + idx + "|" + fractionIdx);
            return null;
        }

        SQ [] outliers = fResult.getOutliers(iter);

        if (debug) {
            int num = outliers != null ? outliers.length : 0;
            log.debug("Found " + num + " outliers for iteration " + iter);
        }

        return outliers;
    }


    @Override
    public Facet deepCopy() {
        SQOutlierFacet copy = new SQOutlierFacet();
        copy.set(this);
        copy.type    = type;
        copy.hash    = hash;
        copy.stateId = stateId;
        copy.fractionIdx = fractionIdx;

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
