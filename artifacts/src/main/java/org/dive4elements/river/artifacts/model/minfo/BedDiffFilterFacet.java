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

import org.dive4elements.river.utils.Formatter;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.math.MovingAverage;
import org.dive4elements.river.artifacts.model.ZoomScale;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


public class BedDiffFilterFacet
extends DataFacet
{
    private static Logger log = LogManager.getLogger(BedDiffFilterFacet.class);

    public BedDiffFilterFacet() {
    }

    public BedDiffFilterFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.beddifference.yaxis.label.diff");
    }

    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for bed density at index: " + index);

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult) flys.compute(context, hash,
            stateId, type, false);

        BedDiffYearResult[] data =
            (BedDiffYearResult[]) res.getData(); // TODO CAST TO SPECIFIC CLASS
        Double start = (Double)context.getContextValue("startkm");
        Double end = (Double)context.getContextValue("endkm");
        if(start != null && end != null) {
            RiverContext fc = (RiverContext)context.globalContext();
            // Adaptive smoothing, based on zoom factor/diagram extents.
            ZoomScale scales = (ZoomScale)fc.get("zoomscale");
            RiverAccess access = new RiverAccess((D4EArtifact)artifact);
            String river = access.getRiverName();

            double radius = scales.getRadius(river, start, end);
            BedDiffYearResult oldData = data[index];
            double[][] diffs = MovingAverage.weighted(
                oldData.getDifferencesData(), radius);
            this.addMetaData(
                Resources.getMsg(context.getMeta(),
                    "chart.subtitle.radius"),
                Formatter.getRawFormatter(context).format(radius) + " km");
            return diffs;
        }
        return null;
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        BedDiffFilterFacet copy = new BedDiffFilterFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
