/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
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
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONArray;
import org.json.JSONException;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.jfree.AxisDataset;
import org.dive4elements.river.jfree.AnnotationHelper;
import org.dive4elements.river.themes.ThemeDocument;


/**
 * An abstract base class for creating XY charts.
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
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class XYChartGenerator extends ChartGenerator {

    /** Enumerator over existing axes. */
    @Override
    protected abstract YAxisWalker getYAxisWalker();

    public static final int AXIS_SPACE = 5;

    /** The log that is used in this generator. */
    private static Logger log = LogManager.getLogger(XYChartGenerator.class);

    protected List<Marker> domainMarkers = new ArrayList<Marker>();

    protected List<Marker> valueMarkers = new ArrayList<Marker>();

    /** The max X range to include all X values of all series for each axis. */
    protected Map<Integer, Bounds> xBounds;

    /** The max Y range to include all Y values of all series for each axis. */
    protected Map<Integer, Bounds> yBounds;

    /** Whether or not the plot is inverted (left-right). */
    private boolean inverted;

    public XYChartGenerator() {
        super();

        xBounds  = new HashMap<Integer, Bounds>();
        yBounds  = new HashMap<Integer, Bounds>();
    }


    /**
     * Generate the chart anew (including localized axis and all).
     */
    @Override
    public JFreeChart generateChart() {
        log.debug("XYChartGenerator.generateChart");

        JFreeChart chart = ChartFactory.createXYLineChart(
            getChartTitle(),
            getXAxisLabel(),
            getYAxisLabel(0),
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
        addAnnotationsToRenderer(plot);

        // Add a logo (maybe).
        addLogo(plot);

        aggregateLegendEntries(plot);

        return chart;
    }


    /**
     * Return left most data points x value (on first axis).
     * Shortcut, especially to be overridden in (LS) charts where
     * axis could be inverted.
     */
    protected double getLeftX() {
        return (Double)getXBounds(0).getLower();
    }


    /**
     * Return right most data points x value (on first axis).
     * Shortcut, especially to be overridden in (LS) charts where
     * axis could be inverted.
     */
    protected double getRightX() {
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
         XYChartGenerator.java
         Timeseries*Generator.java and
         in the flys-client projects Chart*Propert*Editor.java.
         Also, these images have to be put in
         flys-artifacts/src/main/resources/images/
         flys-client/src/main/webapp/images/
         */
        java.net.URL imageURL;
        if (logo.equals("BfG")) {
            imageURL = XYChartGenerator.class.getResource(
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
     * @param series the data series to include in plot.
     * @param index  ('symbolic') index of the series and of its axis.
     * @param visible whether or not the data should be plotted.
     */
    public void addAxisSeries(XYSeries series, int index, boolean visible) {
        if (series == null) {
            return;
        }

        log.debug("Y Range of XYSeries: " +
            series.getMinY() + " | " + series.getMaxY());

        addAxisDataset(new XYSeriesCollection(series), index, visible);

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
            zoomX(plot, xAxis, getXBounds(0), xrange);
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
            zoomY(plot, yaxis, getYBounds(Integer.valueOf(i)), yrange);
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


    protected boolean zoomX(
        XYPlot plot,
        ValueAxis axis,
        Bounds bounds,
        Range x
    ) {
        return zoom(plot, axis, bounds, x);
    }


    protected boolean zoomY(
        XYPlot plot,
        ValueAxis axis,
        Bounds bounds,
        Range x
    ) {
        return zoom(plot, axis, bounds, x);
    }


    /**
     * Zooms the x axis to the range specified in the attribute document.
     *
     * @param plot  The XYPlot.
     * @param axis  The axis the shoud be modified.
     * @param bounds The whole range specified by a dataset.
     * @param x     A user defined range (null permitted).
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
     * X axis.
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
     * Do Points out.
     */
    protected void doPoints(
        Object     o,
        ArtifactAndFacet aandf,
        ThemeDocument theme,
        boolean    visible,
        int        axisIndex
    ) {
        String seriesName = aandf.getFacetDescription();
        XYSeries series = new StyledXYSeries(seriesName, theme);

        // Add text annotations for single points.
        List<XYTextAnnotation> xy = new ArrayList<XYTextAnnotation>();

        try {
            JSONArray points = new JSONArray((String) o);
            for (int i = 0, P = points.length(); i < P; i++) {
                JSONArray array = points.getJSONArray(i);
                double x    = array.getDouble(0);
                double y    = array.getDouble(1);
                String name = array.getString(2);
                boolean act = array.getBoolean(3);
                if (!act) {
                    continue;
                }
                //log.debug(" x " + x + " y " + y );
                series.add(x, y, false);
                xy.add(new CollisionFreeXYTextAnnotation(name, x, y));
            }
        }
        catch(JSONException e){
            log.error("Could not decode json.");
        }

        RiverAnnotation annotations =
            new RiverAnnotation(null, null, null, theme);
        annotations.setTextAnnotations(xy);

        // Do not generate second legend entry.
        // (null was passed for the aand before).
        doAnnotations(annotations, null, theme, visible);
        addAxisSeries(series, axisIndex, visible);
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


    /** Set to true if x axis has been inverted. */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    protected void addAnnotationsToRenderer(XYPlot plot) {
        AnnotationHelper.addAnnotationsToRenderer(annotations, plot,
                getChartSettings(), datasets);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
