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
import java.util.Arrays;
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

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WQ;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WKmsJRDataSource;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ComputedDischargeCurveExporter extends AbstractExporter {

    /** The log used in this exporter.*/
    private static Logger log =
        LogManager.getLogger(ComputedDischargeCurveExporter.class);

    public static final String CSV_W_HEADER =
        "export.computed.discharge.curve.csv.header.w";

    public static final String CSV_Q_HEADER =
        "export.computed.discharge.curve.csv.header.q";

    public static final String DEFAULT_CSV_W_HEADER  = "W [NN + m]";
    public static final String DEFAULT_CSV_Q_HEADER  = "Q [m\u00b3/s]";

    public static final String PDF_HEADER_MODE =
        "export.computed.discharge.pdf.mode";
    public static final String PDF_HEADER_CALC_MODE =
        "export.computed.discharge.pdf.calc.mode";
    public static final String JASPER_FILE =
        "export.computed.discharge.pdf.file";

    protected List<WQKms> data;

    protected String wUnit;
    protected String riverUnit;
    protected String gaugeName;
    protected double gaugeDatum;
    protected Date validSince;

    public ComputedDischargeCurveExporter() {
        data = new ArrayList<WQKms>();
    }

    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
        }
        WQKms referenceWQ = null; // used for gauge / unit observations
        if (d instanceof WQKms[]){
            data.addAll(Arrays.asList((WQKms [])d));
            // If there is a unit mix in this list
            // we are screwed anyway.
            referenceWQ = ((WQKms[])d)[0];
        }
        else if (d instanceof WQKms) {
            data.add((WQKms)d);
            referenceWQ = (WQKms)d;
        } else {
            log.warn("Can't add data for export. Unkown data type " +
                d.getClass().getName());
            return;
        }
        if (referenceWQ != null) {
            D4EArtifact arti = (D4EArtifact)master;
            River river = RiverUtils.getRiver(arti);
            riverUnit = river.getWstUnit().getName();
            RangeAccess rangeAccess = new RangeAccess(arti);

            double[] kms = rangeAccess.getKmRange();

            Gauge gauge = river.determineGaugeAtStation(kms[0]);
            if (gauge != null) {
                wUnit = "cm";
                gaugeName = gauge.getName();
                gaugeDatum = gauge.getDatum().doubleValue();

                // Now convert the data to cm because we are at gauge
                List<WQKms> newData = new ArrayList<WQKms>();
                for (WQKms d2: data) {
                    newData.add(new WQKms(d2.getKms(),
                            WQ.getFixedWQforExportAtGauge(
                                (WQ)d2,
                                gauge.getDatum()
                            )));
                }
                data = newData; // All hail the garbage collector

                validSince = gauge.fetchMasterDischargeTable()
                    .getTimeInterval().getStartTime();
            } else {
                gaugeName = "";
                validSince = null;
                gaugeDatum = Double.NaN;
            }
        }
    }

    protected void writeCSVData(CSVWriter writer) {
        log.info("ComputedDischargeCurveExporter.writeData");

        writeCSVHeader(writer);

        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        double[] res = new double[3];

        for (WQKms wqkms: data) {
            int size = wqkms.size();

            for (int i = 0; i < size; i++) {
                res = wqkms.get(i, res);

                writer.writeNext(new String[] {
                    wf.format(res[0]),
                    qf.format(res[1])
                });
            }
        }
    }


    protected void writeCSVHeader(CSVWriter writer) {
        log.debug("ComputedDischargeCurveExporter.writeCSVHeader");

        RangeAccess access = new RangeAccess((D4EArtifact)master);
        double[] km = access.getLocations();
        // If we are not at gauge (cm) use the river unit
        String realUnit = "cm".equals(wUnit) ? "cm" : riverUnit;
        String header =
            msg(CSV_W_HEADER, DEFAULT_CSV_W_HEADER, new Object[] {realUnit});

        writer.writeNext(new String[] {
            header,
            msg(CSV_Q_HEADER, DEFAULT_CSV_Q_HEADER)
        });
    }


    /**
     * Returns the number formatter for W values.
     *
     * @return the number formatter for W values.
     */
    protected NumberFormat getWFormatter() {
        if ("cm".equals(wUnit)) {
            return Formatter.getFormatter(context, 0, 0);
        }
        return Formatter.getComputedDischargeW(context);
    }


    /**
     * Returns the number formatter for Q values.
     *
     * @return the number formatter for Q values.
     */
    protected NumberFormat getQFormatter() {
        return Formatter.getComputedDischargeQ(context);
    }


    @Override
    protected void writePDF(OutputStream out) {
        WKmsJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/computed-discharge_en.jasper");
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
        addWQData(source);

        return source;
    }


    protected void addMetaData(WKmsJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact flys = (D4EArtifact) master;
        source.addMetaData("gauge", gaugeName);
        if (!Double.isNaN(gaugeDatum)) {
            NumberFormat mf = Formatter.getMeterFormat(context);
            source.addMetaData(
                "datum", mf.format(gaugeDatum) + " " + riverUnit);
        } else {
            source.addMetaData("datum", "");
        }

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        source.addMetaData("date", df.format(new Date()));

        source.addMetaData("wUnit", "cm".equals(wUnit) ? "cm" : riverUnit);

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] kms = rangeAccess.getKmRange();
        source.addMetaData("range",
                Formatter.getCalculationKm(context.getMeta()).format(kms[0]));

        if (!"cm".equals(wUnit)) {
            source.addMetaData("valid_since", "");
            source.addMetaData("calculation", Resources.getMsg(
                                                locale,
                                                PDF_HEADER_CALC_MODE,
                                                "Computed Discharge"));
        } else {
            source.addMetaData(
                "valid_since",
                validSince == null ? "" : df.format(validSince));
            source.addMetaData("calculation", Resources.getMsg(
                                                locale,
                                                PDF_HEADER_MODE,
                                                "Discharge"));
        }
    }

    protected void addWQData(WKmsJRDataSource source) {
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        double[] res = new double[3];

        for (WQKms wqkms: data) {
            int size = wqkms.size();

            for (int i = 0; i < size; i++) {
                res = wqkms.get(i, res);

                source.addData(new String[] {
                    "",   // Empty, the WKmsJRDtasource stores km here.
                    wf.format(res[0]),
                    qf.format(res[1])
                });
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
