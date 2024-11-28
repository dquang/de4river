/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.io.Serializable;



public class SQResult implements Serializable {

    public static final int NUMBER_FRACTIONS = 7;

    public static final int FRACTION_A = 0;
    public static final int FRACTION_B = 1;
    public static final int FRACTION_C = 2;
    public static final int FRACTION_D = 3;
    public static final int FRACTION_E = 4;
    public static final int FRACTION_F = 5;
    public static final int FRACTION_G = 6;

    protected double km;
    protected SQFractionResult[] fractions;

    public SQResult() {
        this(0d, new SQFractionResult[NUMBER_FRACTIONS]);
    }

    public SQResult(double km, SQFractionResult [] fractions) {
        this.km        = km;
        this.fractions = fractions;
    }

    public SQFractionResult getFraction(int idx) {
        return idx >= 0 && idx < fractions.length
            ? fractions[idx]
            : null;
    }

    public void setFraction(int idx, SQFractionResult fraction) {
        if (idx >= 0 && idx < fractions.length) {
            this.fractions[idx] = fraction;
        }
    }

    public static final String [] FRACTION_NAMES = {
        "A", "B", "C", "D", "E", "F", "G"
    };

    public String getFractionName(int idx) {
        return idx >= 0 && idx < FRACTION_NAMES.length
            ? FRACTION_NAMES[idx]
            : "";
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
