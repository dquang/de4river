/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.text.NumberFormat;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess.EvaluationMode;

import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess;

import org.dive4elements.river.artifacts.model.Calculation6;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.HistoricalDischargeData;
import org.dive4elements.river.artifacts.model.HistoricalDischargeDifferenceFacet;
import org.dive4elements.river.artifacts.model.HistoricalDischargeFacet;
import org.dive4elements.river.artifacts.model.HistoricalDischargeWQFacet;
import org.dive4elements.river.artifacts.model.HistoricalWQTimerange;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WQTimerange;

import org.dive4elements.river.artifacts.resources.Resources;

import org.w3c.dom.Element;

/**
 * State to calculate historical discharge curves.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeComputeState
extends     DefaultState
implements  FacetTypes
{
    private static final Logger log = LogManager
        .getLogger(HistoricalDischargeComputeState.class);

    public static final String DEFAULT_UNIT = "cm";

    @Override
    protected void appendItems(Artifact artifact, ElementCreator creator,
        String name, CallContext context, Element select) {
        // TODO IMPLEMENT ME
    }

    @Override
    public Object computeAdvance(D4EArtifact artifact, String hash,
        CallContext context, List<Facet> facets, Object old) {
        log.debug("HistoricalDischargeComputeState.computeAdvance");

        HistoricalDischargeAccess access =
            new HistoricalDischargeAccess(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : new Calculation6(access).calculate();

        if (facets == null) {
            return res;
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
        }

        HistoricalDischargeData data = (HistoricalDischargeData) res.getData();

        WQTimerange[] wqts = (WQTimerange[]) data.getWQTimeranges();
        if (wqts != null && wqts.length > 0) {
            facets.add(new DataFacet(CSV, "CSV data", ComputeType.ADVANCE,
                hash, id));

            facets.add(new DataFacet(PDF, "PDF data", ComputeType.ADVANCE,
                hash, id));

            prepareFacets(facets, wqts, access);
        }

        prepareWQFacets(context, facets, access, hash);

        return res;
    }

    protected void prepareFacets(
        List<Facet>               facets,
        WQTimerange[]             wqts,
        HistoricalDischargeAccess access
    ) {
        int i = 0;

        for (WQTimerange wqt : wqts) {
            log.debug("Prepare facet for: " + wqt.getName());

            EvaluationMode evalMode = access.getEvaluationMode();
            if (evalMode == EvaluationMode.W) {
                facets.add(new HistoricalDischargeFacet(i,
                    HISTORICAL_DISCHARGE_Q, createFacetTitle(wqt)));

                if (wqt instanceof HistoricalWQTimerange) {
                    log.debug(
                        "Create another facet for historical differences.");

                    // TODO CREATE BETTER TITLE FOR FACETS (issue1180)
                    facets.add(new HistoricalDischargeDifferenceFacet(i,
                            HISTORICAL_DISCHARGE_Q_DIFF,
                            "DIFF: " + wqt.getName()));
                }
            }
            else {
                facets.add(new HistoricalDischargeFacet(i,
                    HISTORICAL_DISCHARGE_W, createFacetTitle(wqt)));

                if (wqt instanceof HistoricalWQTimerange) {
                    log.debug(
                        "Create another facet for historical differences.");

                    // TODO CREATE BETTER TITLE FOR FACETS
                    facets.add(new HistoricalDischargeDifferenceFacet(i,
                            HISTORICAL_DISCHARGE_W_DIFF,
                            "DIFF: " + wqt.getName()));
                }
            }

            i++;
        }
    }

    protected void prepareWQFacets(CallContext cc, List<Facet> facets,
        HistoricalDischargeAccess access, String hash) {
        double[] ws = access.getWs();
        double[] qs = access.getQs();

        NumberFormat format = NumberFormat.getInstance(
                        Resources.getLocale(cc.getMeta()));
        for (int k = 0; k < ws.length; k++) {
            facets.add(new HistoricalDischargeWQFacet(k,
                    HISTORICAL_DISCHARGE_WQ_W,
                    "W=" + format.format(ws[k]),
                    ComputeType.ADVANCE,
                    hash,
                    getID(),
                    ws[k]));
        }

        for (int k = 0; k < qs.length; k++) {
            facets.add(new HistoricalDischargeWQFacet(k,
                    HISTORICAL_DISCHARGE_WQ_Q,
                    "Q=" + format.format(qs[k]),
                    ComputeType.ADVANCE,
                    hash,
                    getID(),
                    qs[k]));
        }
    }

    /** Create string for facets name/description. */
    protected String createFacetTitle(WQTimerange wqt) {
        String name = wqt.getName();

        return name != null && name.indexOf("W") >= 0 ? createFacetTitleW(wqt)
            : createFacetTitleQ(wqt);
    }

    protected String createFacetTitleW(WQTimerange wqt) {
        String name = wqt.getName();
        return name + " " + DEFAULT_UNIT;
    }

    protected String createFacetTitleQ(WQTimerange wqt) {
        return wqt.getName();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
