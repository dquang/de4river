/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.CalculationResult;

import org.dive4elements.river.artifacts.model.fixings.AnalysisPeriod;
import org.dive4elements.river.artifacts.model.fixings.FixAnalysisResult;
import org.dive4elements.river.artifacts.model.fixings.QWD;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.exports.AbstractExporter;

import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.KMIndex;

import java.io.IOException;
import java.io.OutputStream;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Exports fixation analysis deltaw(t) computation results to csv. */
public class DeltaWtExporter
extends      AbstractExporter
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(DeltaWtExporter.class);

    public static final String CSV_KM_HEADER =
        "export.fixings.deltawt.csv.header.km";

    public static final String CSV_DELTA_W_HEADER =
        "export.fixings.deltawt.csv.header.deltaw";

    public static final String CSV_Q_HEADER =
        "export.fixings.deltawt.csv.header.q";

    public static final String CSV_W_HEADER =
        "export.fixings.deltawt.csv.header.w";

    public static final String CSV_TRANGE_HEADER =
        "export.fixings.deltawt.csv.header.time.range";

    public static final String CSV_T_HEADER =
        "export.fixings.deltawt.csv.header.t";

    public static final String CSV_T_FORMAT =
        "export.fixings.deltawt.csv.t.format";

    public static final String DEFAULT_CSV_KM_HEADER = "km";

    public static final String DEFAULT_CSV_DELTA_W_HEADER = "\u0394 W [cm]";

    public static final String DEFAULT_CSV_W_HEADER = "Wasserstand [m]";

    public static final String DEFAULT_CSV_Q_HEADER = "Abfluss [m\u00b3/s]";

    public static final String DEFAULT_CSV_T_HEADER = "Datum";

    public static final String DEFAULT_CSV_TRANGE_DESC_HEADER =
        "Status";

    public static final String CSV_REFERENCE =
        "export.fixings.deltawt.csv.reference";

    public static final String CSV_ANALYSIS =
        "export.fixings.deltawt.csv.analysis";

    public static final String DEFAULT_CSV_REFERENCE =
        "B";

    public static final String DEFAULT_CSV_ANALYSIS =
        "A{0,number,integer}";

    public static final String DEFAULT_CSV_T_FORMAT =
        "dd.MM.yyyy";

    protected List<KMIndex<AnalysisPeriod []>> analysisPeriods;

    protected List<KMIndex<QWD[]>> referenceEvents;

    public DeltaWtExporter() {
        analysisPeriods = new ArrayList<KMIndex<AnalysisPeriod []>>();
        referenceEvents = new ArrayList<KMIndex<QWD[]>>();
    }

    @Override
    protected void addData(Object d) {
        log.debug("DeltaWtExporter.addData");
        if (!(d instanceof CalculationResult)) {
            log.warn("Invalid data type");
            return;
        }

        Object data = ((CalculationResult)d).getData();
        if (!(data instanceof FixAnalysisResult)) {
            log.warn("Invalid data stored in result.");
        }
        FixAnalysisResult result = (FixAnalysisResult)data;
        analysisPeriods.add(result.getAnalysisPeriods());
        referenceEvents.add(result.getReferenced());
    }

    @Override
    protected void writeCSVData(CSVWriter writer) throws IOException {

        boolean debug = log.isDebugEnabled();

        writeCSVHeader(writer);

        NumberFormat kmF = getKMFormatter();
        NumberFormat dwF = getDeltaWFormatter();
        NumberFormat qF  = getQFormatter();
        NumberFormat wF  = getWFormatter();

        DateFormat dF = getDateFormatter();

        TreeMap<Double, ArrayList<String []>> sorted =
            new TreeMap<Double, ArrayList<String []>>();

        String referenceS = getReference();

        for (KMIndex<QWD[]> reference: referenceEvents) {

            for (KMIndex.Entry<QWD[]> kmEntry: reference) {

                Double km = kmEntry.getKm();

                ArrayList<String []> list = sorted.get(km);

                if (list == null) {
                    list = new ArrayList<String []>();
                    sorted.put(km, list);
                }

                String kmS = kmF.format(kmEntry.getKm());
                for (QWD qwd: kmEntry.getValue()) {
                    String deltaWS = dwF.format(qwd.getDeltaW());
                    String qS      = qF.format(qwd.getQ());
                    String wS      = wF.format(qwd.getW());
                    String dateS   = dF.format(qwd.getDate());

                    list.add(new String[] {
                        kmS,
                        dateS,
                        qS,
                        wS,
                        referenceS,
                        deltaWS
                        });
                }
            }
        }

        if (debug) {
            log.debug("AnalysisPeriods: " + analysisPeriods.size());
        }

        String analysisTemplate = getAnalysisTemplate();

        for (KMIndex<AnalysisPeriod []> periods: analysisPeriods) {

            for (KMIndex.Entry<AnalysisPeriod []> kmEntry: periods) {

                Double km = kmEntry.getKm();

                ArrayList<String []> list = sorted.get(km);

                if (list == null) {
                    list = new ArrayList<String []>();
                    sorted.put(km, list);
                }

                String kmS = kmF.format(kmEntry.getKm());
                int analysisCount = 1;

                for (AnalysisPeriod period: kmEntry.getValue()) {
                    // Typically resulting in A1,A2...
                    String analyisS = MessageFormat.format(analysisTemplate,
                        analysisCount);
                    QWD [] qwds = period.getQWDs();

                    if (qwds != null) {
                        for (QWD qwd: qwds) {
                            String deltaWS = dwF.format(qwd.getDeltaW());
                            String qS      = qF.format(qwd.getQ());
                            String wS      = wF.format(qwd.getW());
                            String dateS   = dF.format(qwd.getDate());

                            list.add(new String[] {
                                kmS,
                                dateS,
                                qS,
                                wS,
                                analyisS,
                                deltaWS });
                        }
                    }
                    ++analysisCount;
                }
            }
        }

        for (ArrayList<String []> list: sorted.values()) {
            for (String [] row: list) {
                writer.writeNext(row);
            }
        }

        writer.flush();
    }

    /** Template to create "State" strings like A1,A2... */
    protected String getAnalysisTemplate() {
        return Resources.getMsg(
            context.getMeta(),
            CSV_ANALYSIS, DEFAULT_CSV_ANALYSIS);
    }

    protected String getReference() {
        return Resources.getMsg(
            context.getMeta(),
            CSV_REFERENCE, DEFAULT_CSV_REFERENCE);
    }

    protected NumberFormat getKMFormatter() {
        return Formatter.getFixDeltaWKM(context);
    }

    protected NumberFormat getDeltaWFormatter() {
        return Formatter.getFixDeltaWDeltaW(context);
    }

    protected NumberFormat getQFormatter() {
        return Formatter.getFixDeltaWQ(context);
    }

    protected NumberFormat getWFormatter() {
        return Formatter.getFixDeltaWW(context);
    }

    protected DateFormat getDateFormatter() {
        CallMeta meta = context.getMeta();
        return Formatter.getDateFormatter(
            meta,
            Resources.getMsg(
                meta,
                CSV_T_FORMAT,
                DEFAULT_CSV_T_FORMAT));
    }

    protected void writeCSVHeader(CSVWriter writer) {
        log.debug("DeltaWtExporter.writeCSVHeader");

        /* issue825
        km; Ereignis, Abfluss, GEMESSENER Wasserstand;
        Status (RECHTSBÜNDIG), del W
        */

        writer.writeNext(new String[] {
            msg(CSV_KM_HEADER,      DEFAULT_CSV_KM_HEADER),
            msg(CSV_T_HEADER,       DEFAULT_CSV_T_HEADER),
            msg(CSV_Q_HEADER,       DEFAULT_CSV_Q_HEADER),
            msg(CSV_W_HEADER,       DEFAULT_CSV_W_HEADER),
            msg(CSV_TRANGE_HEADER,  DEFAULT_CSV_TRANGE_DESC_HEADER),
            msg(CSV_DELTA_W_HEADER, DEFAULT_CSV_DELTA_W_HEADER)
        });
    }

    @Override
    protected void writePDF(OutputStream out) {
        // TODO: Implement me!
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
