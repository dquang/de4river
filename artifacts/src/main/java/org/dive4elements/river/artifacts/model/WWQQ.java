/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.math.Identity;

import org.dive4elements.river.utils.DoubleUtil;

import gnu.trove.TDoubleArrayList;

public class WWQQ
extends      WW
{
    public static class ApplyFunctionIterator
    extends             WW.ApplyFunctionIterator
    {
        public ApplyFunctionIterator(WWQQ ww) {
            super(ww, Identity.IDENTITY, Identity.IDENTITY);
        }

        @Override
        public void get(int idx, double [] wwqqPair) {
            WWQQ wwqq = (WWQQ)ww;
            wwqqPair[0] = function1.value(wwqq.getW(idx));
            wwqqPair[1] = function2.value(wwqq.getW2(idx));
            wwqqPair[2] = wwqq.getQ1(idx);
            wwqqPair[3] = wwqq.getQ2(idx);
        }
    } // class ApplyFunctionIterator

    protected TDoubleArrayList qs1;
    protected TDoubleArrayList qs2;

    public WWQQ() {
        this("");
    }

    public WWQQ(String name) {
        super(name);
    }

    public WWQQ(int capacity) {
        this(capacity, "");
    }

    public WWQQ(int capacity, String name) {
        super(capacity, name);
    }

    public WWQQ(
        String    name,
        double    startKm,
        Double    startDatum,
        double [] ws1,
        double [] qs1,
        double    endKm,
        Double    endDatum,
        double [] ws2,
        double [] qs2
    ) {
        super(name, startKm, startDatum, ws1, endKm, endDatum, ws2);
        this.qs1 = new TDoubleArrayList(qs1);
        this.qs2 = new TDoubleArrayList(qs2);
    }

    public double getQ1(int idx) {
        return qs1.getQuick(idx);
    }

    public double getQ2(int idx) {
        return qs2.getQuick(idx);
    }

    public double [] getQs1() {
        return qs1.toNativeArray();
    }

    public double [] getQs2() {
        return qs2.toNativeArray();
    }

    @Override
    public double [] get(int idx) {
        return get(idx, new double[4]);
    }

    @Override
    public double [] get(int idx, double [] dst) {
        dst[0] = ws .getQuick(idx);
        dst[1] = ws2.getQuick(idx);
        dst[2] = qs1.getQuick(idx);
        dst[3] = qs2.getQuick(idx);
        return dst;
    }

    @Override
    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws, ws2, qs1, qs2 });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
