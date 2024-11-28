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


public abstract class BedDifferencesResult
implements            Serializable
{

    protected TDoubleArrayList kms;
    protected TDoubleArrayList differences;
    protected TDoubleArrayList height1;
    protected TDoubleArrayList height2;

    public BedDifferencesResult () {
        kms = new TDoubleArrayList();
        differences = new TDoubleArrayList();
        height1 = new TDoubleArrayList();
        height2 = new TDoubleArrayList();
    }

    public BedDifferencesResult(
        TDoubleArrayList kms,
        TDoubleArrayList differences,
        TDoubleArrayList heights1,
        TDoubleArrayList heights2
    ) {
        this.kms = kms;
        this.differences = differences;
        this.height1 = heights1;
        this.height2 = heights2;
    }

    public TDoubleArrayList getKms() {
        return this.kms;
    }

    public TDoubleArrayList getDifferences() {
        return this.differences;
    }

    public void addKm(double value) {
        this.kms.add(value);
    }

    public void addDifference(double value) {
        this.differences.add(value);
    }

    public void addHeight1(double value) {
        this.height1.add(value);
    }

    public void addHeight2(double value) {
        this.height2.add(value);
    }

    public double[][] getDifferencesData() {
        return new double[][] {
            kms.toNativeArray(),
            differences.toNativeArray()
        };
    }

    public TDoubleArrayList getHeights1() {
        return this.height1;
    }

    public TDoubleArrayList getHeights2() {
        return this.height2;
    }

    public double[][] getHeights1Data() {
        return new double[][] {
            kms.toNativeArray(),
            height1.toNativeArray()
        };
    }

    public double[][] getHeights2Data() {
        return new double[][] {
            kms.toNativeArray(),
            height2.toNativeArray()
        };
    }

    public abstract String getDiffDescription();
}
