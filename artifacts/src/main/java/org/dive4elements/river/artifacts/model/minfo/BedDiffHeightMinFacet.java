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

import org.dive4elements.river.model.BedHeight;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


public class BedDiffHeightMinFacet
extends DataFacet
implements FacetTypes
{
    private static Logger log = LogManager.getLogger(BedDiffHeightMinFacet.class);

    public BedDiffHeightMinFacet() {
    }

    public BedDiffHeightMinFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedheight_middle.section.yaxis.label");
    }

    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for bed density at index: " + index);

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(context, hash,
            stateId, type, false);

        BedDiffYearResult[] resultData =
            (BedDiffYearResult[]) res.getData(); // TODO CAST TO SPECIFIC CLASS

        if (resultData != null && resultData.length > index) {
            BedDiffYearResult data = resultData[index];

            BedHeight first = BedHeight.getBedHeightById(
                data.getIdFirst());
            this.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.cur.elevation"),
                first.getCurElevationModel().getName());
            if (first.getOldElevationModel() != null) {
                this.addMetaData(Resources.getMsg(
                        context.getMeta(),
                        "meta.bedheight.old.elevation"),
                    first.getOldElevationModel().getName());
            }
            this.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.river.elevation"),
                new RiverAccess(flys).getRiver().getWstUnit().getName());

            return data.getHeights1Data();
        }
        return null;
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        BedDiffHeightMinFacet copy = new BedDiffHeightMinFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
