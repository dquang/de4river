/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.AnnotationHelper;
import org.dive4elements.river.jfree.StyledTimeSeries;
import org.dive4elements.river.jfree.TimeBounds;
import org.dive4elements.river.jfree.AxisDataset;
import org.dive4elements.river.themes.ThemeDocument;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Generator for diagrams with time on x axis.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class TimeseriesChartGenerator
extends               ChartGenerator {

    protected List<Marker> domainMarker;

    protected List<Marker> valueMarker;

    protected Map<String, String> attributes;

    protected boolean domainZeroLineVisible;

    private static final Logger log =
        LogManager.getLogger(TimeseriesChartGenerator.class);

    public static final int AXIS_SPACE = 5;

    protected Map<Integer, Bounds> xBounds;

    protected Map<Integer, Bounds> yBounds;


    /**
     * The default constructor that initializes internal datastructures.
     */
    public TimeseriesChartGenerator() {
        super();

        xBounds = new HashMap<Integer, Bounds>();
        yBounds = new HashMap<Integer, Bounds>();
        domainMarker = new ArrayList<Marker>();
        valueMarker = new ArrayList<Marker>();
        attributes = new HashMap<String, String>();
    }



    @Override
    public JFreeChart generateChart() {
        log.info("Generate Timeseries Chart.");

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            getChartTitle(),
            getXAxisLabel(),
            getYAxisLabel(0),
            null,
            isLegendVisible(),
            false,
            false);

        XYPlot plot = (XYPlot) chart.getPlot();

        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);

        addSubtitles(chart);
        adjustPlot(plot);
        addDatasets(plot);
        adjustAxes(plot);
        addDomainAxisMarker(plot);
        addValueAxisMarker(plot);
        adaptZoom(plot);

        applySeriesAttributes(plot);

        consumeAxisSettings(plot);

        AnnotationHelper.addAnnotationsToRenderer(
            annotations,
            plot,
            getChartSettings(),
            datasets);
        addLogo(plot);
        aggregateLegendEntries(plot);
        return chart;
    }


    /**
     * Return left most data points x value (on first axis).
     * Shortcut, especially to be overridden in (LS) charts where
     * axis could be inverted.
     */
    protected long getLeftX() {
        return (Long)getXBounds(0).getLower();
    }


    /**
     * Return right most data points x value (on first axis).
     * Shortcut, especially to be overridden in (LS) charts where
     * axis could be inverted.
     */
    protected long getRightX() {
        return (Long)getXBounds(0).getUpper();
    }


    /**
     * Add a logo as background annotation to plot.
     * Copy from XYChartGenerator.
     */
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
            xPos = ((Long)getXBounds(0).getUpper()
                + (Long)getXBounds(0).getLower())/2d;
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

    /**
     * This method zooms the plot to the specified ranges in the attribute
     * document or to the ranges specified by the min/max values in the
     * datasets. <b>Note:</b> We determine the range manually if no zoom ranges
     * are given, because JFreeCharts auto-zoom adds a margin to the left and
     * right of the data area.
     *
     * Copy of implementation in XYChartGenerator.
     *
     * @param plot The XYPlot.
     */
    protected void consumeAxisSettings(XYPlot plot) {
        log.debug("Zoom to specified ranges.");

        Bounds xrange = getDomainAxisRange();
        Bounds yrange = getValueAxisRange();

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


    @Override
    protected Series getSeriesOf(XYDataset dataset, int idx) {
        return ((TimeSeriesCollection) dataset).getSeries(idx);
    }


    /**
     * This method creates new instances of AxisDataset.
     *
     * @param idx The symbol for the new AxisDataset.
     */
    @Override
    protected AxisDataset createAxisDataset(int idx) {
        log.debug("Create a new AxisDataset for index: " + idx);
        return new AxisDataset(idx);
    }


    @Override
    protected void combineXBounds(Bounds bounds, int index) {
        if (bounds != null) {
            Bounds old = getXBounds(index);

            if (old != null) {
                bounds = bounds.combine(old);
            }

            setXBounds(index, bounds);
        }
    }


    @Override
    protected void combineYBounds(Bounds bounds, int index) {
        if (bounds != null) {
            Bounds old = getYBounds(index);

            if (old != null) {
                bounds = bounds.combine(old);
            }

            setYBounds(index, bounds);
        }
    }


    // TODO REPLACE THIS METHOD WITH getBoundsForAxis(index)
    @Override
    public Range[] getRangesForAxis(int index) {
        // TODO
        Bounds[] bounds = getBoundsForAxis(index);

        return new Range[] {
            new Range(
                bounds[0].getLower().doubleValue(),
                bounds[0].getUpper().doubleValue()),
            new Range(
                bounds[1].getLower().doubleValue(),
                bounds[1].getUpper().doubleValue())
        };
    }


    @Override
    public Bounds getXBounds(int axis) {
        return xBounds.get(axis);
    }


    @Override
    protected void setXBounds(int axis, Bounds bounds) {
        xBounds.put(axis, bounds);
    }


    @Override
    public Bounds getYBounds(int axis) {
        return yBounds.get(axis);
    }


    @Override
    protected void setYBounds(int axis, Bounds bounds) {
        if (bounds != null) {
            yBounds.put(axis, bounds);
        }
    }


    public Bounds[] getBoundsForAxis(int index) {
        log.debug("Return x and y bounds for axis at: " + index);

        Bounds rx = getXBounds(Integer.valueOf(index));
        Bounds ry = getYBounds(Integer.valueOf(index));

        if (rx == null) {
            log.warn("Range for x axis not set." +
                        " Using default values: 0 - 1.");
            rx = new TimeBounds(0L, 1L);
        }

        if (ry == null) {
            log.warn("Range for y axis not set." +
                        " Using default values: 0 - 1.");
            ry = new DoubleBounds(0L, 1L);
        }

        log.debug("X Bounds at index " + index + " is: " + rx);
        log.debug("Y Bounds at index " + index + " is: " + ry);

        return new Bounds[] {rx, ry};
    }


    /** Get (zoom)values from request. */
    public Bounds getDomainAxisRange() {
        String[] ranges = getDomainAxisRangeFromRequest();

        if (ranges == null || ranges.length < 2) {
            log.debug("No zoom range for domain axis specified.");
            return null;
        }

        if (ranges[0] == null || ranges[1] == null) {
            log.warn("Invalid ranges for domain axis specified!");
            return null;
        }

        try {
            double lower = Double.parseDouble(ranges[0]);
            double upper = Double.parseDouble(ranges[1]);

            return new DoubleBounds(lower, upper);
        }
        catch (NumberFormatException nfe) {
            log.warn("Invalid ranges for domain axis specified: " + nfe);
        }

        return null;
    }


    public Bounds getValueAxisRange() {
        String[] ranges = getValueAxisRangeFromRequest();

        if (ranges == null || ranges.length < 2) {
            log.debug("No zoom range for domain axis specified.");
            return null;
        }

        if (ranges[0] == null || ranges[1] == null) {
            log.warn("Invalid ranges for domain axis specified!");
            return null;
        }

        try {
            double lower = Double.parseDouble(ranges[0]);
            double upper = Double.parseDouble(ranges[1]);

            return new DoubleBounds(lower, upper);
        }
        catch (NumberFormatException nfe) {
            log.warn("Invalid ranges for domain axis specified: " + nfe);
        }

        return null;
    }


    protected void adaptZoom(XYPlot plot) {
        log.debug("Adapt zoom of Timeseries chart.");

        zoomX(plot, plot.getDomainAxis(), getXBounds(0), getDomainAxisRange());

        Bounds valueAxisBounds = getValueAxisRange();

        for (int j = 0, n = plot.getRangeAxisCount(); j < n; j++) {
            zoomY(
                plot,
                plot.getRangeAxis(j),
                getYBounds(j),
                valueAxisBounds);
        }
    }


    /**
     * @param plot the plot.
     * @param axis the value (x, time) axis of which to set bounds.
     * @param total the current bounds (?).
     */
    protected void zoomX(
        XYPlot    plot,
        ValueAxis axis,
        Bounds    total,//we could equally nicely getXBounds(0)
        Bounds    user
    ) {
        if (log.isDebugEnabled()) {
            log.debug("== Zoom X axis ==");
            log.debug("    Total axis range  : " + total);
            log.debug("    User defined range: " + user);
        }

        if (user != null) {
            long min  = total.getLower().longValue();
            long max  = total.getUpper().longValue();
            long diff = max > min ? max - min : min - max;

            long newMin = Math.round(
                min + user.getLower().doubleValue() * diff);
            long newMax = Math.round(
                min + user.getUpper().doubleValue() * diff);

            TimeBounds newBounds = new TimeBounds(newMin, newMax);

            log.debug("    Zoom axis to: " + newBounds);

            newBounds.applyBounds(axis, AXIS_SPACE);
        }
        else {
            log.debug("No user specified zoom values found!");
            if (total != null && axis != null) {
                total.applyBounds(axis, AXIS_SPACE);
            }
        }
    }


    /**
     * @param user zoom values in percent.
     */
    protected void zoomY(
        XYPlot    plot,
        ValueAxis axis,
        Bounds    total,
        Bounds    user
    ) {
        if (log.isDebugEnabled()) {
            log.debug("== Zoom Y axis ==");
            log.debug("    Total axis range  : " + total);
            log.debug("    User defined range: " + user);
        }

        if (user != null) {
            double min  = total.getLower().doubleValue();
            double max  = total.getUpper().doubleValue();
            double diff = max > min ? max - min : min - max;

            double newMin = min + user.getLower().doubleValue() * diff;
            double newMax = min + user.getUpper().doubleValue() * diff;

            DoubleBounds newBounds = new DoubleBounds(newMin, newMax);

            log.debug("    Zoom axis to: " + newBounds);

            newBounds.applyBounds(axis, AXIS_SPACE);
        }
        else {
            log.debug("No user specified zoom values found!");
            if (total != null && axis != null) {
                total.applyBounds(axis, AXIS_SPACE);
            }
        }
    }


    /**
     * Adjusts the (look of) axes of a plot.
     * This method sets the <i>labelFont</i> of the
     * X axis.
     *
     * (Duplicate in XYChartGenerator).
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


    protected Date decodeXAxisValue(JSONArray array)
        throws JSONException, ParseException {
        try {
            double x = array.getDouble(0);
            long l = (new Double(x)).longValue();
            return new Date(l);
        }
        catch(JSONException ex) {
            String str = array.getString(0);
            DateFormat df = DateFormat.getDateInstance(
                    DateFormat.MEDIUM, Resources.getLocale(context.getMeta()));
            return df.parse(str);
        }
    }

    /**
     * Do Points out.
     */
    protected void doPoints(
        Object     o,
        ArtifactAndFacet aandf,
        ThemeDocument   theme,
        boolean    visible,
        int        axisIndex
    ) {
        String seriesName = aandf.getFacetDescription();
        TimeSeries series = new StyledTimeSeries(seriesName, theme);

        // Add text annotations for single points.
        List<XYTextAnnotation> xy = new ArrayList<XYTextAnnotation>();
        HashMap<FixedMillisecond, String> names =
            new HashMap<FixedMillisecond, String>();

        try {
            JSONArray points = new JSONArray((String) o);
            for (int i = 0, P = points.length(); i < P; i++) {
                JSONArray array = points.getJSONArray(i);

                double y    = array.getDouble(1);
                String name = array.getString(2);
                boolean act = array.getBoolean(3);
                if (!act) {
                    continue;
                }

                Date date = decodeXAxisValue(array);
                long ms = date.getTime();

                FixedMillisecond day = new FixedMillisecond(ms);
                while (names.containsKey(day)) {
                    day = new FixedMillisecond(++ms);
                }
                series.add(day, y, false);
                names.put(day, name);
            }
        }
        catch(JSONException ex) {
            log.error("Could not decode json");
        }
        catch(ParseException ex) {
            log.error("Could not parse date string");
        }

        TimeSeriesCollection tsc = new TimeSeriesCollection();
        tsc.addSeries(series);
        // Add Annotations.
        for (int i = 0, S = series.getItemCount(); i < S; i++) {
            double x = tsc.getXValue(0, i);
            double y = tsc.getYValue(0, i);
            xy.add(new CollisionFreeXYTextAnnotation(
                       names.get(series.getTimePeriod(i)), x, y));
            log.debug("doPoints(): x=" + x + " y=" + y);
        }
        RiverAnnotation annotations =
            new RiverAnnotation(null, null, null, theme);
        annotations.setTextAnnotations(xy);

        // Do not generate second legend entry.
        // (null was passed for the aand before).
        doAnnotations(annotations, null, theme, visible);

        addAxisDataset(tsc, axisIndex, visible);
    }

    public void addDomainAxisMarker(XYPlot plot) {
        log.debug("domainmarkers: " + domainMarker.size());
        for (Marker marker: domainMarker) {
            log.debug("adding domain marker");
            plot.addDomainMarker(marker, Layer.BACKGROUND);
        }
        domainMarker.clear();
    }

    public void addValueAxisMarker(XYPlot plot) {
        for (Marker marker: valueMarker) {
            log.debug("adding value marker..");
            plot.addRangeMarker(marker, Layer.BACKGROUND);
        }
        valueMarker.clear();
    }

    public void addAttribute(String seriesKey, String name) {
        attributes.put(seriesKey, name);
    }

    private LegendItem getLegendItemFor(XYPlot plot, String interSeriesKey) {
        LegendItemCollection litems = plot.getLegendItems();
        Iterator<LegendItem> iter = litems.iterator();
        while(iter.hasNext()) {
            LegendItem item = iter.next();
            if(interSeriesKey.startsWith(item.getSeriesKey().toString())) {
                return item;
            }
        }
        return null;
    }

    protected void applySeriesAttributes(XYPlot plot) {
        int count  = plot.getDatasetCount();
        for (int i = 0; i < count; i++) {
            XYDataset data = plot.getDataset(i);
            if (data == null) {
                continue;
            }

            int seriesCount = data.getSeriesCount();
            for (int j = 0; j < seriesCount; j++) {
                StyledTimeSeries series =
                    (StyledTimeSeries)getSeriesOf(data, j);
                String key = series.getKey().toString();

                if (attributes.containsKey(key)) {
                    // Interpolated points are drawn unfilled
                    if (attributes.get(key).equals("interpolate")) {
                        XYLineAndShapeRenderer renderer =
                                series.getStyle().getRenderer();
                        renderer.setSeriesPaint(
                            j,
                            renderer.getSeriesFillPaint(j));
                        renderer.setSeriesShapesFilled(j, false);

                        LegendItem legendItem = getLegendItemFor(plot, key);
                        if(legendItem != null) {
                            LegendItem interLegend = new LegendItem(
                                    legendItem.getLabel(),
                                    legendItem.getDescription(),
                                    legendItem.getToolTipText(),
                                    legendItem.getURLText(),
                                    legendItem.isShapeVisible(),
                                    legendItem.getShape(),
                                    false, // shapeFilled?
                                    legendItem.getFillPaint(),
                                    true,  // shapeOutlineVisible?
                                    renderer.getSeriesFillPaint(j),
                                    legendItem.getOutlineStroke(),
                                    legendItem.isLineVisible(),
                                    legendItem.getLine(),
                                    legendItem.getLineStroke(),
                                    legendItem.getLinePaint()
                                    );
                            interLegend.setSeriesKey(series.getKey());
                            log.debug("applySeriesAttributes: "
                                + "draw unfilled legend item");
                            plot.getLegendItems().add(interLegend);
                        }
                    }
                }

                if (attributes.containsKey(key)) {
                    if(attributes.get(key).equals("outline")) {
                        XYLineAndShapeRenderer renderer =
                            series.getStyle().getRenderer();
                        renderer.setSeriesPaint(
                            j,
                            renderer.getSeriesFillPaint(j));
                        renderer.setDrawOutlines(true);
                    }
                }
            }
        }
    }

    /** Two Ranges that span a rectangular area. */
    public static class Area {
        protected Range xRange;
        protected Range yRange;

        public Area(Range rangeX, Range rangeY) {
            this.xRange = rangeX;
            this.yRange = rangeY;
        }

        public Area(ValueAxis axisX, ValueAxis axisY) {
            this.xRange = axisX.getRange();
            this.yRange = axisY.getRange();
        }

        public double ofLeft(double percent) {
            return xRange.getLowerBound()
                + xRange.getLength() * percent;
        }

        public double ofRight(double percent) {
            return xRange.getUpperBound()
                - xRange.getLength() * percent;
        }

        public double ofGround(double percent) {
            return yRange.getLowerBound()
                + yRange.getLength() * percent;
        }

        public double atTop() {
            return yRange.getUpperBound();
        }

        public double atGround() {
            return yRange.getLowerBound();
        }

        public double atRight() {
            return xRange.getUpperBound();
        }

        public double atLeft() {
            return xRange.getLowerBound();
        }

        public double above(double percent, double base) {
            return base + yRange.getLength() * percent;
        }
    }

    /* Create an axis section with setUpperTimeRange and
     * setLowerTimeRange */
    @Override
    protected List<AxisSection> buildXAxisSections() {
        List<AxisSection> axisSections = new ArrayList<AxisSection>();

        String identifier = "X";

        AxisSection axisSection = new AxisSection();
        axisSection.setIdentifier(identifier);
        axisSection.setLabel(getXAxisLabel());
        axisSection.setFontSize(14);
        axisSection.setFixed(false);

        long rightX = xBounds.isEmpty() ? 0 : getRightX();
        long leftX = xBounds.isEmpty() ? 0 : getLeftX();
        axisSection.setUpperTimeRange(rightX);
        axisSection.setLowerTimeRange(leftX);

        axisSections.add(axisSection);

        return axisSections;
    }


}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
