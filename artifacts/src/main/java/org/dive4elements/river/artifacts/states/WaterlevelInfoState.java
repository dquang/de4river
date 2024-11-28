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
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;
import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.CalculationResult;


public class WaterlevelInfoState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(WaterlevelInfoState.class);


    @Override
    protected String getUIProvider() {
        return "noinput";
    }


    @Override
    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets
    ) {
        return compute((WINFOArtifact) artifact, hash, facets, null);
    }


    protected Object compute(
        WINFOArtifact winfo,
        String        hash,
        List<Facet>   facets,
        Object        old
    ) {
        log.debug("WaterlevelInfoState.compute");
        String id = getID();

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : winfo.getWaterlevelData();

        if (facets == null) {
            return res;
        }

        WQKms [] wqkms = (WQKms [])res.getData();

        for (int i = 0; i < wqkms.length; i++) {
            String nameW = null;
            String nameQ = null;

            if (winfo.isQ()) {
                nameQ = wqkms[i].getName();
                nameW = "W(" + nameQ + ")";
            }
            else {
                nameW = wqkms[i].getName();
                nameQ = "Q(" + nameQ + ")";
            }

            log.debug("WaterlevelInfoState Create facet: " + nameW);
            log.debug("WaterlevelInfoState Create facet: " + nameQ);

            Facet w = new WaterlevelFacet(
                i, LONGITUDINAL_W, nameW, ComputeType.ADVANCE, id, hash);
            Facet q = new WaterlevelFacet(
                i, LONGITUDINAL_Q, nameQ, ComputeType.ADVANCE, id, hash);

            facets.add(w);
            facets.add(q);
        }

        if (wqkms.length > 0) {
            Facet wst = new DataFacet(
                WST, "WST data", ComputeType.ADVANCE, hash, id);
            Facet csv = new DataFacet(
                CSV, "CSV data", ComputeType.ADVANCE, hash, id);

            facets.add(wst);
            facets.add(csv);
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
        }

        // TODO Adjust to WaterlevelState - implementation.
        facets.add(new CrossSectionWaterLineFacet(0,
                "Q=" + winfo.getDataAsString("wq_single")));

        // Assume to be in wq_single mode.
        return res;
    }


    /**
     * @param context Ignored.
     */
    @Override
    public Object computeFeed(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((WINFOArtifact) artifact, hash, facets, old);
    }


    /**
     * @param context Ignored.
     */
    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return compute((WINFOArtifact) artifact, hash, facets, old);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
