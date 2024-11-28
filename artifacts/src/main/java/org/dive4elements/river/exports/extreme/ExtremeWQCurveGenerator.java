/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.extreme;

import java.awt.Color;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.artifacts.access.FixAnalysisAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.extreme.Curve;
import org.dive4elements.river.artifacts.model.extreme.ExtremeCurveFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.fixings.FixWQCurveGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.JFreeUtil;
import org.dive4elements.river.jfree.StyledXYSeries;

import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;


/**
 * Generator for WQ fixing charts.
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class ExtremeWQCurveGenerator
extends      FixWQCurveGenerator
implements   FacetTypes
{
    /** Private log. */
    private static Logger log =
            LogManager.getLogger(ExtremeWQCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
            "chart.extreme.wq.title";

    public static final String I18N_CHART_SUBTITLE =
            "chart.extreme.wq.subtitle";

    public static final String I18N_CHART_SUBTITLE1 =
            "chart.extreme.wq.subtitle1";

    public static final String I18N_XAXIS_LABEL =
            "chart.extreme.wq.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
            "chart.extreme.wq.yaxis.label";

    public static final String I18N_CHART_TITLE_DEFAULT  =
            "Extremkurvenanalyse";

    public static final String I18N_XAXIS_LABEL_DEFAULT  =
            "Q [m\u00B3/s]";

    public static final String I18N_YAXIS_LABEL_DEFAULT  =
            "W [NN + m]";


    /** First, ask parent to add data, then handle extreme_wq_curve(_base)
     * data.*/
    @Override
    public boolean prepareChartData(
        ArtifactAndFacet aaf,
        ThemeDocument theme,
        boolean visible
    ) {
        if (super.prepareChartData(aaf, theme, visible)) {
            return true;
        }

        String name = aaf.getFacetName();
        if (name.equals(EXTREME_WQ_CURVE)) {
            doExtremeCurveOut(aaf, theme, visible);
            return true;
        }
        else if (name.equals(EXTREME_WQ_CURVE_BASE)) {
            doExtremeCurveBaseOut(aaf, theme, visible);
            return true;
        }
        return false;
    }

    /** Overriden to show second axis also if no visible data present. */
    @Override
    protected void adjustAxes(XYPlot plot) {
        super.adjustAxes(plot);
        if (getCurrentGaugeDatum() != 0d) {
            // Show the W[*m] axis even if there is no data.
            plot.setRangeAxis(1, createYAxis(YAXIS.W.idx));
        }
    }

    /** Do Extreme Curve nonextrapolated points out. */
    protected void doExtremeCurveBaseOut(
        ArtifactAndFacet aaf,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doExtremeCurveBaseOut");
        ExtremeCurveFacet facet = (ExtremeCurveFacet) aaf.getFacet();
        Curve curve = (Curve) facet.getData(aaf.getArtifact(), context);
        if (curve == null) {
            log.warn("doExtremeCurveBaseOut: Facet does not contain Curve");
            return;
        }

        XYSeries qwseries = new StyledXYSeries(
            aaf.getFacetDescription(), theme);

        double gaugeDatum = getCurrentGaugeDatum();

        if (gaugeDatum == 0d) {
            StyledSeriesBuilder.addPointsQW(
                qwseries, curve.getQs(), curve.getWs());
            addAxisSeries(qwseries, YAXIS.W.idx, visible);
        }
        else {
            XYSeries series2 =
                new StyledXYSeries(aaf.getFacetDescription(), theme);
            StyledSeriesBuilder.addPointsQW(
                series2, curve.getQs(), curve.getWs());
            addAxisSeries(series2, YAXIS.W.idx, false);

            StyledSeriesBuilder.addPointsQW(
                qwseries, curve.getQs(), curve.getWs(), -gaugeDatum, 100d);

            addAxisSeries(qwseries, YAXIS.WCm.idx, visible);
        }

        //addAxisSeries(qwseries, YAXIS.W.idx, visible);
    }


    /** Do Extreme Curve out */
    protected void doExtremeCurveOut(
        ArtifactAndFacet aaf,
        ThemeDocument theme,
        boolean visible
    ) {
        log.debug("doExtremeCurveOut");
        ExtremeCurveFacet facet = (ExtremeCurveFacet) aaf.getFacet();
        Curve curve = (Curve) facet.getData(aaf.getArtifact(), context);
        if (curve == null) {
            log.warn("doExtremeCurveOut: Facet does not contain Curve");
            return;
        }

        double maxQ = curve.getSuggestedMaxQ();
        if (maxQ == Double.MAX_VALUE) {
            maxQ = 8000;
        }

        StyledXYSeries series = JFreeUtil.sampleFunction2D(
                curve,
                theme,
                aaf.getFacetDescription(),
                500,   // number of samples
                0.0 ,  // start
                maxQ); // end

        // Add marker from where on its extrapolated.
        if (theme.parseShowExtraMark()) {
            double[] qs = curve.getQs();
            double extrapolateFrom = qs[qs.length-1];

            Marker m = new ValueMarker(extrapolateFrom);
            m.setPaint(Color.black);
            addDomainMarker(m);
        }

        addAxisSeries(series, YAXIS.W.idx, visible);
    }


    @Override
    protected String getChartTitle() {
        return Resources.format(
                context.getMeta(),
                I18N_CHART_TITLE,
                I18N_CHART_TITLE_DEFAULT,
                context.getContextValue(CURRENT_KM));
    }


    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT);
    }

    @Override
    protected String getDefaultChartSubtitle() {
        FixAnalysisAccess access = new FixAnalysisAccess(artifact);
        DateRange dateRange = access.getDateRange();
        DateRange refRange  = access.getReferencePeriod();

        if (dateRange != null && refRange != null) {
            return Resources.format(
                    context.getMeta(),
                    I18N_CHART_SUBTITLE,
                    "",
                    access.getRiverName(),
                    dateRange.getFrom(),
                    dateRange.getTo(),
                    refRange.getFrom(),
                    refRange.getTo());
        }

        return null;
    }

    @Override
    protected void addSubtitles(JFreeChart chart) {
        String defaultSubtitle = getDefaultChartSubtitle();

        if (defaultSubtitle == null || defaultSubtitle.length() == 0) {
            return;
        }

        chart.addSubtitle(new TextTitle(defaultSubtitle));
    }

    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL_DEFAULT);
    }

    @Override
    protected String getDefaultYAxisLabel(int pos) {
        D4EArtifact flys = (D4EArtifact) master;

        String unit = RiverUtils.getRiver(flys).getWstUnit().getName();
        if (pos == 0) {
            unit = "cm";
        }

        return msg(
            I18N_YAXIS_LABEL,
            I18N_YAXIS_LABEL_DEFAULT,
            new Object[] { unit });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
