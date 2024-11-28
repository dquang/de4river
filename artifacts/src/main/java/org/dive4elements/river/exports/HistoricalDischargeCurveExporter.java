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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRException;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.HistoricalDischargeAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.HistoricalDischargeData;
import org.dive4elements.river.artifacts.model.Timerange;
import org.dive4elements.river.artifacts.model.WQTimerange;
import org.dive4elements.river.artifacts.model.WQTJRDataSource;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HistoricalDischargeCurveExporter extends AbstractExporter {

    private static final Logger log =
        LogManager.getLogger(HistoricalDischargeCurveExporter.class);


    public static final String CSV_TIMERANGE_HEADER =
        "export.historical.discharge.csv.header.timerange";

    public static final String CSV_WATERLEVEL_HEADER =
        "export.historical.discharge.csv.header.waterlevel";

    public static final String CSV_DISCHARGE_HEADER =
        "export.historical.discharge.csv.header.discharge";

    public static final String CSV_DIFF_HEADER_W =
        "export.historical.discharge.csv.header.diff.w";

    public static final String CSV_DIFF_HEADER_Q =
        "export.historical.discharge.csv.header.diff.q";

    public static final String CSV_GAUGENAME_HEADER =
        "export.historical.discharge.csv.header.gaugename";

    public static final String PDF_HEADER_MODE =
        "export.historical.discharge.pdf.mode";

    public static final String JASPER_FILE =
        "export.historical.discharge.pdf.file";

    protected List<WQTimerange[]> data;

    public HistoricalDischargeCurveExporter() {
        data = new ArrayList<WQTimerange[]>();
    }

    @Override
    protected void addData(Object d) {
        log.debug("Add data of class: " + d.getClass());

        if (d instanceof CalculationResult) {
            d = ((CalculationResult) d).getData();

            log.debug("Internal data of CalculationResult: " + d.getClass());

            if (d instanceof HistoricalDischargeData) {
                d = (WQTimerange[])((HistoricalDischargeData)d)
                    .getWQTimeranges();

                if (d instanceof WQTimerange[]) {
                    log.debug("Add new data of type WQTimerange");
                    data.add((WQTimerange[]) d);
                }
            }
        }
    }


    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.info("HistoricalDischargeCurveExporter.writeCSVData");
        log.debug("CSV gets " + data.size() + " WQTimerange[] objects.");

        writeCSVHeader(writer);

        for (WQTimerange[] arr: data) {
            for (WQTimerange wqt: arr) {
                wqt2CSV(writer, wqt);
            }
        }
    }


    @Override
    protected void writePDF(OutputStream out) {
        WQTJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/historical-discharge_en.jasper");
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


    protected void writeCSVHeader(CSVWriter writer) {
        HistoricalDischargeAccess hda =
            new HistoricalDischargeAccess((D4EArtifact) master);
        writer.writeNext(new String[] {
            msg(CSV_TIMERANGE_HEADER, CSV_TIMERANGE_HEADER),
            msg(CSV_WATERLEVEL_HEADER, CSV_WATERLEVEL_HEADER),
            msg(CSV_DISCHARGE_HEADER, CSV_DISCHARGE_HEADER),
            (hda.getEvaluationMode()
                == HistoricalDischargeAccess.EvaluationMode.W)
                ? msg(CSV_DIFF_HEADER_W, CSV_DIFF_HEADER_W)
                : msg(CSV_DIFF_HEADER_Q, CSV_DIFF_HEADER_Q),
            msg(CSV_GAUGENAME_HEADER, CSV_GAUGENAME_HEADER)
        });
    }


    protected void wqt2CSV(CSVWriter writer, WQTimerange wqt) {
        log.debug("Add next WQTimerange to CSV");

        DateFormat   df = Formatter.getMediumDateFormat(context);
        NumberFormat wf = Formatter.getHistoricalDischargeW(context);
        NumberFormat qf = Formatter.getHistoricalDischargeQ(context);

        double[] wq = new double[3];

        String gaugeName = getReferenceGaugename();

        List<WQTimerange.TimerangeItem> sorted = wqt.sort();

        for (int i = 0, n = sorted.size(); i < n; i++) {
            WQTimerange.TimerangeItem item = sorted.get(i);

            Timerange tr = item.timerange;
            Date   start = new Date(tr.getStart());
            Date     end = new Date(tr.getEnd());

            item.get(wq);

            writer.writeNext(new String[] {
                df.format(start) + " - " + df.format(end),
                wf.format(wq[0]),
                qf.format(wq[1]),
                qf.format(wq[2]),
                gaugeName
            });
        }
    }


    protected WQTJRDataSource createJRData() {
        WQTJRDataSource source = new WQTJRDataSource();

        addMetaData(source);
        for (WQTimerange[] arr: data) {
            for (WQTimerange wqt: arr) {
                addWQTData(source, wqt);
            }
        }

        return source;
    }


    protected void addMetaData(WQTJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact flys = (D4EArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        source.addMetaData("date", df.format(new Date()));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "Historical Discharge"));
    }


    protected void addWQTData(WQTJRDataSource source, WQTimerange wqt) {
        DateFormat   df = Formatter.getShortDateFormat(context);
        NumberFormat wf = Formatter.getHistoricalDischargeW(context);
        NumberFormat qf = Formatter.getHistoricalDischargeQ(context);

        double[] wq = new double[3];

        String gaugeName = getReferenceGaugename();

        for (int i = 0, n = wqt.size(); i < n; i++) {
            Timerange tr = wqt.getTimerange(i);
            Date   start = new Date(tr.getStart());
            Date     end = new Date(tr.getEnd());

            wqt.get(i, wq);

            source.addData(new String[] {
                df.format(start) + " - " + df.format(end),
                wf.format(wq[0]),
                qf.format(wq[1]),
                qf.format(wq[2]),
                gaugeName
            });
        }
    }


    public String getReferenceGaugename() {
        return RiverUtils.getReferenceGaugeName((D4EArtifact) master);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
