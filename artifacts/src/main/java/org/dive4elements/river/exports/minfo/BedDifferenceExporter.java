/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.minfo;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.Date;
import java.util.Collections;
import java.text.DateFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.lang.StringUtils;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRException;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedDifferencesAccess;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.minfo.BedDiffYearResult;
import org.dive4elements.river.artifacts.model.minfo.BedDifferencesResult;
import org.dive4elements.river.artifacts.model.minfo.BedDifferenceJRDataSource;
import org.dive4elements.river.exports.AbstractExporter;
import org.dive4elements.river.utils.Formatter;

import au.com.bytecode.opencsv.CSVWriter;

public class BedDifferenceExporter
extends AbstractExporter
{

    /** Private log. */
    private static Logger log =
        LogManager.getLogger(BedDifferenceExporter.class);

    private static final String CSV_HEADER_KM =
        "export.minfo.beddifference.km";

    private static final String CSV_HEADER_DIFF =
        "export.minfo.beddifference.diff";

    private static final String CSV_HEADER_DIFF_PAIR =
        "export.minfo.beddifference.diff.pair";

    private static final String CSV_HEADER_SOUNDING1 =
        "export.minfo.beddifference.sounding1";

    private static final String CSV_HEADER_SOUNDING2 =
        "export.minfo.beddifference.sounding2";

    private static final String CSV_HEADER_GAP1 =
        "export.minfo.beddifference.gap1";

    private static final String CSV_HEADER_GAP2 =
        "export.minfo.beddifference.gap2";

    public static final String JASPER_FILE =
        "export.minfo.beddifference.pdf.file";

    public static final String PDF_TITLE=
        "export.minfo.beddifference.pdf.title";

    public static final String PDF_HEADER_MODE=
        "export.minfo.beddifference.pdf.mode";

    private BedDifferencesResult[] results;

    public BedDifferenceExporter() {
        results = new BedDifferencesResult[0];
    }

    protected List<String[]> data2StringArrays() {
        NumberFormat kmf = Formatter.getCalculationKm(context.getMeta());
        NumberFormat mf = Formatter.getMeterFormat(context);
        D4EArtifact arti = (D4EArtifact) master;
        RangeAccess access = new RangeAccess(arti);

        List<String[]> retval = new ArrayList<String[]>();

        for (BedDifferencesResult result : results) {
            BedDiffYearResult yResult = (BedDiffYearResult) result;
            String desc = result.getDiffDescription();
            double[][] kms = yResult.getDifferencesData();
            double[][] sounding1 = yResult.getSoundingWidth1Data();
            double[][] sounding2 = yResult.getSoundingWidth2Data();
            double[][] gap1 = yResult.getDataGap1Data();
            double[][] gap2 = yResult.getDataGap2Data();

            for (int j = 0; j < kms[0].length; j++) {
                String sound1 = !Double.isNaN(sounding1[1][j])
                    ? mf.format(sounding1[1][j])
                    : "";
                String sound2 = !Double.isNaN(sounding2[1][j])
                    ? mf.format(sounding2[1][j])
                    : "";
                String g1 = !Double.isNaN(gap1[1][j])
                    ? mf.format(gap1[1][j])
                    : "";
                String g2 = !Double.isNaN(gap2[1][j])
                    ? mf.format(gap2[1][j])
                    : "";
                retval.add(new String[] {
                    kmf.format(kms[0][j]),
                    desc,
                    mf.format(kms[1][j]),
                    sound1,
                    sound2,
                    g1,
                    g2
                    });
            }
        }
       if (access.getFrom() > access.getTo()) {
           Collections.reverse(retval);
       }
       return retval;
    }
    @Override
    protected void writeCSVData(CSVWriter writer) throws IOException {
        writeCSVHeader(writer);

        writer.writeAll(data2StringArrays());
    }

    @Override
    protected void addData(Object data) {
        if (!(data instanceof CalculationResult)) {
            log.warn("Invalid data type.");
            return;
        }
        Object[] d = (Object[])((CalculationResult)data).getData();

        if (!(d instanceof BedDifferencesResult[])) {
            log.warn("Invalid result object.");
            return;
        }
        results = (BedDifferencesResult[])d;
    }

    protected void addMetaData(BedDifferenceJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact arti = (D4EArtifact) master;

        source.addMetaData ("river", new RiverAccess(arti).getRiverName());

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        NumberFormat kmf = Formatter.getWaterlevelKM(context);

        RangeAccess rangeAccess = new RangeAccess(arti);
        double[] kms = rangeAccess.getKmRange();
        source.addMetaData("range",
                kmf.format(kms[0]) + " - " + kmf.format(kms[kms.length-1]));

        source.addMetaData("date", df.format(new Date()));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "Bedheight difference"));

        BedDifferencesAccess access = new BedDifferencesAccess(arti);

        source.addMetaData("differences", StringUtils.join(
                access.getDifferenceArtifactNamePairs(), "\n"));

        source.addMetaData("kmheader", msg(CSV_HEADER_KM));
        source.addMetaData("diffpairheader", msg(CSV_HEADER_DIFF_PAIR));
        source.addMetaData("diffheader", msg(CSV_HEADER_DIFF));
        source.addMetaData("sounding1header", msg(CSV_HEADER_SOUNDING1));
        source.addMetaData("sounding2header", msg(CSV_HEADER_SOUNDING2));
        source.addMetaData("gap1header", msg(CSV_HEADER_GAP1));
        source.addMetaData("gap2header", msg(CSV_HEADER_GAP2));
    }


    protected void writeCSVHeader(CSVWriter writer) {
        log.debug("writeCSVHeader()");

        List<String> header = new ArrayList<String>();
        if (results != null)  {
            header.add(msg(CSV_HEADER_KM, "km"));
            header.add(msg(CSV_HEADER_DIFF_PAIR, "difference pair"));
            header.add(msg(CSV_HEADER_DIFF, "cm"));
            if (results.length > 0 &&
                results[0] instanceof BedDiffYearResult) {
                header.add(
                    msg(CSV_HEADER_SOUNDING1, "soundung width minuend [m]"));
                header.add(
                    msg(CSV_HEADER_SOUNDING2,
                        "sounding width subtrahend [m]"));
                header.add(msg(CSV_HEADER_GAP1, "data gap minuend"));
                header.add(msg(CSV_HEADER_GAP2, "data gap subtrahend"));
            }
        }
        writer.writeNext(header.toArray(new String[header.size()]));
    }

    protected BedDifferenceJRDataSource createJRData() {
        BedDifferenceJRDataSource source = new BedDifferenceJRDataSource();

        addMetaData(source);
        for (String[] str: data2StringArrays()) {
            source.addData(str);
        }
        return source;
    }

    @Override
    protected void writePDF(OutputStream out) {
        log.debug("write PDF");
        BedDifferenceJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/beddifference_en.jasper");
        String confPath = Config.getConfigDirectory().toString();


        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ReportTitle", Resources.getMsg(
                    context.getMeta(), PDF_TITLE, "Exported Data"));
        try {
            JasperPrint print = JasperFillManager.fillReport(
                confPath + jasperFile,
                parameters,
                source);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
        catch(JRException je) {
            log.warn("Error generating PDF Report!", je);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
