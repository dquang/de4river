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
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.math.MovingAverage;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


public class DifferenceCurveFilterFacet
extends DifferenceCurveFacet
{
    public DifferenceCurveFilterFacet() {
    }

    public DifferenceCurveFilterFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateID,
        String      hash

    ) {
        super(index, name, description, type, stateID, hash);
    }

    /**
     * Get difference curve data.
     * @return a WKms at given index.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        WKms result = (WKms)super.getData(artifact, context);

        Double start = (Double)context.getContextValue("startkm");
        Double end = (Double)context.getContextValue("endkm");
        if(start != null && end != null) {
            RiverContext fc = (RiverContext)context.globalContext();
            // Adaptive smoothing, based on zoom factor/diagram extents.
            ZoomScale scales = (ZoomScale)fc.get("zoomscale");
            RiverAccess access = new RiverAccess((D4EArtifact)artifact);
            String river = access.getRiverName();

            double radius = scales.getRadius(river, start, end);

            double[][] oldData = new double[2][result.size()];
            for (int i = 0; i < result.size(); i++) {
                oldData[0][i] = result.getKm(i);
                oldData[1][i] = result.getW(i);
            }

            double[][] diffs = MovingAverage.weighted(oldData, radius);
            WKmsImpl newData = new WKmsImpl();
            for(int j = 0; j < diffs[0].length; j++) {
                newData.add(diffs[0][j], diffs[1][j]);
            }
            return newData;
        }
        return result;
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        WaterlevelFacet copy = new DifferenceCurveFilterFacet();
        copy.set(this);
        copy.type    = type;
        copy.stateId = stateId;
        copy.hash    = hash;
        return copy;
    }
}
