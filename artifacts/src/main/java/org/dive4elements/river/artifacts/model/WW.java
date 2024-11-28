/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.artifacts.math.Identity;

import org.dive4elements.river.utils.DoubleUtil;

import gnu.trove.TDoubleArrayList;

public class WW
extends      W
{
    public static class ApplyFunctionIterator
    {
        protected Function function1;
        protected Function function2;
        protected int      pos;
        protected WW       ww;

        public ApplyFunctionIterator(WW ww) {
            this(ww, Identity.IDENTITY, Identity.IDENTITY);
        }

        public ApplyFunctionIterator(
            WW       ww,
            Function function1,
            Function function2
        ) {
            this.ww        = ww;
            this.function1 = function1;
            this.function2 = function2;
        }

        public boolean hasNext() {
            return pos < ww.size();
        }

        public int size() {
            return ww.size();
        }

        public void reset() {
            pos = 0;
        }

        public WW getWW() {
            return ww;
        }

        public void get(int idx, double [] wwPair) {
            wwPair[0] = function1.value(ww.getW(idx));
            wwPair[1] = function2.value(ww.getW2(idx));
        }

        public void next(double [] wwPair) {
            get(pos++, wwPair);
        }
    } // class FunctionIterator

    protected TDoubleArrayList ws2;

    protected double startKm;
    protected double endKm;

    protected Double startDatum;
    protected Double endDatum;

    public WW() {
        this("");
    }

    public WW(String name) {
        super(name);
    }

    public WW(int capacity) {
        this(capacity, "");
    }

    public WW(int capacity, String name) {
        super(capacity, name);
        ws2 = new TDoubleArrayList(capacity);
    }

    public WW(
        String    name,
        double    startKm,
        Double    startDatum,
        double [] ws,
        double    endKm,
        Double    endDatum,
        double [] ws2
    ) {
        this.name       = name;
        this.ws         = new TDoubleArrayList(ws);
        this.ws2        = new TDoubleArrayList(ws2);
        this.startKm    = startKm;
        this.startDatum = startDatum;
        this.endKm      = endKm;
        this.endDatum   = endDatum;
    }

    public WW(String name, TDoubleArrayList ws, TDoubleArrayList ws2) {
        this.name = name;
        this.ws  = ws;
        this.ws2 = ws2;
    }

    public void add(double w1, double w2) {
        ws .add(w1);
        ws2.add(w2);
    }

    public double getW1(int idx) {
        return ws.getQuick(idx);
    }

    public double getW2(int idx) {
        return ws2.getQuick(idx);
    }

    public double [] getWs2() {
        return ws2.toNativeArray();
    }

    @Override
    public double [] get(int idx) {
        return get(idx, new double[2]);
    }

    @Override
    public double [] get(int idx, double [] dst) {
        dst[0] = ws .getQuick(idx);
        dst[1] = ws2.getQuick(idx);
        return dst;
    }

    public double getStartKm() {
        return startKm;
    }

    public void setStartKm(double startKm) {
        this.startKm = startKm;
    }

    public double getEndKm() {
        return endKm;
    }

    public void setEndKm(double endKm) {
        this.endKm = endKm;
    }

    public Double getStartDatum() {
        return startDatum;
    }

    public boolean startAtGauge() {
        return startDatum != null;
    }

    public boolean endAtGauge() {
        return endDatum != null;
    }

    public void setStartDatum(Double startDatum) {
        this.startDatum = startDatum;
    }

    public Double getEndDatum() {
        return endDatum;
    }

    public void setEndDatum(Double endDatum) {
        this.endDatum = endDatum;
    }

    @Override
    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws, ws2 });
    }

    public double minWs2() {
        return ws2.min();
    }

    // Note that we can also easily define a Function to do so.
    public double getRelHeight1Cm(int idx) {
        if (this.startAtGauge()) {
            return (ws.getQuick(idx) - getStartDatum())*100d;
        }
        else return ws.getQuick(idx)*100d;
    }

    public double getRelHeight2Cm(int idx) {
        if (this.endAtGauge()) {
            return (ws2.getQuick(idx) - getEndDatum())*100d;
        }
        else return ws2.getQuick(idx)*100d;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
