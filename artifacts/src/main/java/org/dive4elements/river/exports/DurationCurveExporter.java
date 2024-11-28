/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.text.DateFormat;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRException;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.model.WQDay;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WKmsJRDataSource;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DurationCurveExporter extends AbstractExporter {

    /** The log used in this exporter. */
    private static Logger log = LogManager.getLogger(DurationCurveExporter.class);


    public static final String CSV_DURATION_HEADER =
        "export.duration.curve.csv.header.duration";

    public static final String CSV_W_HEADER =
        "export.duration.curve.csv.header.w";

    public static final String CSV_Q_HEADER =
        "export.duration.curve.csv.header.q";

    public static final String PDF_HEADER_MODE = "export.duration.pdf.mode";
    public static final String JASPER_FILE = "export.duration.pdf.file";

    /** The storage that contains all WQKms objects for the different facets. */
    protected List<WQDay> data;

    public DurationCurveExporter() {
        data = new ArrayList<WQDay>();
    }

    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof WQDay) {
                data.add((WQDay)d);
            }
        }
    }


    protected void writeCSVData(CSVWriter writer) {
        log.info("DurationCurveExporter.writeData");

        writeCSVHeader(writer);

        for (WQDay wqday: data) {
            wQDay2CSV(writer, wqday);
        }
    }


    protected void writeCSVHeader(CSVWriter writer) {
        log.info("DurationCurveExporter.writeCSVHeader");

        String unit = new RiverAccess((D4EArtifact)master)
            .getRiver().getWstUnit().getName();

        writer.writeNext(new String[] {
            msg(CSV_W_HEADER, new Object[] { unit }),
            msg(CSV_Q_HEADER),
            msg(CSV_DURATION_HEADER)
        });
    }


    protected void wQDay2CSV(CSVWriter writer, WQDay wqday) {
        log.debug("DurationCurveExporter.wQDay2CSV");

        int size = wqday.size();

        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();
        NumberFormat df  = getDFormatter();

        if (wqday.isIncreasing()) {
            for (int i = size-1; i >= 0; i --) {
                writer.writeNext(new String[] {
                    wf.format(wqday.getW(i)),
                    qf.format(wqday.getQ(i)),
                    df.format(wqday.getDay(i))
                });
            }
        }
        else {
            for (int i = 0; i < size; i ++) {
                writer.writeNext(new String[] {
                    wf.format(wqday.getW(i)),
                    qf.format(wqday.getQ(i)),
                    df.format(wqday.getDay(i))
                });
            }
        }
    }


    /**
     * Returns the number formatter for W values.
     *
     * @return the number formatter for W values.
     */
    @Override
    protected NumberFormat getWFormatter() {
        return Formatter.getDurationW(context);
    }


    /**
     * Returns the number formatter for Q values.
     *
     * @return the number formatter for Q values.
     */
    @Override
    protected NumberFormat getQFormatter() {
        return Formatter.getDurationQ(context);
    }


    /**
     * Returns the number formatter for duration values.
     *
     * @return the number formatter for duration values.
     */
    protected NumberFormat getDFormatter() {
        return Formatter.getDurationD(context);
    }


    @Override
    protected void writePDF(OutputStream out) {
        WKmsJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/duration_en.jasper");
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

    protected WKmsJRDataSource createJRData() {
        WKmsJRDataSource source = new WKmsJRDataSource();

        addMetaData(source);
        for (WQDay wqday: data) {
            addWQDayData(source, wqday);
        }

        return source;
    }


    protected void addMetaData(WKmsJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact flys = (D4EArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        source.addMetaData("date", df.format(new Date()));

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] kms = rangeAccess.getKmRange();
        source.addMetaData("range", String.valueOf(kms[0]));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "Duration"));
    }

    protected void addWQDayData(WKmsJRDataSource source, WQDay wqday) {
        int size = wqday.size();

        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();
        NumberFormat df  = getDFormatter();

        if (wqday.isIncreasing()) {
            for (int i = size-1; i >= 0; i --) {
                source.addData(new String[] {
                    "",
                    wf.format(wqday.getW(i)),
                    qf.format(wqday.getQ(i)),
                    "", "", "",
                    df.format(wqday.getDay(i))
                });
            }
        }
        else {
            for (int i = 0; i < size; i ++) {
                source.addData(new String[] {
                    "",
                    wf.format(wqday.getW(i)),
                    qf.format(wqday.getQ(i)),
                    "", "", "",
                    df.format(wqday.getDay(i))
                });
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
