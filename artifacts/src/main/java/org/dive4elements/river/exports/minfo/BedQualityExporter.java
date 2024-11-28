/* Copyright (C) 2011, 2012, 2013, 2015 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.minfo;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import au.com.bytecode.opencsv.CSVWriter;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.minfo.BedQualityResult;
import org.dive4elements.river.artifacts.model.minfo.BedQualityResultValue;
import org.dive4elements.river.exports.AbstractExporter;
import org.dive4elements.river.utils.Formatter;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;


public class BedQualityExporter
extends AbstractExporter
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(BedQualityExporter.class);

    private static final String CSV_HEADER_KM =
        "export.minfo.bedquality.km";
    private static final String CSV_HEADER_BASE =
        "export.minfo.bedquality";

    private BedQualityResult[] results;

    public BedQualityExporter() {
        results = new BedQualityResult[0];
    }

    /** Create double[] containing the data for rows in csv. */
    private List<double[]> createDataRows() {

        double[] kms = new RangeAccess((D4EArtifact) master).getKmSteps();

        int cols = 1;
        for (BedQualityResult result: results) {
            for (BedQualityResultValue value :result.getValues()) {
                if (value.isInterpolateable()) {
                    /* Only add results that can be interpolated */
                    cols++;
                }
            }
        }
        if (cols == 1) {
            return new ArrayList<double[]>();
        }

        List<double[]> rows = new ArrayList<double[]>(kms.length);
        for (double km: kms) {
            double[] row = new double[cols];
            row[0] = km;
            int resultOffset = 1;
            for (BedQualityResult result: results) {
                int i = resultOffset;
                for (BedQualityResultValue value: result.getValues()) {
                    if (value.isInterpolateable()) {
                        row[i++] = value.getDataInterpolated(km);
                    }
                }
                resultOffset = i;
            }
            rows.add(row);
        }

        return rows;
    }

    @Override
    protected void writeCSVData(CSVWriter writer) throws IOException {
        writeCSVHeader(writer);

        NumberFormat nf = Formatter.getFormatter(context, 1, 3);

        for (double[] d : createDataRows()) {
            List<String> cells = new ArrayList<String>(d.length);
            for (int i = 0; i < d.length; i++) {
                if (!Double.isNaN(d[i])) {
                    cells.add(nf.format(d[i]));
                }
                else {
                    cells.add("");
                }
            }
            writer.writeNext(cells.toArray(new String[cells.size()]));
        }
    }

    @Override
    protected void writePDF(OutputStream out) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addData(Object data) {
        log.debug("addData()");
        if (!(data instanceof CalculationResult)) {
            log.warn("Invalid data type.");
            return;
        }
        Object[] d = (Object[])((CalculationResult)data).getData();

        if (!(d instanceof BedQualityResult[])) {
            log.warn("Invalid result object.");
            return;
        }
        results = (BedQualityResult[])d;
    }

    protected void writeCSVHeader(CSVWriter writer) {
        log.debug("writeCSVHeader()");

        List<String> header = new ArrayList<String>();
        if (results == null)  {
            writer.writeNext(header.toArray(new String[header.size()]));
            return;
        }

        header.add(msg(CSV_HEADER_KM, "km"));
        DateFormat df = Formatter.getDateFormatter(
            context.getMeta(), "dd.MM.yyyy");
        for (BedQualityResult result: results) {
            String d1 = df.format(result.getDateRange().getFrom());
            String d2 = df.format(result.getDateRange().getTo());
            for (BedQualityResultValue value: result.getValues()) {
                if (!value.isInterpolateable()) {
                    continue;
                }
                String i18n;
                if (value.isDiameterResult()) {
                    i18n = CSV_HEADER_BASE + ".diameter." + value.getType();
                    header.add(msg(i18n, i18n)
                        + " - " + value.getName().toUpperCase()
                        + " - " + d1 + "-" + d2);
                } else {
                    i18n = CSV_HEADER_BASE + "." + value.getName()
                        + "." + value.getType();
                    header.add(msg(i18n, i18n) + " - " + d1 + "-" + d2);
                }
            }
        }
        writer.writeNext(header.toArray(new String[header.size()]));
    }
}
