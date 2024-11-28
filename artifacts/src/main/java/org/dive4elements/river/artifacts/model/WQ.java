/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.utils.DoubleUtil;

import gnu.trove.TDoubleArrayList;

import java.math.BigDecimal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class WQ
extends      W
{
    public static final Pattern NUMBERS_PATTERN =
        Pattern.compile("\\D*(\\d++.\\d*)\\D*");

    private static Logger log = LogManager.getLogger(WQ.class);

    protected TDoubleArrayList qs;

    public WQ() {
        this("");
    }

    public WQ(String name) {
        super(name);
        qs = new TDoubleArrayList();
    }

    public WQ(int capacity) {
        this(capacity, "");
    }


    public WQ(int capacity, String name) {
        super(capacity, name);
        qs = new TDoubleArrayList(capacity);
    }

    public WQ(double [] qs, double [] ws) {
        this(qs, ws, "");
    }

    public WQ(double [] qs, double [] ws, String name) {
        super(name);
        this.ws = new TDoubleArrayList(ws);
        this.qs = new TDoubleArrayList(qs);
    }

    public WQ(TDoubleArrayList qs, TDoubleArrayList ws, String name) {
        super(name);
        this.ws = ws;
        this.qs = qs;
    }


    public Double getRawValue() {
        if (name == null || name.length() == 0) {
            // this should never happen
            return null;
        }

        Matcher m = NUMBERS_PATTERN.matcher(name);

        if (m.matches()) {
            String raw = m.group(1);

            try {
                return Double.valueOf(raw);
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        return null;
    }

    public void add(double w, double q) {
        ws.add(w);
        qs.add(q);
    }

    public double getQ(int idx) {
        return qs.getQuick(idx);
    }

    @Override
    public double [] get(int idx) {
        return get(idx, new double [2]);
    }

    @Override
    public double [] get(int idx, double [] dst) {
        dst[0] = ws.getQuick(idx);
        dst[1] = qs.getQuick(idx);
        return dst;
    }

    public double [] getQs() {
        return qs.toNativeArray();
    }

    @Override
    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws, qs });
    }

    /** Returns either a modified copy or the same Object with fixed W values.
     * If a conversion takes place converted is set to true
     */
    public static WQ getFixedWQforExportAtGauge(WQ wq, BigDecimal datum) {
        // If we convert we work on a copy to avoid side effects.
        WQ ret = new WQ(wq.size(), wq.getName());

        double subtractDatum = datum == null ? 0 : datum.doubleValue();
        double [] data = new double[8];
        for (int i = 0, WQ = wq.size(); i < WQ; i++) {
            wq.get(i, data);
            ret.add((data[0] - subtractDatum)*100d, data[1]);
        }
        return ret;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
