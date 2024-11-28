/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReferenceCurveFacet;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WWQQ;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** State of WINFO in which reference curves can be produced. */
public class ReferenceCurveState
extends      DefaultState
implements   FacetTypes
{
    private static Logger log = LogManager.getLogger(ReferenceCurveState.class);


    public ReferenceCurveState() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        if (!(artifact instanceof WINFOArtifact)) {
            return null;
        }

        String id = getID();

        WINFOArtifact winfo = (WINFOArtifact)artifact;

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : winfo.getReferenceCurveData(context);

        if (facets == null) {
            return res;
        }

        WWQQ [] wws = (WWQQ [])res.getData();

        for (int i = 0; i < wws.length; ++i) {
            String wwsName = wws[i].getName();
            facets.add(new ReferenceCurveFacet(i,
                REFERENCE_CURVE,
                wwsName
                ));
            facets.add(new ReferenceCurveFacet(i,
                REFERENCE_CURVE_NORMALIZED,
                wwsName
                ));
        }

        if (wws.length > 0) {
            log.debug("Adding CSV and PDF data facet.");
            Facet csv = new DataFacet(
                CSV, "CSV data", ComputeType.ADVANCE, hash, id);
            Facet pdf = new DataFacet(
                PDF, "PDF data", ComputeType.ADVANCE, hash, id);
            facets.add(csv);
            facets.add(pdf);
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
        }

        return res;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
