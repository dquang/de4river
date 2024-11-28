/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;


public class BedDiameterData
extends BedQualityDiameterResult
{

    private TDoubleArrayList data;

    public BedDiameterData() {
        super();
    }

    public BedDiameterData (
        String type,
        TDoubleArrayList kms,
        TDoubleArrayList data) {
        super(type, kms);
        this.data = data;
    }

    public double[][] getDiameterData() {
        return new double[][]{kms.toNativeArray(), data.toNativeArray()};
    }
}
