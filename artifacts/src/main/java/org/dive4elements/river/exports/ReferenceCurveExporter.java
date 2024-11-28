/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WWQQ;
import org.dive4elements.river.artifacts.model.WWQQJRDataSource;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;

import java.io.IOException;
import java.io.OutputStream;

import java.text.DateFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * (CSV)Exporter for Reference Curves.
 */
public class ReferenceCurveExporter extends AbstractExporter {

    /** The log used in this exporter. */
    private static Logger log = LogManager.getLogger(ReferenceCurveExporter.class);

    public static final String RC_CSV_KM_HEADER =
        "export.reference_curve.csv.header.km";

    public static final String RC_CSV_W_CM_HEADER =
        "export.reference_curve.csv.header.w.cm";

    public static final String RC_CSV_W_M_HEADER =
        "export.reference_curve.csv.header.w.m";

    public static final String RC_CSV_Q_HEADER =
        "export.reference_curve.csv.header.w.q";

    public static final String CSV_LOCATION_HEADER =
        "export.waterlevel.csv.header.location";

    public static final String DEFAULT_CSV_LOCATION_HEADER = "Lage";

    public static final String RC_DEFAULT_CSV_KM_HEADER =
        "Fluss-Km";
    public static final String RC_DEFAULT_CSV_W_M_HEADER =
        "W (m + NHN)";
    public static final String RC_DEFAULT_CSV_W_CM_HEADER =
        "W (cm am Pegel)";
    public static final String RC_DEFAULT_CSV_Q_HEADER =
        "gleichw. Q (m\u00b3/s)";

    public static final String PDF_HEADER_MODE =
        "export.reference_curve.pdf.mode";
    public static final String JASPER_FILE =
        "export.reference_curve.pdf.file";
    public static final String JASPER_FILE_GAUGE =
        "export.reference_curve.pdf.file.gauge";
    public static final String JASPER_FILE_GAUGE_END =
        "export.reference_curve.pdf.file.gauge.end";
    public static final String JASPER_FILE_GAUGE_START_END =
        "export.reference_curve.pdf.file.gauge.start.end";

    /** The storage that contains all WKms objects for the different facets. */
    protected List<WWQQ[]> data;

    protected boolean startAtGauge = false;

    protected boolean endAtGauge = false;

    public ReferenceCurveExporter() {
        this.data = new ArrayList<WWQQ[]>();
    }

    /**
     * Genereate data in csv format.
     */
    @Override
    public void generate()
    throws IOException
    {
        log.debug("ReferenceCurveExporter.generate");

        if (facet == null) {
            throw new IOException("invalid (null) facet for exporter");
        }
        else if (facet.equals(AbstractExporter.FACET_CSV)) {
            generateCSV();
        }
        else if (facet.equals(AbstractExporter.FACET_PDF)) {
            generatePDF();
        }
        else {
            throw new IOException(
                "invalid facet (" + facet + ") for exporter");
        }
    }


    /**
     * Adds given data.
     * @param d A CalculationResult with WWQQ[].
     */
    @Override
    protected void addData(Object d) {
        log.debug("ReferenceCurveExporter.addData");

        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof WWQQ []) {
                WWQQ[] wwqqs = (WWQQ []) d;
                for (WWQQ wwqq: wwqqs) {
                    if (wwqq.startAtGauge()) {
                        startAtGauge = true;
                    }
                    // TODO this one probably has to be inverted.
                    if (wwqq.endAtGauge()) {
                        endAtGauge = true;
                    }
                }
                data.add(wwqqs);
                log.debug("ReferenceCurveExporter.addData wwqq[].");
            }
            else {
                log.warn("ReferenceCurveExporter.addData/1 unknown type ("
                    + d + ").");
            }
        }
        else {
            log.warn("ReferenceCurveExporter.addData/2 unknown type ("
                + d + ").");
        }
    }


    /**
     * Lets writer write all data (including header).
     * @param writer Writer to write data with.
     */
    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.debug("ReferenceCurveExporter.writeData");

        writeCSVHeader(writer);

        for (WWQQ[] tmp: data) {
            for (WWQQ ww: tmp) {
                wWQQ2CSV(writer, ww);
            }
        }
    }


    /**
     * Lets csvwriter write the header (first line in file).
     * @param writer Writer to write header with.
     */
    protected void writeCSVHeader(CSVWriter writer) {
        log.info("ReferenceCurveExporter.writeCSVHeader");

        StepCSVWriter stepWriter = new StepCSVWriter();
        stepWriter.setCSVWriter(writer);

        stepWriter.addNexts(
            msg(RC_CSV_KM_HEADER, RC_DEFAULT_CSV_KM_HEADER),
            msg(RC_CSV_W_M_HEADER, RC_DEFAULT_CSV_W_M_HEADER)
            );
        if (startAtGauge) {
            stepWriter.addNext(
                msg(RC_CSV_W_CM_HEADER, RC_DEFAULT_CSV_W_CM_HEADER));
        }
        stepWriter.addNexts(
            msg(RC_CSV_Q_HEADER, RC_DEFAULT_CSV_Q_HEADER),
            msg(CSV_LOCATION_HEADER, DEFAULT_CSV_LOCATION_HEADER),
            msg(RC_CSV_KM_HEADER, RC_DEFAULT_CSV_KM_HEADER),
            msg(RC_CSV_W_M_HEADER, RC_DEFAULT_CSV_W_M_HEADER)
            );
        if (endAtGauge) {
            stepWriter.addNext(
                msg(RC_CSV_W_CM_HEADER, RC_DEFAULT_CSV_W_CM_HEADER)
                );
        }
        stepWriter.addNexts(
                msg(RC_CSV_Q_HEADER, RC_DEFAULT_CSV_Q_HEADER),
                msg(CSV_LOCATION_HEADER, DEFAULT_CSV_LOCATION_HEADER)
                );

        stepWriter.flush();
    }


    protected void wWQQ2CSV(CSVWriter writer, WWQQ ww) {
        log.debug("ReferenceCurveExporter.wWQQ2CSV");

        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        int         size = ww.size();

        D4EArtifact flys       = (D4EArtifact) master;

        StepCSVWriter stepWriter = new StepCSVWriter();
        stepWriter.setCSVWriter(writer);

        String startLocationDescription = RiverUtils.getLocationDescription(
            flys, ww.getStartKm());

        String endLocationDescription = RiverUtils.getLocationDescription(
            flys, ww.getEndKm());

        for (int i = 0; i < size; i ++) {
            stepWriter.addNexts(kmf.format(ww.getStartKm()));
            stepWriter.addNext(wf.format(ww.getW1(i)));
            if (startAtGauge) {
                stepWriter.addNext(wf.format(ww.getRelHeight1Cm(i)));
            }
            stepWriter.addNexts(
                qf.format(ww.getQ1(i)), // "Q"
                startLocationDescription,
                kmf.format(ww.getEndKm())
                );
            stepWriter.addNext(wf.format(ww.getW2(i)));
            if (endAtGauge) {
                if (ww.endAtGauge()) {
                    stepWriter.addNext(wf.format(ww.getRelHeight2Cm(i)));
                }
                else {
                    stepWriter.addNext("-");
                }
            }
            stepWriter.addNexts(
                qf.format(ww.getQ2(i)), // "Q"
                endLocationDescription
                );
            stepWriter.flush();
        }
    }


    @Override
    protected void writePDF(OutputStream out) {
        WWQQJRDataSource source = createJRData();

        String filename = JASPER_FILE;
        if (startAtGauge && endAtGauge) {
            filename = JASPER_FILE_GAUGE_START_END;
        }
        else if (startAtGauge) {
            filename = JASPER_FILE_GAUGE;
        }
        else if (endAtGauge) {
            filename = JASPER_FILE_GAUGE_END;
        }

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                filename,
                                "/jasper/reference_en.jasper");
        String confPath = Config.getConfigDirectory().toString();

        Map parameters = new HashMap();
        parameters.put("ReportTitle", "Exported Data");
        try {
            JasperPrint print = JasperFillManager.fillReport(
                confPath + jasperFile,
                parameters,
                source);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
        catch(JRException je) {
            log.warn("Error generating PDF Report!");
            je.printStackTrace();
        }
    }

    protected WWQQJRDataSource createJRData() {
        WWQQJRDataSource source = new WWQQJRDataSource();

        addMetaData(source);

        for (WWQQ[] tmp: data) {
            for (WWQQ ww: tmp) {
                addWWQQData(source, ww);
            }
        }
        return source;
    }


    protected void addMetaData(WWQQJRDataSource source) {
        CallMeta meta = context.getMeta();

        WINFOArtifact flys = (WINFOArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        source.addMetaData("date", df.format(new Date()));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "Reference Curve"));
    }


    protected void addWWQQData(WWQQJRDataSource source, WWQQ ww) {
        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        int          size = ww.size();

        D4EArtifact flys = (D4EArtifact) master;

        String startLocationDescription = RiverUtils.getLocationDescription(
            flys, ww.getStartKm());

        String endLocationDescription = RiverUtils.getLocationDescription(
            flys, ww.getEndKm());

        for (int i = 0; i < size; i ++) {
            String start = "-";
            String end = "-";
            if (startAtGauge) {
                start = wf.format(ww.getRelHeight1Cm(i));
            }
            if (ww.endAtGauge()) {
                end = wf.format(ww.getRelHeight2Cm(i));
            }
            source.addData(new String[] {
                kmf.format(ww.getStartKm()),
                startLocationDescription,
                wf.format(ww.getW1(i)),
                qf.format(ww.getQ1(i)), // "Q"
                kmf.format(ww.getEndKm()),
                endLocationDescription,
                wf.format(ww.getW2(i)),
                qf.format(ww.getQ2(i)), // "Q"
                start,
                end
            });
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
