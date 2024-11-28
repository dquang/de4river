/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.awt.Color;
import java.awt.Font;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.OutputStream;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.exports.process.Processor;

import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.AnnotationHelper;
import org.dive4elements.river.jfree.AxisDataset;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.XYMetaSeriesCollection;

import org.dive4elements.river.themes.ThemeDocument;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYImageAnnotation;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import org.jfree.data.Range;

import org.jfree.data.general.Series;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.w3c.dom.Document;

import org.apache.commons.lang.StringUtils;


/**
 * The main diagram creation class.
 *
 * This class is the glue between output processors and facets.
 * The generator creates one diagram and calls the appropiate
 * processors for the state and
 *
 * With respect to datasets, ranges and axis, there are following requirements:
 * <ul>
 *   <li> First in, first drawn: "Early" datasets should be of lower Z-Oder
 *        than later ones (only works per-axis). </li>
 *   <li> Visible axis should initially show the range of all datasets that
 *        show data for this axis (even invisible ones). Motivation: Once
 *        a dataset (theme) has been activated, it should be on screen. </li>
 *   <li> There should always be a Y-Axis on the "left". </li>
 * </ul>
 */
public class DiagramGenerator extends ChartGenerator2 {

    public static final int AXIS_SPACE = 5;

    /** The log that is used in this generator. */
    private static Logger log = LogManager.getLogger(DiagramGenerator.class);

    protected List<Marker> domainMarkers = new ArrayList<Marker>();

    protected List<Marker> valueMarkers = new ArrayList<Marker>();

    /** The max X range to include all X values of all series for each axis. */
    protected Map<Integer, Bounds> xBounds;

    /** The max Y range to include all Y values of all series for each axis. */
    protected Map<Integer, Bounds> yBounds;

    /** Whether or not the plot is inverted (left-right). */
    private boolean inverted;

    private static final Pattern UNIT_PATTERN =
        Pattern.compile("\\s*\\[[\\w\\s\\+\\-]*\\]\\s*");

    protected Map<Integer, LinkedHashSet<String>> axesLabels;

    protected DiagramAttributes.Instance diagramAttributes;

    protected HashSet<String> subTitleParts;

    public DiagramGenerator() {
        super();

        axesLabels = new HashMap<Integer, LinkedHashSet<String>>();
        xBounds  = new HashMap<Integer, Bounds>();
        yBounds  = new HashMap<Integer, Bounds>();
        subTitleParts = new LinkedHashSet<String>();
    }

    @Override
    public void setup(Object config) {

        if (!(config instanceof DiagramAttributes)) {
            log.error("invalid config type");
            return;
        }
        DiagramAttributes da = (DiagramAttributes)config;
        diagramAttributes = da.new Instance();
    }

    @Override
    public void init(
        String       outName,
        Document     request,
        OutputStream out,
        CallContext  context
    ) {
        super.init(outName, request, out, context);
    }

    private void setInvertedFromConfig() {
        DiagramAttributes.DomainAxisAttributes dx =
            diagramAttributes.getDomainAxis();

        if (dx != null) {
            inverted = (Boolean)dx.isInverted()
                .evaluate((D4EArtifact)getMaster(), context);
            log.debug("setInvertedFromConfig: " + inverted);
        } else {
            log.debug("setInvertedFromConfig no domain axis found?");
        }
    }

    protected void postProcess() {
        return;
    }

    /**
     * Generate the chart anew (including localized axis and all).
     */
    @Override
    public JFreeChart generateChart() {
        log.debug("DiagramGenerator.generateChart");

        postProcess();

        JFreeChart chart = ChartFactory.createXYLineChart(
            getChartTitle(),
            "",
            "",
            null,
            PlotOrientation.VERTICAL,
            isLegendVisible(),
            false,
            false);

        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis axis = createXAxis(getXAxisLabel());
        plot.setDomainAxis(axis);

        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        addSubtitles(chart);
        adjustPlot(plot);

        //debugAxis(plot);

        addDatasets(plot);

        //debugDatasets(plot);

        addMarkers(plot);

        recoverEmptyPlot(plot);
        preparePointRanges(plot);

        //debugAxis(plot);

        localizeAxes(plot);

        setInvertedFromConfig();

        adjustAxes(plot);
        if (!(axis instanceof LogarithmicAxis)) {
            // XXX:
            // The auto zoom without a range tries
            // to include 0 in a logarithmic axis
            // which triggers a bug in jfreechart that causes
            // the values to be drawn carthesian
            autoZoom(plot);
        }

        //debugAxis(plot);

        // These have to go after the autozoom.
        AnnotationHelper.addAnnotationsToRenderer(annotations, plot,
                getChartSettings(), datasets);
        AnnotationHelper.addYAnnotationsToRenderer(yAnnotations, plot,
                getChartSettings(), datasets);

        // Add a logo (maybe).
        addLogo(plot);

        aggregateLegendEntries(plot);

        return chart;
    }

    public String getOutName() {
        return outName;
    }

    /**
     * Return left most data points x value (on first axis).
     */
    protected double getLeftX() {
        if (inverted) {
            return (Double)getXBounds(0).getUpper();
        }
        return (Double)getXBounds(0).getLower();
    }


    /**
     * Return right most data points x value (on first axis).
     */
    protected double getRightX() {
        if (inverted) {
            return (Double)getXBounds(0).getLower();
        }
        return (Double)getXBounds(0).getUpper();
    }


    /** Add a logo as background annotation to plot. */
    protected void addLogo(XYPlot plot) {
        String logo = showLogo();
        if (logo  == null) {
            log.debug("No logo to show chosen");
            return;
        }

        /*
         If you want to add images, remember to change code in these places:
         flys-artifacts:
         DiagramGenerator.java
         Timeseries*Generator.java and
         in the flys-client projects Chart*Propert*Editor.java.
         Also, these images have to be put in
         flys-artifacts/src/main/resources/images/
         flys-client/src/main/webapp/images/
         */
        java.net.URL imageURL;
        if (logo.equals("BfG")) {
            imageURL = DiagramGenerator.class.getResource(
                "/images/bfg_logo.png");
        } else {
            return;
        }
        ImageIcon imageIcon = new ImageIcon(imageURL);


        double xPos = 0d, yPos = 0d;

        String placeh = logoHPlace();
        String placev = logoVPlace();

        if (placev == null || placev.equals("none")) {
            placev = "top";
        }
        if (placev.equals("top")) {
            yPos = (Double)getYBounds(0).getUpper();
        }
        else if (placev.equals("bottom")) {
            yPos = (Double)getYBounds(0).getLower();
        }
        else if (placev.equals("center")) {
            yPos = ((Double)getYBounds(0).getUpper()
                + (Double)getYBounds(0).getLower())/2d;
        }
        else {
            log.debug("Unknown place-v value: " + placev);
        }

        if (placeh == null || placeh.equals("none")) {
            placeh = "center";
        }
        if (placeh.equals("left")) {
            xPos = getLeftX();
        }
        else if (placeh.equals("right")) {
            xPos = getRightX();
        }
        else if (placeh.equals("center")) {
            xPos = ((Double)getXBounds(0).getUpper()
                + (Double)getXBounds(0).getLower())/2d;
        }
        else {
            log.debug("Unknown place-h value: " + placeh);
        }

        log.debug("logo position: " + xPos + "/" + yPos);

        org.jfree.ui.RectangleAnchor anchor
            = org.jfree.ui.RectangleAnchor.TOP;
        if (placev.equals("top")) {
            if (placeh.equals("left")) {
                anchor = org.jfree.ui.RectangleAnchor.TOP_LEFT;
            }
            else if (placeh.equals("right")) {
                anchor = org.jfree.ui.RectangleAnchor.TOP_RIGHT;
            }
            else if (placeh.equals("center")) {
                anchor = org.jfree.ui.RectangleAnchor.TOP;
            }
        }
        else if (placev.equals("bottom")) {
            if (placeh.equals("left")) {
                anchor = org.jfree.ui.RectangleAnchor.BOTTOM_LEFT;
            }
            else if (placeh.equals("right")) {
                anchor = org.jfree.ui.RectangleAnchor.BOTTOM_RIGHT;
            }
            else if (placeh.equals("center")) {
                anchor = org.jfree.ui.RectangleAnchor.BOTTOM;
            }
        }
        else if (placev.equals("center")) {
            if (placeh.equals("left")) {
                anchor = org.jfree.ui.RectangleAnchor.LEFT;
            }
            else if (placeh.equals("right")) {
                anchor = org.jfree.ui.RectangleAnchor.RIGHT;
            }
            else if (placeh.equals("center")) {
                anchor = org.jfree.ui.RectangleAnchor.CENTER;
            }
        }

        XYAnnotation xyannotation =
            new XYImageAnnotation(xPos, yPos, imageIcon.getImage(), anchor);
        plot.getRenderer().addAnnotation(
            xyannotation, org.jfree.ui.Layer.BACKGROUND);
    }


    protected NumberAxis createXAxis(String label) {
        boolean logarithmic = (Boolean)diagramAttributes.getDomainAxis().
            isLog().evaluate((D4EArtifact)getMaster(), context);

        if (logarithmic) {
            return new LogarithmicAxis(label);
        }
        return new NumberAxis(label);
    }


    @Override
    protected Series getSeriesOf(XYDataset dataset, int idx) {
        return ((XYSeriesCollection) dataset).getSeries(idx);
    }


    @Override
    protected AxisDataset createAxisDataset(int idx) {
        log.debug("Create new AxisDataset for index: " + idx);
        return new AxisDataset(idx);
    }


    /**
     * Put debug output about datasets.
     */
    public void debugDatasets(XYPlot plot) {
        log.debug("Number of datasets: " + plot.getDatasetCount());
        for (int i = 0, P = plot.getDatasetCount(); i < P; i++) {
            if (plot.getDataset(i) == null) {
                log.debug("Dataset #" + i + " is null");
                continue;
            }
            log.debug("Dataset #" + i + ":" + plot.getDataset(i));
            XYSeriesCollection series = (XYSeriesCollection) plot.getDataset(i);
            log.debug("X-Extend of Dataset: " + series.getSeries(0).getMinX()
                    + " " + series.getSeries(0).getMaxX());
            log.debug("Y-Extend of Dataset: " + series.getSeries(0).getMinY()
                    + " " + series.getSeries(0).getMaxY());
        }
    }


    /**
     * Put debug output about axes.
     */
    public void debugAxis(XYPlot plot) {
        log.debug("...............");
        for (int i = 0, P =  plot.getRangeAxisCount(); i < P; i++) {
            if (plot.getRangeAxis(i) == null)
                log.debug("Range-Axis #" + i + " == null");
            else {
                log.debug("Range-Axis " + i + " != null [" +
                    plot.getRangeAxis(i).getRange().getLowerBound() +
                    "  " + plot.getRangeAxis(i).getRange().getUpperBound() +
                    "]");
            }
        }
        for (int i = 0, P =  plot.getDomainAxisCount(); i < P; i++) {
            if (plot.getDomainAxis(i) == null)
                log.debug("Domain-Axis #" + i + " == null");
            else {
                log.debug("Domain-Axis " + i + " != null [" +
                    plot.getDomainAxis(i).getRange().getLowerBound() +
                    "  " + plot.getDomainAxis(i).getRange().getUpperBound() +
                    "]");
            }
        }
        log.debug("...............");
    }

    /**
     * Registers an area to be drawn.
     * @param area Area to be drawn.
     * @param axisName Name of the axis.
     * @param visible Whether or not to be visible
     *                (important for range calculations).
     */
    public void addAreaSeries(
        StyledAreaSeriesCollection area,
        String axisName,
        boolean visible
    ) {
        addAreaSeries(area, diagramAttributes.getAxisIndex(axisName), visible);
    }

    /**
     * Registers an area to be drawn.
     * @param area Area to be drawn.
     * @param index 'axis index'
     * @param visible Whether or not to be visible
     *                (important for range calculations).
     */
    public void addAreaSeries(
        StyledAreaSeriesCollection area,
        int index,
        boolean visible
    ) {
        if (area == null) {
            log.warn("Cannot yet render above/under curve.");
            return;
        }

        AxisDataset axisDataset = (AxisDataset) getAxisDataset(index);

        if (visible) {
            axisDataset.addArea(area);
        }
        else {
            /* No range merging, for areas extending to infinity this
             * causes problems. */
        }
    }

    /**
     * Add given series if visible, if not visible adjust ranges (such that
     * all points in data would be plotted once visible).
     * @param series   the data series to include in plot.
     * @param index    index of the axis.
     * @param visible  whether or not the data should be plotted.
     */
    public void addAxisSeries(XYSeries series, int index, boolean visible) {
        if (series == null) {
            return;
        }

        log.debug("Y Range of XYSeries: " +
            series.getMinY() + " | " + series.getMaxY());

        addAxisDataset(new XYMetaSeriesCollection(series), index, visible);
    }

    /**
     * Add given series if visible, if not visible adjust ranges (such that
     * all points in data would be plotted once visible).
     * @param series   the data series to include in plot.
     * @param axisName name of the axis.
     * @param visible  whether or not the data should be plotted.
     */
    public void addAxisSeries(
        XYSeries series,
        String axisName,
        boolean visible
    ) {
        addAxisSeries(
            series, diagramAttributes.getAxisIndex(axisName), visible);
    }

    public void addAxisDataset(
        XYDataset dataset,
        String axisName,
        boolean visible
    ) {
        addAxisDataset(
            dataset, diagramAttributes.getAxisIndex(axisName), visible);
    }

    /**
     * Add the given vertical marker to the chart.
     */
    public void addDomainMarker(Marker marker) {
        addDomainMarker(marker, true);
    }


    /**
     * Add the given vertical marker to the chart.<b>Note:</b> the marker is
     * added to the chart only if it is not null and if <i>visible</i> is true.
     * @param marker The marker that should be added to the chart.
     * @param visible The visibility of the marker.
     */
    public void addDomainMarker(Marker marker, boolean visible) {
        if (visible && marker != null) {
            domainMarkers.add(marker);
        }
    }


    /**
     * Add the given vertical marker to the chart.
     */
    public void addValueMarker(Marker marker) {
        addValueMarker(marker, true);
    }


    /**
     * Add the given horizontal marker to the chart.<b>Note:</b> the marker is
     * added to the chart only if it is not null and if <i>visible</i> is true.
     * @param marker The marker that should be added to the chart.
     * @param visible The visibility of the marker.
     */
    public void addValueMarker(Marker marker, boolean visible) {
        if (visible && marker != null) {
            valueMarkers.add(marker);
        }
    }


    protected void addMarkers(XYPlot plot) {
        for(Marker marker : domainMarkers) {
            plot.addDomainMarker(marker);
        }
        for(Marker marker : valueMarkers) {
            plot.addRangeMarker(marker);
        }
    }


    public void addYAnnotation(
        RiverAnnotation annotation,
        String axisName
    ) {
        addYAnnotation(annotation, diagramAttributes.getAxisIndex(axisName));
    }


    /**
     * Effect: extend range of x axis to include given limits.
     *
     * @param bounds the given ("minimal") bounds.
     * @param index index of axis to be merged.
     */
    @Override
    protected void combineXBounds(Bounds bounds, int index) {
        if (!(bounds instanceof DoubleBounds)) {
            log.warn("Unsupported Bounds type: " + bounds.getClass());
            return;
        }

        DoubleBounds dBounds = (DoubleBounds) bounds;

        if (dBounds == null
            || Double.isNaN((Double) dBounds.getLower())
            || Double.isNaN((Double) dBounds.getUpper())) {
            return;
        }

        Bounds old = getXBounds(index);

        if (old != null) {
            dBounds = (DoubleBounds) dBounds.combine(old);
        }

        setXBounds(index, dBounds);
    }


    @Override
    protected void combineYBounds(Bounds bounds, int index) {
        if (!(bounds instanceof DoubleBounds)) {
            log.warn("Unsupported Bounds type: " + bounds.getClass());
            return;
        }

        DoubleBounds dBounds = (DoubleBounds) bounds;

        if (dBounds == null
            || Double.isNaN((Double) dBounds.getLower())
            || Double.isNaN((Double) dBounds.getUpper())) {
            return;
        }

        Bounds old = getYBounds(index);

        if (old != null) {
            dBounds = (DoubleBounds) dBounds.combine(old);
        }

        setYBounds(index, dBounds);
    }


    /**
     * If no data is visible, draw at least empty axis.
     */
    private void recoverEmptyPlot(XYPlot plot) {
        if (plot.getRangeAxis() == null) {
            log.debug("debug: No range axis");
            plot.setRangeAxis(createYAxis(0));
        }
    }


    /**
     * Expands X axes if only a point is shown.
     */
    private void preparePointRanges(XYPlot plot) {
        for (int i = 0, num = plot.getDomainAxisCount(); i < num; i++) {

            Integer key = Integer.valueOf(i);
            Bounds  b   = getXBounds(key);


            if (b != null && b.getLower().equals(b.getUpper())) {
                log.debug("Check whether to expand a x axis.i ("
                    + b.getLower() + "-" + b.getUpper() + ")");
                setXBounds(key, ChartHelper.expandBounds(b, 5));
            }
        }
    }


    /**
     * This method zooms the plot to the specified ranges in the attribute
     * document or to the ranges specified by the min/max values in the
     * datasets. <b>Note:</b> We determine the range manually if no zoom ranges
     * are given, because JFreeCharts auto-zoom adds a margin to the left and
     * right of the data area.
     *
     * @param plot The XYPlot.
     */
    protected void autoZoom(XYPlot plot) {
        log.debug("Zoom to specified ranges.");

        Range xrange = getDomainAxisRange();
        Range yrange = getValueAxisRange();

        ValueAxis xAxis = plot.getDomainAxis();

        Range fixedXRange = getRangeForAxisFromSettings("X");
        if (fixedXRange != null) {
            xAxis.setRange(fixedXRange);
        }
        else {
            zoom(plot, xAxis, getXBounds(0), xrange);
        }

        for (int i = 0, num = plot.getRangeAxisCount(); i < num; i++) {
            ValueAxis yaxis = plot.getRangeAxis(i);

            if (yaxis instanceof IdentifiableNumberAxis) {
                IdentifiableNumberAxis idAxis = (IdentifiableNumberAxis) yaxis;

                Range fixedRange = getRangeForAxisFromSettings(idAxis.getId());
                if (fixedRange != null) {
                    yaxis.setRange(fixedRange);
                    continue;
                }
            }

            if (yaxis == null) {
                log.debug("Zoom problem: no Y Axis for index: " + i);
                continue;
            }

            log.debug("Prepare zoom settings for y axis at index: " + i);
            zoom(plot, yaxis, getYBounds(Integer.valueOf(i)), yrange);
        }
    }


    protected Range getDomainAxisRange() {
        String[] ranges = getDomainAxisRangeFromRequest();

        if (ranges == null || ranges.length < 2) {
            log.debug("No zoom range for domain axis specified.");
            return null;
        }

        if (ranges[0].length() > 0 && ranges[1].length() > 0) {
            try {
                double from = Double.parseDouble(ranges[0]);
                double to   = Double.parseDouble(ranges[1]);

                if (from == 0 && to == 0) {
                    log.debug("No range specified. Lower and upper X == 0");
                    return null;
                }

                if (from > to) {
                    double tmp = to;
                    to         = from;
                    from       = tmp;
                }

                return new Range(from, to);
            }
            catch (NumberFormatException nfe) {
                log.warn("Wrong values for domain axis range.");
            }
        }

        return null;
    }


    protected Range getValueAxisRange() {
        String[] ranges = getValueAxisRangeFromRequest();

        if (ranges == null || ranges.length < 2) {
            log.debug("No range specified. Lower and upper Y == 0");
            return null;
        }

        if (ranges[0].length() > 0 && ranges[1].length() > 0) {
            try {
                double from = Double.parseDouble(ranges[0]);
                double to   = Double.parseDouble(ranges[1]);

                if (from == 0 && to == 0) {
                    log.debug("No range specified. Lower and upper Y == 0");
                    return null;
                }

                return from > to
                       ? new Range(to, from)
                       : new Range(from, to);
            }
            catch (NumberFormatException nfe) {
                log.warn("Wrong values for value axis range.");
            }
        }

        return null;
    }


    /**
     * Zooms the axis to the range specified in the attribute document.
     *
     * @param plot   The XYPlot.
     * @param axis   The axis that should be modified.
     * @param bounds The whole range specified by a dataset.
     * @param x      A user defined range (null permitted).
     *
     * @return true, if a zoom range was specified, otherwise false.
     */
    protected boolean zoom(
        XYPlot plot,
        ValueAxis axis,
        Bounds bounds,
        Range x
    ) {
        if (bounds == null) {
            return false;
        }

        if (x != null) {
            Bounds computed = calculateZoom(bounds, x);
            computed.applyBounds(axis, AXIS_SPACE);

            log.debug("Zoom axis to: " + computed);

            return true;
        }

        bounds.applyBounds(axis, AXIS_SPACE);
        return false;
    }

    /**
     * Calculates the start and end km for zoomed charts.
     * @param bounds    The given total bounds (unzoomed).
     * @param range     The range specifying the zoom.
     *
     * @return The start and end km for the zoomed chart.
     */
    protected Bounds calculateZoom(Bounds bounds, Range range) {
        double min  = bounds.getLower().doubleValue();
        double max  = bounds.getUpper().doubleValue();

        log.debug("Minimum is: " + min);
        log.debug("Maximum is: " + max);
        log.debug("Lower zoom is: " + range.getLowerBound());
        log.debug("Upper zoom is: " + range.getUpperBound());

        double diff = max > min ? max - min : min - max;

        DoubleBounds computed = new DoubleBounds(
            min + range.getLowerBound() * diff,
            min + range.getUpperBound() * diff);
        return computed;
    }

    /**
     * Extract the minimum and maximum values for x and y axes
     * which are stored in <i>xRanges</i> and <i>yRanges</i>.
     *
     * @param index The index of the y-Axis.
     *
     * @return a Range[] as follows: [x-Range, y-Range].
     */
    @Override
    public Range[] getRangesForAxis(int index) {
        log.debug("getRangesForAxis " + index);

        Bounds rx = getXBounds(Integer.valueOf(0));
        Bounds ry = getYBounds(Integer.valueOf(index));

        if (rx == null) {
            log.warn("Range for x axis not set." +
                        " Using default values: 0 - 1.");
            rx = new DoubleBounds(0, 1);
        }
        if (ry == null) {
            log.warn("Range for y" + index +
                        " axis not set. Using default values: 0 - 1.");
            ry = new DoubleBounds(0, 1);
        }

        return new Range[] {
            new Range(rx.getLower().doubleValue(), rx.getUpper().doubleValue()),
            new Range(ry.getLower().doubleValue(), ry.getUpper().doubleValue())
        };
    }


    /** Get X (usually horizontal) extent for given axis. */
    @Override
    public Bounds getXBounds(int axis) {
        return xBounds.get(axis);
    }


    /** Set X (usually horizontal) extent for given axis. */
    @Override
    protected void setXBounds(int axis, Bounds bounds) {
        if (bounds.getLower() == bounds.getUpper()) {
            xBounds.put(axis, ChartHelper.expandBounds(bounds, 5d));
        }
        else {
            xBounds.put(axis, bounds);
        }
    }


    /** Get Y (usually vertical) extent for given axis. */
    @Override
    public Bounds getYBounds(int axis) {
        return yBounds.get(axis);
    }


    /** Set Y (usually vertical) extent for given axis. */
    @Override
    protected void setYBounds(int axis, Bounds bounds) {
        yBounds.put(axis, bounds);
    }


    /**
     * Adjusts the axes of a plot. This method sets the <i>labelFont</i> of the
     * X axis and applies the inversion if inverted is true.
     *
     * (Duplicate in TimeseriesChartGenerator)
     *
     * @param plot The XYPlot of the chart.
     */
    protected void adjustAxes(XYPlot plot) {
        ValueAxis xaxis = plot.getDomainAxis();

        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return;
        }

        Font labelFont = new Font(
            DEFAULT_FONT_NAME,
            Font.BOLD,
            getXAxisLabelFontSize());

        xaxis.setLabelFont(labelFont);
        xaxis.setTickLabelFont(labelFont);

        log.debug("Adjusting xAxis. Inverted?: " + inverted);
        if (inverted) {
            xaxis.setInverted(true);
        }
    }


    /**
     * This method walks over all axes (domain and range) of <i>plot</i> and
     * calls localizeDomainAxis() for domain axes or localizeRangeAxis() for
     * range axes.
     *
     * @param plot The XYPlot.
     */
    private void localizeAxes(XYPlot plot) {
        for (int i = 0, num = plot.getDomainAxisCount(); i < num; i++) {
            ValueAxis axis = plot.getDomainAxis(i);

            if (axis != null) {
                localizeDomainAxis(axis);
            }
            else {
                log.warn("Domain axis at " + i + " is null.");
            }
        }

        for (int i = 0, num = plot.getRangeAxisCount(); i < num; i++) {
            ValueAxis axis = plot.getRangeAxis(i);

            if (axis != null) {
                localizeRangeAxis(axis);
            }
            else {
                log.warn("Range axis at " + i + " is null.");
            }
        }
    }


    /**
     * Overrides the NumberFormat with the NumberFormat for the current locale
     * that is provided by getLocale().
     *
     * @param domainAxis The domain axis that needs localization.
     */
    protected void localizeDomainAxis(ValueAxis domainAxis) {
        NumberFormat nf = NumberFormat.getInstance(getLocale());
        ((NumberAxis) domainAxis).setNumberFormatOverride(nf);
    }


    /**
     * Overrides the NumberFormat with the NumberFormat for the current locale
     * that is provided by getLocale().
     *
     * @param rangeAxis The domain axis that needs localization.
     */
    protected void localizeRangeAxis(ValueAxis rangeAxis) {
        NumberFormat nf = NumberFormat.getInstance(getLocale());
        ((NumberAxis) rangeAxis).setNumberFormatOverride(nf);
    }


    /**
     * Create a hash from a legenditem.
     * This hash can then be used to merge legend items labels.
     * @return hash for given legenditem to identify mergeables.
     */
    public static String legendItemHash(LegendItem li) {
        // TODO Do proper implementation.
        // Ensure that only mergable sets are created.
        // getFillPaint()
        // getFillPaintTransformer()
        // getLabel()
        // getLine()
        // getLinePaint()
        // getLineStroke()
        // getOutlinePaint()
        // getOutlineStroke()
        // Shape getShape()
        // String getToolTipText()
        // String getURLText()
        // boolean isLineVisible()
        // boolean isShapeFilled()
        // boolean isShapeOutlineVisible()
        // boolean isShapeVisible()
        String hash = li.getLinePaint().toString();
        String label = li.getLabel();
        if (label.startsWith("W (") || label.startsWith("W(")) {
            hash += "-W-";
        }
        else if (label.startsWith("Q(") || label.startsWith("Q (")) {
            hash += "-Q-";
        }

        // WQ.java holds example of using regex Matcher/Pattern.

        return hash;
    }

    /** True if x axis has been inverted. */
    public boolean isInverted() {
        return inverted;
    }


    /** Set to true if x axis should be inverted.
     * This can not be set to false afterwards. */
    public void setInverted(boolean value) {
        /* One request to invert dominates. */
        if (!inverted) {
            inverted = value;
        }
    }

    @Override
    public String getDefaultChartTitle() {
        DiagramAttributes.Title dTitle = diagramAttributes.getTitle();
        if (dTitle == null) {
            return "Title not configured in conf.xml";
        }

        return dTitle.evaluate((D4EArtifact)getMaster(), context);
    }

    @Override
    public String getDefaultChartSubtitle() {
        String parts = "";
        DiagramAttributes.Title dTitle = diagramAttributes.getSubtitle();
        if (dTitle == null &&
            (subTitleParts == null || subTitleParts.isEmpty())) {
            /* Subtitle is optional */
            return null;
        }
        if (subTitleParts != null && !subTitleParts.isEmpty()) {
            boolean first = true;
            if (dTitle != null) {
                first = false;
            }
             for (String p : subTitleParts) {
                 if (!first) {
                     parts += ", ";
                 }
                 parts += p;
                 first = false;
             }
        }
        if (dTitle == null && parts.length() > 0) {
            return parts;
        }
        return dTitle.evaluate((D4EArtifact)getMaster(), context) + parts;
    }

    /**
     * Get internationalized label for the x axis.
     */
    @Override
    protected String getDefaultXAxisLabel() {
        DiagramAttributes.DomainAxisAttributes dx =
            diagramAttributes.getDomainAxis();

        if (dx != null) {
            DiagramAttributes.Title t = dx.getTitle();
            if (t != null) {
                return t.evaluate((D4EArtifact)getMaster(), context);
            }
        }
        return "Domain Axis Title not configured in conf.xml";
    }

    @Override
    protected String getDefaultYAxisLabel(String axisName) {
        Set labelSet = axesLabels.get(diagramAttributes.getAxisIndex(axisName));
        log.debug("Labels for axis: " + labelSet);
        if (labelSet != null && !labelSet.isEmpty()) {
            String label = StringUtils.join(labelSet, ", ");
            Matcher units = UNIT_PATTERN.matcher(label);
            if (units.find()) {
                String firstUnit = units.group();
                label = units.replaceAll("");
                label += firstUnit;
            }
            return label;
        }
        for (
            Processor pr: diagramAttributes.getProcessorsForAxisName(axisName)
        ) {
            String label = pr.getAxisLabel(this);
            if (label != null) {
                return label;
            }
        }
        return "No configured axis label";
    }


    /**
     * Creates a list of Section for the chart's Y axes.
     *
     * @return a list of Y axis sections.
     */
    protected List<AxisSection> buildYAxisSections() {
        List<AxisSection> axisSections = new ArrayList<AxisSection>();

        List<DiagramAttributes.AxisAttributes> axesAttrs =
            diagramAttributes.getAxesAttributes();

        for (int i = 0, n = axesAttrs.size(); i < n; i++) {
            AxisSection ySection = new AxisSection();
            String axisName = diagramAttributes.getAxisName(i);
            ySection.setIdentifier(axisName);
            ySection.setLabel(getYAxisLabel(axisName));
            ySection.setSuggestedLabel(getDefaultYAxisLabel(axisName));
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

    protected String getYAxisLabel(int index) {
        return getYAxisLabel(diagramAttributes.getAxisName(index));
    }

    /**
     * Returns the Y-Axis label of a chart at position <i>pos</i>.
     *
     * @return the Y-Axis label of a chart at position <i>0</i>.
     */
    protected String getYAxisLabel(String axisName) {
        ChartSettings chartSettings = getChartSettings();
        if (chartSettings == null) {
            return getDefaultYAxisLabel(axisName);
        }
        AxisSection as = chartSettings.getAxisSection(axisName);
        if (as != null) {
            String label = as.getLabel();
            if (label != null && !label.equals(as.getSuggestedLabel())) {
                // Only if the suggested label is not the current label
                // the user has modified the label. Otherwise lets
                // recalculate the label
                return label;
            }
        }

        return getDefaultYAxisLabel(axisName);
    }

    protected String axisIndexToName(int index) {
        return diagramAttributes.getAxisName(index);
    }

    /** Add the acutal data to the diagram according to the processors.
     * For every outable facets, this function is
     * called and handles the data accordingly. */
    @Override
    public void doOut(
        ArtifactAndFacet bundle,
        ThemeDocument    theme,
        boolean          visible
    ) {
        String facetName = bundle.getFacetName();
        Facet facet = bundle.getFacet();

        /* A conservative security check */
        if (facetName == null || facet == null) {
            /* Can't happen,.. */
            log.error("doOut called with null facet.");
            return;
        }

        log.debug("DoOut for facet: " + facetName);

        boolean found = false;
        List<Processor> prL = diagramAttributes.getProcessors();
        for (Processor pr: prL) {
            if (pr.canHandle(facetName)) {
                found = true;
                pr.doOut(this, bundle, theme, visible);

                if (visible) {
                    // Save the label that should be added for this processor
                    int axisIdx = diagramAttributes.getAxisIndex(
                        pr.getAxisName());
                    LinkedHashSet<String> curLabels = axesLabels.get(axisIdx);
                    if (curLabels == null) {
                        curLabels = new LinkedHashSet<String>(5);
                    }
                    curLabels.add(pr.getAxisLabel(this));
                    axesLabels.put(axisIdx, curLabels);
                }
            }
        }
        if (!found) {
            log.warn("No processor found for: " + facetName);
            if (log.isDebugEnabled()) {
                log.debug("Configured processors for this diagram are:");
                for (Processor pr: prL) {
                    log.debug(pr.getClass().getName());
                }
            }
        }
    }

    @Override
    protected NumberAxis createYAxis(int index) {
        NumberAxis axis;
        boolean logarithmic = (Boolean)diagramAttributes.getAxesAttributes().
            get(index).isLog().evaluate((D4EArtifact)getMaster(), context);

        if (logarithmic) {
            axis = new LogarithmicAxis(getYAxisLabel(index));
        } else {
            axis = super.createYAxis(index);
        }

        if (diagramAttributes.getAxesAttributes().get(index).includeZero()) {
            axis.setAutoRangeIncludesZero(true);
        }
        return axis;
    }

    /**
     * @return the subtitle parts
     */
    public HashSet<String> getSubTitleParts() {
        return subTitleParts;
    }

    /**
     * @param part the subtitle part to set
     */
    public void addSubtitle(String part) {
        this.subTitleParts.add(part);
    }
}
