/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.utils.DataUtil;
import org.dive4elements.river.utils.DoubleUtil;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class W
extends      NamedObjectImpl
{
    private static Logger log = LogManager.getLogger(W.class);

    protected TDoubleArrayList ws;

    public W() {
        ws = new TDoubleArrayList();
    }

    public W(String name) {
        super(name);
        ws = new TDoubleArrayList();
    }

    public W(int capacity) {
        this(capacity, "");
    }

    public W(int capacity, String name) {
        super(name);
        ws = new TDoubleArrayList(capacity);
    }

    public void add(double value) {
        ws.add(value);
    }

    public int size() {
        return ws.size();
    }

    public double getW(int idx) {
        return ws.getQuick(idx);
    }

    public double [] getWs() {
        return ws.toNativeArray();
    }

    public double [] get(int idx) {
        return get(idx, new double [1]);
    }

    public double [] get(int idx, double [] dst) {
        dst[0] = ws.getQuick(idx);
        return dst;
    }

    public double minWs() {
        return ws.min();
    }

    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws });
    }

    public boolean guessWaterIncreasing() {
        return guessWaterIncreasing(0.05f);
    }

    public boolean guessWaterIncreasing(float factor) {
        return DataUtil.guessDataIncreasing(ws, factor);
    }

    public int [] longestIncreasingWRangeIndices() {
        return longestIncreasingWRangeIndices(new int[2]);
    }

    public int [] longestIncreasingWRangeIndices(int [] bounds) {

        int N = size();
        int start = 0;
        int stop  = 0;

        double lastW = Double.MAX_VALUE;

        for (int i = 0; i < N; ++i) {
            double v = ws.getQuick(i);
            if (v <= lastW) {
                if (stop-start > bounds[1]-bounds[0]) {
                    bounds[0] = start;
                    bounds[1] = stop;
                    if (log.isDebugEnabled()) {
                        log.debug("new range: " +
                            bounds[0] + " - " + bounds[1] + " (" +
                            ws.getQuick(bounds[0]) + ", " +
                            ws.getQuick(bounds[1]) + ")");

                    }
                }
                start = stop = i;
            }
            else {
                stop = i;
            }
            lastW = v;
        }

        if (stop-start > bounds[1]-bounds[0]) {
            bounds[0] = start;
            bounds[1] = stop;
            if (log.isDebugEnabled()) {
                log.debug("new range @end: " +
                    bounds[0] + " - " + bounds[1] + " (" +
                    ws.getQuick(bounds[0]) + ", " +
                    ws.getQuick(bounds[1]) + ")");
            }
        }

        return bounds;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
