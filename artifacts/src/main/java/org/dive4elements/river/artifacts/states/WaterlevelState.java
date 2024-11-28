/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.ChartArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.OfficialLineFinder;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.RiverUtils;

/** State in which a waterlevel has been calculated. */
public class WaterlevelState
extends      DefaultState
implements   FacetTypes
{
    /** The log that is used in this state. */
    private static Logger log = LogManager.getLogger(WaterlevelState.class);


    /**
     * From this state can only be continued trivially.
     */
    @Override
    protected String getUIProvider() {
        return "continue";
    }


    /**
     * Compute result or returned object from cache, create facets.
     * @param old Object that was cached.
     */
    protected Object compute(
        WINFOArtifact winfo,
        CallContext   cc,
        String        hash,
        List<Facet>   facets,
        Object        old
    ) {
        String id = getID();

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult) old
            : winfo.getWaterlevelData();

        if (facets == null) {
            return res;
        }

        boolean debug = log.isDebugEnabled();

        WQKms [] wqkms = (WQKms []) res.getData();

        for (int i = 0; i < wqkms.length; i++) {
            String name = wqkms[i].getName();

            String nameW = RiverUtils.createWspWTitle(winfo, cc, name);
            String nameQ = RiverUtils.createWspQTitle(winfo, cc, name);

            // Hotfix for theme names. Themes with the same name cause problems
            // aggregating chart legend items.
            if (i > 0 && name.equals(wqkms[i - 1].getName())) {
                nameW += "; Q=" + wqkms[i].get(0, new double[3])[1];
                nameQ += " = " + wqkms[i].get(0, new double[3])[1];
            }

            if (debug) {
                log.debug("Create facet: " + nameW);
                log.debug("Create facet: " + nameQ);
            }

            Facet w = new WaterlevelFacet(
                i, LONGITUDINAL_W, nameW, ComputeType.ADVANCE, id, hash);
            Facet q = new WaterlevelFacet(
                i, LONGITUDINAL_Q, nameQ, ComputeType.ADVANCE, id, hash);

            facets.add(new CrossSectionWaterLineFacet(i, nameW));

            facets.add(w);
            facets.add(q);
        }

        if (wqkms.length > 0) {
            Facet wst = new DataFacet(
                WST, "WST data", ComputeType.ADVANCE, hash, id);
            Facet csv = new DataFacet(
                CSV, "CSV data", ComputeType.ADVANCE, hash, id);
            Facet pdf = new DataFacet(
                PDF, "PDF data", ComputeType.ADVANCE, hash, id);

            facets.add(wst);
            facets.add(csv);
            facets.add(pdf);
        }

        Calculation report = res.getReport();

        List<OfficialLineFinder.ValueRange> ols =
            OfficialLineFinder.findOfficialLines(winfo);

        if (!ols.isEmpty()) {
            for (OfficialLineFinder.ValueRange ol: ols) {
                report.addProblem(Resources.format(
                    cc.getMeta(),
                    "official.line.found",
                    "Found official line for {0} from year {1,date,yyyy} "
                    + "from {2}.",
                    ol.getName(), nn(ol.getDate()), nn(ol.getSource())));
            }
        }

        if (report.hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
        }

        return res;
    }

    /** Returns empty String if argument is null, argument itself otherwise. */
    private static final String nn(String s) {
        return s != null ? s : "";
    }

    private static final Date nn(Date d) {
        return d != null ? d : new Date();
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
        if (artifact instanceof ChartArtifact) {
            facets.add(new EmptyFacet());
            return null;
        }
        return compute((WINFOArtifact) artifact, context, hash, facets, old);
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
        if (artifact instanceof ChartArtifact) {
            facets.add(new EmptyFacet());
            return null;
        }
        return compute((WINFOArtifact) artifact, context, hash, facets, old);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
