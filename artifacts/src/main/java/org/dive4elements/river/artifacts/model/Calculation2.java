/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** ComputedDischargeCurve. */
public class Calculation2
extends      Calculation
{
    private static Logger log = LogManager.getLogger(Calculation2.class);

    protected double km;

    public Calculation2() {
    }

    public Calculation2(double km) {
        this.km = km;
    }

    private void dump(double [][] wqs) {
        double [] ws = wqs[0];
        double [] qs = wqs[1];

        String filename = "/tmp/computed-discharge-curve-" + km + "-" +
            System.currentTimeMillis() + ".txt";

        PrintWriter pw = null;
        try {
            pw =
                new PrintWriter(
                new FileWriter(filename));

            for (int i = 0; i < ws.length; ++i) {
                pw.println(ws[i] + " " + qs[i]);
            }

            pw.flush();
        }
        catch (IOException ioe) {
            log.error(ioe);
        }
        finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public CalculationResult calculate(WstValueTable wst) {

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Calculation2.calculate: km " + km);
        }

        double [][] wqs = wst.interpolateWQ(km, this);

        if (debug) {
            if (hasProblems()) {
                log.debug("problems: " + problemsToString());
            }
            log.debug("wqs: " + wqs);
            if (wqs != null && wqs[0] != null) {
                log.debug("wqs length: " + wqs[0].length);
                // TODO: Uncomment to see the data externally.
                //dump(wqs);
            }
        }

        if (wqs == null || wqs[0].length == 0) {
            addProblem("cannot.compute.discharge.curve");
            return new CalculationResult(new WQKms[0], this);
        }

        double [] ws = wqs[0];
        double [] qs = wqs[1];
        double [] kms = new double[ws.length];

        Arrays.fill(kms, km);

        WQKms wqkms = new WQKms(kms, qs, ws, String.valueOf(km));

        if (hasProblems()) {
            log.debug("found " + numProblems() + " problems.");
            wqkms.removeNaNs();
        }

        return new CalculationResult(new WQKms[] { wqkms }, this);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
