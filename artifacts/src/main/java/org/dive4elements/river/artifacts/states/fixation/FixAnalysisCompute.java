/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.fixation;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.FixAnalysisAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.ReportFacet;
import org.dive4elements.river.artifacts.model.fixings.FixAnalysisCalculation;
import org.dive4elements.river.artifacts.model.fixings.FixAnalysisEventsFacet;
import org.dive4elements.river.artifacts.model.fixings.FixAnalysisPeriodsFacet;
import org.dive4elements.river.artifacts.model.fixings.FixAnalysisResult;
import org.dive4elements.river.artifacts.model.fixings.FixAvSectorFacet;
import org.dive4elements.river.artifacts.model.fixings.FixDerivateFacet;
import org.dive4elements.river.artifacts.model.fixings.FixDeviationFacet;
import org.dive4elements.river.artifacts.model.fixings.FixLongitudinalAnalysisFacet;
import org.dive4elements.river.artifacts.model.fixings.FixLongitudinalAvSectorFacet;
import org.dive4elements.river.artifacts.model.fixings.FixLongitudinalDeviationFacet;
import org.dive4elements.river.artifacts.model.fixings.FixLongitudinalReferenceFacet;
import org.dive4elements.river.artifacts.model.fixings.FixOutlierFacet;
import org.dive4elements.river.artifacts.model.fixings.FixReferenceEventsFacet;
import org.dive4elements.river.artifacts.model.fixings.FixWQCurveFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.IdGenerator;
import org.dive4elements.river.utils.UniqueDateFormatter;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixAnalysisCompute
extends      DefaultState
implements   FacetTypes
{
    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(FixAnalysisCompute.class);

    private static final String I18N_REFERENCEPERIOD_SHORT =
        "fix.reference.period.event.short";

    private static final String I18N_ANALYSISPERIODS = "fix.analysis.periods";

    private static final String I18N_DERIVATIVE = "fix.derivative";

    private static final String I18N_OUTLIER = "fix.outlier";

    private static final String I18N_ANALYSIS = "fix.analysis.short";

    private static final String I18N_DEVIATION = "fix.deviation";

    private static final String I18N_REFERENCEDEVIATION =
        "fix.reference.deviation";

    private static final String I18N_REFERENCEPERIOD =
        "state.fix.analysis.referenceperiod";

    public static final String [] SECTOR_LABELS = {
        "fix.mnq",
        "fix.mq",
        "fix.mhq",
        "fix.hq5"
    };

    static {
        // Active/deactivate facets.
        FacetActivity.Registry.getInstance().register(
            "fixanalysis",
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   output
                ) {
                    if (
                        output.contains(FacetTypes.ChartType.FLSC.toString())
                    ) {
                        // Longitudinal section chart
                        String name = facet.getName();

                        if (name.contains(FacetTypes.FIX_ANALYSIS_EVENTS_DWT)
                         || name.contains(FacetTypes.FIX_ANALYSIS_EVENTS_LS)
                         || name.contains(FacetTypes.FIX_ANALYSIS_EVENTS_WQ)
                         || name.contains(FacetTypes.FIX_REFERENCE_EVENTS_DWT)
                         || name.contains(FacetTypes.FIX_REFERENCE_EVENTS_LS)
                         || name.contains(FacetTypes.FIX_REFERENCE_EVENTS_WQ)
                        ) {
                            return Boolean.FALSE;
                        }
                    }
                    if (output.contains(FacetTypes.ChartType.FDWC.toString())
                        && facet.getName().contains(
                            FacetTypes.FIX_SECTOR_AVERAGE_DWT)) {
                        return Boolean.FALSE;
                    }

                    return Boolean.TRUE;
                }
            });
    }


    /**
     * The default constructor that initializes an empty State object.
     */
    public FixAnalysisCompute() {
    }


    @Override
    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        log.debug("FixAnalysisCompute.computeAdvance");

        CalculationResult res;

        FixAnalysisAccess access = new FixAnalysisAccess(artifact);

        if (old instanceof CalculationResult) {
            res = (CalculationResult)old;
        }
        else {
            FixAnalysisCalculation calc = new FixAnalysisCalculation(access);
            res = calc.calculate();
        }

        if (facets == null) {
            return res;
        }

        if (res.getReport().hasProblems()) {
            facets.add(new ReportFacet(ComputeType.ADVANCE, hash, id));
        }

        FixAnalysisResult fr = (FixAnalysisResult)res.getData();
        if (fr == null) {
            return res;
        }

        facets.add(
            new DataFacet(CSV, "CSV data", ComputeType.ADVANCE, hash, id));
        facets.add(
            new DataFacet(
                FIX_PARAMETERS, "parameters", ComputeType.ADVANCE, hash, id));
        facets.add(
            new DataFacet(AT, "AT data", ComputeType.ADVANCE, hash, id));

        int maxId = -100;

        int sectorMask = fr.getUsedSectorsInAnalysisPeriods();

        int qsS = access.getQSectorStart();
        int qsE = access.getQSectorEnd();

        DateFormat df = Formatter.getDateFormatter(
            context.getMeta(), "dd.MM.yyyy");
        DateFormat lf = Formatter.getDateFormatter(
            context.getMeta(), "dd.MM.yyyy'T'HH:mm");

        DateRange [] periods = access.getAnalysisPeriods();

        for (int i = 0; i < periods.length; i++) {
            DateRange period = periods[i];
            String startDate = df.format(period.getFrom());
            String endDate   = df.format(period.getTo());

            for (int j = qsS; j <= qsE; j++) {

                // Only emit facets for sectors that really have data.
                if ((sectorMask & (1 << j)) == 0) {
                    continue;
                }

                String sector = SECTOR_LABELS[j];
                String description = "\u0394W (" +
                    Resources.getMsg(context.getMeta(),
                        sector,
                        sector) +
                        ")";

                int sectorNdx = j - qsS;
                int facetNdx = i << 2;
                facetNdx = facetNdx | j;

                if (facetNdx > maxId) {
                    maxId = facetNdx;
                }

                facets.add(
                    new FixAvSectorFacet(
                        facetNdx,
                        FIX_SECTOR_AVERAGE_DWT + "_" + sectorNdx,
                        description));
                facets.add(
                    new FixLongitudinalAvSectorFacet(
                        facetNdx,
                        FIX_SECTOR_AVERAGE_LS + "_" + sectorNdx,
                        description + ":" + startDate + " - " + endDate));
                // TODO: i18n
                String dev = "Abweichung: " + description;
                facets.add(
                    new FixLongitudinalAvSectorFacet(
                        facetNdx,
                        FIX_SECTOR_AVERAGE_LS_DEVIATION + "_" + sectorNdx,
                        dev));
                facets.add(
                    new FixAvSectorFacet(
                        facetNdx,
                        FIX_SECTOR_AVERAGE_WQ + "_" + sectorNdx,
                        description));

            }

            String eventDesc =
                Resources.getMsg(context.getMeta(),
                                 I18N_ANALYSIS,
                                 I18N_ANALYSIS);

            Collection<Date> aeds = fr.getAnalysisEventsDates(i);
            UniqueDateFormatter cf = new UniqueDateFormatter(df, lf, aeds);

            int k = 0;
            for (Date d: aeds) {
                int anaNdx = i << 8;
                anaNdx = anaNdx | k;
                facets.add(new FixAnalysisEventsFacet(anaNdx,
                    FIX_ANALYSIS_EVENTS_DWT,
                    eventDesc + (i+1) + " - " + cf.format(d)));
                facets.add(new FixLongitudinalAnalysisFacet(anaNdx,
                    FIX_ANALYSIS_EVENTS_LS,
                    eventDesc + (i+1) + " - " + cf.format(d)));
                facets.add(new FixAnalysisEventsFacet(anaNdx,
                    FIX_ANALYSIS_EVENTS_WQ,
                    eventDesc + (i+1) +" - " + cf.format(d)));
                k++;
            }
        }

        IdGenerator idg = new IdGenerator(maxId + 1);

        String i18n_ref = Resources.getMsg(context.getMeta(),
                I18N_REFERENCEPERIOD_SHORT,
                I18N_REFERENCEPERIOD_SHORT);
        String i18n_dev = Resources.getMsg(context.getMeta(),
                I18N_REFERENCEDEVIATION,
                I18N_REFERENCEDEVIATION);

        Collection<Date> reds = fr.getReferenceEventsDates();
        UniqueDateFormatter cf = new UniqueDateFormatter(df, lf, reds);

        int i = 0;
        for (Date d: reds) {
            int refNdx = idg.next() << 8;
            refNdx |=  i;
            facets.add(new FixReferenceEventsFacet(refNdx,
                FIX_REFERENCE_EVENTS_DWT,
                i18n_ref + " - " + cf.format(d)));
            refNdx = idg.next() << 8;
            refNdx = refNdx | i;
            facets.add(new FixLongitudinalReferenceFacet(refNdx,
                FIX_REFERENCE_EVENTS_LS,
                i18n_ref + " - " + cf.format(d)));
            refNdx = idg.next() << 8;
            refNdx |= i;
            facets.add(new FixReferenceEventsFacet(refNdx,
                FIX_REFERENCE_EVENTS_WQ,
                i18n_ref + " - " + cf.format(d)));
            i++;
        }

        facets.add(new FixLongitudinalDeviationFacet(idg.next(),
            FIX_DEVIATION_LS,
            i18n_dev));

        String i18n_ana = Resources.getMsg(context.getMeta(),
                I18N_ANALYSISPERIODS,
                I18N_ANALYSISPERIODS);
        facets.add(new FixAnalysisPeriodsFacet(idg.next(),
            FIX_ANALYSIS_PERIODS_DWT,
            i18n_ana));
        facets.add(new FixAnalysisPeriodsFacet(idg.next(),
            FIX_ANALYSIS_PERIODS_LS,
            i18n_ana));
        facets.add(new FixAnalysisPeriodsFacet(idg.next(),
            FIX_ANALYSIS_PERIODS_WQ,
            i18n_ana));

        String i18n_refp = Resources.getMsg(context.getMeta(),
                I18N_REFERENCEPERIOD,
                I18N_REFERENCEPERIOD);
        facets.add(new DataFacet(idg.next(),
            FIX_REFERENCE_PERIOD_DWT,
            i18n_refp,
            ComputeType.ADVANCE, null, null));

        facets.add(new FixWQCurveFacet(idg.next(), "W/Q"));

        Boolean preprocessing = access.getPreprocessing();

        if (preprocessing != null && preprocessing) {
            facets.add(new FixOutlierFacet(
                idg.next(),
                FIX_OUTLIER,
                Resources.getMsg(
                    context.getMeta(), I18N_OUTLIER, I18N_OUTLIER)));
        }

        facets.add(new FixDerivateFacet(
            idg.next(),
            FIX_DERIVATE_CURVE,
            Resources.getMsg(
                context.getMeta(),
                I18N_DERIVATIVE,
                I18N_DERIVATIVE)));

        facets.add(new FixDeviationFacet(
            idg.next(),
            FIX_DEVIATION_DWT,
            Resources.getMsg(context.getMeta(),
                I18N_DEVIATION,
                I18N_DEVIATION)));
        return res;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
