/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.extreme;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.ExtremeAccess;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;
import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.artifacts.model.extreme.ExtremeCurveFacet;
import org.dive4elements.river.artifacts.model.extreme.ExtremeCalculation;
import org.dive4elements.river.artifacts.model.extreme.ExtremeResult;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** State in which to deliver extreme value analysis result. */
public class ExtremeCompute
extends      DefaultState
implements   FacetTypes
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(ExtremeCompute.class);


    public ExtremeCompute() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("ExtremeCompute.computeAdvance");

        CalculationResult res;

        ExtremeAccess access = new ExtremeAccess(artifact);

        if (old instanceof CalculationResult) {
            res = (CalculationResult)old;
        }
        else {
            ExtremeCalculation calc = new ExtremeCalculation(access);
            res = calc.calculate();
        }

        if (facets == null) {
            return res;
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet());
        }

        ExtremeResult eres = (ExtremeResult) res.getData();
        WQKms [] wqkms = (WQKms []) eres.getWQKms();

        if (wqkms == null) {
            log.error("No computation result!");
            return res;
        }

        for (int i = 0; i < wqkms.length; i++) {
            String name = wqkms[i].getName();
            // The name already contains "W(...)".
            String qname = name.replace("W(","Q=");
            qname = qname.substring(0,qname.length()-1);

            Facet w = new WaterlevelFacet(
                i, LONGITUDINAL_W, name, ComputeType.ADVANCE, id, hash);
            Facet q = new WaterlevelFacet(
                i, LONGITUDINAL_Q, qname, ComputeType.ADVANCE, id, hash);
            Facet csFacet = new CrossSectionWaterLineFacet(i, name);

            facets.add(w);
            facets.add(q);
            facets.add(csFacet);
        }


        facets.add(
            new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));
        facets.add(
            new DataFacet(PDF, "PDF data", ComputeType.ADVANCE, hash, id));
        facets.add(
            new DataFacet(WST, "WST data", ComputeType.ADVANCE, hash, id));

        facets.add(new ExtremeCurveFacet(Resources.getMsg(context.getMeta(),
                    "extreme_wq_curve", "extreme_wq_curve")));
        facets.add(new ExtremeCurveFacet(Resources.getMsg(context.getMeta(),
                    "extreme_wq_base_curve", "extreme_wq_base_curve"),
                true));

        return res;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
