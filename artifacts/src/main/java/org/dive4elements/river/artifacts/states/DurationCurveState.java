/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.ChartArtifact;

import org.dive4elements.river.artifacts.model.DurationCurveFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQDay;

import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.CalculationResult;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;


/**
 * The final state that will be reached after the duration curve calculation
 * mode has been chosen.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DurationCurveState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(DurationCurveState.class);

    public DurationCurveState() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        if (artifact instanceof WINFOArtifact) {
            WINFOArtifact winfo = (WINFOArtifact)artifact;

            CalculationResult res;

            if (old instanceof CalculationResult) {
                res = (CalculationResult)old;
            }
            else {
                res = winfo.getDurationCurveData();
            }

            WQDay wqday = (WQDay)res.getData();

            if (wqday != null && facets != null) {
                RangeAccess rangeAccess = new RangeAccess(winfo);
                // Create an i18ed name for a (w or q) duration curve facet.
                Object[] args = new Object[] {
                    RiverUtils.getRiver(winfo).getName(),
                    rangeAccess.getLocations()[0]
                };

                String nameW = Resources.getMsg(
                    context.getMeta(),
                    "chart.duration.curve.curve.w",
                    "",
                    args);

                String nameQ = Resources.getMsg(
                    context.getMeta(),
                    "chart.duration.curve.curve.q",
                    "",
                    args);

                Facet w = new DurationCurveFacet(DURATION_W, nameW);
                Facet q = new DurationCurveFacet(DURATION_Q, nameQ);

                facets.add(w);
                facets.add(q);

                facets.add(new DataFacet(CSV, "CSV data"));
                facets.add(new DataFacet(PDF, "PDF data"));

                if (res.getReport().hasProblems()) {
                    facets.add(new ReportFacet());
                }
            }

            return res;
        }
        else if (artifact instanceof ChartArtifact) {
            ChartArtifact chart = (ChartArtifact)artifact;
            facets.add(new EmptyFacet());
            return null;
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
