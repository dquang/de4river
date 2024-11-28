/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.utils.Formatter;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Calculation5
extends      Calculation
{
    private static Logger log = LogManager.getLogger(Calculation5.class);

    protected double    startKm;
    protected double [] endKms;

    public Calculation5() {
    }

    public Calculation5(double startKm, double [] endKms) {
        this.startKm = startKm;
        this.endKms  = endKms;
    }

    public CalculationResult calculate(
        WstValueTable       wst,
        Map<Double, Double> kms2gaugeDatums,
        CallContext         context
    ) {
        ArrayList<WWQQ> results = new ArrayList<WWQQ>();

        int numProblems = numProblems();

        CallMeta meta = context.getMeta();

        for (double endKm: endKms) {

            double [][] wws = wst.relateWs(startKm, endKm, this);
            int newNumProblems = numProblems();

            if (wws.length == 4) {
                WWQQ wwqq = new WWQQ(
                    generateName(meta, startKm, endKm),
                    startKm, kms2gaugeDatums.get(startKm), wws[0], wws[1],
                    endKm,   kms2gaugeDatums.get(endKm),   wws[2], wws[3]);

                if (newNumProblems > numProblems) {
                    wwqq.removeNaNs();
                }

                results.add(wwqq);
            }
            numProblems = newNumProblems;
        }

        return new CalculationResult(
            results.toArray(new WWQQ[results.size()]),
            this);
    }

    protected static String generateName(
        CallMeta meta,
        double   startKm,
        double   endKm
    ) {
        NumberFormat nf = Formatter.getCalculationKm(meta);
        return "W(km " + nf.format(startKm) +
               ") ~ W(km " + nf.format(endKm) + ")";
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
