/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.ChartArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.Calculation4Access;

import org.dive4elements.river.artifacts.model.Calculation4;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.ConstantWQKms;
import org.dive4elements.river.artifacts.model.CrossSectionWaterLineFacet;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.EmptyFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WaterlevelFacet;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DischargeLongitudinalSection
extends      DefaultState
implements   FacetTypes
{
    private static Logger log =
        LogManager.getLogger(DischargeLongitudinalSection.class);

    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "winfo",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    String name = facet.getName();
                    if (name.equals(DISCHARGE_LONGITUDINAL_Q_INFOLD_CUT)) {
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
            });
    }

    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        if (artifact instanceof ChartArtifact) {
            ChartArtifact chart = (ChartArtifact)artifact;
            facets.add(new EmptyFacet());
            return null;
        }

        Calculation4Access access = new Calculation4Access(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : new Calculation4(access).calculate();

        if (facets == null) {
            return res;
        }

        WQKms [] wqkms = (WQKms [])res.getData();

        for (int i = 0; i < wqkms.length; i++) {
            String nameW = null;
            String nameQ = null;

            if (access.isQ()) {
                nameQ = wqkms[i].getName();
                nameW = "W(" + nameQ + ")";
            }
            else {
                nameW = wqkms[i].getName();
                nameQ = "Q(" + nameW + ")";
            }

            // Do not generate Waterlevel/Waterline facets
            // for Q only curves.
            if (!(wqkms[i] instanceof ConstantWQKms)) {

                Facet w = new WaterlevelFacet(
                    i, DISCHARGE_LONGITUDINAL_W, nameW);

                Facet s = new CrossSectionWaterLineFacet(i, nameW);

                Facet q = new WaterlevelFacet(
                    i, DISCHARGE_LONGITUDINAL_Q, nameQ);
                facets.add(s);
                facets.add(w);
                facets.add(q);
            }
            else {
                Facet q;
                if (nameQ.contains("geschnitten")) {
                    q = new WaterlevelFacet(
                        i, DISCHARGE_LONGITUDINAL_Q_INFOLD_CUT, nameQ);
                }
                else {
                    q = new WaterlevelFacet(
                        i, DISCHARGE_LONGITUDINAL_Q_INFOLD, nameQ);
                }
                facets.add(q);
            }

            if (wqkms[i] instanceof WQCKms) {
                // TODO DO i18n

                String nameC = nameW.replace(
                    "benutzerdefiniert",
                    "benutzerdefiniert [korrigiert]");

                Facet c = new WaterlevelFacet(
                    i, DISCHARGE_LONGITUDINAL_C, nameC);

                // Here, avoid index clash with Facet "s" above and
                // signal the WINFO later that we want to access Cs.
                Facet r = new CrossSectionWaterLineFacet(i + 1, nameC);

                facets.add(c);
                facets.add(r);
            }
        }

        if (wqkms.length > 0) {
            facets.add(new DataFacet(CSV, "CSV data"));
            facets.add(new DataFacet(WST, "WST data"));
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet());
        }

        return res;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
