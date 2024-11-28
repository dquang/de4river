/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedDifferencesAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffCalculation;
import org.dive4elements.river.artifacts.model.minfo.BedDiffFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffHeightMinFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffHeightMinFilterFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffHeightSubFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffHeightSubFilterFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffPerYearFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffPerYearFilterFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffFilterFacet;
import org.dive4elements.river.artifacts.model.minfo.BedDiffYearResult;
import org.dive4elements.river.artifacts.model.minfo.BedDifferencesResult;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;

/**
 * State for BedDifferences.
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DifferencesState
extends DefaultState
implements FacetTypes
{
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(DifferencesState.class);
    public static final String I18N_DIFF_YEAR = "beddifference.year";

    public static final String I18N_FACET_BED_DIFF_YEAR =
        "facet.bedheight.diff.year";
    public static final String I18N_FACET_BED_DIFF_YEAR_RAW =
        "facet.bedheight.diff.year.raw";
    public static final String I18N_FACET_BED_DIFF_ABSOLUTE =
        "facet.bedheight.diff.absolute";
    public static final String I18N_FACET_BED_DIFF_ABSOLUTE_RAW =
        "facet.bedheight.diff.absolute.raw";
    public static final String I18N_FACET_BED_DIFF_SOUNDING =
        "facet.bedheight.diff.sounding";
    public static final String I18N_FACET_BED_DIFF_MORPH1 =
        "facet.bedheight.diff.morph1";
    public static final String I18N_FACET_BED_DIFF_MORPH2 =
        "facet.bedheight.diff.morph2";
    public static final String I18N_FACET_BED_DIFF_HEIGHT1 =
        "facet.bedheight.diff.height1";
    public static final String I18N_FACET_BED_DIFF_HEIGHT1_RAW =
        "facet.bedheight.diff.height1.raw";
    public static final String I18N_FACET_BED_DIFF_HEIGHT2 =
        "facet.bedheight.diff.height2";
    public static final String I18N_FACET_BED_DIFF_HEIGHT2_RAW =
        "facet.bedheight.diff.height2.raw";

    public static final String I18N_FACET_BED_DIFF_HEIGHT_RAW =
        "facet.bedheight.diff.height.raw";

    public DifferencesState() {
    }

    @Override
    public Object computeAdvance(D4EArtifact artifact, String hash,
        CallContext context, List<Facet> facets, Object old) {
        log.debug("DifferencesState.computeAdvance");

        List<Facet> newFacets = new ArrayList<Facet>();

        BedDifferencesAccess access = new BedDifferencesAccess(artifact);

        CalculationResult res = old instanceof CalculationResult
            ? (CalculationResult)old
            : new BedDiffCalculation().calculate(access, context);

        if (facets == null || res == null) {
            return res;
        }

        BedDifferencesResult[] results =
            (BedDifferencesResult[])res.getData();

        if (results == null || results.length == 0) {
            log.warn("Calculation computed no results!");
            return res;
        }

        generateFacets(context, newFacets, results, getID(), hash);
        log.debug("Created " + newFacets.size() + " new Facets.");

        facets.addAll(newFacets);

        Calculation report = res.getReport();
        if (report != null && report.hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
            log.warn("Problems: " + report.problemsToString());
        }

        return res;
    }

    /** Generate Facets based on given results.
     * @param newFacets list to place new facets into.
     */
    protected void generateFacets(
        CallContext context,
        List<Facet> newFacets,
        BedDifferencesResult[] results,
        String stateId,
        String hash
    ) {
        log.debug("DifferencesState.generateFacets");

        CallMeta meta = context.getMeta();

        newFacets.add(
            new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));
        newFacets.add(
            new DataFacet(PDF, "PDF data", ComputeType.ADVANCE, hash, id));
        for (int idx = 0; idx < results.length; idx++) {
            if (results[idx] instanceof BedDiffYearResult) {
                newFacets.add(new BedDiffFacet(
                    idx,
                    BED_DIFFERENCE_YEAR,
                    createBedDiffYearDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        true),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                newFacets.add(new BedDiffHeightMinFacet(
                    idx,
                    BED_DIFFERENCE_YEAR_HEIGHT1,
                    createBedDiffHeightDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        0,
                        true),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                newFacets.add(new BedDiffHeightSubFacet(
                    idx,
                    BED_DIFFERENCE_YEAR_HEIGHT2,
                    createBedDiffHeightDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        1,
                        true),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                if (((BedDiffYearResult)results[idx])
                    .getHeightPerYearData()[1].length > 0) {
                    /* Skip facets with data per year if there are none
                       (because of missing start or end year) */
                    newFacets.add(new BedDiffPerYearFacet(
                        idx,
                        BED_DIFFERENCE_HEIGHT_YEAR,
                        createBedDiffAbsoluteDescription(
                            meta,
                            (BedDiffYearResult)results[idx],
                            true),
                        ComputeType.ADVANCE,
                        stateId,
                        hash));
                    newFacets.add(new BedDiffPerYearFilterFacet(
                        idx,
                        BED_DIFFERENCE_HEIGHT_YEAR_FILTERED,
                        createBedDiffAbsoluteDescription(
                            meta,
                            (BedDiffYearResult)results[idx],
                            false),
                        ComputeType.ADVANCE,
                        stateId,
                        hash));
                }
                newFacets.add(new BedDiffFilterFacet(
                    idx,
                    BED_DIFFERENCE_YEAR_FILTERED,
                    createBedDiffYearDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        false),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                newFacets.add(new BedDiffHeightMinFilterFacet(
                    idx,
                    BED_DIFFERENCE_YEAR_HEIGHT1_FILTERED,
                    createBedDiffHeightDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        0,
                        false),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
                newFacets.add(new BedDiffHeightSubFilterFacet(
                    idx,
                    BED_DIFFERENCE_YEAR_HEIGHT2_FILTERED,
                    createBedDiffHeightDescription(
                        meta,
                        (BedDiffYearResult)results[idx],
                        1,
                        false),
                    ComputeType.ADVANCE,
                    stateId,
                    hash));
            }
        }
    }

    private String createBedDiffHeightDescription(
        CallMeta meta,
        BedDiffYearResult result,
        int ndx,
        boolean raw
    ) {
        if (raw && ndx == 0) {
            return Resources.getMsg(
                meta,
                I18N_FACET_BED_DIFF_HEIGHT_RAW,
                I18N_FACET_BED_DIFF_HEIGHT_RAW,
                new Object[] {result.getNameFirst()});
        }
        if (raw && ndx == 1) {
            return Resources.getMsg(
                meta,
                I18N_FACET_BED_DIFF_HEIGHT_RAW,
                I18N_FACET_BED_DIFF_HEIGHT_RAW,
                new Object[] {result.getNameSecond()});
        }
        if (ndx == 0) {
            return result.getNameFirst();
        }
        else {
            return result.getNameSecond();
        }
    }

    protected String createBedDiffYearDescription(
        CallMeta meta,
        BedDiffYearResult result,
        boolean raw
    ) {
        String start = result.getStart() != null ?
            result.getStart().toString() : result.getNameFirst();
        String end = result.getEnd() != null ?
            result.getEnd().toString() : result.getNameSecond();
        String range = start + " - " + end;

        String i18n = I18N_FACET_BED_DIFF_YEAR;
        if (raw) {
            i18n = I18N_FACET_BED_DIFF_YEAR_RAW;
        }
        return Resources.getMsg(meta, i18n, new Object[] { range });
    }

    protected String createBedDiffSoundingDescription(
        CallMeta meta) {
        return Resources.getMsg(meta, I18N_FACET_BED_DIFF_SOUNDING,
            I18N_FACET_BED_DIFF_SOUNDING);
    }

    protected String createBedDiffMorph1Description(
        CallMeta meta) {
        return Resources.getMsg(meta, I18N_FACET_BED_DIFF_MORPH1,
            I18N_FACET_BED_DIFF_MORPH1);
    }

    protected String createBedDiffMorph2Description(
        CallMeta meta) {
        return Resources.getMsg(meta, I18N_FACET_BED_DIFF_MORPH2,
            I18N_FACET_BED_DIFF_MORPH2);
    }

    protected String createBedDiffAbsoluteDescription(
        CallMeta meta,
        BedDiffYearResult result,
        boolean raw
    ) {
        String range = result.getStart() + " - " + result.getEnd();

        String i18n = I18N_FACET_BED_DIFF_ABSOLUTE;
        if (raw) {
            i18n = I18N_FACET_BED_DIFF_ABSOLUTE_RAW;
        }
        return Resources.getMsg(meta, i18n, i18n, new Object[] { range });
    }

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
                    if (name.equals(BED_DIFFERENCE_HEIGHT_YEAR) ||
                        name.equals(BED_DIFFERENCE_YEAR) ||
                        name.equals(BED_DIFFERENCE_YEAR_HEIGHT1) ||
                        name.equals(BED_DIFFERENCE_YEAR_HEIGHT2) ||
                        name.equals(BED_DIFFERENCE_YEAR_HEIGHT1_FILTERED) ||
                        name.equals(BED_DIFFERENCE_YEAR_HEIGHT2_FILTERED)){
                        return Boolean.FALSE;
                    }
                    if (name.equals(BEDHEIGHT_SOUNDING_WIDTH) ||
                        name.equals(BED_DIFFERENCE_HEIGHT_YEAR_FILTERED) ||
                        name.equals(BED_DIFFERENCE_YEAR_FILTERED)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
            });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
