/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.minfo;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import java.io.OutputStream;
import java.io.IOException;

import java.text.NumberFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.access.SedimentLoadAccess;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataResult.Fraction;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataResult;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.exports.AbstractExporter;

import org.dive4elements.river.utils.Formatter;

import au.com.bytecode.opencsv.CSVWriter;


/**
 * Do CSV export for sediment load calculations (will also be shown in
 * client). */
public class SedimentLoadExporter
extends      AbstractExporter
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(SedimentLoadExporter.class);

    // i18n keys.
    public static final String CSV_KM =
        "export.csv.header.km";

    public static final String CSV_YEAR =
        "export.csv.header.year";

    public static final String CSV_COARSE =
        "export.sedimentload.csv.header.coarse";

    public static final String CSV_FINEMIDDLE =
        "export.sedimentload.csv.header.fine_middle";

    public static final String CSV_SAND =
        "export.sedimentload.csv.header.sand";

    public static final String CSV_SUSP_SAND =
        "export.sedimentload.csv.header.susp_sand";

    public static final String CSV_SUSP_SAND_BB =
        "export.sedimentload.csv.header.susp_sand_bed";

    public static final String CSV_SUSP_SEDIMENT =
        "export.sedimentload.csv.header.suspended_sediment";

    public static final String CSV_BED_LOAD =
        "export.sedimentload.csv.header.bed_load";

    public static final String CSV_BED_LOAD_SUSP_SAND =
        "export.sedimentload.csv.header.bed_load_susp_sand";

    public static final String CSV_TOTAL =
        "export.sedimentload.csv.header.total";

    private static final String[] FRACTION_ORDER = {
        "suspended_sediment",
        "susp_sand",
        "susp_sand_bed",
        "sand",
        "fine_middle",
        "coarse",
        "bed_load",
        "bed_load_susp_sand",
        "total"
    };

    /** Collected results. */
    private SedimentLoadDataResult result;

    /** Empty constructor. */
    public SedimentLoadExporter() {
    }

    /** Process all stored data and write csv. */
    @Override
    protected void writeCSVData(CSVWriter writer) throws IOException {
        if (result == null) {
            return;
        }
        writeCSVHeader(writer);

        /* Prepare the values. The order of the fractions is given by the
         * header and thus static. */

        /* The result is ordered by the periods. For each period there is
         * then a map of km-fraction pairs which are the actual result. */

        TreeMap <String, TreeMap <Double, Double[]>> result_map =
            new TreeMap<String, TreeMap<Double, Double[]>>();
        for (int i = 0; i < FRACTION_ORDER.length; i++) {
            String name = FRACTION_ORDER[i];
            List<Fraction> fractions = result.getFractionsByName(name);
            if (fractions == null) {
                continue;
            }
            for (Fraction fract: fractions) {
                String period = fract.getPeriod();
                TreeMap<Double, Double[]> cur_map;
                if (result_map.containsKey(period)) {
                    cur_map = result_map.get(period);
                } else {
                    cur_map = new TreeMap<Double, Double[]>();
                    result_map.put(period, cur_map);
                }
                double[][] values = fract.getData();
                for (int j = 0; j < values[0].length; j++) {
                    Double km = values[0][j];
                    Double val = values[1][j];
                    Double[] old = cur_map.get(km);
                    if (old == null) {
                        old = new Double[FRACTION_ORDER.length];
                        for (int k = 0; k < old.length; k++) {
                            old [k] = Double.NaN;
                        }
                    }
                    old [i] = val;
                    cur_map.put(km, old);
                }
            }
        }
        for (String period: result_map.keySet()) {
            TreeMap<Double, Double[]> cur_map = result_map.get(period);
            for (Double km: cur_map.keySet()) {
                writeRecord(writer, km, period, cur_map.get(km));
            }
        }
    }


    /** Return space when val is NaN, apply NumberFormat otherwise. */
    private String numberToString(NumberFormat valf, double val) {
        if (Double.isNaN(val)) {
            return " ";
        }
        return valf.format(val);
    }

    /** Write a line. */
    private void writeRecord(
        CSVWriter writer,
        double km,
        String years,
        Double[] fractions
    ) {
        NumberFormat kmf = Formatter.getCalculationKm(context.getMeta());
        NumberFormat valf = Formatter.getFormatter(context.getMeta(), 0, 2);

        String[] record = new String[fractions.length+2];
        record[0] = kmf.format(km);
        record[1] = years;
        for (int i = 0; i < fractions.length; ++i) {
            record[i+2] = numberToString(valf,  fractions[i]);
        }

        writer.writeNext(record);
    }

    /** Writes i18ned header for csv file/stream. */
    protected void writeCSVHeader(CSVWriter writer) {
        log.debug("writeCSVHeader()");

        List<String> header = new LinkedList<String>();
        SedimentLoadAccess access =
            new SedimentLoadAccess((D4EArtifact) master);

        String unit = msg("state.minfo." + access.getUnit());

        header.add(msg(CSV_KM));
        header.add(msg(CSV_YEAR));
        for (String head: new String[] {
                CSV_SUSP_SEDIMENT,
                CSV_SUSP_SAND,
                CSV_SUSP_SAND_BB,
                CSV_SAND,
                CSV_FINEMIDDLE,
                CSV_COARSE,
                CSV_BED_LOAD,
                CSV_BED_LOAD_SUSP_SAND,
                CSV_TOTAL
            }) {
            header.add(msg(head, new Object[] { unit }));
        }
        writer.writeNext(header.toArray(new String[header.size()]));
    }

    /** Store data internally, accepting only SedimentLoadResults[] in
     * calculationresults data. */
    @Override
    protected void addData(Object data) {
        if (!(data instanceof CalculationResult)) {
            log.warn("Invalid data type.");
            return;
        }
        Object d = ((CalculationResult)data).getData();

        if (!(d instanceof SedimentLoadDataResult)) {
            log.warn("Invalid result object.");
            return;
        }
        log.debug("addData: Data added.");
        result = (SedimentLoadDataResult)d;
    }

    /** Write PDF to outputstream (not implemented yet). */
    @Override
    protected void writePDF(OutputStream out) {
        log.warn("Not implemented.");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
