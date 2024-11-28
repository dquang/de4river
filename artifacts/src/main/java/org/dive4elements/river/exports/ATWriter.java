/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.math.BigDecimal;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.WQ;
import org.dive4elements.river.artifacts.resources.Resources;

import org.apache.commons.math.analysis.UnivariateRealFunction;

import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.interpolation.LinearInterpolator;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import org.apache.commons.math.FunctionEvaluationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Write AT files. */
public class ATWriter
{
    private static Logger log = LogManager.getLogger(ATWriter.class);

    public static final int COLUMNS = 10;

    public static final String I18N_AT_HEADER =
        "export.discharge.curve.at.header";

    public static final String I18N_AT_GAUGE_HEADER =
        "export.discharge.curve.at.gauge.header";

    public static final String EMPTY = "         ";

    public static double getQ(int w, UnivariateRealFunction qFunc) {
        try {
            double val = qFunc.value(w);
            return val;
        }
        catch (FunctionEvaluationException aode) {
            // should not happen
            log.error("spline interpolation failed", aode);
            return Double.NaN;
        }
    }

    public static void printQ(PrintWriter out, double q) {
        String format;
             if (q <   1d) format = " % 8.3f";
        else if (q <  10d) format = " % 8.2f";
        else if (q < 100d) format = " % 8.1f";
        else {
            format = " % 8.0f";
            if (q > 1000d) q = Math.rint(q/10d)*10d;
        }
        out.printf(Locale.US, format, q);
    }

    protected static void printGaugeHeader(
        PrintWriter out,
        CallMeta    callMeta,
        String      river,
        double      km,
        String      gName,
        BigDecimal  datum,
        Date        date,
        String      unit
    ) {
        DateFormat f = DateFormat.getDateInstance(DateFormat.MEDIUM,
                Resources.getLocale(callMeta));
        out.print("*" + Resources.getMsg(
            callMeta,
            I18N_AT_GAUGE_HEADER,
            I18N_AT_GAUGE_HEADER,
            new Object[] { river, gName, f.format(date), datum, unit} ));
        out.print("\r\n");
    }

    protected static void printHeader(
        PrintWriter out,
        CallMeta    callMeta,
        String      river,
        double      km
    ) {
        out.print("*" + Resources.getMsg(
            callMeta,
            I18N_AT_HEADER,
            I18N_AT_HEADER,
            new Object[] { river, km } ));
        out.print("\r\n");
    }

    public static void write(
        WQ values,
        Writer writer,
        CallMeta meta,
        String river,
        double km,
        String gName,
        BigDecimal datum,
        Date date,
        String unit)
    throws IOException
    {
        int minW;
        int maxW;
        double minQ;
        double maxQ;

        UnivariateRealFunction qFunc;

        WQ wq = WQ.getFixedWQforExportAtGauge(values, datum);

        int [] bounds = wq.longestIncreasingWRangeIndices();

        if (bounds[1]-bounds[0] < 1) { // Only first w can be written out.
            minW = maxW = (int)Math.round(wq.getW(bounds[0]));
            minQ = maxQ = wq.getQ(bounds[0]);
            // constant function
            qFunc = new PolynomialFunction(new double [] { minQ });
            return;
        }

        /* example: bounds[0] = 0
         * bounds[1] = 5 -> we need to store 6 values.*/

        double [] ws = new double[bounds[1]-bounds[0] + 1];
        double [] qs = new double[ws.length];

        for (int i = 0; i < ws.length; i++) {
            int idx = bounds[0]+i;
            ws[i] = wq.getW(idx);
            qs[i] = wq.getQ(idx);
        }

        qFunc = ws.length < 3
            ? new LinearInterpolator().interpolate(ws, qs)
            : new SplineInterpolator().interpolate(ws, qs);

        minW = (int)Math.ceil(wq.getW(bounds[0]));
        maxW = (int)Math.floor(wq.getW(bounds[1]));
        minQ = wq.getQ(bounds[0]);
        maxQ = wq.getQ(bounds[1]);
        PrintWriter out = new PrintWriter(writer);

        // A header is required, because the desktop version of FLYS will skip
        // the first row.
        if (gName != null) {
            printGaugeHeader(out, meta, river, km, gName, datum, date, unit);
        }
        else {
            printHeader(out, meta, river, km);
        }

        int rest = minW % 10;

        int startW = minW - rest;

        if (log.isDebugEnabled()) {
            log.debug("startW: " + startW);
            log.debug("rest: " + rest);
            log.debug("maxW: " + maxW);
        }

        int col = 0;
        for (int w = startW; w <= maxW; w++) {
            if (col == 0) {
                out.printf(Locale.US, "%8d", w);
            }

            if (w < minW) {
                out.print(EMPTY);
            } else {
                double actQ = getQ(w, qFunc);
                if (Double.isNaN(actQ)) {
                    // Can't happen™
                    break;
                } else {
                    printQ(out, actQ);
                }
            }

            if (++col >= COLUMNS) {
                out.print("\r\n");
                col = 0;
            }
        }

        if (col > 0) {
            out.print("\r\n");
        }

        out.flush();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
