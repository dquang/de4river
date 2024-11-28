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


/**
 * This class represents a pool of data triples that consists of 'W', 'Q' and
 * 'KM' data.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQKms
extends      WQ
implements   WKms, QKms
{
    private static Logger log = LogManager.getLogger(WQKms.class);

    /** The array that contains the 'KMs' values. */
    protected TDoubleArrayList kms;


    public WQKms() {
        this("");
    }


    public WQKms(String name) {
        super(name);
        this.kms = new TDoubleArrayList();
    }


    public WQKms(int capacity) {
        this(capacity, "");
    }


    public WQKms(int capacity, String name) {
        super(capacity, name);
        this.kms = new TDoubleArrayList(capacity);
    }

    public WQKms(double [] kms, double [] qs, double [] ws) {
        this(kms, qs, ws, "");
    }

    public WQKms(double []kms, WQ wq) {
        this(kms, wq.getQs(), wq.getWs(), wq.getName());
    }

    public WQKms(
        TDoubleArrayList kms,
        TDoubleArrayList qs,
        TDoubleArrayList ws,
        String name
    ) {
        super(qs, ws, name);
        this.kms = kms;
    }

    public WQKms(double [] kms, double [] qs, double [] ws, String name) {
        super(qs, ws, name);
        this.kms = new TDoubleArrayList(kms);
    }

    /** Create a WQKms from WKms, filling the Qs with given q. */
    public static WQKms fromWKms(WKms wkms, double q) {
        TDoubleArrayList qs = new TDoubleArrayList(wkms.allKms().size());
        qs.fill(q);
        WQKms wqkms =
            new WQKms(wkms.allKms(), qs, wkms.allWs(), wkms.getName());
        return wqkms;
    }

    @Override
    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws, qs, kms });
    }

    /**
     * Adds a new row to this data pool.
     *
     * @param w a W.
     * @param q a Q.
     * @param km a kms.
     */
    public void add(double w, double q, double km) {
        super.add(w, q);
        kms.add(km);
    }

    @Override
    public double [] get(int idx) {
        return get(idx, new double [3]);
    }

    /**
     * This method returns a triple of W, Q and Kms in a single 3dim array.
     *
     * @param idx The position of the triple.
     * @param dst destination array
     *
     * @return a triple of [W, Q, Kms] in dst.
     */
    @Override
    public double [] get(int idx, double [] dst) {
        dst[0] = ws .getQuick(idx);
        dst[1] = qs .getQuick(idx);
        dst[2] = kms.getQuick(idx);
        return dst;
    }

    @Override
    public double getKm(int idx) {
        return kms.getQuick(idx);
    }

    @Override
    public TDoubleArrayList allKms() {
        return kms;
    }

    @Override
    public TDoubleArrayList allWs() {
        return ws;
    }

    @Override
    public TDoubleArrayList allQs() {
        return qs;
    }

    @Override
    public WKms filteredKms(double from, double to) {
        WQKms retval = new WQKms(getName());
        for (int i = 0, S = size(); i < S; i++) {
            double km = getKm(i);
            if (km >= from && km <= to) {
                retval.add(getW(i), getQ(i), km);
            }
        }
        return retval;
    }

    public double[] getKms() {
        return kms.toNativeArray();
    }

    /**
     * Returns a string that consist of the first and last kilometer.
     *
     * @return a string that consist of the first and last kilometer.
     */
    public String toString() {
        double from = getKm(0);
        double to   = getKm(size()-1);
        return from + " - " + to;
    }

    /**
     * Returns an array of two double values the first and last kilometer.
     *
     * @return a double array with the first and last km
     */
    public double[] getFirstLastKM() {
        /* Behold the first km might be larger then the last! */
        return new double[] {getKm(0), getKm(size()-1)};
    }

    private static final double EPS = 1e-5;

    public Double sameKm() {
        int s = size();
        if (s < 1) return null;
        if (s == 1) return getKm(0);
        double a = getKm(0);
        double b = getKm(s-1);
        double c = getKm(s/2);

        return Math.abs(a-b) > EPS
            || Math.abs(a-c) > EPS
            || Math.abs(b-c) > EPS
            ? null
            : a;
    }

    @Override
    public boolean guessRTLData() {
        return DataUtil.guessSameDirectionData(ws, allKms());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
