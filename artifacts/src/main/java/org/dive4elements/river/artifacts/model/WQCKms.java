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

/**
 * This class represents a pool of data triples that consists of 'W', 'Q' and
 * 'KM' data with corrected 'W' values computed by a BackJumpCorrector.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WQCKms
extends      WQKms
{
    protected TDoubleArrayList cws;

    public WQCKms() {
    }

    public WQCKms(WQKms other, double [] cws) {
        this.ws  = other.ws;
        this.qs  = other.qs;
        this.kms = other.kms;
        this.cws = new TDoubleArrayList(cws);
    }


    public WQCKms(double[] kms, double[] qs, double[] ws, double[] cws) {
        super(kms, qs, ws);

        this.cws = new TDoubleArrayList(cws);
    }

    @Override
    public void removeNaNs() {
        DoubleUtil.removeNaNs(new TDoubleArrayList [] { ws, qs, cws, kms });
    }

    /**
     * Adds a new row to this data pool with corrected W.
     *
     * @param w a W.
     * @param q a Q.
     * @param kms a Kms.
     * @param cw The corrected W.
     */
    public void add(double w, double q, double kms, double cw) {
        super.add(w, q, kms);
        cws.add(cw);
    }

    @Override
    public double[] get(int idx) {
        return get(idx, new double[4]);
    }

    /**
     * This method returns a 4dim array of W, Q,Kms and corrected W.
     *
     * @param idx The position of the triple.
     * @param dst destination array
     *
     * @return a 4dim array of [W, Q, Kms, CW] in dst.
     */
    @Override
    public double[] get(int idx, double[] dst) {
        dst = super.get(idx, dst);

        if (dst.length < 4) {
            return dst;
        }

        if (cws != null && cws.size() > idx) {
            dst[3] = cws.getQuick(idx);
        }

        return dst;
    }

    public double getC(int idx) {
        return cws.getQuick(idx);
    }


    /**
     * Returns the double array of corrected W values.
     *
     * @return the double array of corrected W values.
     */
    public double[] getCWs() {
        return cws.toNativeArray();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
