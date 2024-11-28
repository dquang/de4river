/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.SedimentLoadAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataCalculation;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataResult;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataResult.Fraction;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataFacet;
import org.dive4elements.river.artifacts.states.DefaultState;

/** State in which Sediment Load(s) are calculated/retrieved. */
public class SedimentLoadDataCalculate
extends DefaultState
implements FacetTypes
{

    private static final long serialVersionUID = 1L;

    private static final Logger log = LogManager
        .getLogger(SedimentLoadDataCalculate.class);

    public static final String I18N_FACET_SEDIMENTLOAD_COARSE =
        "facet.sedimentload.coarse";
    public static final String I18N_FACET_SEDIMENTLOAD_SAND =
        "facet.sedimentload.sand";
    public static final String I18N_FACET_SEDIMENTLOAD_FINE_MIDDLE =
        "facet.sedimentload.fine_middle";
    public static final String I18N_FACET_SEDIMENTLOAD_SUSPSAND =
        "facet.sedimentload.susp_sand";
    public static final String I18N_FACET_SEDIMENTLOAD_SUSPSANDBED =
        "facet.sediemntload.susp_sand_bed";
    public static final String I18N_FACET_SEDIMENTLOAD_SUSPSEDIMENT =
        "facet.sedimentload.susp_sediment";
    public static final String I18N_FACET_SEDIMENTLOAD_TOTAL_LOAD =
        "facet.sedimentload.total_load";
    public static final String I18N_FACET_SEDIMENTLOAD_TOTAL =
        "facet.sedimentload.total";

    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "minfo",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    String name = facet.getName();
                    if (name.endsWith("total") || name.endsWith("bed_load")
                        || name.endsWith("bed_load_susp_sand")) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
            });
    }

    public SedimentLoadDataCalculate() {
    }

    @Override
    public Object computeAdvance(D4EArtifact artifact, String hash,
        CallContext context, List<Facet> facets, Object old) {
        log.debug("SedimentLoadDataCalculate.computeAdvance");

        SedimentLoadAccess access = new SedimentLoadAccess(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult) old
            : new SedimentLoadDataCalculation().calculate(access);


        if (res == null) {
            log.error ("No calculation result.");
        }

        if (facets == null) {
            /* No need to create facets as they already exist in this case. */
            return res;
        }

        Calculation report = res.getReport();

        if (report != null && report.hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
            log.warn ("Problems : " + report.problemsToString());
        }

        Object raw = res.getData();
        if (raw == null) {
            log.warn("No result data.");
            return res;
        }

        SedimentLoadDataResult sdRes;
        if (raw instanceof SedimentLoadDataResult) {
            sdRes = (SedimentLoadDataResult) raw;
        } else {
            log.error ("Unknown result");
            return null;
        }
        String unit = access.getUnit().replace("_per_","/");

        int i = 0;
        for (Fraction fract: sdRes.getFractions()) {
            log.debug("Adding facet for fraction '" + fract.getName() +
                         "' and period '" + fract.getPeriod() + "'");
            facets.add(new SedimentLoadDataFacet(i, fract.getName(),
                          unit, fract.getPeriod(),
                          ComputeType.ADVANCE, id, hash, context));
            i++;
        }

        facets.add(
            new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));

        return res;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
