/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.river.artifacts.model.DischargeTables;
import org.dive4elements.river.artifacts.model.GaugesFactory;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.backend.SessionHolder;
import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.TimeInterval;


/** Generate Discharge Table chart. */
public class DischargeTablesOverview extends AbstractChartService {

    private static final Logger log = LogManager
        .getLogger(DischargeTablesOverview.class);

    private static final long serialVersionUID = 1L;

    public static final String I18N_CHART_TITLE =
        "gauge.discharge.service.chart.title";
    public static final String DEFAULT_CHART_TITLE = "Pegel: XXX";

    public static final String I18N_CHART_X_AXIS_TITLE =
        "gauge.discharge.service.chart.x.title";
    public static final String DEFAULT_X_AXIS_TITLE = "Q [m^3/s]";

    public static final String I18N_CHART_Y_AXIS_TITLE =
        "gauge.discharge.service.chart.y.title";
    public static final String DEFAULT_Y_AXIS_TITLE = "W [cm]";

    public static final String I18N_CHART_SERIES_TITLE =
        "gauge.discharge.service.chart.series.title";
    public static final String DEFAULT_CHART_SERIES_TITLE = "Abflusskurve";

    public static final String I18N_CHART_SERIES_TITLE_MASTER =
        "gauge.discharge.service.chart.series.title.master";
    public static final String DEFAULT_CHART_SERIES_TITLE_MASTER =
        "Aktuelle Abflusskurve";

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(
        DateFormat.SHORT, Locale.GERMANY);


    @Override
    protected void init() {
        SessionHolder.acquire();
    }

    @Override
    protected void finish() {
        SessionHolder.release();
    }

    protected JFreeChart createChart(Document data,
        GlobalContext globalContext, CallMeta callMeta) {

        Gauge gauge = extractGauge(data);

        if (gauge == null) {
            log.warn("Could not determine Gauge from request!");
            return null;
        }

        log.info("create discharge chart for gauge '" + gauge.getName() + "'");
        TimeInterval timerange = extractTimeInterval(data);

        List<DischargeTable> dts = getDischargeTables(gauge, timerange);
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (DischargeTable dt : dts) {
            try {
                XYSeries series = createSeries(callMeta, dt);
                if (series != null) {
                    dataset.addSeries(series);
                }
            }
            catch (IllegalArgumentException iae) {
                log.warn("unable to create discharge curve: "
                    + iae.getMessage());
            }
        }

        String title = Resources.format(callMeta, I18N_CHART_TITLE,
            DEFAULT_CHART_TITLE, gauge.getName());

        String xAxis = Resources.getMsg(callMeta, I18N_CHART_X_AXIS_TITLE,
            DEFAULT_X_AXIS_TITLE);

        String yAxis = Resources.format(callMeta, I18N_CHART_Y_AXIS_TITLE,
            DEFAULT_Y_AXIS_TITLE);

        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxis, yAxis,
            null, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(0, dataset);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        applyMainValueMarkers(
            plot,
            gauge,
            callMeta);

        return chart;
    }

    protected XYSeries createSeries(CallMeta callMeta, DischargeTable dt)
        throws IllegalArgumentException {

        double[][] xy = null;

        xy = DischargeTables.loadDischargeTableValues(dt);

        XYSeries series = new XYSeries(createSeriesTitle(callMeta, dt), false);
        for (int i = 0, n = xy[0].length; i < n; i++) {
            series.add(xy[0][i], xy[1][i]);
        }

        return series;
    }


    /** Add domain markers to plot that indicate mainvalues. */
    protected static void applyMainValueMarkers(
        XYPlot   plot,
        Gauge    gauge,
        CallMeta meta
    ) {
        String river = gauge.getRiver().getName();
        double km    = gauge.getStation().doubleValue();

        // Get Gauge s mainvalues.
        List<MainValue> mainValues = gauge.getMainValues();
        for (MainValue mainValue : mainValues) {
            if (mainValue.getMainValue().getType().getName().equals("Q")) {
                // Its a Q main value.
                Marker m = FixingsKMChartService.createQSectorMarker(
                    mainValue.getValue().doubleValue(),
                    mainValue.getMainValue().getName());
                plot.addDomainMarker(m);
            }
            else if (
                mainValue.getMainValue().getType().getName().equals("W")
            ) {
                // Its a W main value.
                Marker m = FixingsKMChartService.createQSectorMarker(
                    mainValue.getValue().doubleValue(),
                    mainValue.getMainValue().getName());
                plot.addRangeMarker(m);
            }
        }
    }

    protected String createSeriesTitle(CallMeta callMeta, DischargeTable dt)
        throws IllegalArgumentException {
        TimeInterval timeInterval = dt.getTimeInterval();

        if (timeInterval == null) {
            return Resources.format(callMeta, DEFAULT_CHART_SERIES_TITLE);
        }

        Date start = timeInterval.getStartTime();
        Date end = timeInterval.getStopTime();

        if (start != null && end != null) {
            return Resources.format(callMeta, I18N_CHART_SERIES_TITLE,
                DEFAULT_CHART_SERIES_TITLE, start, end);
        }
        else if (start != null) {
            return Resources.format(callMeta, I18N_CHART_SERIES_TITLE_MASTER,
                DEFAULT_CHART_SERIES_TITLE, start);
        }
        else {
            throw new IllegalArgumentException(
                "Missing start date of DischargeTable " + dt.getId());
        }
    }

    protected Gauge extractGauge(Document data) {
        NodeList gauges = data.getElementsByTagName("gauge");

        if (gauges.getLength() > 0) {
            String name = ((Element) gauges.item(0)).getAttribute("name");

            try {
                long officialNumber = Long.valueOf(name);
                return Gauge.getGaugeByOfficialNumber(officialNumber);
            }
            catch (NumberFormatException nfe) {
                // it seems, that the client uses the name of the gauge instead
                // of its official number
            }

            if (name != null && name.length() > 0) {
                return GaugesFactory.getGauge(name);
            }
        }

        return null;
    }

    protected TimeInterval extractTimeInterval(Document data) {
        NodeList timeranges = data.getElementsByTagName("timerange");

        if (timeranges != null && timeranges.getLength() > 0) {
            Element timerange = (Element) timeranges.item(0);

            String lower = timerange.getAttribute("lower");
            String upper = timerange.getAttribute("upper");

            if (lower != null && upper != null) {
                try {
                    Date d1 = DATE_FORMAT.parse(lower);
                    Date d2 = DATE_FORMAT.parse(upper);

                    return new TimeInterval(d1, d2);
                }
                catch (ParseException pe) {
                    log.warn("Wrong time format: " + pe.getMessage());
                }
            }
        }

        return null;
    }

    protected List<DischargeTable> getDischargeTables(Gauge gauge,
        TimeInterval timerange) {
        List<DischargeTable> all = gauge.getDischargeTables();
        Collections.sort(all);

        if (timerange == null) {
            return all;
        }

        List<DischargeTable> dts = new ArrayList<DischargeTable>(all.size());
        long startDate = timerange.getStartTime().getTime();
        long stopDate = timerange.getStopTime().getTime();

        for (DischargeTable dt : all) {
            TimeInterval tmp = dt.getTimeInterval();
            if (tmp == null) {
                // this should never happen because all discharge tables should
                // have a time interval set!
                continue;
            }

            Date start = tmp.getStartTime();
            Date stop = tmp.getStartTime();

            if (start.getTime() > startDate && start.getTime() < stopDate) {
                dts.add(dt);
                continue;
            }
            else if (stop != null && stop.getTime() < stopDate
                && stop.getTime() > startDate) {
                dts.add(dt);
                continue;
            }
        }

        return dts;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
