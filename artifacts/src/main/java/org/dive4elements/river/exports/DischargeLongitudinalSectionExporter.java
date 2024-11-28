/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import au.com.bytecode.opencsv.CSVWriter;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.WQKms;

import java.text.NumberFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DischargeLongitudinalSectionExporter extends WaterlevelExporter {

    /** The log used in this exporter.*/
    private static Logger log =
        LogManager.getLogger(DischargeLongitudinalSectionExporter.class);


    public static final String CSV_KM_HEADER =
        "export.discharge.longitudinal.section.csv.header.km";

    public static final String CSV_W_HEADER =
        "export.discharge.longitudinal.section.csv.header.w";

    public static final String CSV_CW_HEADER =
        "export.discharge.longitudinal.section.csv.header.cw";

    public static final String CSV_Q_HEADER =
        "export.discharge.longitudinal.section.csv.header.q";

    public static final String DEFAULT_CSV_KM_HEADER = "Fluss-Km";
    public static final String DEFAULT_CSV_W_HEADER  = "W [NN + m]";
    public static final String DEFAULT_CSV_CW_HEADER = "W korr.";
    public static final String DEFAULT_CSV_Q_HEADER  = "Q [m\u00b3/s]";


    @Override
    protected void addData(Object d) {
        if (d instanceof CalculationResult) {
            d = ((CalculationResult)d).getData();
            if (d instanceof WQKms []) {
                data.add((WQKms [])d);
            }
        }
    }


    @Override
    protected void writeCSVHeader(
        CSVWriter writer,
        boolean   atGauge,
        boolean   isQ
    ) {
        log.info("DischargeLongitudinalSectionExporter.writeCSVHeader");

        writer.writeNext(new String[] {
            msg(CSV_KM_HEADER, DEFAULT_CSV_KM_HEADER),
            msg(CSV_W_HEADER, DEFAULT_CSV_W_HEADER),
            msg(CSV_CW_HEADER, DEFAULT_CSV_CW_HEADER),
            msg(CSV_Q_HEADER, DEFAULT_CSV_Q_HEADER),
            msg(CSV_Q_DESC_HEADER, DEFAULT_CSV_Q_DESC_HEADER)
        });
    }

    @Override
    protected void wQKms2CSV(
        CSVWriter writer,
        WQKms     wqkms,
        boolean   atGauge,
        boolean   isQ,
        boolean   isOfficial
    ) {
        log.debug("DischargeLongitudinalSectionExporter.wQKms2CSV");

        int      size   = wqkms.size();
        double[] result = new double[4];

        NumberFormat kmf = getKmFormatter();
        NumberFormat wf  = getWFormatter();
        NumberFormat qf  = getQFormatter();

        for (int i = 0; i < size; i ++) {
            result = wqkms.get(i, result);

            String name = wqkms.getName();
            String wc = "";
            if (wqkms instanceof WQCKms) {
                wc = wf.format(result[3]);
            }

            writer.writeNext(new String[] {
                kmf.format(result[2]),
                wf.format(result[0]),
                wc,
                qf.format(result[1]),
                name
            });
        }
    }


    @Override
    protected void addWSTColumn(WstWriter writer, WQKms wqkms) {
        String name = wqkms.getName();

        // is it a W or a Q mode?
        int wIdx = name.indexOf("W");
        int qIdx = name.indexOf("Q");

        String wq = null;
        if (wIdx >= 0) {
            wq = "W";
        }
        else if (qIdx >= 0) {
            wq = "Q";
        }

        // we just want to display the first W or Q value in the WST
        int start = name.indexOf("(");
        int end   = name.indexOf(")");

        String   tmp    = name.substring(start+1, end);
        String[] values = tmp.split(";");

        String column = wq + "=" + values[0];

        writer.addColumn(column);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
