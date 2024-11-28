/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.dive4elements.artifactdatabase.DefaultService;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.Service;

import org.dive4elements.river.artifacts.model.FixingsColumn;
import org.dive4elements.river.artifacts.model.FixingsColumnFactory;
import org.dive4elements.river.artifacts.model.FixingsFilterBuilder;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing;

import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.FixingsOverviewFactory;
import org.dive4elements.river.artifacts.model.GaugeFinder;
import org.dive4elements.river.artifacts.model.GaugeFinderFactory;
import org.dive4elements.river.artifacts.model.GaugeRange;

import org.dive4elements.river.artifacts.model.fixings.QWI;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.backend.SessionHolder;

import org.dive4elements.river.jfree.ShapeRenderer;

import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.Pair;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Transparency;

import java.awt.geom.Rectangle2D;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;

import org.jfree.chart.axis.NumberAxis;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import org.jfree.data.Range;

import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/** Serve chart of Fixings at certain km. */
public class FixingsKMChartService
extends      DefaultService
{
    private static final Logger log =
        LogManager.getLogger(FixingsKMChartService.class);

    public static final int DEFAULT_WIDTH  = 240;
    public static final int DEFAULT_HEIGHT = 180;

    public static final String [] I18N_Q_SECTOR_BOARDERS = {
        "fix.km.chart.q.sector.border0",
        "fix.km.chart.q.sector.border1",
        "fix.km.chart.q.sector.border2"
    };

    public static final String [] DEFAULT_Q_SECTOR_BORDERS = {
        "(MNQ + MQ)/2",
        "(MQ + MHQ)/2",
        "HQ5"
    };

    public static final String I18N_CHART_LABEL_DATE =
        "fix.km.chart.label.date";

    public static final String DEFAULT_CHART_LABEL_DATE =
        "yyyy/MM/dd";

    public static final String I18N_CHART_TITLE =
        "fix.km.chart.title";

    public static final String DEFAULT_CHART_TITLE =
        "Fixings {0} km {1,number,#.###}";

    public static final String I18N_Q_AXIS =
        "fix.km.chart.q.axis";

    public static final String DEFAULT_Q_AXIS =
        "Q [m\u00b3/s]";

    public static final String I18N_W_AXIS =
        "fix.km.chart.w.axis";

    public static final String DEFAULT_W_AXIS =
        "W [NN + m]";

    public static final String I18N_MEASURED =
        "fix.km.chart.measured";

    public static final String DEFAULT_MEASURED =
        "measured";

    public static final String I18N_INTERPOLATED =
        "fix.km.chart.interpolated";

    public static final String DEFAULT_INTERPOLATED =
        "interpolated";

    public static final String DEFAULT_FORMAT = "png";

    // TODO: Load fancy image from resources.
    public static final byte [] EMPTY = {
        (byte)0x89, (byte)0x50, (byte)0x4e, (byte)0x47,
        (byte)0x0d, (byte)0x0a, (byte)0x1a, (byte)0x0a,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0d,
        (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
        (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x3a, (byte)0x7e, (byte)0x9b,
        (byte)0x55, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x01, (byte)0x73, (byte)0x52, (byte)0x47,
        (byte)0x42, (byte)0x00, (byte)0xae, (byte)0xce,
        (byte)0x1c, (byte)0xe9, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x09, (byte)0x70, (byte)0x48,
        (byte)0x59, (byte)0x73, (byte)0x00, (byte)0x00,
        (byte)0x0b, (byte)0x13, (byte)0x00, (byte)0x00,
        (byte)0x0b, (byte)0x13, (byte)0x01, (byte)0x00,
        (byte)0x9a, (byte)0x9c, (byte)0x18, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x07, (byte)0x74,
        (byte)0x49, (byte)0x4d, (byte)0x45, (byte)0x07,
        (byte)0xdc, (byte)0x04, (byte)0x04, (byte)0x10,
        (byte)0x30, (byte)0x15, (byte)0x7d, (byte)0x77,
        (byte)0x36, (byte)0x0b, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x08, (byte)0x74, (byte)0x45,
        (byte)0x58, (byte)0x74, (byte)0x43, (byte)0x6f,
        (byte)0x6d, (byte)0x6d, (byte)0x65, (byte)0x6e,
        (byte)0x74, (byte)0x00, (byte)0xf6, (byte)0xcc,
        (byte)0x96, (byte)0xbf, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x0a, (byte)0x49, (byte)0x44,
        (byte)0x41, (byte)0x54, (byte)0x08, (byte)0xd7,
        (byte)0x63, (byte)0xf8, (byte)0x0f, (byte)0x00,
        (byte)0x01, (byte)0x01, (byte)0x01, (byte)0x00,
        (byte)0x1b, (byte)0xb6, (byte)0xee, (byte)0x56,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x49, (byte)0x45, (byte)0x4e, (byte)0x44,
        (byte)0xae, (byte)0x42, (byte)0x60, (byte)0x82
    };

    private static final Output empty() {
        return new Output(EMPTY, "image/png");
    }

    @Override
    public Service.Output process(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        log.debug("FixingsKMChartService.process");

        SessionHolder.acquire();
        try {
            return doProcess(data, globalContext, callMeta);
        }
        finally {
            SessionHolder.release();
        }
    }

    protected Service.Output doProcess(
        Document      input,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        String    river  = getRiverName(input);
        Double    km     = getKM(input);
        Dimension extent = getExtent(input);
        String    format = getFormat(input);

        if (river == null || km == null) {
            log.warn("River and/or km invalid.");
            return empty();
        }

        FixingsOverview overview = FixingsOverviewFactory.getOverview(river);

        if (overview == null) {
            log.warn("No overview found for river '" + river + "'");
            return empty();
        }

        FixingsFilterBuilder ffb = new FixingsFilterBuilder(input);

        List<Fixing.Column> columns = overview.filter(
            ffb.getRange(),
            ffb.getFilter());

        List<Pair<Fixing.Column, FixingsColumn>> cols =
            new ArrayList<Pair<Fixing.Column, FixingsColumn>>();

        for (Fixing.Column col: columns) {
            FixingsColumn data =
                FixingsColumnFactory.INSTANCE.getColumnData(col);
            if (data != null) {
                cols.add(new Pair<Fixing.Column, FixingsColumn>(col, data));
            }
        }

        JFreeChart chart = createChart(cols, river, km, callMeta);

        return encode(chart, extent, format);
    }

    protected static Output encode(
        JFreeChart chart,
        Dimension  extent,
        String     format
    ) {
        BufferedImage image = chart.createBufferedImage(
            extent.width, extent.height,
            Transparency.BITMASK,
            null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, format, out);
        }
        catch (IOException ioe) {
            log.warn("writing image failed", ioe);
            return empty();
        }

        return new Output(out.toByteArray(), "image/" + format);
    }

    protected static JFreeChart createChart(
        List<Pair<Fixing.Column, FixingsColumn>> cols,
        String      river,
        double      km,
        CallMeta    callMeta
    ) {
        String labelFormat = Resources.getMsg(
            callMeta, I18N_CHART_LABEL_DATE, DEFAULT_CHART_LABEL_DATE);

        QWSeriesCollection.LabelGenerator lg =
            new QWSeriesCollection.DateFormatLabelGenerator(labelFormat);

        QWSeriesCollection dataset = new QWSeriesCollection(lg);

        double [] w = new double[1];
        for (Pair<Fixing.Column, FixingsColumn> col: cols) {
            boolean interpolated = !col.getB().getW(km, w);
            double q = col.getB().getQ(km);
            if (!Double.isNaN(w[0]) && !Double.isNaN(q)) {
                QWI qw = new QWI(
                    q, w[0],
                    col.getA().getDescription(),
                    col.getA().getStartTime(),
                    interpolated, 0);
                dataset.add(qw);
            }
        }

        String title = Resources.format(
            callMeta, I18N_CHART_TITLE, DEFAULT_CHART_TITLE, river, km);

        String qAxis = Resources.getMsg(
            callMeta, I18N_Q_AXIS, DEFAULT_Q_AXIS);

        String wAxis = Resources.getMsg(
            callMeta, I18N_W_AXIS, DEFAULT_W_AXIS);

        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            qAxis,
            wAxis,
            null,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);

        XYPlot plot = (XYPlot)chart.getPlot();

        NumberAxis qA = (NumberAxis)plot.getDomainAxis();
        qA.setNumberFormatOverride(Formatter.getWaterlevelQ(callMeta));

        NumberAxis wA = (NumberAxis)plot.getRangeAxis();
        wA.setNumberFormatOverride(Formatter.getWaterlevelW(callMeta));

        plot.setRenderer(0, dataset.createRenderer());
        plot.setDataset(0, dataset);

        Rectangle2D area = dataset.getArea();

        if (area != null) {
            double height = area.getHeight();
            double wInset = Math.max(height, 0.01) * 0.25d;

            wA.setAutoRangeIncludesZero(false);
            wA.setRange(new Range(
                area.getMinY() - wInset,
                area.getMaxY() + wInset));
        }

        final String measuredS = Resources.getMsg(
            callMeta, I18N_MEASURED, DEFAULT_MEASURED);

        final String interpolatedS = Resources.getMsg(
            callMeta, I18N_INTERPOLATED, DEFAULT_INTERPOLATED);

        LegendItemCollection lic = plot.getLegendItems();
        dataset.addLegendItems(lic, new ShapeRenderer.LabelGenerator() {
            @Override
            public String createLabel(ShapeRenderer.Entry entry) {
                return entry.getFilled() ? measuredS : interpolatedS;
            }
        });
        plot.setFixedLegendItems(lic);

        applyQSectorMarkers(plot, river, km, callMeta);

        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        return chart;
    }

    /** Add domain markers to plot that indicate Q-sectors. */
    protected static void applyQSectorMarkers(
        XYPlot   plot,
        String   river,
        double   km,
        CallMeta meta
    ) {
        GaugeFinderFactory ggf = GaugeFinderFactory.getInstance();
        GaugeFinder        gf  = ggf.getGaugeFinder(river);

        if (gf == null) {
            log.warn("No gauge finder found for river '" + river + "'");
            return;
        }

        GaugeRange gr = gf.find(km);
        if (gr == null) {
            log.debug("No gauge range found for km "
                + km + " on river " + river + ".");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(gr);
        }

        for (int i = 0; i < I18N_Q_SECTOR_BOARDERS.length; ++i) {
            String key   = I18N_Q_SECTOR_BOARDERS[i];
            String def   = DEFAULT_Q_SECTOR_BORDERS[i];
            String label = Resources.getMsg(meta, key, def);

            Marker m = createQSectorMarker(
                gr.getSectorBorder(i),
                label);

            if (m != null) {
                plot.addDomainMarker(m);
            }
        }
    }

    /** Create Marker at value with label. */
    protected static Marker createQSectorMarker(
         double value, String label
    ) {
        if (Double.isNaN(value)) {
            return null;
        }
        Marker m = new ValueMarker(value);
        m.setPaint(Color.black);
        m.setStroke(new BasicStroke());
        m.setLabel(label);
        m.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        m.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        return m;
    }

    protected static String getRiverName(Document input) {
        NodeList rivers = input.getElementsByTagName("river");

        if (rivers.getLength() == 0) {
            return null;
        }

        String river = ((Element)rivers.item(0)).getAttribute("name");

        return river.length() > 0 ? river : null;
    }

    protected static Double getKM(Document input) {
        NodeList kms = input.getElementsByTagName("km");

        if (kms.getLength() == 0) {
            return null;
        }

        String km = ((Element)kms.item(0)).getAttribute("value");

        try {
            return Double.valueOf(km);
        }
        catch (NumberFormatException nfe) {
            log.warn("Km '" + km + " is not a valid number.");
            return null;
        }
    }

    protected static Dimension getExtent(Document input) {

        int width  = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        NodeList extents = input.getElementsByTagName("extent");

        if (extents.getLength() > 0) {
            Element element = (Element)extents.item(0);
            String w = element.getAttribute("width");
            String h = element.getAttribute("height");

            try {
                width = Math.max(1, Integer.parseInt(w));
            }
            catch (NumberFormatException nfe) {
                log.warn("width '" + w + "' is not a valid.");
            }

            try {
                height = Math.max(1, Integer.parseInt(h));
            }
            catch (NumberFormatException nfe) {
                log.warn("height '" + h + "' is not a valid");
            }
        }

        return new Dimension(width, height);
    }

    protected static String getFormat(Document input) {
        String format = DEFAULT_FORMAT;

        NodeList formats = input.getElementsByTagName("format");

        if (formats.getLength() > 0) {
            String type = ((Element)formats.item(0)).getAttribute("type");
            if (type.length() > 0) {
                format = type;
            }
        }

        return format;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
