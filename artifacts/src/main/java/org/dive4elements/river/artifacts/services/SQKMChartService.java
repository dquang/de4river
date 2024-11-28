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

import org.dive4elements.river.artifacts.model.SQOverview;
import org.dive4elements.river.artifacts.model.SQOverviewFactory;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.backend.SedDBSessionHolder;

import org.dive4elements.river.utils.KMIndex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Transparency;

import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.DateAxis;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SQKMChartService
extends DefaultService {

    private static final Logger log =
        LogManager.getLogger(SQKMChartService.class);

    public static final int DEFAULT_WIDTH  = 240;
    public static final int DEFAULT_HEIGHT = 180;

    public static final String I18N_CHART_LABEL =
        "sq.km.chart.label";

    public static final String DEFAULT_CHART_LABEL =
        "Measuring Points";

    public static final String I18N_CHART_TITLE =
        "sq.km.chart.title";

    public static final String DEFAULT_CHART_TITLE =
        "Measuring points";

    public static final String I18N_KM_AXIS =
        "sq.km.chart.km.axis";

    public static final String DEFAULT_KM_AXIS =
        "km";

    public static final String I18N_DATE_AXIS =
        "sq.km.chart.date.axis";

    public static final String DEFAULT_DATE_AXIS =
        "Date";

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
        log.debug("SQKMChartService.process");

        SedDBSessionHolder.acquire();
        try {
            return doProcess(data, globalContext, callMeta);
        }
        finally {
            SedDBSessionHolder.release();
        }
    }

    protected Service.Output doProcess(
        Document      input,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        String    river  = getRiverName(input);
        Dimension extent = getExtent(input);
        String    format = getFormat(input);

        if (river == null) {
            log.warn("River invalid.");
            return empty();
        }

        SQOverview overview = SQOverviewFactory.getOverview(river);

        if (overview == null) {
            log.warn("No overview found for river '" + river + "'");
            return empty();
        }

        KMIndex<List<Date>> entries = overview.filter(SQOverview.ACCEPT);

        JFreeChart chart = createChart(entries, river, callMeta);

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
        KMIndex<List<Date>> entries,
        String      river,
        CallMeta    callMeta
    ) {

        XYSeriesCollection dataset = new XYSeriesCollection();
        String key = Resources.format(
            callMeta, I18N_CHART_LABEL, DEFAULT_CHART_LABEL, river);

        XYSeries series = new XYSeries(key);
        for (KMIndex.Entry<List<Date>> e: entries) {
            double km = e.getKm();
            List<Date> ds = e.getValue();
            for (Date d: ds) {
                series.add(km, d.getTime());
            }
        }

        dataset.addSeries(series);
        String title = Resources.format(
            callMeta, I18N_CHART_TITLE, DEFAULT_CHART_TITLE, river);

        String kmAxis = Resources.getMsg(
            callMeta, I18N_KM_AXIS, DEFAULT_KM_AXIS);

        String dateAxis = Resources.getMsg(
            callMeta, I18N_DATE_AXIS, DEFAULT_DATE_AXIS);

        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            kmAxis,
            dateAxis,
            null,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);

        XYPlot plot = (XYPlot)chart.getPlot();

        DateAxis dA = new DateAxis();
        plot.setRangeAxis(dA);
        plot.setDataset(0, dataset);

        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        XYLineAndShapeRenderer renderer =
            (XYLineAndShapeRenderer)plot.getRenderer();

        renderer.setSeriesPaint(0, Color.gray);
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setDrawOutlines(true);
        return chart;
    }


    protected static String getRiverName(Document input) {
        NodeList rivers = input.getElementsByTagName("river");

        if (rivers.getLength() == 0) {
            return null;
        }

        String river = ((Element)rivers.item(0)).getAttribute("name");

        return river.length() > 0 ? river : null;
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
