/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedQualityAccess;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

public class BedDiameterDataFacet
extends DataFacet
{
    private static final Logger log = LogManager.getLogger(
        BedDiameterDataFacet.class);

    public BedDiameterDataFacet() {
    }

    public BedDiameterDataFacet(
        int ndx,
        String name,
        String description,
        ComputeType type,
        String stateId,
        String hash
    ) {
        super(ndx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedquality.yaxis.label.diameter");
    }

    public Object getData(Artifact artifact, CallContext context) {
        D4EArtifact d4e = (D4EArtifact) artifact;
        BedQualityAccess access = new BedQualityAccess(d4e, context);
        int ndx = getIndex() & 7;
        int top = (getIndex() >> 3) & 1;
        int diam = (getIndex() >> 4);
        String diameter = "";
        switch (diam) {
            case 1: diameter = "d10"; break;
            case 2: diameter = "d16"; break;
            case 3: diameter = "d20"; break;
            case 4: diameter = "d25"; break;
            case 5: diameter = "d30"; break;
            case 6: diameter = "d40"; break;
            case 7: diameter = "d50"; break;
            case 8: diameter = "d60"; break;
            case 9: diameter = "d70"; break;
            case 10: diameter = "d75"; break;
            case 11: diameter = "d80"; break;
            case 12: diameter = "d84"; break;
            case 13: diameter = "d90"; break;
            case 14: diameter = "dmin"; break;
            case 15: diameter = "dmax"; break;
            case 16: diameter = "dm"; break;
            default: return null;
        }

        QualityMeasurements measurements =
            QualityMeasurementFactory.getBedMeasurements(
                access.getRiverName(),
                access.getFrom(),
                access.getTo(),
                access.getDateRanges().get(ndx).getFrom(),
                access.getDateRanges().get(ndx).getTo());
        TDoubleArrayList kms = new TDoubleArrayList();
        TDoubleArrayList data = new TDoubleArrayList();
        for (QualityMeasurement m : measurements.getMeasurements()) {
            if (top == 1 && m.getDepth1() == 0d && m.getDepth2() <= 0.3) {
                kms.add(m.getKm());
                data.add(m.getDiameter(diameter) * 1000);
            }
            else if (top == 0 && m.getDepth1() > 0d && m.getDepth2() <= 0.5){
                kms.add(m.getKm());
                data.add(m.getDiameter(diameter) * 1000);
            }
        }
        BedDiameterData bdd = new BedDiameterData(diameter, kms, data);
        return bdd.getDiameterData();
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        BedDiameterDataFacet copy = new BedDiameterDataFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
