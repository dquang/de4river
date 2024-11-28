/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.text.NumberFormat;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.DataProvider;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.geom.Lines;
import org.dive4elements.river.artifacts.model.CrossSectionFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.HYKFactory;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.model.FastCrossSectionLine;
import org.dive4elements.river.themes.TextStyle;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;


/**
 * An OutGenerator that generates cross section graphs.
 */
public class CrossSectionGenerator
extends      LongitudinalSectionGenerator
implements   FacetTypes
{
    /** The log that is used in this generator. */
    private static Logger log =
            LogManager.getLogger(CrossSectionGenerator.class);

    public static final String I18N_CHART_TITLE =
            "chart.cross_section.title";

    public static final String I18N_CHART_SUBTITLE =
            "chart.cross_section.subtitle";

    public static final String I18N_XAXIS_LABEL =
            "chart.cross_section.xaxis.label";

    public static final String I18N_YAXIS_LABEL =
            "chart.cross_section.yaxis.label";

    public static final String I18N_CHART_TITLE_DEFAULT = "Querprofildiagramm";
    public static final String I18N_XAXIS_LABEL_DEFAULT = "Abstand [m]";
    public static final String I18N_YAXIS_LABEL_DEFAULT = "W [NN + m]";


    /** Trivial Constructor. */
    public CrossSectionGenerator() {
    }


    @Override
    protected YAxisWalker getYAxisWalker() {
        return new YAxisWalker() {
            @Override
            public int length() {
                return 1;
            }

            /** Get identifier for this index. */
            @Override
            public String getId(int idx) {
                return "W";
            }
        };
    }


    /**
     * Get localized chart title.
     */
    @Override
    public String getDefaultChartTitle() {
        Object[] i18n_msg_args = new Object[] {
                getRiverName()
        };
        return msg(I18N_CHART_TITLE, I18N_CHART_TITLE_DEFAULT, i18n_msg_args);
    }


    /** Always return default subtitle. */
    @Override
    protected String getChartSubtitle() {
        // XXX NOTE: overriding this method disables ChartSettings subtitle!
        // The default implementation of this method in ChartGenerator returns
        // the subtitle changed via the chart settings dialog. This method
        // always returns the subtitle containing river and km, NEVER the
        // ChartSettings subtitle!
        return getDefaultChartSubtitle();
    }


    /** Get Charts default subtitle. */
    @Override
    protected String getDefaultChartSubtitle() {
        List<DataProvider> providers =
            context.getDataProvider(
                CrossSectionFacet.BLACKBOARD_CS_MASTER_DATA);
        double km = 0d;
        if (providers.size() > 0) {
            FastCrossSectionLine csl = (FastCrossSectionLine) providers.get(0).
                    provideData(CrossSectionFacet.BLACKBOARD_CS_MASTER_DATA,
                            null, context);
            km = csl == null ? -1 : csl.getKm();
        }

        Object[] args = new Object[] {
                getRiverName(),
                km
        };

        log.debug("Locale: " + Resources.getLocale(context.getMeta()));

        return msg(I18N_CHART_SUBTITLE, "", args);
    }


    /** Get color for hyk zones by their type (which is the name). */
    protected Paint colorForHYKZone(String zoneName) {
        if (zoneName.startsWith("R")) {
            // Brownish.
            return new Color(153, 60, 0);
        }
        else if (zoneName.startsWith("V")) {
            // Greenish.
            return new Color(0, 255, 0);
        }
        else if (zoneName.startsWith("B")) {
            // Grayish.
            return new Color(128, 128, 128);
        }
        else if (zoneName.startsWith("H")) {
            // Blueish.
            return new Color(0, 0, 255);
        }
        else {
            // Default.
            log.debug("Unknown zone type found.");
            return new Color(255, 0, 0);
        }
    }

    @Override
    protected void addAnnotationsToRenderer(XYPlot plot) {
        super.addAnnotationsToRenderer(plot);

        // Paints for the boxes/lines.
        Stroke basicStroke = new BasicStroke(1.0f);

        // XXX: DEAD CODE // Paint linePaint = new Color(255,  0,0,60);
        Paint fillPaint = new Color(0,  255,0,60);
        Paint tranPaint = new Color(0,    0,0, 0);

        // OPTMIMIZE: Pre-calculate positions
        ChartArea area = new ChartArea(
                plot.getDomainAxis(0),
                plot.getRangeAxis());

        for(RiverAnnotation fa : this.annotations) {

            // Access text styling, if any.
            ThemeDocument theme = fa.getTheme();
            TextStyle textStyle = null;

            // Get Themeing information and add legend item.
            if (theme != null) {
                textStyle = theme.parseComplexTextStyle();
                if (fa.getLabel() != null) {
                    LegendItemCollection lic = new LegendItemCollection();
                    LegendItemCollection old = plot.getFixedLegendItems();
                    lic.add(createLegendItem(theme, fa.getLabel()));
                    // (Re-)Add prior legend entries.
                    if (old != null) {
                        old.addAll(lic);
                    }
                    else {
                        old = lic;
                    }
                    plot.setFixedLegendItems(old);
                }
            }

            // Hyks.
            for (HYKFactory.Zone zone: fa.getBoxes()) {
                // For each zone, create a box to fill with color, a box to draw
                // the lines and a text to display the type.
                fillPaint = colorForHYKZone(zone.getName());

                XYBoxAnnotation boxA = new XYBoxAnnotation(
                    zone.getFrom(),
                    area.atGround(),
                    zone.getTo(),
                    area.ofGround(0.03f),
                    basicStroke,
                    tranPaint,
                    fillPaint);
                XYBoxAnnotation boxB = new XYBoxAnnotation(
                    zone.getFrom(),
                    area.atGround(),
                    zone.getTo(),
                    area.atTop(),
                    basicStroke,
                    fillPaint,
                    tranPaint);

                XYTextAnnotation tex = new XYTextAnnotation(
                    zone.getName(),
                    zone.getFrom() + (zone.getTo() - zone.getFrom()) / 2.0d,
                    area.ofGround(0.015f));
                if (textStyle != null) {
                    textStyle.apply(tex);
                }

                plot.getRenderer().addAnnotation(
                    boxA, org.jfree.ui.Layer.BACKGROUND);
                plot.getRenderer().addAnnotation(
                    boxB, org.jfree.ui.Layer.BACKGROUND);
                plot.getRenderer().addAnnotation(
                    tex,  org.jfree.ui.Layer.BACKGROUND);
            }
        }
    }

    @Override
    protected String getDefaultXAxisLabel() {
        return msg(I18N_XAXIS_LABEL, I18N_XAXIS_LABEL_DEFAULT);
    }


    @Override
    protected String getDefaultYAxisLabel(int pos) {
        D4EArtifact flys = (D4EArtifact) master;

        String unit = RiverUtils.getRiver(flys).getWstUnit().getName();

        return msg(I18N_YAXIS_LABEL,
                   I18N_YAXIS_LABEL_DEFAULT,
                   new Object[] { unit });
    }


    /**
     * Let one facet do its job.
     */
    @Override
    public void doOut(
            ArtifactAndFacet artifactFacet,
            ThemeDocument    attr,
            boolean          visible
            ) {
        String name = artifactFacet.getFacetName();

        log.debug("CrossSectionGenerator.doOut: " + name);

        if (name == null) {
            log.error("No facet name for doOut(). No output generated!");
            return;
        }

        if (name.equals(CROSS_SECTION)) {
            doCrossSectionOut(
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    attr,
                    visible);
        }
        else if (name.equals(CROSS_SECTION_WATER_LINE)) {
            doCrossSectionWaterLineOut(
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    attr,
                    visible);
        }
        else if (FacetTypes.IS.AREA(name)) {
            doArea(artifactFacet.getData(context),
                    artifactFacet,
                    attr,
                    visible);
        }
        else if (name.equals(HYK)) {
            doHyk(artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    attr,
                    visible);
        }
        else if (FacetTypes.IS.MANUALLINE(name)) {
            doCrossSectionWaterLineOut(
                    artifactFacet.getData(context),
                    artifactFacet.getFacetDescription(),
                    attr,
                    visible);
        }
        else if (FacetTypes.IS.MANUALPOINTS(name)) {
            doPoints(artifactFacet.getData(context),
                    artifactFacet,
                    attr, visible, YAXIS.W.idx);
        }
        else {
            log.warn("CrossSection.doOut: Unknown facet name: " + name);
            return;
        }
    }


    /** Look up the axis identifier for a given facet type. */
    @Override
    public int axisIdxForFacet(String facetName) {
        // TODO Where to add thid axis too.
        return 0;
    }


    /**
     * Do cross sections waterline out.
     *
     * @param seriesName name of the data (line) to display in legend.
     * @param theme Theme for the data series.
     */
    protected void doCrossSectionWaterLineOut(
            Object   o,
            String   seriesName,
            ThemeDocument theme,
            boolean  visible
            ) {
        log.debug("CrossSectionGenerator.doCrossSectionWaterLineOut");

        Lines.LineData lines = (Lines.LineData) o;
        // DO NOT SORT DATA! This destroys the gaps indicated by NaNs.
        StyledXYSeries series = new StyledXYSeries(seriesName, false, theme);

        if (!theme.parseShowLineLabel()) {
            series.setLabel("");
        }
        if (theme.parseShowWidth()) {
            NumberFormat nf = Formatter.getMeterFormat(this.context);
            String labelAdd = "b=" + nf.format(lines.width) + "m";
            if (series.getLabel().length() == 0) {
                series.setLabel(labelAdd);
            }
            else {
                series.setLabel(series.getLabel() + ", " + labelAdd);
            }
        }
        if (theme.parseShowLevel() && lines.points.length > 1
                && lines.points[1].length > 0) {
            NumberFormat nf = Formatter.getMeterFormat(this.context);
            D4EArtifact flys = (D4EArtifact) master;

            String unit = RiverUtils.getRiver(flys).getWstUnit().getName();

            String labelAdd = "W=" + nf.format(lines.points[1][0]) + unit;
            if (series.getLabel().length() == 0) {
                series.setLabel(labelAdd);
            }
            else {
                series.setLabel(series.getLabel() + ", " + labelAdd);
            }
        }
        if (theme.parseShowMiddleHeight() && lines.width != 0) {
            NumberFormat nf = Formatter.getMeterFormat(this.context);
            String labelAdd = "T=" + nf.format(lines.area / lines.width) + "m";
            // : " + lines.area + "/" + lines.width);
            if (series.getLabel().length() == 0) {
                series.setLabel(labelAdd);
            }
            else {
                series.setLabel(series.getLabel() + ", " + labelAdd);
            }
        }

        StyledSeriesBuilder.addPoints(series, lines.points, false);

        addAxisSeries(series, 0, visible);
    }


    /** Add HYK-Annotations (colorize and label some areas, draw lines. */
    protected void doHyk(
            Object   o,
            String   seriesName,
            ThemeDocument theme,
            boolean  visible
            ) {
        log.debug("CrossSectionGenerator.doHyk");

        List<HYKFactory.Zone> zones = (List<HYKFactory.Zone>) o;

        if (zones == null || zones.isEmpty()) {
            log.warn("CrossSectionGenerator.doHYK: empty zone list received.");
            return;
        }

        // Actual Styling is done in XYChartGenerator.
        if (visible) {
            addAnnotations(new RiverAnnotation(seriesName, null, zones, theme));
        }
    }


    /**
     * Do cross sections out.
     *
     * @param seriesName name of the data (line) to display in legend.
     * @param theme Theme for the data series.
     */
    protected void doCrossSectionOut(
            Object   o,
            String   seriesName,
            ThemeDocument theme,
            boolean  visible
            ) {
        log.debug("CrossSectionGenerator.doCrossSectionOut");

        XYSeries series = new StyledXYSeries(seriesName, theme);

        StyledSeriesBuilder.addPoints(series, (double [][]) o, false);

        addAxisSeries(series, 0, visible);
    }


    /**
     * Creates a new <i>ChartSection</i>.
     *
     * Overridden to prevent inclusion of subtitle.
     *
     * @return a new <i>ChartSection</i>.
     */
    @Override
    protected ChartSection buildChartSection() {
        ChartSection chartSection = new ChartSection();
        chartSection.setTitle(getChartTitle());
        chartSection.setDisplayGrid(isGridVisible());
        chartSection.setDisplayLogo(showLogo());
        chartSection.setLogoVPlacement(logoVPlace());
        chartSection.setLogoHPlacement(logoHPlace());
        return chartSection;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
