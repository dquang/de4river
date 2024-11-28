/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.IOException;
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

import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RangeAccess;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WKmsJRDataSource;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;

/**
 * (CSV)Exporter for WDifferences.
 */
public class WDifferencesExporter extends AbstractExporter {

    /** The log used in this exporter. */
    private static Logger log = LogManager.getLogger(WDifferencesExporter.class);


    public static final String WDIFF_CSV_KM_HEADER =
        "export.w_differences.csv.header.km";

    public static final String WDIFF_CSV_W_HEADER =
        "export.w_differences.csv.header.w";

    public static final String WDIFF_DEFAULT_CSV_KM_HEADER = "Fluss-Km";
    public static final String WDIFF_DEFAULT_CSV_W_HEADER  = "m";

    public static final String PDF_HEADER_MODE = "export.wdifferences.pdf.mode";
    public static final String JASPER_FILE = "export.wdifferences.pdf.file";

    /** The storage that contains all WKms objects for the different facets. */
    protected List<WKms[]> data;

    protected List<String[]> stringData;

    public WDifferencesExporter() {
        data = new ArrayList<WKms[]>();
    }

    /**
     * Genereate data in csv format.
     */
    @Override
    public void generate()
    throws IOException
    {
        log.debug("WDifferencesExporter.generate");
        if (stringData == null) {
            D4EArtifact arti = (D4EArtifact) master;
            RangeAccess access = new RangeAccess(arti);
            stringData = data2StringArrays(access.getFrom() > access.getTo());
        }
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
            throw new IOException("invalid facet (" + facet + ") for exporter");
        }
    }


    /**
     * Adds given data.
     * @param d either a WKms or a CalculationResult to add to data.
     */
    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof WKms []) {
                data.add((WKms [])d);
            }
        }
        else if (d instanceof WKms) {
            data.add(new WKms[] { (WKms) d });
        }
    }


    /**
     * Lets writer write all data (including header).
     * @param writer Writer to write data with.
     */
    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.info("WDifferencesExporter.writeData");

        writeCSVHeader(writer);

        writer.writeAll(stringData);
    }


    /**
     * Lets csvwriter write the header (first line in file).
     * @param writer Writer to write header with.
     */
    protected void writeCSVHeader(CSVWriter writer) {
        log.info("WDifferencesExporter.writeCSVHeader");

        writer.writeNext(new String[] {
            msg(WDIFF_CSV_KM_HEADER, WDIFF_DEFAULT_CSV_KM_HEADER),
            msg(WDIFF_CSV_W_HEADER, WDIFF_DEFAULT_CSV_W_HEADER)
        });
    }


    /**
     * Returns the number formatter for kilometer values.
     *
     * @return the number formatter for kilometer values.
     */
    protected NumberFormat getKmFormatter() {
        return Formatter.getWaterlevelKM(context);
    }


    /**
     * Returns the number formatter for W values.
     *
     * @return the number formatter for W values.
     */
    protected NumberFormat getWFormatter() {
        return Formatter.getWaterlevelW(context);
    }

    protected List<String[]> data2StringArrays(boolean inverted) {
        NumberFormat kmf = getKmFormatter();
        NumberFormat wf = getWFormatter();
        List<String[]> retval = new ArrayList<String[]>();

        for (WKms[] tmp: data) {
            for (WKms wkms: tmp) {
                int size = wkms.size();
                if (inverted) {
                    for (int i = size - 1; i >= 0; i--) {
                        retval.add(new String[] {
                            kmf.format(wkms.getKm(i)),
                            wf.format(wkms.getW(i))
                        });
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        retval.add(new String[] {
                            kmf.format(wkms.getKm(i)),
                            wf.format(wkms.getW(i))
                        });
                    }
                }
            }
        }
        return retval;
    }

    @Override
    protected void writePDF(OutputStream out) {
        WKmsJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/wdifferences_en.jasper");
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
        for (String[] str: stringData) {
            source.addData(str);
        }
        return source;
    }


    protected void addMetaData(WKmsJRDataSource source) {
        CallMeta meta = context.getMeta();

        WINFOArtifact flys = (WINFOArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        NumberFormat kmf = getKmFormatter();

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] kms = rangeAccess.getKmRange();
        source.addMetaData("range",
                kmf.format(kms[0]) + " - " + kmf.format(kms[kms.length-1]));

        source.addMetaData("date", df.format(new Date()));

        String differences = RiverUtils.getWDifferences(flys, context);
        source.addMetaData("differences", differences);

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "W Differences"));
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
