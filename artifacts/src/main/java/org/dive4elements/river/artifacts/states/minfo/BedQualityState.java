/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedQualityAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.minfo.BedQualityDataFacet;
import org.dive4elements.river.artifacts.model.minfo.BedQualityInterpolFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiameterDataFacet;
import org.dive4elements.river.artifacts.model.minfo.BedloadDiameterDataFacet;
import org.dive4elements.river.artifacts.model.minfo.BedQualityCalculation;
import org.dive4elements.river.artifacts.model.minfo.BedQualityResult;
import org.dive4elements.river.artifacts.model.minfo.BedQualityResultValue;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;

/* TODO: Change data facets to live in the generalized data scheme and
 * obsolute the obfuscated index magic. */

public class BedQualityState extends DefaultState implements FacetTypes {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LogManager
        .getLogger(BedQualityState.class);

    /* The suffix to append to interpol facets. */
    public static final String I18N_INTERPOL_SUFFIX =
        "facet.bedquality.interpol.suffix";

    /* I18n is in the pattern base.<name>.<type> with optional suffix .data */
    public static final String I18N_FACET_BED_BASE = "facet.bedquality.bed";

    /* Data Layers */
    public static final String I18N_FACET_BEDLOAD_DIAMETER_DATA =
        "facet.bedquality.bed.diameter.bedload.data";
    public static final String I18N_FACET_BED_DIAMETER_DATA_TOPLAYER =
        "facet.bedquality.bed.diameter.toplayer.data";
    public static final String I18N_FACET_BED_DIAMETER_DATA_SUBLAYER =
        "facet.bedquality.bed.diameter.sublayer.data";
    public static final String I18N_TOPLAYER = "bedquality.toplayer";
    public static final String I18N_SUBLAYER = "bedquality.sublayer";

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
                    String name = facet.getName().replace(".interpol","");
                    if (name.equals(BED_QUALITY_SEDIMENT_DENSITY_TOPLAYER) ||
                        name.equals(BED_QUALITY_SEDIMENT_DENSITY_SUBLAYER) ||
                        name.equals(BED_DIAMETER_DATA_TOP) ||
                        name.equals(BED_DIAMETER_DATA_SUB) ||
                        name.equals(BEDLOAD_DIAMETER_DATA)){
                        return Boolean.FALSE;
                    }
                    if (name.equals(BED_QUALITY_POROSITY_TOPLAYER) ||
                        name.equals(BED_QUALITY_POROSITY_SUBLAYER) ||
                        name.equals(BED_QUALITY_BED_DIAMETER_TOPLAYER) ||
                        name.equals(BED_QUALITY_BED_DIAMETER_SUBLAYER) ||
                        name.equals(BED_QUALITY_BEDLOAD_DIAMETER)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
            });
    }

    @Override
    public Object computeAdvance(D4EArtifact artifact, String hash,
        CallContext context, List<Facet> facets, Object old) {
        log.debug("BedQualityState.computeAdvance");

        List<Facet> newFacets = new ArrayList<Facet>();

        BedQualityAccess access = new BedQualityAccess(artifact, context);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : new BedQualityCalculation().calculate(access);

        if (facets == null || res == null) {
            return res;
        }

        Calculation report = res.getReport();

        if (report != null && report.hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
            log.debug("Problems : " + report.problemsToString());
        }

        BedQualityResult[] results = (BedQualityResult[]) res.getData();

        if (results == null || results.length == 0) {
            log.warn("Calculation computed no results!");
            return res;
        }

        generateFacets(context, newFacets, results, getID(), hash);
        List<Facet> candidates = new ArrayList<Facet>();
        generateDataFacets(context, candidates, access, getID(), hash);
        /* Do not create empty facets */
        for (Facet f: candidates) {
            DataFacet candidate = (DataFacet)f;
            Object d = candidate.getData((Artifact)artifact, context);
            if (d != null) {
                double [][] data = (double[][]) d;
                if (data.length > 0 && data[0].length > 0) {
                    boolean onlyNaN = true;
                    for (int i = 0; i < data[1].length; i++) {
                        if (!Double.isNaN(data[1][i])) {
                            onlyNaN = false;
                            break;
                        }
                    }
                    if (!onlyNaN) {
                        newFacets.add(candidate);
                    } else {
                        log.debug("Not adding measurement facet " +
                                  "because it only contains NaN values");
                    }
                    // else adding a problem would be nice
                }
            }
        }
        log.debug("Created " + newFacets.size() + " new Facets.");
        facets.addAll(newFacets);

        return res;
    }

    private int generateIndex(String diameter) {
        int d = 0;
        if(diameter.equals("d10")) {
            d = 1;
        }
        else if (diameter.equals("d16")) {
            d = 2;
        }
        else if (diameter.equals("d20")) {
            d = 3;
        }
        else if (diameter.equals("d25")) {
            d = 4;
        }
        else if (diameter.equals("d30")) {
            d = 5;
        }
        else if (diameter.equals("d40")) {
            d = 6;
        }
        else if (diameter.equals("d50")) {
            d = 7;
        }
        else if (diameter.equals("d60")) {
            d = 8;
        }
        else if (diameter.equals("d70")) {
            d = 9;
        }
        else if (diameter.equals("d75")) {
            d = 10;
        }
        else if (diameter.equals("d80")) {
            d = 11;
        }
        else if (diameter.equals("d84")) {
            d = 12;
        }
        else if (diameter.equals("d90")) {
            d = 13;
        }
        else if (diameter.equals("dmin")) {
            d = 14;
        }
        else if (diameter.equals("dmax")) {
            d = 15;
        }
        else if (diameter.equals("dm")) {
            d = 16;
        }
        int ndx = d << 1;
        return ndx;
    }

    private void generateDataFacets(
        CallContext context,
        List<Facet> newFacets,
        BedQualityAccess access,
        String stateId,
        String hash) {
        List<String> diameters = access.getBedDiameter();
        List<String> loadDiameters = access.getBedloadDiameter();
        List<DateRange> ranges = access.getDateRanges();
        for (int i = 0, R = ranges.size(); i < R; i++) {
            DateRange range = ranges.get(i);
            for (String diameter: diameters) {
                int ndxTop = generateIndex(diameter);
                int ndxSub = ndxTop;
                ndxTop += 1;
                ndxTop = ndxTop << 3;
                ndxSub = ndxSub << 3;
                ndxTop += i;
                ndxSub += i;
                String toplayer =
                    Resources.getMsg(
                        context.getMeta(), I18N_TOPLAYER, I18N_TOPLAYER);
                String sublayer =
                    Resources.getMsg(
                        context.getMeta(), I18N_SUBLAYER, I18N_SUBLAYER);
                //toplayer
                newFacets.add(new BedDiameterDataFacet(
                    ndxTop,
                    BED_DIAMETER_DATA_TOP,
                    Resources.getMsg(
                        context.getMeta(),
                        I18N_FACET_BED_DIAMETER_DATA_TOPLAYER,
                        I18N_FACET_BED_DIAMETER_DATA_TOPLAYER,
                        new Object[] { diameter.toUpperCase(),
                            range.getFrom(), range.getTo(), toplayer}),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                //sublayer
                newFacets.add(new BedDiameterDataFacet(
                    ndxSub,
                    BED_DIAMETER_DATA_SUB,
                    Resources.getMsg(
                        context.getMeta(),
                        I18N_FACET_BED_DIAMETER_DATA_TOPLAYER,
                        I18N_FACET_BED_DIAMETER_DATA_TOPLAYER,
                        new Object[] { diameter.toUpperCase(),
                            range.getFrom(), range.getTo(), sublayer}),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
            }
            for (String loadDiameter: loadDiameters) {
                int ndx = generateIndex(loadDiameter);
                ndx = ndx << 3;
                ndx += i;
                newFacets.add(new BedloadDiameterDataFacet(
                    ndx,
                    BEDLOAD_DIAMETER_DATA,
                    Resources.getMsg(
                        context.getMeta(),
                        I18N_FACET_BEDLOAD_DIAMETER_DATA,
                        I18N_FACET_BEDLOAD_DIAMETER_DATA,
                        new Object[] { loadDiameter.toUpperCase(),
                            range.getFrom(), range.getTo()}),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
            }
        }
    }


    protected String getFacetName(BedQualityResultValue value) {
        /* basename + name or "diameter" + .type */
        return BED_QUALITY_DATA_FACET + "." +
            (value.isDiameterResult() ? "diameter" : value.getName()) + "." +
            value.getType();
    }

    protected void generateFacets(CallContext context, List<Facet> newFacets,
        BedQualityResult[] results, String stateId, String hash) {
        log.debug("BedQualityState.generateFacets");

        CallMeta meta = context.getMeta();

        newFacets.add(
            new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));
        for (int idx = 0; idx < results.length; idx++) {
            BedQualityResult result = results[idx];
            DateRange range = result.getDateRange();
            int i = 0;
            for (BedQualityResultValue value: result.getValues()) {
                newFacets.add(new BedQualityDataFacet((idx << 8) + i++,
                    getFacetName(value),
                    getFacetDescription(meta, range, value),
                    ComputeType.ADVANCE,
                    stateId, hash, value.getName(), value.getType()));
                if (value.isInterpolateable()) {
                    newFacets.add(new BedQualityInterpolFacet((idx << 8) + i++,
                        getFacetName(value) + ".interpol",
                        getFacetInterpolDescription(meta, range, value),
                        ComputeType.ADVANCE,
                        stateId, hash, value.getName(), value.getType()));
                }
            }
        }
    }

    protected String getFacetInterpolDescription(CallMeta meta,
                                                 DateRange range,
                                                 BedQualityResultValue value) {
        String part1 = getFacetDescription(meta, range, value);
        /* We could add a step description here */
        return part1 + " " + Resources.getMsg(meta,
                                              I18N_INTERPOL_SUFFIX,
                                              I18N_INTERPOL_SUFFIX,
                                              new Object[] {});
    }


    protected String getFacetDescription(CallMeta meta,
                                         DateRange range,
                                         BedQualityResultValue value) {
        Date from = range != null ? range.getFrom() : new Date();
        Date to = range != null ? range.getTo() : new Date();

        final String layerSuffix =
            Resources.getMsg(meta, "bedquality." + value.getType(), "");

        /* This could probably be unified with the facet name */
        final String i18n = I18N_FACET_BED_BASE + "." +
            (value.isDiameterResult() ? "diameter" : value.getName()) + "." +
            value.getType();

        if (value.isDiameterResult()) {
            /* Include the diameter in the description */
            return Resources.getMsg(meta, i18n, i18n, new Object[] {
                value.getName().toUpperCase(), from, to, layerSuffix });
        } else {
            return Resources.getMsg(meta, i18n, i18n, new Object[] {
                    from, to, layerSuffix });
        }
    }
}
