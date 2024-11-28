/* Copyright (C) 2011, 2012, 2013, 2015 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.sq;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.NumberFormat;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRException;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.sq.SQFractionResult;
import org.dive4elements.river.artifacts.model.sq.SQResult;
import org.dive4elements.river.artifacts.model.sq.SQ;
import org.dive4elements.river.artifacts.model.sq.SQRelationJRDataSource;
import org.dive4elements.river.artifacts.model.sq.SQMeasurementsJRDataSource;
import org.dive4elements.river.artifacts.model.Parameters;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.access.SQRelationAccess;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.exports.AbstractExporter;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.Formatter;

import org.dive4elements.artifacts.common.utils.Config;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SQRelationExporter extends AbstractExporter {

    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(SQRelationExporter.class);

    public static final String INFO_COEFF_A =
        "export.sqrelation.csv.info.coeff.a";

    public static final String INFO_COEFF_B =
        "export.sqrelation.csv.info.coeff.b";

    public static final String INFO_QMAX =
        "export.sqrelation.csv.info.qmax";

    public static final String INFO_STDERR =
        "export.sqrelation.csv.info.stderr";

    public static final String INFO_R2 =
        "export.sqrelation.csv.info.r2";

    public static final String INFO_NTOT =
        "export.sqrelation.csv.info.ntot";

    public static final String INFO_NOUTL =
        "export.sqrelation.csv.info.noutl";

    public static final String INFO_CFERGUSON =
        "export.sqrelation.csv.info.cferguson";

    public static final String INFO_CDUAN =
        "export.sqrelation.csv.info.cduan";

    public static final String INFO_PARAM_A =
        "export.sqrelation.csv.info.param.a";

    public static final String INFO_PARAM_B =
        "export.sqrelation.csv.info.param.b";

    public static final String INFO_PARAM_C =
        "export.sqrelation.csv.info.param.c";

    public static final String INFO_PARAM_D =
        "export.sqrelation.csv.info.param.d";

    public static final String INFO_PARAM_E =
        "export.sqrelation.csv.info.param.e";

    public static final String INFO_PARAM_F =
        "export.sqrelation.csv.info.param.f";

    public static final String INFO_Q =
        "export.sqrelation.csv.info.q";

    public static final String INFO_S_KG =
        "export.sqrelation.csv.info.s_kg";

    public static final String INFO_DATE =
        "export.sqrelation.csv.info.date";

    public static final String CSV_PARAMETER =
        "export.sqrelation.csv.header.parameter";

    public static final String CSV_STATION =
        "export.sqrelation.csv.header.station";

    public static final String CSV_KM =
        "export.sqrelation.csv.header.km";

    public static final String CSV_FUNCTION =
        "export.sqrelation.csv.header.function";

    public static final String CSV_GAUGE =
        "export.sqrelation.csv.header.gauge";

    public static final String CSV_COEFF_A =
        "export.sqrelation.csv.header.coeff.a";

    public static final String CSV_COEFF_B =
        "export.sqrelation.csv.header.coeff.b";

    public static final String CSV_COEFF_Q =
        "export.sqrelation.csv.header.coeff.q";

    public static final String CSV_COEFF_R =
        "export.sqrelation.csv.header.coeff.r";

    public static final String CSV_N_TOTAL =
        "export.sqrelation.csv.header.n.total";

    public static final String CSV_N_OUTLIERS =
        "export.sqrelation.csv.header.n.outliers";

    public static final String CSV_C_DUAN =
        "export.sqrelation.csv.header.c.duan";

    public static final String CSV_C_FERGUSON =
        "export.sqrelation.csv.header.c.ferguson";

    public static final String CSV_QMAX =
        "export.sqrelation.csv.header.qmax";

    public static final String CSV_SD =
        "export.sqrelation.csv.header.sd";

    public static final String CSV_S_KG =
        "export.sqrelation.csv.header.s_kg";

    public static final String CSV_Q =
        "export.sqrelation.csv.header.q";

    public static final String CSV_DATE =
        "export.sqrelation.csv.header.date";

    public static final String PDF_TITLE=
        "export.sqrelation.pdf.title";

    public static final String PDF_HEADER_MODE =
        "export.sqrelation.pdf.mode";

    public static final String JASPER_FILE =
        "export.sqrelation.pdf.file";

    public static final String JASPER_MEASUREMENTS_FILE =
        "export.sqrelation.measurements.pdf.file";

    protected List<SQResult []> data;

    public SQRelationExporter() {
        data = new ArrayList<SQResult []>();
    }

    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof SQResult []) {
                data.add((SQResult [])d);
            }
        }
    }

    protected void writeCSVHeader(CSVWriter writer) {
        writer.writeNext(new String[] {
            msg(CSV_KM),
            msg(CSV_PARAMETER),
            msg(CSV_COEFF_A),
            msg(CSV_COEFF_B),
            msg(CSV_SD),
            msg(CSV_QMAX),
            msg(CSV_COEFF_R),
            msg(CSV_N_TOTAL),
            msg(CSV_N_OUTLIERS),
            msg(CSV_C_DUAN),
            msg(CSV_C_FERGUSON),
            msg(CSV_S_KG),
            msg(CSV_Q),
            msg(CSV_DATE)
        });
    }

    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.debug("writeCSVData");

        writeCSVInfo(writer, new String[] {
                msg(INFO_PARAM_A),
                msg(INFO_PARAM_B),
                msg(INFO_PARAM_C),
                msg(INFO_PARAM_D),
                msg(INFO_PARAM_E),
                msg(INFO_PARAM_F),
                msg(INFO_COEFF_A),
                msg(INFO_COEFF_B),
                msg(INFO_QMAX),
                msg(INFO_STDERR),
                msg(INFO_R2),
                msg(INFO_NTOT),
                msg(INFO_NOUTL),
                msg(INFO_CFERGUSON),
                msg(INFO_CDUAN),
                msg(INFO_S_KG),
                msg(INFO_Q),
                msg(INFO_DATE)
            });

        writeCSVHeader(writer);

        for (SQResult [] results: data) {
            for (SQResult result: results) {
                writer.writeAll(data2StringArrays(result, true));
            }
        }
    }

    protected List<String[]> data2StringArrays(
        SQResult result,
        boolean includeMeasurements
    ) {
        String km = Formatter.getSQRelationKM(context
                    ).format(result.getKm());
        List<String[]> retval = new ArrayList<String[]>();

        NumberFormat sqAFormatter = Formatter.getSQRelationA(context);
        NumberFormat sqBFormatter = Formatter.getSQRelationB(context);
        NumberFormat fThreeFormatter = Formatter.getFormatter(context, 3, 3);
        NumberFormat fTwoFormatter = Formatter.getFormatter(context, 2, 2);
        NumberFormat fZeroFormatter = Formatter.getFormatter(context, 0, 0);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
                Resources.getLocale(context.getMeta()));

        for (int i = 0; i < SQResult.NUMBER_FRACTIONS; ++i) {
            SQFractionResult fraction = result.getFraction(i);

            String name = result.getFractionName(i);

            Parameters parameters = fraction.getParameters();

            if (parameters == null) {
                continue;
            }

            String a, b, sd, o, t, max_q, c_ferguson, c_duan, r2;
            a  = sqAFormatter.format(parameters.getValue(0, "a"));
            b  = sqBFormatter.format(parameters.getValue(0, "b"));

            /* The std_dev parameter contains the standard error actually */
            sd = fThreeFormatter.format(parameters.getValue(0, "std_dev"));
            max_q = fZeroFormatter.format(parameters.getValue(0, "max_q"));
            c_ferguson = fTwoFormatter.format(
                parameters.getValue(0, "c_ferguson"));
            c_duan = fTwoFormatter.format(parameters.getValue(0, "c_duan"));
            r2 = fTwoFormatter.format(parameters.getValue(0, "r2"));


            o  = String.valueOf(fraction.totalNumOutliers());
            t  = String.valueOf(fraction.numMeasurements());

            if (includeMeasurements) {
                for (SQ sq: fraction.getMeasurements()) {
                    retval.add(new String[] {
                        km,
                        name,
                        a,
                        b,
                        sd, // 4
                        max_q, // 5
                        r2, // 6
                        t, // 7
                        o, // 8
                        c_duan, // 9
                        c_ferguson, // 10
                        fThreeFormatter.format(sq.getS()),
                        fZeroFormatter.format(sq.getQ()),
                        df.format(sq.getDate())
                    });
                }
            } else {
                retval.add(new String[] {
                    km,
                    name,
                    a,
                    b,
                    sd, // 4
                    max_q, // 5
                    r2, // 6
                    t, // 7
                    o, // 8
                    c_duan, // 9
                    c_ferguson // 10
                });
            }

        }
        return retval;
    }


    protected SQRelationJRDataSource createJRData() {
        SQRelationJRDataSource source = new SQRelationJRDataSource();

        addMetaData(source);
        for (SQResult [] results: data) {
            for (SQResult result: results) {
                for (String[] res: data2StringArrays(result, false)) {
                    source.addData(res);
                }
            }
        }
        return source;
    }

    protected SQMeasurementsJRDataSource createMeasurementJRData() {
        SQMeasurementsJRDataSource source = new SQMeasurementsJRDataSource();
        NumberFormat fZeroFormatter = Formatter.getFormatter(context, 0, 0);
        NumberFormat fThreeFormatter = Formatter.getFormatter(context, 3, 3);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
                Resources.getLocale(context.getMeta()));

        for (SQResult [] results: data) {
            for (SQResult result: results) {
                for (int i = 0; i < SQResult.NUMBER_FRACTIONS; ++i) {
                    String name = result.getFractionName(i);
                    SQFractionResult fraction = result.getFraction(i);
                    for (SQ sq: fraction.getMeasurements()) {
                        source.addData(new String[] {
                            name,
                            fThreeFormatter.format(sq.getS()),
                            fZeroFormatter.format(sq.getQ()),
                            df.format(sq.getDate()),
                            null
                        });
                    }
                    for (int j = 0; j < fraction.numIterations(); j++) {
                        for (SQ sq: fraction.getOutliers(j)) {
                            source.addData(new String[] {
                                name,
                                fThreeFormatter.format(sq.getS()),
                                fZeroFormatter.format(sq.getQ()),
                                df.format(sq.getDate()),
                                Integer.toString(j + 1)
                            });
                        }
                    }
                }
            }
        }
        return source;
    }

    protected void addMetaData(SQRelationJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact arti = (D4EArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(arti));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        source.addMetaData("date", df.format(new Date()));

        SQRelationAccess access = new SQRelationAccess(arti);
        source.addMetaData(
            "location", "KM " + getKmFormatter().format(access.getLocation()));

        DateRange period = access.getPeriod();
        source.addMetaData("periods", df.format(period.getFrom()) + " - " +
            df.format(period.getTo()));

        source.addMetaData("outliertest", Resources.getMsg(meta,
                    access.getOutlierMethod(),
                    access.getOutlierMethod()));
        source.addMetaData(
            "outliers", Formatter.getRawFormatter(context).format(
                    access.getOutliers()));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "SQRelation"));

        String measurementStationName = access.getMeasurementStationName();

        if (measurementStationName != null) {
            source.addMetaData("msName", measurementStationName);
        } else {
            source.addMetaData("msName", "");
        }

        String measurementStationGaugeName = access
            .getMeasurementStationGaugeName();

        if (measurementStationGaugeName != null) {
            source.addMetaData("msGauge", measurementStationGaugeName);
        } else {
            source.addMetaData("msGauge", "");
        }

    }

    @Override
    protected void writePDF(OutputStream out) {
        log.debug("write PDF");
        SQRelationJRDataSource source = createJRData();
        SQMeasurementsJRDataSource measureSource = createMeasurementJRData();

        String jasperFile = Resources.getMsg(
                context.getMeta(),
                JASPER_FILE,
                "/jasper/sqrelation_en.jasper");
        String jasperMeasurementsFile = Resources.getMsg(
                context.getMeta(),
                JASPER_MEASUREMENTS_FILE,
                "/jasper/sqmeasurements_en.jasper");
        String confPath = Config.getConfigDirectory().toString();


        Map parameters = new HashMap();
        parameters.put("ReportTitle", Resources.getMsg(
                    context.getMeta(), PDF_TITLE, "Exported Data"));
        try {
            /* Page numbers start have a built in offset of 1 so this
             * is fine. */
            JasperPrint p2 = JasperFillManager.fillReport(
                confPath + jasperMeasurementsFile,
                parameters,
                measureSource);
            parameters.put("MEASUREMENT_PAGE_NUM", p2.getPages().size());
            JasperPrint p1 = JasperFillManager.fillReport(
                confPath + jasperFile,
                parameters,
                source);
            for (Object page: p2.getPages()) {
                JRPrintPage object = (JRPrintPage)page;
                p1.addPage(object);
            }
            JasperExportManager.exportReportToPdfStream(p1, out);
        }
        catch(JRException je) {
            log.warn("Error generating PDF Report!", je);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
