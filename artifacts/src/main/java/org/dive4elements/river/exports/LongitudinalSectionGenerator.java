/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.geom.Lines;
import org.dive4elements.river.artifacts.model.AreaFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.exports.process.Processor;
import org.dive4elements.river.exports.process.BedDiffHeightYearProcessor;
import org.dive4elements.river.exports.process.BedDiffYearProcessor;
import org.dive4elements.river.exports.process.QOutProcessor;
import org.dive4elements.river.exports.process.WOutProcessor;
import org.dive4elements.river.exports.process.AnnotationProcessor;

import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;


/**
 * An OutGenerator that generates longitudinal section curves.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class LongitudinalSectionGenerator
extends      XYChartGenerator
implements   FacetTypes
{
    public enum YAXIS {
        W(0),
        D(1),
        Q(2);
        protected int idx;
        private YAXIS(int c) {
            idx = c;
        }
    }

    /** The log that is used in this generator. */
    private static Logger log =
        LogManager.getLogger(LongitudinalSectionGenerator.class);

    /** Key to look up internationalized String for annotations label. */
    public static final String I18N_ANNOTATIONS_LABEL =
        "chart.longitudinal.annotations.label";

    /**
     * Key to look up internationalized String for LongitudinalSection diagrams
     * titles.
     */
    public static final String I18N_CHART_TITLE =
        "chart.longitudinal.section.title";

    /**
     * Key to look up internationalized String for LongitudinalSection diagrams
     * subtitles.
     */
    public static final String I18N_CHART_SUBTITLE =
        "chart.longitudinal.section.subtitle";

    /**
     * Key to look up internationalized String for LongitudinalSection diagrams
     * short subtitles.
     */
    public static final String I18N_CHART_SHORT_SUBTITLE =
        "chart.longitudinal.section.shortsubtitle";

    public static final String I18N_XAXIS_LABEL =
        "chart.longitudinal.section.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
        "chart.longitudinal.section.yaxis.label";

    public static final String I18N_2YAXIS_LABEL =
        "chart.longitudinal.section.yaxis.second.label";

    public static final String I18N_CHART_TITLE_DEFAULT =
        "W-L\u00e4ngsschnitt";
    public static final String I18N_XAXIS_LABEL_DEFAULT  = "km";
    public static final String I18N_YAXIS_LABEL_DEFAULT  = "W [NN + m]";
    public static final String I18N_2YAXIS_LABEL_DEFAULT = "Q [m\u00b3/s]";

    public final static String I18N_WDIFF_YAXIS_LABEL =
        "chart.w_differences.yaxis.label";

    public final static String I18N_WDIFF_YAXIS_LABEL_DEFAULT = "m";

    public LongitudinalSectionGenerator() {
        super();
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


    /**
     * Return left most data points x value (on first axis).
     * Overridden because axis could be inverted.
     */
    @Override
    protected double getLeftX() {
        if (isInverted()) {
            return (Double)getXBounds(0).getUpper();
        }
        return (Double)getXBounds(0).getLower();
    }


    /**
     * Return right most data points x value (on first axis).
     * Overridden because axis could be inverted.
     */
    @Override
    protected double getRightX() {
        if (isInverted()) {
            return (Double)getXBounds(0).getLower();
        }
        return (Double)getXBounds(0).getUpper();
    }


    /**
     * Returns the default title for this chart.
     *
     * @return the default title for this chart.
     */
    @Override
    public String getDefaultChartTitle() {
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT);
    }


    /**
     * Returns the default subtitle for this chart.
     *
     * @return the default subtitle for this chart.
     */
    @Override
    protected String getDefaultChartSubtitle() {
        double[] dist = getRange();

        Object[] args = null;
        if (dist == null) {
            args = new Object[] {getRiverName()};
            return msg(getChartShortSubtitleKey(), "", args);
        }
        args = new Object[] {
            getRiverName(),
            dist[0],
            dist[1]
        };
        return msg(getChartSubtitleKey(), "", args);
    }


    /**
     * Gets key to look up internationalized String for the charts subtitle.
     * @return key to look up translated subtitle.
     */
    protected String getChartSubtitleKey() {
        return I18N_CHART_SUBTITLE;
    }


    /**
     * Gets key to look up internationalized String for the charts short
     * subtitle.
     * @return key to look up translated subtitle.
     */
    protected String getChartShortSubtitleKey() {
        return I18N_CHART_SHORT_SUBTITLE;
    }


    /**
     * Get internationalized label for the x axis.
     */
    @Override
    protected String getDefaultXAxisLabel() {
        D4EArtifact flys = (D4EArtifact) master;

        return msg(
            I18N_XAXIS_LABEL,
            I18N_XAXIS_LABEL_DEFAULT,
            new Object[] { RiverUtils.getRiver(flys).getName() });
    }


    @Override
    protected String getDefaultYAxisLabel(int index) {
        String label = "default";

        if (index == YAXIS.W.idx) {
            label = getWAxisLabel();
        }
        else if (index == YAXIS.Q.idx) {
            label = msg(getQAxisLabelKey(), getQAxisDefaultLabel());
        }
        else if (index == YAXIS.D.idx) {
            label = msg(
                I18N_WDIFF_YAXIS_LABEL, I18N_WDIFF_YAXIS_LABEL_DEFAULT);
        }

        return label;
    }


    /**
     * Get internationalized label for the y axis.
     */
    protected String getWAxisLabel() {
        D4EArtifact flys = (D4EArtifact) master;

        String unit = RiverUtils.getRiver(flys).getWstUnit().getName();

        return msg(
            I18N_YAXIS_LABEL,
            I18N_YAXIS_LABEL_DEFAULT,
            new Object[] { unit });
    }


    /**
     * Create Axis for given index.
     * @return axis with according internationalized label.
     */
    @Override
    protected NumberAxis createYAxis(int index) {
        NumberAxis axis = super.createYAxis(index);

        // "Q" Axis shall include 0.
        if (index == YAXIS.Q.idx) {
            axis.setAutoRangeIncludesZero(true);
        }
        else {
            axis.setAutoRangeIncludesZero(false);
        }

        return axis;
    }


    /**
     * Get default value for the second Y-Axis' label (if no translation was
     * found).
     */
    protected String getQAxisDefaultLabel() {
        return I18N_2YAXIS_LABEL_DEFAULT;
    }


    /**
     * Get key for internationalization of the second Y-Axis' label.
     */
    protected String getQAxisLabelKey() {
        return I18N_2YAXIS_LABEL;
    }


    /**
     * Trigger inversion.
     */
    @Override
    protected void adjustAxes(XYPlot plot) {
        super.adjustAxes(plot);
        invertXAxis(plot.getDomainAxis());
    }


    /**
     * This method inverts the x-axis based on the kilometer information of the
     * selected river. If the head of the river is at kilometer 0, the axis is
     * not inverted, otherwise it is.
     *
     * @param xaxis The domain axis.
     */
    protected void invertXAxis(ValueAxis xaxis) {
        if (isInverted()) {
            log.debug("X-Axis.setInverted(true)");
            xaxis.setInverted(true);
        }
    }


    /**
     * Produce output.
     * @param artifactAndFacet current facet and artifact.
     * @param attr  theme for facet
     */
    @Override
    public void doOut(
        ArtifactAndFacet artifactAndFacet,
        ThemeDocument    attr,
        boolean          visible
    ) {
        String name = artifactAndFacet.getFacetName();

        log.debug("LongitudinalSectionGenerator.doOut: " + name);

        if (name == null) {
            log.error("No facet name for doOut(). No output generated!");
            return;
        }

        Facet facet = artifactAndFacet.getFacet();

        if (facet == null) {
            return;
        }

        Processor wProcessor = new WOutProcessor();
        Processor qProcessor = new QOutProcessor();
        Processor bdyProcessor = new BedDiffYearProcessor();
        Processor bdhyProcessor = new BedDiffHeightYearProcessor();
        Processor annotationProcessor = new AnnotationProcessor();

        if (wProcessor.canHandle(name)) {
            wProcessor.doOut(
                this, artifactAndFacet, attr, visible, YAXIS.W.idx);
        }
        if (qProcessor.canHandle(name)) {
            qProcessor.doOut(
                this, artifactAndFacet, attr, visible, YAXIS.Q.idx);
        }
        else if (bdyProcessor.canHandle(name)) {
           bdyProcessor.doOut(
               this, artifactAndFacet, attr, visible, YAXIS.W.idx);
        }
        else if (bdhyProcessor.canHandle(name)) {
           bdhyProcessor.doOut(
               this, artifactAndFacet, attr, visible, YAXIS.W.idx);
        }
        else if (annotationProcessor.canHandle(name)) {
            annotationProcessor.doOut(
                this, artifactAndFacet, attr, visible, 0);
        }
        else if (name.equals(W_DIFFERENCES)) {
            doWDifferencesOut(
                (WKms) artifactAndFacet.getData(context),
                artifactAndFacet,
                attr,
                visible);
        }
        else if (FacetTypes.IS.AREA(name)) {
            doArea(
                artifactAndFacet.getData(context),
                artifactAndFacet,
                attr,
                visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints(
                artifactAndFacet.getData(context),
                artifactAndFacet,
                attr,
                visible,
                YAXIS.W.idx);
        }
        else {
            log.warn("Unknown facet name: " + name);
            return;
        }
    }

    /**
     * Add items to dataseries which describes the differences.
     */
    protected void doWDifferencesOut(
        WKms       wkms,
        ArtifactAndFacet aandf,
        ThemeDocument   theme,
        boolean    visible
    ) {
        log.debug("WDifferencesCurveGenerator.doWDifferencesOut");
        if (wkms == null) {
            log.warn("No data to add to WDifferencesChart.");
            return;
         }

        XYSeries series =
            new StyledXYSeries(aandf.getFacetDescription(), theme);

        if (log.isDebugEnabled()) {
            if (wkms.size() > 0) {
                log.debug("Generate series: " + series.getKey());
                log.debug("Start km: " + wkms.getKm(0));
                log.debug("End   km: " + wkms.getKm(wkms.size() - 1));
                log.debug("Values  : " + wkms.size());
            }
        }

        StyledSeriesBuilder.addPoints(series, wkms);

        addAxisSeries(series, YAXIS.D.idx, visible);
    }


    /**
     * Get name of series (displayed in legend).
     * @return name of the series.
     */
    protected String getSeriesName(WQKms wqkms, String mode) {
        String name   = wqkms.getName();
        String prefix = name != null && name.indexOf(mode) >= 0 ? null : mode;

        return prefix != null && prefix.length() > 0
            ? prefix + "(" + name +")"
            : name;
    }


    /** Look up the axis identifier for a given facet type. */
    public int axisIdxForFacet(String facetName) {
        if (FacetTypes.IS.W(facetName)) {
            return YAXIS.W.idx;
        }
        else if (FacetTypes.IS.Q(facetName)) {
            return YAXIS.Q.idx;
        }
        else {
            log.warn("Could not find axis for facet " + facetName);
            return YAXIS.W.idx;
        }
    }


    /**
     * Do Area out.
     * @param theme styling information.
     * @param visible whether or not visible.
     */
    protected void doArea(
        Object     o,
        ArtifactAndFacet aandf,
        ThemeDocument   theme,
        boolean    visible
    ) {
        log.debug("LongitudinalSectionGenerator.doArea");
        StyledAreaSeriesCollection area = new StyledAreaSeriesCollection(theme);

        String seriesName = aandf.getFacetDescription();

        AreaFacet.Data data = (AreaFacet.Data) o;

        XYSeries up   = null;
        XYSeries down = null;

        if (data.getUpperData() != null) {
            up = new StyledXYSeries(seriesName, false, theme);
            if (data.getUpperData() instanceof WQKms) {
                if (FacetTypes.IS.Q(data.getUpperFacetName())) {
                    StyledSeriesBuilder.addPointsKmQ(
                        up, (WQKms)data.getUpperData());
                }
                else {
                    StyledSeriesBuilder.addPoints(
                        up, (WKms) data.getUpperData());
                }
            }
            else if (data.getUpperData() instanceof double[][]) {
                StyledSeriesBuilder.addPoints(
                    up, (double [][]) data.getUpperData(), false);
            }
            else if (data.getUpperData() instanceof WKms) {
                StyledSeriesBuilder.addPoints(up, (WKms) data.getUpperData());
            }
            else if (data.getUpperData() instanceof Lines.LineData) {
                StyledSeriesBuilder.addPoints(
                    up, ((Lines.LineData) data.getUpperData()).points, false);
            }
            else {
                log.error("Do not know how to deal with (up) area info from: "
                    + data.getUpperData());
            }
        }

        // TODO Depending on style, the area (e.g. 20m^2)
        // should be added as annotation.

        if (data.getLowerData() != null) {
            // TODO: Sort this out: when the two series have the same name,
            // the renderer (or anything in between) will not work correctly.
            down = new StyledXYSeries(seriesName + " ", false, theme);
            if (data.getLowerData() instanceof WQKms) {
                if (FacetTypes.IS.Q(data.getLowerFacetName())) {
                    StyledSeriesBuilder.addPointsKmQ(
                        down, (WQKms) data.getLowerData());
                }
                else {
                    StyledSeriesBuilder.addPoints(
                        down, (WQKms) data.getLowerData());
                }
            }
            else if (data.getLowerData() instanceof double[][]) {
                StyledSeriesBuilder.addPoints(
                    down, (double[][]) data.getLowerData(), false);
            }
            else if (data.getLowerData() instanceof WKms) {
                StyledSeriesBuilder.addPoints(
                    down, (WKms) data.getLowerData());
            }
            else if (data.getLowerData() instanceof Lines.LineData) {
                StyledSeriesBuilder.addPoints(
                    down,
                    ((Lines.LineData) data.getLowerData()).points,
                    false);
            }
            else {
                log.error(
                    "Do not know how to deal with (down) area info from: "
                    + data.getLowerData());
            }
        }

        if (up == null && down != null) {
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.ABOVE);
            down.setKey(seriesName);
            area.addSeries(down);
            area.addSeries(StyledSeriesBuilder.createGroundAtInfinity(down));
        }
        else if (up != null && down == null) {
            area.setMode(StyledAreaSeriesCollection.FILL_MODE.UNDER);
            area.addSeries(up);
            area.addSeries(StyledSeriesBuilder.createGroundAtInfinity(up));
        }
        else if (up != null && down != null) {
            if (data.doPaintBetween()) {
                area.setMode(StyledAreaSeriesCollection.FILL_MODE.BETWEEN);
            }
            else {
                area.setMode(StyledAreaSeriesCollection.FILL_MODE.ABOVE);
            }
            area.addSeries(up);
            area.addSeries(down);
        }
        // Add area to the respective axis.
        String facetAxisName = data.getUpperFacetName() == null
            ? data.getLowerFacetName()
            : data.getUpperFacetName();
        addAreaSeries(area, axisIdxForFacet(facetAxisName), visible);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
