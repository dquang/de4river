/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.fixings;

import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.math.fitting.Function;

import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.exports.ATWriter;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.Locale;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Export Fixation Analysis Results to AT. */
public class FixATWriter
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(FixATWriter.class);

    public static final String I18N_HEADER_KEY =
        "fix.export.at.header";

    public static final String I18N_GAUGE_HEADER_KEY =
        "fix.export.at.gauge.header";

    public static final String I18N_HEADER_DEFAULT =
        "Exported fixings discharge curve for {0} {0}-km: {1}";

    public static final String I18N_GAUGE_HEADER_DEFAULT =
        "Exported fixings discharge curve for {0}, gauge: {1} datum[{3}] = {2}";

    public static final String [] Q_MAX_COLUMN = new String [] { "max_q" };

    private static final int    MAX_ITERATIONS = 10000;
    private static final double EPSILON        = 1e-8;
    private static final double MIN_Q          = 1e-4;

    protected Function   function;
    protected Parameters parameters;

    public FixATWriter() {
    }

    public FixATWriter(Function function, Parameters parameters) {
        this.function   = function;
        this.parameters = parameters;
    }

    public void write(
        Writer   writer,
        CallMeta meta,
        River    river,
        double   km
    )
    throws IOException {
        PrintWriter out = new PrintWriter(writer);

        int subtractPNP = 0;
        // Special case handling for at's at gauges
        Gauge gauge = river.determineGaugeAtStation(km);
        if (gauge != null) {
            printGaugeHeader(out, meta, river, gauge);
            subtractPNP = (int)Math.round(gauge.getDatum().doubleValue() * 100);
        } else {
            printHeader(out, meta, river.getName(), km);
        }

        double [] coeffs = parameters.interpolate(
            "km", km, function.getParameterNames());

        double [] qMax = parameters.interpolate(
            "km", km, Q_MAX_COLUMN);

        if (coeffs == null || qMax == null) {
            log.debug("No data found at km " + km + ".");
            return;
        }

        org.dive4elements.river.artifacts.math.Function funcInst =
            function.instantiate(coeffs);

        // Increase Q max about 5%.
        qMax[0] += Math.abs(qMax[0])*0.05;

        double wMax = funcInst.value(qMax[0]);

        if (Double.isNaN(wMax) || wMax < 0d) {
            log.debug("function '" + function.getName() +
                "' eval failed at " + wMax);
            return;
        }

        Function inverse = function.getInverse();

        org.dive4elements.river.artifacts.math.Function invInst =
            inverse.instantiate(coeffs);

        double wMin = minW(invInst, wMax, qMax[0]);

        double wMinCM = wMin * 100d - subtractPNP;
        double wMaxCM = wMax * 100d;

        if ((wMinCM - (int)wMinCM) > 0d) {
            wMinCM = (int)wMinCM + 1d;
        }

        int wRow = ((int)wMinCM / 10) * 10;

        double w = (wMinCM + subtractPNP) / 100.0;

        int wcm = ((int)wMinCM) % 10;

        if (log.isDebugEnabled()) {
            log.debug("km: " + km);
            log.debug("wMinCM: " + wMinCM);
            log.debug("wMaxCM: " + wMaxCM);
            log.debug("wcm: " + wcm);
            log.debug("subtractPNP: " + subtractPNP);
            log.debug("coeffs: " + Arrays.toString(coeffs));
            log.debug("function description: " + inverse.getDescription());
        }

        out.printf(Locale.US, "%8d", wRow);

        for (int i = 0; i < wcm; i++) {
            out.print(ATWriter.EMPTY);
        }

        for (;;) {
            while (wcm++ < 10) {
                if (w > wMax) {
                    break;
                }
                double q = invInst.value(w);
                if (Double.isNaN(w)) {
                    out.print(ATWriter.EMPTY);
                }
                else {
                    ATWriter.printQ(out, q);
                }
                w += 0.01d;
            }
            out.println();
            if (w > wMax) {
                break;
            }
            out.printf(Locale.US, "%8d", (wRow += 10));
            wcm = 0;
        }

        out.flush();
    }

    protected void printHeader(
        PrintWriter out,
        CallMeta    meta,
        String      river,
        double      km
    ) {
        out.println("*" + Resources.format(
            meta,
            I18N_HEADER_KEY,
            I18N_HEADER_DEFAULT,
            river, km));
    }

    protected void printGaugeHeader(
        PrintWriter out,
        CallMeta    meta,
        River       river,
        Gauge       gauge
    ) {
        out.println("*" + Resources.format(
            meta,
            I18N_GAUGE_HEADER_KEY,
            I18N_GAUGE_HEADER_DEFAULT,
            new Object[] { river.getName(), gauge.getName(),
                gauge.getDatum(), river.getWstUnit().getName() }));
    }

    private static double minW(
        org.dive4elements.river.artifacts.math.Function function,
        double maxW,
        double maxQ
    ) {
        double stepWidth = 10d;

        double lastW = maxW;
        double lastQ = maxQ;

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            double w = lastW - stepWidth;

            if (w <= 0) {
                return 0;
            }

            double q = function.value(w);

            if (Double.isNaN(q) || q > lastQ || q < MIN_Q) {
                if (stepWidth < EPSILON) {
                    break;
                }
                stepWidth *= 0.5d;
                continue;
            }

            lastW = w;
            lastQ = q;
        }

        return lastW;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
