/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import gnu.trove.TDoubleArrayList;

import org.dive4elements.river.utils.DataUtil;


public class WKmsImpl
extends      NamedObjectImpl
implements   WKms
{
    protected TDoubleArrayList kms;
    protected TDoubleArrayList ws;

    public WKmsImpl() {
        super("");
        kms = new TDoubleArrayList();
        ws  = new TDoubleArrayList();
    }


    /**
     * Create named, empty WKms.
     */
    public WKmsImpl(String name) {
        super(name);
        kms = new TDoubleArrayList();
        ws  = new TDoubleArrayList();
    }


    public WKmsImpl(int capacity) {
        super("");
        kms = new TDoubleArrayList(capacity);
        ws  = new TDoubleArrayList(capacity);
    }


    public WKmsImpl(TDoubleArrayList kms, TDoubleArrayList ws) {
        this(kms, ws, "");
    }


    public WKmsImpl(
        TDoubleArrayList kms,
        TDoubleArrayList ws,
        String           name
    ) {
        super(name);
        this.kms = kms;
        this.ws  = ws;
    }


    /**
     * Add a W (in NN+m) for a km (in km).
     */
    public void add(double km, double w) {
        kms.add(km);
        ws .add(w);
    }


    @Override
    public double getW(int index) {
        return ws.getQuick(index);
    }


    @Override
    public double getKm(int index) {
        return kms.getQuick(index);
    }

    @Override
    public boolean guessWaterIncreasing() {
        return guessDataIncreasing(0.05f);
    }

    protected boolean guessDataIncreasing(float factor) {
        return DataUtil.guessDataIncreasing(ws, factor);
    }

    @Override
    public boolean guessRTLData() {
        return DataUtil.guessSameDirectionData(ws, allKms());
    }

    @Override
    public int size() {
        return kms.size();
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
    public WKms filteredKms(double from, double to) {
        WKmsImpl retval = new WKmsImpl(getName());
        for (int i = 0, S = size(); i < S; i++) {
            double km = getKm(i);
            if (km >= from && km <= to) {
                retval.add(km, getW(i));
            }
        }
        return retval;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
