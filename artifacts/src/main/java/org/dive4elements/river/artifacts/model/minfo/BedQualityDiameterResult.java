/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;

import java.io.Serializable;

public class BedQualityDiameterResult implements Serializable {

    public static enum DIAMETER_TYPE {
        D90,
        D84,
        D80,
        D75,
        D70,
        D60,
        D50,
        D40,
        D30,
        D25,
        D20,
        D16,
        D10,
        DM,
        DMIN,
        DMAX
    }

    protected DIAMETER_TYPE type;
    protected TDoubleArrayList kms;
    protected boolean empty;

    public BedQualityDiameterResult () {
        empty = true;
    }

    public BedQualityDiameterResult (
        String type,
        TDoubleArrayList km
    ) {
        if (km.size() > 0) {
            empty = false;
        }
        this.type = DIAMETER_TYPE.valueOf(type.toUpperCase());
        this.kms = km;
    }

    public DIAMETER_TYPE getType() {
        return this.type;
    }

    public TDoubleArrayList getKms() {
        return this.kms;
    }

    public void setType(DIAMETER_TYPE type) {
        this.type = type;
    }

    public boolean isEmpty() {
        return empty;
    }
}
