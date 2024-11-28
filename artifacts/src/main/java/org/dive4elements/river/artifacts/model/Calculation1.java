/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Calculation1
extends      Calculation
{
    private static Logger log = LogManager.getLogger(Calculation1.class);

    protected double [] kms;
    protected double [] qs;
    protected double [] ws;
    protected double    refKm;

    public Calculation1() {
    }

    public Calculation1(
        double [] kms,
        double [] qs,
        double [] ws,
        double    refKm
    ) {
        this.kms   = kms;
        this.qs    = qs;
        this.ws    = ws;
        this.refKm = refKm;
    }

    public CalculationResult calculate(WstValueTable wst) {

        ArrayList<WQKms> results = new ArrayList<WQKms>();

        String    prefix;
        double [] origData;

        if (ws != null) { prefix = "W="; origData = ws; }
        else            { prefix = "Q="; origData = qs; }

        int oldNumProblems = numProblems();

        for (int i = 0; i < qs.length; i++) {

            double [] oqs = new double[kms.length];
            double [] ows = new double[kms.length];

            boolean success =
                wst.interpolate(qs[i], refKm, kms, ows, oqs, this) != null;

            int newNumProblems = numProblems();

            if (success) {
                WQKms result = new WQKms(kms, oqs, ows, prefix + origData[i]);
                if (oldNumProblems != newNumProblems) {
                    log.debug(
                        qs[i] + " caused " + (newNumProblems-oldNumProblems) +
                        " new problem(s).");
                    result.removeNaNs();
                }
                results.add(result);
            }

            oldNumProblems = newNumProblems;
        }

        return new CalculationResult(
            results.toArray(new WQKms[results.size()]),
            this);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
