/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.math.Linear;

import java.util.Arrays;

import java.io.Serializable;

public class FixingsColumn
implements   Serializable
{
    protected double [] kms;
    protected double [] ws;

    protected QRangeTree qs;

    public FixingsColumn() {
    }

    public FixingsColumn(
        double []  kms,
        double []  ws,
        QRangeTree qs
    ) {
        this.kms = kms;
        this.ws  = ws;
        this.qs  = qs;
    }

    public boolean getW(double km, double [] w) {
        return getW(km, w, 0);
    }

    public boolean getW(double km, double [] w, int index) {

        if (kms.length == 0 || km < kms[0] || km > kms[kms.length-1]) {
            w[index] = Double.NaN;
            return true;
        }

        int idx = Arrays.binarySearch(kms, km);

        if (idx >= 0) {
            w[index] = ws[idx];
            return true;
        }

        idx = -idx - 1;

        w[index] = Linear.linear(km, kms[idx-1], kms[idx], ws[idx-1], ws[idx]);
        return false;
    }

    public double getQ(double km) {
        return qs.findQ(km);
    }

    public QRangeTree getQRanges() {
        return qs;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
