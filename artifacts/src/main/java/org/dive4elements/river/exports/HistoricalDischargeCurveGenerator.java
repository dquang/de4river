/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.HistoricalWQTimerange;
import org.dive4elements.river.artifacts.model.Timerange;
import org.dive4elements.river.artifacts.model.WQTimerange;

import org.dive4elements.river.jfree.StyledTimeSeries;

import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;

import org.jfree.chart.plot.XYPlot;

import org.jfree.data.general.SeriesException;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeCurveGenerator extends TimeseriesChartGenerator
    implements FacetTypes {

    private static Logger log = LogManager
        .getLogger(HistoricalDischargeCurveGenerator.class);

    public static final String I18N_CHART_TITLE =
        "chart.historical.discharge.title";

    public static final String I18N_CHART_SUBTITLE =
        "chart.historical.discharge.subtitle";

    public static final String I18N_XAXIS_LABEL =
        "chart.historical.discharge.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
        "chart.historical.discharge.yaxis.label";

    public static final String I18N_YAXIS_SECOND_LABEL =
        "chart.historical.discharge.yaxis.second.label";

    public static enum YAXIS {
        W(0), Q(1);

        protected int idx;

        private YAXIS(int c) {
            idx = c;
        }
    }

    @Override
    protected YAxisWalker getYAxisWalker() {
        return new YAxisWalker() {

            @Override
            public int length() {
                return YAXIS.values().length;
            }

            @Override
            public String getId(int idx) {
                YAXIS[] yaxes = YAXIS.values();
                return yaxes[idx].toString();
            }
        };
    }

    @Override
    protected String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE);
    }

    @Override
    protected String getDefaultChartSubtitle() {
        D4EArtifact flys = (D4EArtifact) master;
        Timerange evalTime = new HistoricalDischargeAccess(flys)
            .getEvaluationTimerange();

        Object[] args = new Object[] { RiverUtils.getReferenceGaugeName(flys),
            evalTime.getStart(), evalTime.getEnd() };

        return msg(I18N_CHART_SUBTITLE, "", args);
    }

    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL);
    }

    @Override
    protected String getDefaultYAxisLabel(int pos) {
        if (pos == 0) {
            return msg(I18N_YAXIS_LABEL, I18N_YAXIS_LABEL);
        }
        else if (pos == 1) {
            return msg(I18N_YAXIS_SECOND_LABEL, I18N_YAXIS_SECOND_LABEL);
        }
        else {
            return "NO TITLE FOR Y AXIS: " + pos;
        }
    }

    @Override
    protected void adjustPlot(XYPlot plot) {
        super.adjustPlot(plot);
        plot.setRangeZeroBaselineVisible(true);
    }

    @Override
    public void doOut(ArtifactAndFacet artifactFacet, ThemeDocument theme,
        boolean visible) {
        String name = artifactFacet.getFacetName();
        log.debug("HistoricalDischargeCurveGenerator.doOut: " + name);
        log.debug("Theme description is: "
            + artifactFacet.getFacetDescription());

        if (name.equals(HISTORICAL_DISCHARGE_Q)) {
            doHistoricalDischargeOutQ(
                (D4EArtifact) artifactFacet.getArtifact(),
                artifactFacet.getData(context),
                artifactFacet.getFacetDescription(), theme, visible);
        }
        else if (name.equals(HISTORICAL_DISCHARGE_W)) {
            doHistoricalDischargeOutW(
                (D4EArtifact) artifactFacet.getArtifact(),
                artifactFacet.getData(context),
                artifactFacet.getFacetDescription(), theme, visible);
        }
        else if (name.equals(HISTORICAL_DISCHARGE_Q_DIFF)) {
            doHistoricalDischargeDifferenceOutQ(
                (D4EArtifact) artifactFacet.getArtifact(),
                artifactFacet.getData(context),
                artifactFacet.getFacetDescription(), theme, visible);
        }
        else if (name.equals(HISTORICAL_DISCHARGE_W_DIFF)) {
            doHistoricalDischargeDifferenceOutW(
                (D4EArtifact) artifactFacet.getArtifact(),
                artifactFacet.getData(context),
                artifactFacet.getFacetDescription(), theme, visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            HistoricalDischargeAccess.EvaluationMode mode =
                new HistoricalDischargeAccess(
                    (D4EArtifact)getMaster()).getEvaluationMode();
            int axis = mode == HistoricalDischargeAccess.EvaluationMode.W
                ? YAXIS.Q.idx
                : YAXIS.W.idx;

            doPoints(artifactFacet.getData(context), artifactFacet, theme,
                visible, axis);
        }
        else {
            log.warn("doOut(): unknown facet name: " + name);
            return;
        }
    }

    protected void doHistoricalDischargeOutQ(D4EArtifact artifact,
        Object data, String desc, ThemeDocument theme, boolean visible) {
        log.debug("doHistoricalDischargeOut(): description = " + desc);

        WQTimerange wqt = (WQTimerange) data;

        TimeSeriesCollection tsc = newTimeSeriesCollection(wqt.getTimeranges(),
            wqt.getQs(), theme, desc);

        addAxisDataset(tsc, YAXIS.Q.idx, visible);
    }

    protected void doHistoricalDischargeOutW(D4EArtifact artifact,
        Object data, String desc, ThemeDocument theme, boolean visible) {
        log.debug("doHistoricalDischargeOut(): description = " + desc);

        WQTimerange wqt = (WQTimerange) data;

        TimeSeriesCollection tsc = newTimeSeriesCollection(wqt.getTimeranges(),
            wqt.getWs(), theme, desc);

        addAxisDataset(tsc, YAXIS.W.idx, visible);
    }

    protected void doHistoricalDischargeDifferenceOutQ(D4EArtifact artifact,
        Object data, String desc, ThemeDocument theme, boolean visible) {
        log.debug("doHistoricalDischargeDifferenceOut: desc = " + desc);

        HistoricalWQTimerange wqt = (HistoricalWQTimerange) data;

        TimeSeriesCollection tsc = newTimeSeriesCollection(wqt.getTimeranges(),
            wqt.getDiffs(), theme, desc);

        addAxisDataset(tsc, YAXIS.Q.idx, visible);
    }

    protected void doHistoricalDischargeDifferenceOutW(D4EArtifact artifact,
        Object data, String desc, ThemeDocument theme, boolean visible) {
        log.debug("doHistoricalDischargeDifferenceOut: desc = " + desc);

        HistoricalWQTimerange wqt = (HistoricalWQTimerange) data;

        TimeSeriesCollection tsc = newTimeSeriesCollection(wqt.getTimeranges(),
            wqt.getDiffs(), theme, desc);

        addAxisDataset(tsc, YAXIS.W.idx, visible);
    }

    /**
     * Creates a new TimeSeriesCollection with a single TimeSeries. The
     * TimeSeries will consist of two RegularTimePeriods for each W/Q value
     * provided by <i>wqt</i>. This has the effect, that the line in the chart
     * looks like a "step chart".
     */
    protected TimeSeriesCollection newTimeSeriesCollection(
        Timerange[] timeranges,
        double[] values,
        ThemeDocument theme,
        String desc
    ) {
        log.debug("Create new TimeSeriesCollection for: " + desc);

        TimeSeriesCollection tsc = new TimeSeriesCollection();
        TimeSeries series = new StyledTimeSeries(desc, theme);

        for (int i = 0, n = timeranges.length; i < n; i++) {
            RegularTimePeriod[] rtp = newRegularTimePeriod(timeranges[i]);

            try {
                if (Double.isNaN(values[i])) {
                    log.warn("Skip TimePeriod because value is NaN.");
                    continue;
                }

                series.add(rtp[0], values[i]);
                series.add(rtp[1], values[i]);

                if (log.isDebugEnabled()) {
                    log.debug("added Item to TimeSeries:");
                    log.debug("   TimePeriod: " + rtp[0] + " - " + rtp[1]);
                    log.debug("   Value:      " + values[i]);
                }
            }
            catch (SeriesException se) {
                log.warn("Error while adding TimePeriod: " + se);
            }
        }

        tsc.addSeries(series);

        return tsc;
    }

    /**
     * Create array that consists of two
     * <i>FixedMillisecond</i> periods [start, end].
     *
     * @param timerange
     *            Supports start and end time.
     *
     * @return an array with two <i>FixedMillisecond</i> periods [start, end].
     */
    protected RegularTimePeriod[] newRegularTimePeriod(Timerange timerange) {
        Date start = new Date(timerange.getStart());
        Date end = new Date(timerange.getEnd() - 1000 * 60 * 60 * 24);

        return new RegularTimePeriod[] {
            new FixedMillisecond(start),
            new FixedMillisecond(end) };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
