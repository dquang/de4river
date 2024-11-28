/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.PreferredLocale;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.EnhancedLineAndShapeRenderer;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StableXYDifferenceRenderer;
import org.dive4elements.river.jfree.Style;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledSeries;
import org.dive4elements.river.jfree.AxisDataset;
import org.dive4elements.river.themes.ThemeDocument;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.river.utils.Formatter;

/**
 * The base class for chart creation. It should provide some basic things that
 * equal in all chart types.
 *
 * Annotations are added as RiverAnnotations and come in mutliple basic forms:
 * TextAnnotations are labels somewhere in data space, StickyAnnotations are
 * labels of a slice or line in one data dimension (i.e. visualized as label
 * on a single axis).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class ChartGenerator implements OutGenerator {

    private static Logger log = LogManager.getLogger(ChartGenerator.class);

    public static final int    DEFAULT_CHART_WIDTH     = 600;
    public static final int    DEFAULT_CHART_HEIGHT    = 400;
    public static final String DEFAULT_CHART_FORMAT    = "png";
    public static final Color  DEFAULT_GRID_COLOR      = Color.GRAY;
    public static final float  DEFAULT_GRID_LINE_WIDTH = 0.3f;
    public static final int    DEFAULT_FONT_SIZE       = 12;
    public static final String DEFAULT_FONT_NAME       = "Tahoma";

    protected static float ANNOTATIONS_AXIS_OFFSET = 0.02f;

    public static final String XPATH_CHART_SIZE =
        "/art:action/art:attributes/art:size";

    public static final String XPATH_CHART_FORMAT =
        "/art:action/art:attributes/art:format/@art:value";

    public static final String XPATH_CHART_X_RANGE =
        "/art:action/art:attributes/art:xrange";

    public static final String XPATH_CHART_Y_RANGE =
        "/art:action/art:attributes/art:yrange";


    /** The document of the incoming out() request.*/
    protected Document request;

    /** The output stream where the data should be written to.*/
    protected OutputStream out;

    /** The CallContext object.*/
    protected CallContext context;

    protected D4EArtifactCollection collection;

    /** Artifact that is used to decorate the chart with meta information.*/
    protected Artifact master;

    /** The settings that should be used during output creation.*/
    protected Settings settings;

    /** Map of datasets ("index"). */
    protected SortedMap<Integer, AxisDataset> datasets;

    /** List of annotations to insert in plot. */
    protected List<RiverAnnotation> annotations =
        new ArrayList<RiverAnnotation>();

    protected String outName;

    /**
     * A mini interface that allows to walk over the YAXIS enums defined in
     * subclasses.
     */
    public interface YAxisWalker {

        int length();

        String getId(int idx);
    } // end of YAxisWalker interface


    /**
     * Default constructor that initializes internal data structures.
     */
    public ChartGenerator() {
        datasets = new TreeMap<Integer, AxisDataset>();
    }

    @Override
    public void setup(Object config) {
        log.debug("ChartGenerator.setup");
    }

    /**
     * Adds annotations to list. The given annotation will be visible.
     */
    public void addAnnotations(RiverAnnotation annotation) {
        annotations.add(annotation);
    }


    /**
     * This method needs to be implemented by concrete subclasses to create new
     * instances of JFreeChart.
     *
     * @return a new instance of a JFreeChart.
     */
    public abstract JFreeChart generateChart();


    /** For every outable (i.e. facets), this function is
     * called and handles the data accordingly. */
    @Override
    public abstract void doOut(
        ArtifactAndFacet bundle,
        ThemeDocument    attr,
        boolean          visible);


    protected abstract YAxisWalker getYAxisWalker();


    protected abstract Series getSeriesOf(XYDataset dataset, int idx);

    /**
     * Returns the default title of a chart.
     *
     * @return the default title of a chart.
     */
    protected abstract String getDefaultChartTitle();


    /**
     * Returns the default X-Axis label of a chart.
     *
     * @return the default X-Axis label of a chart.
     */
    protected abstract String getDefaultXAxisLabel();


    /**
     * This method is called to retrieve the default label for an Y axis at
     * position <i>pos</i>.
     *
     * @param pos The position of an Y axis.
     *
     * @return the default Y axis label at position <i>pos</i>.
     */
    protected abstract String getDefaultYAxisLabel(int pos);


    /**
     * This method is used to create new AxisDataset instances which may differ
     * in concrete subclasses.
     *
     * @param idx The index of an axis.
     */
    protected abstract AxisDataset createAxisDataset(int idx);


    /**
     * Combines the ranges of the X axis at index <i>idx</i>.
     *
     * @param bounds A new Bounds.
     * @param idx The index of the X axis that should be comined with
     * <i>range</i>.
     */
    protected abstract void combineXBounds(Bounds bounds, int idx);


    /**
     * Combines the ranges of the Y axis at index <i>idx</i>.
     *
     * @param bounds A new Bounds.
     * @param index The index of the Y axis that should be comined with.
     * <i>range</i>.
     */
    protected abstract void combineYBounds(Bounds bounds, int index);


    /**
     * This method is used to determine the ranges for axes at a given index.
     *
     * @param index The index of the axes at the plot.
     *
     * @return a Range[] with [xrange, yrange];
     */
    public abstract Range[] getRangesForAxis(int index);

    public abstract Bounds getXBounds(int axis);

    protected abstract void setXBounds(int axis, Bounds bounds);

    public abstract Bounds getYBounds(int axis);

    protected abstract void setYBounds(int axis, Bounds bounds);


    /**
     * This method retrieves the chart subtitle by calling getChartSubtitle()
     * and adds it as TextTitle to the chart.
     * The default implementation of getChartSubtitle() returns the same
     * as getDefaultChartSubtitle() which must be implemented by derived
     * classes. If you want to add multiple subtitles to the chart override
     * this method and add your subtitles manually.
     *
     * @param chart The JFreeChart chart object.
     */
    protected void addSubtitles(JFreeChart chart) {
        String subtitle = getChartSubtitle();

        if (subtitle != null && subtitle.length() > 0) {
            chart.addSubtitle(new TextTitle(subtitle));
        }
    }


    /**
     * Register annotations like MainValues for later plotting
     *
     * @param annotations list of annotations (data of facet).
     * @param aandf   Artifact and the facet.
     * @param theme   Theme document for given annotations.
     * @param visible The visibility of the annotations.
     */
    public void doAnnotations(
        RiverAnnotation annotations,
        ArtifactAndFacet aandf,
        ThemeDocument theme,
        boolean visible
    ){
        log.debug("doAnnotations");

        // Add all annotations to our annotation pool.
        annotations.setTheme(theme);
        if (aandf != null) {
            annotations.setLabel(aandf.getFacetDescription());
        }
        else {
            log.error(
                "Art/Facet for Annotations is null. " +
                "This should never happen!");
        }

        if (visible) {
            addAnnotations(annotations);
        }
    }


    /**
     * Generate chart.
     */
    @Override
    public void generate()
    throws IOException
    {
        log.debug("ChartGenerator.generate");

        JFreeChart chart = generateChart();

        String format = getFormat();
        int[]  size   = getSize();

        if (size == null) {
            size = getExportDimension();
        }

        context.putContextValue("chart.width",  size[0]);
        context.putContextValue("chart.height", size[1]);

        if (format.equals(ChartExportHelper.FORMAT_PNG)) {
            context.putContextValue("chart.image.format", "png");

            ChartExportHelper.exportImage(
                out,
                chart,
                context);
        }
        else if (format.equals(ChartExportHelper.FORMAT_PDF)) {
            preparePDFContext(context);

            ChartExportHelper.exportPDF(
                out,
                chart,
                context);
        }
        else if (format.equals(ChartExportHelper.FORMAT_SVG)) {
            prepareSVGContext(context);

            ChartExportHelper.exportSVG(
                out,
                chart,
                context);
        }
        else if (format.equals(ChartExportHelper.FORMAT_CSV)) {
            context.putContextValue("chart.image.format", "csv");

            ChartExportHelper.exportCSV(
                out,
                chart,
                context);
        }
    }


    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        log.debug("ChartGenerator.init");

        this.outName = outName;
        this.request = request;
        this.out     = out;
        this.context = context;
    }


    /** Sets the master artifact. */
    @Override
    public void setMasterArtifact(Artifact master) {
        this.master = master;
    }


    /**
     * Gets the master artifact.
     * @return the master artifact.
     */
    public Artifact getMaster() {
        return master;
    }


    /** Sets the collection. */
    @Override
    public void setCollection(D4EArtifactCollection collection) {
        this.collection = collection;
    }


    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }


    /**
     * Returns instance of <i>ChartSettings</i> with a chart specific section
     * but with no axes settings.
     *
     * @return an instance of <i>ChartSettings</i>.
     */
    @Override
    public Settings getSettings() {
        if (this.settings != null) {
            return this.settings;
        }

        ChartSettings settings = new ChartSettings();

        ChartSection  chartSection  = buildChartSection();
        LegendSection legendSection = buildLegendSection();
        ExportSection exportSection = buildExportSection();

        settings.setChartSection(chartSection);
        settings.setLegendSection(legendSection);
        settings.setExportSection(exportSection);

        List<AxisSection> axisSections = buildAxisSections();
        for (AxisSection axisSection: axisSections) {
            settings.addAxisSection(axisSection);
        }

        return settings;
    }


    /**
     * Creates a new <i>ChartSection</i>.
     *
     * @return a new <i>ChartSection</i>.
     */
    protected ChartSection buildChartSection() {
        ChartSection chartSection = new ChartSection();
        chartSection.setTitle(getChartTitle());
        chartSection.setSubtitle(getChartSubtitle());
        chartSection.setDisplayGrid(isGridVisible());
        chartSection.setDisplayLogo(showLogo());
        chartSection.setLogoVPlacement(logoVPlace());
        chartSection.setLogoHPlacement(logoHPlace());
        return chartSection;
    }


    /**
     * Creates a new <i>LegendSection</i>.
     *
     * @return a new <i>LegendSection</i>.
     */
    protected LegendSection buildLegendSection() {
        LegendSection legendSection = new LegendSection();
        legendSection.setVisibility(isLegendVisible());
        legendSection.setFontSize(getLegendFontSize());
        legendSection.setAggregationThreshold(10);
        return legendSection;
    }


    /**
     * Creates a new <i>ExportSection</i> with default values <b>WIDTH=600</b>
     * and <b>HEIGHT=400</b>.
     *
     * @return a new <i>ExportSection</i>.
     */
    protected ExportSection buildExportSection() {
        ExportSection exportSection = new ExportSection();
        exportSection.setWidth(600);
        exportSection.setHeight(400);
        return exportSection;
    }


    /**
     * Create list of Sections that contains all axes of the chart (including
     * X and Y axes).
     *
     * @return a list of Sections for each axis in this chart.
     */
    protected List<AxisSection> buildAxisSections() {
        List<AxisSection> axisSections = new ArrayList<AxisSection>();

        axisSections.addAll(buildXAxisSections());
        axisSections.addAll(buildYAxisSections());

        return axisSections;
    }


    /**
     * Creates a new Section for chart's X axis.
     *
     * @return a List that contains a Section for the X axis.
     */
    protected List<AxisSection> buildXAxisSections() {
        List<AxisSection> axisSections = new ArrayList<AxisSection>();

        String identifier = "X";

        AxisSection axisSection = new AxisSection();
        axisSection.setIdentifier(identifier);
        axisSection.setLabel(getXAxisLabel());
        axisSection.setFontSize(14);
        axisSection.setFixed(false);

        // XXX We are able to find better default ranges that [0,0], but the Y
        // axes currently have no better ranges set.
        axisSection.setUpperRange(0d);
        axisSection.setLowerRange(0d);

        axisSections.add(axisSection);

        return axisSections;
    }


    /**
     * Creates a list of Section for the chart's Y axes. This method makes use
     * of <i>getYAxisWalker</i> to be able to access all Y axes defined in
     * subclasses.
     *
     * @return a list of Y axis sections.
     */
    protected List<AxisSection> buildYAxisSections() {
        List<AxisSection> axisSections = new ArrayList<AxisSection>();

        YAxisWalker walker = getYAxisWalker();
        for (int i = 0, n = walker.length(); i < n; i++) {
            AxisSection ySection = new AxisSection();
            ySection.setIdentifier(walker.getId(i));
            ySection.setLabel(getYAxisLabel(i));
            ySection.setFontSize(14);
            ySection.setFixed(false);

            // XXX We are able to find better default ranges that [0,0], the
            // only problem is, that we do NOT have a better range than [0,0]
            // for each axis, because the initial chart will not have a dataset
            // for each axis set!
            ySection.setUpperRange(0d);
            ySection.setLowerRange(0d);

            axisSections.add(ySection);
        }

        return axisSections;
    }


    /**
     * Returns the <i>settings</i> as <i>ChartSettings</i>.
     *
     * @return the <i>settings</i> as <i>ChartSettings</i> or null, if
     * <i>settings</i> is not an instance of <i>ChartSettings</i>.
     */
    public ChartSettings getChartSettings() {
        if (settings instanceof ChartSettings) {
            return (ChartSettings) settings;
        }

        return null;
    }


    /**
     * Returns the chart title provided by <i>settings</i>.
     *
     * @param settings A ChartSettings object.
     *
     * @return the title provided by <i>settings</i> or null if no
     * <i>ChartSection</i> is provided by <i>settings</i>.
     *
     * @throws NullPointerException if <i>settings</i> is null.
     */
    public String getChartTitle(ChartSettings settings) {
        ChartSection cs = settings.getChartSection();
        return cs != null ? cs.getTitle() : null;
    }


    /**
     * Returns the chart subtitle provided by <i>settings</i>.
     *
     * @param settings A ChartSettings object.
     *
     * @return the subtitle provided by <i>settings</i> or null if no
     * <i>ChartSection</i> is provided by <i>settings</i>.
     *
     * @throws NullPointerException if <i>settings</i> is null.
     */
    public String getChartSubtitle(ChartSettings settings) {
        ChartSection cs = settings.getChartSection();
        return cs != null ? cs.getSubtitle() : null;
    }


    /**
     * Returns a boolean object that determines if the chart grid should be
     * visible or not. This information needs to be provided by <i>settings</i>,
     * otherweise the default is true.
     *
     * @param settings A ChartSettings object.
     *
     * @return true, if the chart grid should be visible otherwise false.
     *
     * @throws NullPointerException if <i>settings</i> is null.
     */
    public boolean isGridVisible(ChartSettings settings) {
        ChartSection     cs = settings.getChartSection();
        Boolean displayGrid = cs.getDisplayGrid();

        return displayGrid != null ? displayGrid : true;
    }


    /**
     * Returns a boolean object that determines if the chart legend should be
     * visible or not. This information needs to be provided by <i>settings</i>,
     * otherwise the default is true.
     *
     * @param settings A ChartSettings object.
     *
     * @return true, if the chart legend should be visible otherwise false.
     *
     * @throws NullPointerException if <i>settings</i> is null.
     */
    public boolean isLegendVisible(ChartSettings settings) {
        LegendSection      ls = settings.getLegendSection();
        Boolean displayLegend = ls.getVisibility();

        return displayLegend != null ? displayLegend : true;
    }


    /**
     * Returns the legend font size specified in <i>settings</i> or null if no
     * <i>LegendSection</i> is provided by <i>settings</i>.
     *
     * @param settings A ChartSettings object.
     *
     * @return the legend font size or null.
     *
     * @throws NullPointerException if <i>settings</i> is null.
     */
    public Integer getLegendFontSize(ChartSettings settings) {
        LegendSection ls = settings.getLegendSection();
        return ls != null ? ls.getFontSize() : null;
    }


    /**
     * Returns the title of a chart. The return value depends on the existence
     * of ChartSettings: if there are ChartSettings set, this method returns the
     * chart title provided by those settings. Otherwise, this method returns
     * getDefaultChartTitle().
     *
     * @return the title of a chart.
     */
    protected String getChartTitle() {
        ChartSettings chartSettings = getChartSettings();

        if (chartSettings != null) {
            return getChartTitle(chartSettings);
        }

        return getDefaultChartTitle();
    }


    /**
     * Returns the subtitle of a chart. The return value depends on the
     * existence of ChartSettings: if there are ChartSettings set, this method
     * returns the chart title provided by those settings. Otherwise, this
     * method returns getDefaultChartSubtitle().
     *
     * @return the subtitle of a chart.
     */
    protected String getChartSubtitle() {
        ChartSettings chartSettings = getChartSettings();

        if (chartSettings != null) {
            return getChartSubtitle(chartSettings);
        }

        return getDefaultChartSubtitle();
    }


    /**
     * This method always returns null. Override it in subclasses that require
     * subtitles.
     *
     * @return null.
     */
    protected String getDefaultChartSubtitle() {
        // Override this method in subclasses
        return null;
    }


    /**
     * This method is used to determine, if the chart's legend is visible or
     * not. If a <i>settings</i> instance is set, this instance determines the
     * visibility otherwise, this method returns true as default if no
     * <i>settings</i> is set.
     *
     * @return true, if the legend should be visible, otherwise false.
     */
    protected boolean isLegendVisible() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings != null) {
            return isLegendVisible(chartSettings);
        }

        return true;
    }


    /** Where to place the logo. */
    protected String logoHPlace() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings != null) {
            ChartSection cs    = chartSettings.getChartSection();
            String       place = cs.getLogoHPlacement();

            return place;
        }
        return "center";
    }


    /** Where to place the logo. */
    protected String logoVPlace() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings != null) {
            ChartSection cs    = chartSettings.getChartSection();
            String       place = cs.getLogoVPlacement();

            return place;
        }
        return "top";
    }


    /** Return the logo id from settings. */
    protected String showLogo(ChartSettings chartSettings) {
        if (chartSettings != null) {
            ChartSection cs   = chartSettings.getChartSection();
            String       logo = cs.getDisplayLogo();

            return logo;
        }
        return "none";
    }


    /**
     * This method is used to determine if a logo should be added to the plot.
     *
     * @return logo name (null if none).
     */
    protected String showLogo() {
        ChartSettings chartSettings = getChartSettings();
        return showLogo(chartSettings);
    }


    /**
     * This method is used to determine the font size of the chart's legend. If
     * a <i>settings</i> instance is set, this instance determines the font
     * size, otherwise this method returns 12 as default if no <i>settings</i>
     * is set or if it doesn't provide a legend font size.
     *
     * @return a legend font size.
     */
    protected int getLegendFontSize() {
        Integer fontSize = null;

        ChartSettings chartSettings = getChartSettings();
        if (chartSettings != null) {
            fontSize = getLegendFontSize(chartSettings);
        }

        return fontSize != null ? fontSize : DEFAULT_FONT_SIZE;
    }


    /**
     * This method is used to determine if the resulting chart should display
     * grid lines or not. <b>Note: this method always returns true!</b>
     *
     * @return true, if the chart should display grid lines, otherwise false.
     */
    protected boolean isGridVisible() {
        return true;
    }


    /**
     * Returns the X-Axis label of a chart.
     *
     * @return the X-Axis label of a chart.
     */
    protected String getXAxisLabel() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return getDefaultXAxisLabel();
        }

        AxisSection as = chartSettings.getAxisSection("X");
        if (as != null) {
            String label = as.getLabel();

            if (label != null) {
                return label;
            }
        }

        return getDefaultXAxisLabel();
    }


    /**
     * This method returns the font size for the X axis. If the font size is
     * specified in ChartSettings (if <i>chartSettings</i> is set), this size is
     * returned. Otherwise the default font size 12 is returned.
     *
     * @return the font size for the x axis.
     */
    protected int getXAxisLabelFontSize() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return DEFAULT_FONT_SIZE;
        }

        AxisSection   as = chartSettings.getAxisSection("X");
        Integer fontSize = as.getFontSize();

        return fontSize != null ? fontSize : DEFAULT_FONT_SIZE;
    }


    /**
     * This method returns the font size for an Y axis. If the font size is
     * specified in ChartSettings (if <i>chartSettings</i> is set), this size is
     * returned. Otherwise the default font size 12 is returned.
     *
     * @return the font size for the x axis.
     */
    protected int getYAxisFontSize(int pos) {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return DEFAULT_FONT_SIZE;
        }

        YAxisWalker walker = getYAxisWalker();

        AxisSection   as = chartSettings.getAxisSection(walker.getId(pos));
        if (as == null) {
            return DEFAULT_FONT_SIZE;
        }
        Integer fontSize = as.getFontSize();

        return fontSize != null ? fontSize : DEFAULT_FONT_SIZE;
    }


    /**
     * This method returns the export dimension specified in ChartSettings as
     * int array [width,height].
     *
     * @return an int array with [width,height].
     */
    protected int[] getExportDimension() {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return new int[] { 600, 400 };
        }

        ExportSection export = chartSettings.getExportSection();
        Integer width  = export.getWidth();
        Integer height = export.getHeight();

        if (width != null && height != null) {
            return new int[] { width, height };
        }

        return new int[] { 600, 400 };
    }


    /**
     * Returns the Y-Axis label of a chart at position <i>pos</i>.
     *
     * @return the Y-Axis label of a chart at position <i>0</i>.
     */
    protected String getYAxisLabel(int pos) {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return getDefaultYAxisLabel(pos);
        }

        YAxisWalker walker = getYAxisWalker();
        AxisSection     as = chartSettings.getAxisSection(walker.getId(pos));
        if (as != null) {
            String label = as.getLabel();

            if (label != null) {
                return label;
            }
        }

        return getDefaultYAxisLabel(pos);
    }


    /**
     * This method searches for a specific axis in the <i>settings</i> if
     * <i>settings</i> is set. If the axis was found, this method returns the
     * specified axis range if the axis range is fixed. Otherwise, this method
     * returns null.
     *
     * @param axisId The identifier of an axis.
     *
     * @return the specified axis range from <i>settings</i> if the axis is
     * fixed, otherwise null.
     */
    public Range getRangeForAxisFromSettings(String axisId) {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return null;
        }

        AxisSection as = chartSettings.getAxisSection(axisId);

        if (as == null) {
            return null;
        }

        Boolean fixed = as.isFixed();

        if (fixed != null && fixed) {

            /* Only time series charts have time ranges so prefer those. */
            if (axisId.equals("X")) {
                Long lowerTime = as.getLowerTimeRange();
                Long upperTime = as.getUpperTimeRange();
                if ( lowerTime != null && upperTime != null ) {
                    log.debug("Using time range: "
                        + lowerTime + " - " + upperTime);
                    return lowerTime < upperTime
                            ? new Range(lowerTime, upperTime)
                            : new Range(upperTime, lowerTime);
                }
            }

            Double upper = as.getUpperRange();
            Double lower = as.getLowerRange();

            if (upper != null && lower != null) {
                return lower < upper
                    ? new Range(lower, upper)
                    : new Range(upper, lower);
            }
        }

        return null;
    }


    /**
     * Adds a new AxisDataset which contains <i>dataset</i> at index <i>idx</i>.
     *
     * @param dataset An XYDataset.
     * @param idx The axis index.
     * @param visible Determines, if the dataset should be visible or not.
     */
    public void addAxisDataset(XYDataset dataset, int idx, boolean visible) {
        if (dataset == null || idx < 0) {
            return;
        }

        AxisDataset axisDataset = getAxisDataset(idx);

        Bounds[] xyBounds = ChartHelper.getBounds(dataset);

        if (xyBounds == null) {
            log.warn("Skip XYDataset for Axis (invalid ranges): " + idx);
            return;
        }

        if (visible) {
            if (log.isDebugEnabled()) {
                log.debug("Add new AxisDataset at index: " + idx);
                log.debug("X extent: " + xyBounds[0]);
                log.debug("Y extent: " + xyBounds[1]);
            }

            axisDataset.addDataset(dataset);
        }

        combineXBounds(xyBounds[0], 0);
        combineYBounds(xyBounds[1], idx);
    }


    /**
     * This method grants access to the AxisDatasets stored in <i>datasets</i>.
     * If no AxisDataset exists for index <i>idx</i>, a new AxisDataset is
     * created using <i>createAxisDataset()</i>.
     *
     * @param idx The index of the desired AxisDataset.
     *
     * @return an existing or new AxisDataset.
     */
    public AxisDataset getAxisDataset(int idx) {
        AxisDataset axisDataset = datasets.get(idx);

        if (axisDataset == null) {
            axisDataset = createAxisDataset(idx);
            datasets.put(idx, axisDataset);
        }

        return axisDataset;
    }


    /**
     * Adjust some Stroke/Grid parameters for <i>plot</i>. The chart
     * <i>Settings</i> are applied in this method.
     *
     * @param plot The XYPlot which is adapted.
     */
    protected void adjustPlot(XYPlot plot) {
        Stroke gridStroke = new BasicStroke(
            DEFAULT_GRID_LINE_WIDTH,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            3.0f,
            new float[] { 3.0f },
            0.0f);

        ChartSettings      cs = getChartSettings();
        boolean isGridVisible = cs != null ? isGridVisible(cs) : true;

        plot.setDomainGridlineStroke(gridStroke);
        plot.setDomainGridlinePaint(DEFAULT_GRID_COLOR);
        plot.setDomainGridlinesVisible(isGridVisible);

        plot.setRangeGridlineStroke(gridStroke);
        plot.setRangeGridlinePaint(DEFAULT_GRID_COLOR);
        plot.setRangeGridlinesVisible(isGridVisible);

        plot.setAxisOffset(new RectangleInsets(0d, 0d, 0d, 0d));
    }


    /**
     * This helper mehtod is used to extract the current locale from instance
     * vairable <i>context</i>.
     *
     * @return the current locale.
     */
    protected Locale getLocale() {
        CallMeta           meta = context.getMeta();
        PreferredLocale[] prefs = meta.getLanguages();

        int len = prefs != null ? prefs.length : 0;

        Locale[] locales = new Locale[len];

        for (int i = 0; i < len; i++) {
            locales[i] = prefs[i].getLocale();
        }

        return meta.getPreferredLocale(locales);
    }


    /**
     * Look up \param key in i18n dictionary.
     * @param key key for which to find i18nd version.
     * @param def default, returned if lookup failed.
     * @return value found in i18n dictionary, \param def if no value found.
     */
    protected String msg(String key, String def) {
        return Resources.getMsg(context.getMeta(), key, def);
    }

    /**
     * Look up \param key in i18n dictionary.
     * @param key key for which to find i18nd version.
     * @return value found in i18n dictionary, key itself if failed.
     */
    protected String msg(String key) {
        return Resources.getMsg(context.getMeta(), key, key);
    }

    protected String msg(String key, Object[] args) {
        return Resources.getMsg(context.getMeta(), key, key, args);
    }

    protected String msg(String key, String def, Object[] args) {
        return Resources.getMsg(context.getMeta(), key, def, args);
    }


    protected String getRiverName() {
        return new RiverAccess((D4EArtifact)master).getRiver().getName();
    }

    protected String getRiverUnit() {
        return new RiverAccess((D4EArtifact)master).getRiver()
            .getWstUnit().getName();
    }

    protected double[] getRange() {
        D4EArtifact flys = (D4EArtifact) master;

        RangeAccess rangeAccess = new RangeAccess(flys);
        return rangeAccess.getKmRange();
    }


    /**
     * Returns the size of a chart export as array which has been specified by
     * the incoming request document.
     *
     * @return the size of a chart as [width, height] or null if no width or
     * height are given in the request document.
     */
    protected int[] getSize() {
        int[] size = new int[2];

        Element sizeEl = (Element)XMLUtils.xpath(
            request,
            XPATH_CHART_SIZE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (sizeEl != null) {
            String uri = ArtifactNamespaceContext.NAMESPACE_URI;

            String w = sizeEl.getAttributeNS(uri, "width");
            String h = sizeEl.getAttributeNS(uri, "height");

            if (w.length() > 0 && h.length() > 0) {
                try {
                    size[0] = Integer.parseInt(w);
                    size[1] = Integer.parseInt(h);
                }
                catch (NumberFormatException nfe) {
                    log.warn("Wrong values for chart width/height.");
                }
            }
        }

        return size[0] > 0 && size[1] > 0 ? size : null;
    }


    /**
     * This method returns the format specified in the <i>request</i> document
     * or <i>DEFAULT_CHART_FORMAT</i> if no format is specified in
     * <i>request</i>.
     *
     * @return the format used to export this chart.
     */
    protected String getFormat() {
        String format = (String) XMLUtils.xpath(
            request,
            XPATH_CHART_FORMAT,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);

        return format == null || format.length() == 0
            ? DEFAULT_CHART_FORMAT
            : format;
    }


    /**
     * Returns the X-Axis range as String array from request document.
     * If the (x|y)range elements are not found in request document, return
     * null (i.e. not zoomed).
     *
     * @return a String array with [lower, upper], null if not in document.
     */
    protected String[] getDomainAxisRangeFromRequest() {
        Element xrange = (Element)XMLUtils.xpath(
            request,
            XPATH_CHART_X_RANGE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (xrange == null) {
            return null;
        }

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        String lower = xrange.getAttributeNS(uri, "from");
        String upper = xrange.getAttributeNS(uri, "to");

        return new String[] { lower, upper };
    }


    /** Returns null if the (x|y)range-element was not found in
     * request document.
     * This usally means that the axis are not manually zoomed, i.e. showing
     * full data extent. */
    protected String[] getValueAxisRangeFromRequest() {
        Element yrange = (Element)XMLUtils.xpath(
            request,
            XPATH_CHART_Y_RANGE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (yrange == null) {
            return null;
        }


        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        String lower = yrange.getAttributeNS(uri, "from");
        String upper = yrange.getAttributeNS(uri, "to");

        return new String[] { lower, upper };
    }


    /**
     * Returns the default size of a chart export as array.
     *
     * @return the default size of a chart as [width, height].
     */
    protected int[] getDefaultSize() {
        return new int[] { DEFAULT_CHART_WIDTH, DEFAULT_CHART_HEIGHT };
    }


    /**
     * Add datasets stored in instance variable <i>datasets</i> to plot.
     * <i>datasets</i> actually stores instances of AxisDataset, so each of this
     * datasets is mapped to a specific axis as well.
     *
     * @param plot plot to add datasets to.
     */
    protected void addDatasets(XYPlot plot) {
        log.debug("addDatasets()");

        // AxisDatasets are sorted, but some might be empty.
        // Thus, generate numbering on the fly.
        int axisIndex    = 0;
        int datasetIndex = 0;

        for (Map.Entry<Integer, AxisDataset> entry: datasets.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                // Add axis and range information.
                AxisDataset axisDataset = entry.getValue();
                NumberAxis  axis        = createYAxis(entry.getKey());

                plot.setRangeAxis(axisIndex, axis);

                if (axis.getAutoRangeIncludesZero()) {
                    axisDataset.setRange(
                        Range.expandToInclude(axisDataset.getRange(), 0d));
                }

                setYBounds(
                    axisIndex, expandPointRange(axisDataset.getRange()));

                // Add contained datasets, mapping to axis.
                for (XYDataset dataset: axisDataset.getDatasets()) {
                    plot.setDataset(datasetIndex, dataset);
                    plot.mapDatasetToRangeAxis(datasetIndex, axisIndex);

                    applyThemes(plot, dataset,
                        datasetIndex,
                        axisDataset.isArea(dataset));

                    datasetIndex++;
                }

                axisDataset.setPlotAxisIndex(axisIndex);
                axisIndex++;
            }
        }
    }


    /**
     * @param idx "index" of dataset/series (first dataset to be drawn has
     *            index 0), correlates with renderer index.
     * @param isArea true if the series describes an area and shall be rendered
     *                as such.
     */
    protected void applyThemes(
        XYPlot    plot,
        XYDataset series,
        int       idx,
        boolean   isArea
    ) {
        if (isArea) {
            applyAreaTheme(plot, (StyledAreaSeriesCollection) series, idx);
        }
        else {
            applyLineTheme(plot, series, idx);
        }
    }


    /**
     * This method applies the themes defined in the series itself. Therefore,
     * <i>StyledXYSeries.applyTheme()</i> is called, which modifies the renderer
     * for the series.
     *
     * @param plot The plot.
     * @param dataset The XYDataset which needs to support Series objects.
     * @param idx The index of the renderer / dataset.
     */
    protected void applyLineTheme(XYPlot plot, XYDataset dataset, int idx) {
        log.debug("Apply LineTheme for dataset at index: " + idx);

        LegendItemCollection lic  = new LegendItemCollection();
        LegendItemCollection anno = plot.getFixedLegendItems();

        Font legendFont = createLegendLabelFont();

        XYLineAndShapeRenderer renderer = createRenderer(plot, idx);

        for (int s = 0, num = dataset.getSeriesCount(); s < num; s++) {
            Series series = getSeriesOf(dataset, s);

            if (series instanceof StyledSeries) {
                Style style = ((StyledSeries) series).getStyle();
                style.applyTheme(renderer, s);
            }

            // special case: if there is just one single item, we need to enable
            // points for this series, otherwise we would not see anything in
            // the chart area.
            if (series.getItemCount() == 1) {
                renderer.setSeriesShapesVisible(s, true);
            }

            LegendItem legendItem = renderer.getLegendItem(idx, s);
            if (legendItem.getLabel().endsWith(" ") ||
                legendItem.getLabel().endsWith("interpol")) {
                legendItem = null;
            }

            if (legendItem != null) {
                legendItem.setLabelFont(legendFont);
                lic.add(legendItem);
            }
            else {
                log.warn("Could not get LegentItem for renderer: "
                    + idx + ", series-idx " + s);
            }
        }

        if (anno != null) {
            lic.addAll(anno);
        }

        plot.setFixedLegendItems(lic);

        plot.setRenderer(idx, renderer);
    }


    /**
     * @param plot The plot.
     * @param area A StyledAreaSeriesCollection object.
     * @param idx The index of the dataset.
     */
    protected void applyAreaTheme(
        XYPlot                     plot,
        StyledAreaSeriesCollection area,
        int                        idx
    ) {
        LegendItemCollection lic  = new LegendItemCollection();
        LegendItemCollection anno = plot.getFixedLegendItems();

        Font legendFont = createLegendLabelFont();

        log.debug("Registering an 'area'renderer at idx: " + idx);

        StableXYDifferenceRenderer dRenderer =
            new StableXYDifferenceRenderer();

        if (area.getMode() == StyledAreaSeriesCollection.FILL_MODE.UNDER) {
            dRenderer.setPositivePaint(createTransparentPaint());
        }

        plot.setRenderer(idx, dRenderer);

        area.applyTheme(dRenderer);

        // i18n
        dRenderer.setAreaLabelNumberFormat(
            Formatter.getFormatter(context.getMeta(), 2, 4));

        dRenderer.setAreaLabelTemplate(Resources.getMsg(
            context.getMeta(), "area.label.template", "Area=%sm2"));

        LegendItem legendItem = dRenderer.getLegendItem(idx, 0);
        if (legendItem != null) {
            legendItem.setLabelFont(legendFont);
            lic.add(legendItem);
        }
        else {
            log.warn("Could not get LegentItem for renderer: "
                + idx + ", series-idx " + 0);
        }

        if (anno != null) {
            lic.addAll(anno);
        }

        plot.setFixedLegendItems(lic);
    }


    /**
     * Expands a given range if it collapses into one point.
     *
     * @param range Range to be expanded if upper == lower bound.
     *
     * @return Bounds of point plus 5 percent in each direction.
     */
    private Bounds expandPointRange(Range range) {
        if (range == null) {
            return null;
        }
        else if (range.getLowerBound() == range.getUpperBound()) {
            Range expandedRange = ChartHelper.expandRange(range, 5d);
            return new DoubleBounds(
                expandedRange.getLowerBound(), expandedRange.getUpperBound());
        }

        return new DoubleBounds(range.getLowerBound(), range.getUpperBound());
    }


    /**
     * Creates a new instance of EnhancedLineAndShapeRenderer.
     *
     * @param plot The plot which is set for the new renderer.
     * @param idx This value is not used in the current implementation.
     *
     * @return a new instance of EnhancedLineAndShapeRenderer.
     */
    protected XYLineAndShapeRenderer createRenderer(XYPlot plot, int idx) {
        log.debug("Create EnhancedLineAndShapeRenderer for idx: " + idx);

        EnhancedLineAndShapeRenderer r =
            new EnhancedLineAndShapeRenderer(true, false);

        r.setPlot(plot);

        return r;
    }


    /**
     * Creates a new instance of <i>IdentifiableNumberAxis</i>.
     *
     * @param idx The index of the new axis.
     * @param label The label of the new axis.
     *
     * @return an instance of IdentifiableNumberAxis.
     */
    protected NumberAxis createNumberAxis(int idx, String label) {
        YAxisWalker walker = getYAxisWalker();

        return new IdentifiableNumberAxis(walker.getId(idx), label);
    }


    /**
     * Create Y (range) axis for given index.
     * Shall be overriden by subclasses.
     */
    protected NumberAxis createYAxis(int index) {
        YAxisWalker walker = getYAxisWalker();

        Font labelFont = new Font(
            DEFAULT_FONT_NAME,
            Font.BOLD,
            getYAxisFontSize(index));

        IdentifiableNumberAxis axis = new IdentifiableNumberAxis(
            walker.getId(index),
            getYAxisLabel(index));

        axis.setAutoRangeIncludesZero(false);
        axis.setLabelFont(labelFont);
        axis.setTickLabelFont(labelFont);

        return axis;
    }


    /**
     * Creates a new LegendItem with <i>name</i> and font provided by
     * <i>createLegendLabelFont()</i>.
     *
     * @param theme The theme of the chart line.
     * @param name The displayed name of the item.
     *
     * @return a new LegendItem instance.
     */
    public LegendItem createLegendItem(ThemeDocument theme, String name) {
        // OPTIMIZE Pass font, parsed Theme items.

        Color color = theme.parseLineColorField();
        if (color == null) {
            color = Color.BLACK;
        }

        LegendItem legendItem  = new LegendItem(name, color);

        legendItem.setLabelFont(createLegendLabelFont());
        return legendItem;
    }


    /**
     * Creates Font (Family and size) to use when creating Legend Items. The
     * font size depends in the return value of <i>getLegendFontSize()</i>.
     *
     * @return a new Font instance with <i>DEFAULT_FONT_NAME</i>.
     */
    protected Font createLegendLabelFont() {
        return new Font(
            DEFAULT_FONT_NAME,
            Font.PLAIN,
            getLegendFontSize()
        );
    }


    /**
     * Create new legend entries, dependent on settings.
     * @param plot The plot for which to modify the legend.
     */
    public void aggregateLegendEntries(XYPlot plot) {
        int AGGR_THRESHOLD = 0;

        if (getChartSettings() == null) {
            return;
        }
        Integer threshold = getChartSettings().getLegendSection()
            .getAggregationThreshold();

        AGGR_THRESHOLD = (threshold != null) ? threshold.intValue() : 0;

        LegendProcessor.aggregateLegendEntries(plot, AGGR_THRESHOLD);
    }


    /**
     * Returns a transparently textured paint.
     *
     * @return a transparently textured paint.
     */
    protected static Paint createTransparentPaint() {
        // TODO why not use a transparent color?
        BufferedImage texture = new BufferedImage(
            1, 1, BufferedImage.TYPE_4BYTE_ABGR);

        return new TexturePaint(
            texture, new Rectangle2D.Double(0d, 0d, 0d, 0d));
    }


    protected void preparePDFContext(CallContext context) {
        int[] dimension = getExportDimension();

        context.putContextValue("chart.width", dimension[0]);
        context.putContextValue("chart.height", dimension[1]);
        context.putContextValue("chart.marginLeft",   5f);
        context.putContextValue("chart.marginRight",  5f);
        context.putContextValue("chart.marginTop",    5f);
        context.putContextValue("chart.marginBottom", 5f);
        context.putContextValue(
            "chart.page.format",
            ChartExportHelper.DEFAULT_PAGE_SIZE);
    }


    protected void prepareSVGContext(CallContext context) {
        int[] dimension = getExportDimension();

        context.putContextValue("chart.width", dimension[0]);
        context.putContextValue("chart.height", dimension[1]);
        context.putContextValue(
            "chart.encoding",
            ChartExportHelper.DEFAULT_ENCODING);
    }

    /**
     * Retuns the call context. May be null if init hasn't been called yet.
     *
     * @return the CallContext instance
     */
    public CallContext getCallContext() {
        return context;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
