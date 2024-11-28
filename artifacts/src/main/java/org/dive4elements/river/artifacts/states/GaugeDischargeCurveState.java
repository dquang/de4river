/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;
import java.util.Date;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.GaugeDischargeCurveFacet;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.model.TimeInterval;

public class GaugeDischargeCurveState
extends DefaultState
{

    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String hash,
        CallContext context,
        List<Facet> facets,
        Object old
    ) {
        String gaugeName = artifact.getDataAsString("gauge_name");
        TimeInterval validity = new RiverAccess(artifact).getRiver()
            .determineGaugeByName(gaugeName).fetchMasterDischargeTable()
            .getTimeInterval();
        Date stopTime = validity.getStopTime();
        String description = Resources.getMsg(
            context.getMeta(),
            "chart.discharge.curve.model"
            + (stopTime != null ? "" : ".nostop"),
            new Object[] {gaugeName,
                          validity.getStartTime(),
                          stopTime
                }
        );

        facets.add(new GaugeDischargeCurveFacet("gauge_discharge_curve",
                description));
        facets.add(new GaugeDischargeCurveFacet("at",
                "gauge_discharge_curve"));
        facets.add(new GaugeDischargeCurveFacet("csv",
                "gauge_discharge_curve"));
        facets.add(new GaugeDischargeCurveFacet("pdf",
                "gauge_discharge_curve"));
        return null;
    }
}
