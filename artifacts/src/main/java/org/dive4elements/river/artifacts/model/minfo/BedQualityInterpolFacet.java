/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.artifacts.access.RangeAccess;


/**
 * Facet for serving interpolated BedQualityResults
 */
public class BedQualityInterpolFacet extends DataFacet {

    private static final long serialVersionUID = 1L;

    private static Logger log = LogManager.getLogger(
        BedQualityInterpolFacet.class);

    private String valueName; /* Name of ResultValue underlying this facet */
    private String valueType; /* Type of ResultValue underlying this facet */

    public BedQualityInterpolFacet() {
        // required for clone operation deepCopy()
    }

    public BedQualityInterpolFacet(
        int idx,
        String name,
        String description,
        ComputeType type,
        String stateId,
        String hash,
        String valueName,
        String valueType
    ) {
        super(idx, name, description, type, hash, stateId);
        this.valueName = valueName;
        this.valueType = valueType;
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        if (!valueName.equals("porosity") && !valueName.equals("density")) {
            this.metaData.put("Y", "chart.bedquality.yaxis.label.diameter");
        } else {
            this.metaData.put(
                "Y", "chart.bedquality.yaxis.label." + valueName);
        }
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get bedquality data: " + valueName + " - " + valueType);

        D4EArtifact flys = (D4EArtifact) artifact;
        double[] kms = new RangeAccess(flys).getKmSteps();

        CalculationResult res = (CalculationResult) flys.compute(context, hash,
            stateId, type, false);

        int ndx = index >> 8;

        BedQualityResultValue value =
            ((BedQualityResult[]) res.getData())[ndx].getValue(
                valueName, valueType);

        if (value == null) {
            /* Other facets check this so we do too */
            return null;
        }
        return value.getDataInterpolated(kms);
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        BedQualityInterpolFacet copy = new BedQualityInterpolFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        copy.valueName = valueName;
        copy.valueType = valueType;
        return copy;
    }
}
