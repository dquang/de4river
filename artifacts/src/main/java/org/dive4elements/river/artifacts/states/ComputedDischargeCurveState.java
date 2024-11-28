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

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.ChartArtifact;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.CalculationResult;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;

/**
 * The final state that will be reached after the discharge curve calculation
 * mode has been chosen.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ComputedDischargeCurveState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log =
        LogManager.getLogger(ComputedDischargeCurveState.class);


    public ComputedDischargeCurveState() {
    }


    /**
     * Get computed discharge curve data from cache (if available) or
     * compute anew. Create Waterlevel and DataFacets.
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("ComputedDischargeCurveState.computeAdvance");
        if(artifact instanceof WINFOArtifact) {
            WINFOArtifact winfo = (WINFOArtifact)artifact;

            CalculationResult res = old instanceof CalculationResult
                ? (CalculationResult)old
                : winfo.getComputedDischargeCurveData();

            WQKms [] wqkms = (WQKms [])res.getData();

            if (facets != null && wqkms.length > 0) {
                for (int i = 0; i < wqkms.length; ++i) {

                    Object[] args = new Object[] {
                        RiverUtils.getRiver(winfo).getName(),
                        // Parse Double to allow i18n.
                        Double.parseDouble(wqkms[i].getName())
                    };

                    String name = Resources.getMsg(
                        context.getMeta(),
                        "chart.computed.discharge.curve.curve.label",
                        args);

                    facets.add(
                        new WaterlevelFacet(i, COMPUTED_DISCHARGE_Q, name));
                    facets.add(new WaterlevelFacet(i, AT, "AT data"));
                }

                facets.add(new DataFacet(CSV, "CSV data"));
                facets.add(new DataFacet(PDF, "PDF data"));

                if (res.getReport().hasProblems()) {
                    facets.add(new ReportFacet());
                }
            }

            return res;
        }
        else if(artifact instanceof ChartArtifact) {
            ChartArtifact chart = (ChartArtifact)artifact;
            facets.add(new EmptyFacet());
            return null;
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
