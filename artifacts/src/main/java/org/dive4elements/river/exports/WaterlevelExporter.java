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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;

import gnu.trove.TDoubleArrayList;

import org.dive4elements.river.artifacts.model.ConstantWQKms;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRException;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.river.model.Gauge;

import org.dive4elements.river.artifacts.access.FixRealizingAccess;
import org.dive4elements.river.artifacts.access.IsOfficialAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.FixationArtifact;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.WINFOArtifact;
import org.dive4elements.river.artifacts.StaticWQKmsArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.Segment;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WstLine;
import org.dive4elements.river.artifacts.model.WKmsJRDataSource;
import org.dive4elements.river.artifacts.model.WQKmsResult;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.utils.RiverUtils.WQ_MODE;
import org.dive4elements.river.utils.Formatter;

/**
 * Generates different output formats (wst, csv, pdf) of data that resulted from
 * a waterlevel computation.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WaterlevelExporter extends AbstractExporter {

    /** The log used in this exporter.*/
    private static Logger log = LogManager.getLogger(WaterlevelExporter.class);

    public static final String FACET_WST = "wst";

    /* This should be the same as in the StaticWQKmsArtifact */
    public static final String STATICWQKMSNAME = "staticwqkms";

    public static final String CSV_KM_HEADER =
        "export.waterlevel.csv.header.km";

    public static final String CSV_W_HEADER =
        "export.waterlevel.csv.header.w";

    public static final String CSV_Q_HEADER =
        "export.waterlevel.csv.header.q";

    public static final String CSV_Q_DESC_HEADER =
        "export.waterlevel.csv.header.q.desc";

    public static final String CSV_W_DESC_HEADER =
        "export.waterlevel.csv.header.w.desc";

    public static final String CSV_LOCATION_HEADER =
        "export.waterlevel.csv.header.location";

    public static final String CSV_GAUGE_HEADER =
        "export.waterlevel.csv.header.gauge";

    public static final String CSV_META_RESULT =
        "export.waterlevel.csv.meta.result";

    public static final String CSV_META_CREATION =
        "export.waterlevel.csv.meta.creation";

    public static final String CSV_META_CALCULATIONBASE =
        "export.waterlevel.csv.meta.calculationbase";

    public static final String CSV_META_RIVER =
        "export.waterlevel.csv.meta.river";

    public static final String CSV_META_RANGE =
        "export.waterlevel.csv.meta.range";

    public static final String CSV_META_GAUGE =
        "export.waterlevel.csv.meta.gauge";

    public static final String CSV_META_Q =
        "export.waterlevel.csv.meta.q";

    public static final String CSV_META_W =
        "export.waterlevel.csv.meta.w";

    public static final String CSV_NOT_IN_GAUGE_RANGE =
        "export.waterlevel.csv.not.in.gauge.range";

    public static final Pattern NUMBERS_PATTERN =
        Pattern.compile("\\D*(\\d++.\\d*)\\D*");

    public static final String DEFAULT_CSV_KM_HEADER       = "Fluss-Km";
    public static final String DEFAULT_CSV_W_HEADER        = "W [NN + m]";
    public static final String DEFAULT_CSV_Q_HEADER        = "Q [m\u00b3/s]";
    public static final String DEFAULT_CSV_Q_DESC_HEADER   = "Bezeichnung";
    public static final String DEFAULT_CSV_W_DESC_HEADER   = "W/Pegel [cm]";
    public static final String DEFAULT_CSV_LOCATION_HEADER = "Lage";
    public static final String DEFAULT_CSV_GAUGE_HEADER    = "Bezugspegel";
    public static final String DEFAULT_CSV_NOT_IN_GAUGE_RANGE =
        "außerhalb des gewählten Bezugspegels";

    public static final String PDF_HEADER_MODE = "export.waterlevel.pdf.mode";
    public static final String JASPER_FILE     = "export.waterlevel.pdf.file";

    /** The storage that contains all WQKms objects that are calculated.*/
    protected List<WQKms[]> data;

    /** The storage that contains official fixings if available.*/
    protected List<WQKms> officalFixings;

    public WaterlevelExporter() {
        data = new ArrayList<WQKms[]>();
    }

    @Override
    public void generate()
    throws IOException
    {
        log.debug("WaterlevelExporter.generate");

        /* Check for official fixings. They should also be included in the
         * export but only the calculation result is added with addData */

        officalFixings = new ArrayList<WQKms>();

        for (Artifact art: collection.getArtifactsByName(
                STATICWQKMSNAME, context)
        ) {
            if (art instanceof StaticWQKmsArtifact) {
                IsOfficialAccess access =
                    new IsOfficialAccess((D4EArtifact)art);
                StaticWQKmsArtifact sart = (StaticWQKmsArtifact) art;
                if (!access.isOfficial()) {
                    continue;
                }

                /* Check that we add the data only once */
                WQKms toAdd = sart.getWQKms();
                String newName = toAdd.getName();

                boolean exists = false;
                for (WQKms wqkm: officalFixings) {
                    /* The same official fixing could be in two
                       artifacts/outs so let's deduplicate */
                    if (wqkm.getName().equals(newName)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    officalFixings.add(toAdd);
                    log.debug("Adding additional offical fixing: " + newName);
                }
            }
        }

        if (facet != null && facet.equals(AbstractExporter.FACET_CSV)) {
            generateCSV();
        }
        else if (facet != null && facet.equals(FACET_WST)) {
            generateWST();
        }
        else if (facet != null && facet.equals(AbstractExporter.FACET_PDF)) {
            generatePDF();
        }
        else {
            throw new IOException("invalid facet for exporter");
        }
    }


    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof WQKms []) {
                data.add((WQKms [])d);
            }
            else if (d instanceof WQKmsResult) {
                data.add(((WQKmsResult) d).getWQKms());
            }
        }
    }


    /**
     * Prepare the column titles of waterlevel exports.
     * Titles in this export include the Q value. If a Q value matches a named
     * main value (as HQ100 or MNQ) this named main value should be used as
     * title. This method resets the name of the <i>wqkms</i> object if such
     * named main value fits to the chosen Q.
     *
     * @param winfo A WINFO Artifact.
     * @param wqkms A WQKms object that should be prepared.
     */
    protected String getColumnTitle(WINFOArtifact winfo, WQKms wqkms) {
        log.debug("WaterlevelExporter.getColumnTitle");

        String name = wqkms.getName();

        log.debug("Name of WQKms = '" + name + "'");

        if (name.indexOf("W=") >= 0) {
            return name;
        }

        Matcher m = NUMBERS_PATTERN.matcher(name);

        if (m.matches()) {
            String raw = m.group(1);

            try {
                double v = Double.valueOf(raw);

                String nmv = RiverUtils.getNamedMainValue(winfo, v);

                if (nmv != null && nmv.length() > 0) {
                    nmv  = RiverUtils.stripNamedMainValue(nmv);
                    nmv += "=" + String.valueOf(v);
                    log.debug("Set named main value '" + nmv + "'");

                    return nmv;
                }
            }
            catch (NumberFormatException nfe) {
                // do nothing here
            }
        }

        return name;
    }


    protected String getCSVRowTitle(WINFOArtifact winfo, WQKms wqkms) {
        log.debug("WaterlevelExporter.prepareNamedValue");

        String name = wqkms.getName();

        log.debug("Name of WQKms = '" + name + "'");

        WQ_MODE wqmode = RiverUtils.getWQMode(winfo);

        if (wqmode == WQ_MODE.WFREE || wqmode == WQ_MODE.QGAUGE) {
            return localizeWQKms(winfo, wqkms);
        }

        Double v = wqkms.getRawValue();

        String nmv = RiverUtils.getNamedMainValue(winfo, v);

        if (nmv != null && nmv.length() > 0) {
            nmv = RiverUtils.stripNamedMainValue(nmv);
            log.debug("Set named main value '" + nmv + "'");

            return nmv;
        }

        return localizeWQKms(winfo, wqkms);
    }


    /**
     * Get a string like 'W=' or 'Q=' with a number following in localized
     * format.
     */
    protected String localizeWQKms(WINFOArtifact winfo, WQKms wqkms) {
        WQ_MODE wqmode   = RiverUtils.getWQMode(winfo);
        Double  rawValue = wqkms.getRawValue();

        if (rawValue == null) {
            return wqkms.getName();
        }

        NumberFormat nf = Formatter.getRawFormatter(context);

        if (wqmode == WQ_MODE.WFREE || wqmode == WQ_MODE.WGAUGE) {
            return "W=" + nf.format(rawValue);
        }
        else {
            return "Q=" + nf.format(rawValue);
        }
    }


    @Override
    protected void writeCSVData(CSVWriter writer) {
        log.info("WaterlevelExporter.writeData");

        WQ_MODE mode    = RiverUtils.getWQMode((D4EArtifact)master);
        boolean atGauge = mode == WQ_MODE.QGAUGE || mode == WQ_MODE.WGAUGE;
        boolean isQ     = mode == WQ_MODE.QGAUGE || mode == WQ_MODE.QFREE;
        RiverUtils.WQ_INPUT input
            = RiverUtils.getWQInputMode((D4EArtifact)master);

        writeCSVMeta(writer);
        writeCSVHeader(writer, atGauge, isQ);

        Double first = Double.NaN;
        Double last = Double.NaN;

        for (WQKms[] tmp: data) {
            for (WQKms wqkms: tmp) {
                wQKms2CSV(writer, wqkms, atGauge, isQ, false);
                double[] firstLast = wqkms.getFirstLastKM();
                if (first.isNaN()) {
                    /* Initialize */
                    first = firstLast[0];
                    last = firstLast[1];
                }
                if (firstLast[0] > firstLast[1]) {
                    /* Calculating upstream we assert that it is
                     * impossible that the direction changes during this
                     * loop */
                    first = Math.max(first, firstLast[0]);
                    last = Math.min(last, firstLast[1]);
                } else if (firstLast[0] < firstLast[1]) {
                    first = Math.min(first, firstLast[0]);
                    last = Math.max(last, firstLast[1]);
                } else {
                    first = last = firstLast[0];
                }
            }
        }
        /* Append the official fixing at the bottom */
        for (WQKms wqkms: officalFixings) {
            wQKms2CSV(
                writer, filterWQKms(wqkms, first, last), atGauge, isQ, true);
        }
    }


    /** Filter a wqkms object to a distance.
     *
     * To handle upstream / downstream and to limit
     * the officialFixings to the calculation distance
     * we create a new wqkms object here and fill it only
     * with the relevant data.
     *
     * @param wqkms: The WQKms Object to filter
     * @param first: The fist kilometer of the range
     * @param last: The last kilometer of the range
     *
     * @return A new WQKms with the relevant data sorted by direction
     */
    private WQKms filterWQKms(WQKms wqkms, Double first, Double last) {
        if (first.isNaN() || last.isNaN()) {
            log.warn("Filtering official fixing without valid first/last.");
            return wqkms;
        }
        int firstIdx = first > last ? wqkms.size() - 1 : 0;
        int lastIdx  = first > last ? 0 : wqkms.size() -1;
        WQKms filtered = new WQKms (wqkms.size());
        filtered.setName(wqkms.getName());
        double [] dp = new double [3];

        if (first > last) {
            for (int i = wqkms.size() - 1; i >= 0; i--) {
                dp = wqkms.get(i, dp);
                if (dp[2] <= first + 1E-5 && dp[2] > last - 1E-5) {
                    filtered.add(dp[0], dp[1], dp[2]);
                }
            }
        } else {
            for (int i = 0, N = wqkms.size(); i < N; i++) {
                dp = wqkms.get(i, dp);
                if (dp[2] < last + 1E-5 && dp[2] > first - 1E-5) {
                    filtered.add(dp[0], dp[1], dp[2]);
                }
            }
        }
        return filtered;
    }


    protected void writeCSVMeta(CSVWriter writer) {
        log.info("WaterlevelExporter.writeCSVMeta");

        // TODO use Access instead of RiverUtils

        CallMeta meta = context.getMeta();

        D4EArtifact flys = (D4EArtifact) master;

        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_RESULT,
                CSV_META_RESULT,
                new Object[] { RiverUtils.getRivername(flys) })
        });

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_CREATION,
                CSV_META_CREATION,
                new Object[] { df.format(new Date()) })
        });

        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_CALCULATIONBASE,
                CSV_META_CALCULATIONBASE,
                new Object[] { "" }) // TODO what is required at this place?
        });

        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_RIVER,
                CSV_META_RIVER,
                new Object[] { RiverUtils.getRivername(flys) })
        });

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] kms = rangeAccess.getKmRange();
        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_RANGE,
                CSV_META_RANGE,
                new Object[] { kms[0], kms[kms.length-1] })
        });

        writer.writeNext(new String[] {
            Resources.getMsg(
                meta,
                CSV_META_GAUGE,
                CSV_META_GAUGE,
                new Object[] { RiverUtils.getGaugename(flys) })
        });

        RiverUtils.WQ_MODE wq = RiverUtils.getWQMode(flys);
        if (wq == RiverUtils.WQ_MODE.QFREE || wq == RiverUtils.WQ_MODE.QGAUGE) {
            double[] qs  = RiverUtils.getQs(flys);
            RiverUtils.WQ_INPUT input = RiverUtils.getWQInputMode(flys);

            String data = "";

            if ((input == RiverUtils.WQ_INPUT.ADAPTED ||
                input == RiverUtils.WQ_INPUT.RANGE) &&
                qs != null && qs.length > 0)
            {
                data = String.valueOf(qs[0]);
                data += " - " + String.valueOf(qs[qs.length-1]);
            }
            else if (input == RiverUtils.WQ_INPUT.SINGLE && qs != null){
                data = String.valueOf(qs[0]);
                for (int i = 1; i < qs.length; i++) {
                    data += ", " + String.valueOf(qs[i]);
                }
            }
            else {
                log.warn("Could not determine Q range!");
            }

            writer.writeNext(new String[] {
                Resources.getMsg(
                    meta,
                    CSV_META_Q,
                    CSV_META_Q,
                    new Object[] {data})
            });
        }
        else {
            double[] ws = RiverUtils.getWs(flys);

            String lower = "";
            String upper = "";

            if (ws != null && ws.length > 0) {
                lower = String.valueOf(ws[0]);
                upper = String.valueOf(ws[ws.length-1]);
            }
            else {
                log.warn("Could not determine W range!");
            }

            writer.writeNext(new String[] {
                Resources.getMsg(
                    meta,
                    CSV_META_W,
                    CSV_META_W,
                    new Object[] { lower, upper })
            });
        }

        writer.writeNext(new String[] { "" });
    }


    /**
     * Write the header, with different headings depending on whether at a
     * gauge or at a location.
     */
    protected void writeCSVHeader(
        CSVWriter writer,
        boolean   atGauge,
        boolean   isQ
    ) {
        log.info("WaterlevelExporter.writeCSVHeader");

        String unit = RiverUtils.getRiver(
            (D4EArtifact) master).getWstUnit().getName();

        if (atGauge) {
            writer.writeNext(new String[] {
                msg(CSV_KM_HEADER, DEFAULT_CSV_KM_HEADER),
                msg(CSV_W_HEADER, DEFAULT_CSV_W_HEADER, new Object[] { unit }),
                msg(CSV_Q_HEADER, DEFAULT_CSV_Q_HEADER),
                (isQ
                    ? msg(CSV_Q_DESC_HEADER, DEFAULT_CSV_Q_DESC_HEADER)
                    : msg(CSV_W_DESC_HEADER, DEFAULT_CSV_W_DESC_HEADER)),
                msg(CSV_LOCATION_HEADER, DEFAULT_CSV_LOCATION_HEADER),
                msg(CSV_GAUGE_HEADER, DEFAULT_CSV_GAUGE_HEADER)
            });
        }
        else {
            writer.writeNext(new String[] {
                msg(CSV_KM_HEADER, DEFAULT_CSV_KM_HEADER),
                    // TODO flys/issue1128 (unit per river)
                msg(CSV_W_HEADER, DEFAULT_CSV_W_HEADER, new Object[] { unit }),
                msg(CSV_Q_HEADER, DEFAULT_CSV_Q_HEADER),
                msg(CSV_LOCATION_HEADER, DEFAULT_CSV_LOCATION_HEADER)
            });
        }
    }


    /** Linearly search for gauge which is valid at km. */
    private static Gauge findGauge(double km, List<Gauge> gauges) {
        for (Gauge gauge: gauges) {
            if (gauge.getRange().contains(km)) {
                return gauge;
            }
        }
        return null;
    }

    private static Segment findSegment(double km, List<Segment> segments) {
        for (Segment segment: segments) {
            if (segment.inside(km)) {
                return segment;
            }
        }
        return null;
    }


    private void writeRow4(CSVWriter writer, double wqkm[], D4EArtifact flys) {
        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();
        writer.writeNext(new String[] {
            kmf.format(wqkm[2]),
            wf.format(wqkm[0]),
            qf.format(RiverUtils.roundQ(wqkm[1])),
            RiverUtils.getLocationDescription(flys, wqkm[2])
        });
    }

    /** Write an csv-row at gauge location. */
    private void writeRow6(CSVWriter writer, double wqkm[], String wOrQDesc,
        D4EArtifact flys, String gaugeName) {
        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        writer.writeNext(new String[] {
            kmf.format(wqkm[2]),
            wf.format(wqkm[0]),
            qf.format(RiverUtils.roundQ(wqkm[1])),
            wOrQDesc,
            RiverUtils.getLocationDescription(flys, wqkm[2]),
            gaugeName
        });
    }

    private String getDesc(WQKms wqkms, boolean isQ, boolean isOfficial) {
        if (isOfficial) {
            return wqkms.getName();
        }

        D4EArtifact flys = (D4EArtifact) master;
        String colDesc = "";

        Double value = wqkms.getRawValue();

        if (flys instanceof WINFOArtifact && isQ) {
            colDesc = getCSVRowTitle((WINFOArtifact)flys, wqkms);
        }
        else if (!isQ) {
            colDesc = (value != null) ?
                Formatter.getWaterlevelW(context).format(value) : null;
        }

        if (flys instanceof WINFOArtifact) {
            if (wqkms != null && value != null) {
                WINFOArtifact winfo = (WINFOArtifact) flys;
                colDesc = RiverUtils.getNamedMainValue(winfo, value);
                // For 'W am Pegel' s
                if (colDesc == null) {
                    colDesc = Formatter.getWaterlevelW(context).format(value);
                }
            }
        }

        return colDesc == null ? "" : colDesc;
    }

    /**
     * Write "rows" of csv data from wqkms with writer.
     */
    protected void wQKms2CSV(
        CSVWriter writer,
        WQKms     wqkms,
        boolean   atGauge,
        boolean   isQ,
        boolean   isOfficial
    ) {
        log.debug("WaterlevelExporter.wQKms2CSV");

        // Skip constant data.
        if (wqkms instanceof ConstantWQKms) {
            return;
        }

        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        int      size   = wqkms.size();
        double[] result = new double[3];

        D4EArtifact flys        = (D4EArtifact) master;
        RangeAccess rangeAccess = new RangeAccess(flys);

        List<Gauge>  gauges     = RiverUtils.getGauges(flys);

        Gauge gauge = rangeAccess.getRiver().determineRefGauge(
            rangeAccess.getKmRange(), rangeAccess.isRange());

        String       gaugeName  = gauge.getName();
        String       desc       = "";
        String       notinrange = msg(
            CSV_NOT_IN_GAUGE_RANGE,
            DEFAULT_CSV_NOT_IN_GAUGE_RANGE);
        List<Segment> segments = null;
        boolean isFixRealize = false;

        double a = gauge.getRange().getA().doubleValue();
        double b = gauge.getRange().getB().doubleValue();
        long startTime = System.currentTimeMillis();

        desc = getDesc(wqkms, isQ, isOfficial);

        if (flys instanceof FixationArtifact) {
            // Get W/Q input per gauge for this case.
            FixRealizingAccess fixAccess = new FixRealizingAccess(flys);
            segments = fixAccess.getSegments();
            if (segments != null && !segments.isEmpty()) {
                isFixRealize = true;
            }
        }

        if (atGauge) { // "At gauge" needs more output.

            // Kms tend to be close together so caching the last sector
            // is a good time saving heuristic.
            Segment lastSegment = null;
            Gauge   lastGauge   = null;

            NumberFormat nf =
                Formatter.getFormatter(context.getMeta(), 0, 0);

            for (int i = 0; i < size; ++i) {
                result = wqkms.get(i, result);
                double km = result[2];

                if (segments != null) {
                    Segment found = lastSegment != null
                                    && lastSegment.inside(km)
                        ? lastSegment
                        : findSegment(km, segments);

                    if (found != null) {
                        desc = nf.format(found.getValues()[0]);
                    }
                    lastSegment = found;
                }

                String gaugeN;
                if (isFixRealize) {
                    Gauge found = lastGauge != null
                                  && lastGauge.getRange().contains(km)
                        ? lastGauge
                        : findGauge(km, gauges);

                    gaugeN = found != null ? found.getName() : notinrange;
                    lastGauge = found;
                }
                else {
                    // TODO issue1114: Take correct gauge
                    gaugeN = km >= a && km <= b
                        ? gaugeName
                        : notinrange;
                }
                writeRow6(writer, result, desc, flys, gaugeN);
            }
        }
        else { // Not at gauge.
            for (int i = 0; i < size; ++i) {
                result = wqkms.get(i, result);
                writeRow4(writer, result, flys);
            }
        }

        long stopTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Writing CSV took " +
                (float)(stopTime-startTime)/1000f + " secs.");
        }
    }


    /**
     * Generates the output in WST format.
     */
    protected void generateWST()
    throws    IOException
    {
        log.info("WaterlevelExporter.generateWST");

        int cols = data.get(0).length + officalFixings.size();
        WstWriter writer = new WstWriter(cols);

        writeWSTData(writer);

        writer.write(out);
    }


    protected void writeWSTData(WstWriter writer) {
        log.debug("WaterlevelExporter.writeWSTData");

        double[] result = new double[4];

        for (WQKms[] tmp: data) {
            for (WQKms wqkms: tmp) {
                if (wqkms instanceof ConstantWQKms) {
                    continue;
                }
                int size = wqkms != null ? wqkms.size() : 0;

                addWSTColumn(writer, wqkms);

                for (int i = 0; i < size; i++) {
                    result = wqkms.get(i, result);

                    writer.add(result);
                }

                if (wqkms instanceof WQCKms) {
                    addWSTColumn(writer, wqkms);

                    for (int c = 0; c < size; c++) {
                        result = wqkms.get(c, result);

                        writer.addCorrected(result);
                    }
                }
            }
        }

        // Append the official fixing interpolated to the calculation steps
        //
        // There was some confusion how to implement this. see flys/issue1620
        // for details.
        for (WQKms wqkms: officalFixings) {
            // To add some spaces here or to add them in the writer,..
            writer.addColumn(getDesc(wqkms, true, true));

            // Get all lines from the calculation
            Map <Double, WstLine> calcLines = writer.getLines();

            // All KM values where we have a point for
            TDoubleArrayList officialKms = wqkms.allKms();

            for (Map.Entry<Double, WstLine> entry : calcLines.entrySet()) {
                // Bad for perfomance but the user can wait a bit for WST
                // so lets not spend time optimizing too much,.. *hides*
                double km = entry.getKey().doubleValue();
                int idx = officialKms.indexOf(km);
                if (idx != -1) {
                    entry.getValue().add(wqkms.getW(idx), wqkms.getQ(idx));
                }
            }
        }
    }


    /**
     * Register a new column at <i>writer</i>. The name /
     * title of the column depends on the Q or W value of <i>wqkms</i>. If a Q
     * was selected and the Q fits to a named main value, the title is set to
     * the named main value. Otherwise, the name returned by
     * <i>WQKms.getName()</i> is set.
     *
     * @param writer The WstWriter.
     * @param wqkms The new WST column.
     */
    protected void addWSTColumn(WstWriter writer, WQKms wqkms) {
        if (wqkms instanceof ConstantWQKms) {
            return;
        }
        if (master instanceof WINFOArtifact) {
            writer.addColumn(getColumnTitle((WINFOArtifact) master, wqkms));
        }
        else {
            writer.addColumn(wqkms.getName());
        }
    }


    @Override
    protected void writePDF(OutputStream out) {
        log.debug("write PDF");
        WKmsJRDataSource source = createJRData();

        String jasperFile = Resources.getMsg(
                                context.getMeta(),
                                JASPER_FILE,
                                "/jasper/waterlevel_en.jasper");
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
            log.warn("Error generating PDF Report!", je);
        }
    }

    protected WKmsJRDataSource createJRData() {
        WKmsJRDataSource source = new WKmsJRDataSource();

        WQ_MODE mode    = RiverUtils.getWQMode((D4EArtifact)master);
        boolean atGauge = mode == WQ_MODE.QGAUGE || mode == WQ_MODE.WGAUGE;
        boolean isQ     = mode == WQ_MODE.QGAUGE || mode == WQ_MODE.QFREE;

        Double first = Double.NaN;
        Double last = Double.NaN;

        addMetaData(source);
        for (WQKms[] tmp: data) {
            for (WQKms wqkms: tmp) {
                addWKmsData(source, wqkms, atGauge, isQ, false);
                double[] firstLast = wqkms.getFirstLastKM();
                if (first.isNaN()) {
                    /* Initialize */
                    first = firstLast[0];
                    last = firstLast[1];
                }
                if (firstLast[0] > firstLast[1]) {
                    /* Calculating upstream we assert that it is
                     * impossible that the direction changes during this
                     * loop */
                    first = Math.max(first, firstLast[0]);
                    last = Math.min(last, firstLast[1]);
                } else if (firstLast[0] < firstLast[1]) {
                    first = Math.min(first, firstLast[0]);
                    last = Math.max(last, firstLast[1]);
                } else {
                    first = last = firstLast[0];
                }
            }
        }

        /* Append the official fixing at the bottom */
        for (WQKms wqkms: officalFixings) {
            addWKmsData(
                source, filterWQKms(wqkms, first, last), atGauge, isQ, true);
        }
        return source;
    }

    protected void addMetaData(WKmsJRDataSource source) {
        CallMeta meta = context.getMeta();

        D4EArtifact flys = (D4EArtifact) master;

        source.addMetaData ("river", RiverUtils.getRivername(flys));

        Locale locale = Resources.getLocale(meta);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        NumberFormat kmf = getKmFormatter();

        source.addMetaData("date", df.format(new Date()));

        RangeAccess rangeAccess = new RangeAccess(flys);
        double[] kms = rangeAccess.getKmRange();
        source.addMetaData("range",
                kmf.format(kms[0]) + " - " + kmf.format(kms[kms.length-1]));

        source.addMetaData("gauge", RiverUtils.getGaugename(flys));

        source.addMetaData("calculation", Resources.getMsg(
                                            locale,
                                            PDF_HEADER_MODE,
                                            "Waterlevel"));
    }

    protected void addWKmsData(
        WKmsJRDataSource source,
        WQKms wqkms,
        boolean atGauge,
        boolean isQ,
        boolean isOfficial
    ) {
        log.debug("WaterlevelExporter.addWKmsData");

        // Skip constant data.
        if (wqkms instanceof ConstantWQKms) {
            return;
        }

        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        int      size   = wqkms.size();
        double[] result = new double[3];

        D4EArtifact flys        = (D4EArtifact) master;
        RangeAccess rangeAccess = new RangeAccess(flys);

        Gauge gauge = rangeAccess.getRiver().determineRefGauge(
            rangeAccess.getKmRange(), rangeAccess.isRange());

        String       gaugeName  = gauge.getName();
        String       desc       = "";
        String       notinrange = msg(
            CSV_NOT_IN_GAUGE_RANGE,
            DEFAULT_CSV_NOT_IN_GAUGE_RANGE);

        double a = gauge.getRange().getA().doubleValue();
        double b = gauge.getRange().getB().doubleValue();

        desc = getDesc(wqkms, isQ, isOfficial);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < size; i ++) {
            result = wqkms.get(i, result);

            if (atGauge) {
                source.addData(new String[] {
                    kmf.format(result[2]),
                    wf.format(result[0]),
                    qf.format(RiverUtils.roundQ(result[1])),
                    desc,
                    RiverUtils.getLocationDescription(flys, result[2]),
                    result[2] >= a && result[2] <= b
                        ? gaugeName
                        : notinrange
                });
            }
            else {
                source.addData(new String[] {
                    kmf.format(result[2]),
                    wf.format(result[0]),
                    qf.format(RiverUtils.roundQ(result[1])),
                    desc,
                    RiverUtils.getLocationDescription(flys, result[2]),
                    result[2] >= a && result[2] <= b
                        ? gaugeName
                        : notinrange
                });
            }
        }

        long stopTime = System.currentTimeMillis();

        if (log.isDebugEnabled()) {
            log.debug("Writing PDF data took " +
                (float)(stopTime-startTime)/1000f + " secs.");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
