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
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Result from a SedimentLoadCalculation. */
public class SedimentLoadResult
implements Serializable
{
    private static final Logger log = LogManager
        .getLogger(SedimentLoadResult.class);
    protected int startYear;
    protected int endYear;
    protected SedimentLoadLSData load;

    public SedimentLoadResult() {
    }

    public SedimentLoadResult(
        int startYear,
        int endYear,
        SedimentLoadLSData load
    ) {
        this.startYear = startYear;
        this.endYear = endYear;
        this.load = load;
    }

    public SedimentLoadLSData getLoad() {
        return this.load;
    }

    public int getStartYear() {
        return this.startYear;
    }

    public void setStartYear(int year) {
        this.startYear = year;
    }

    public int getEndYear() {
        return this.endYear;
    }

    public void setEndYear(int year) {
        this.endYear = year;
    }

    public double[][] getTotalData () {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList total = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getTotal() > 0d) {
                k.add(km);
                total.add(load.getFraction(km).getTotal());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            total.toNativeArray()
        };
    }

    /** Search all SedimenLoads fractions for sand and returns
     * an array [[km1, km2][sand1, sand2]]. */
    public double[][] getSandData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList sand = new TDoubleArrayList();
        for(double km : kms) {
            if (load.getFraction(km).getSand() > 0d) {
                k.add(km);
                sand.add(load.getFraction(km).getSand());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            sand.toNativeArray()
        };
    }

    public double[][] getFineMiddleData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList fm = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getFineMiddle() > 0d) {
                k.add(km);
                fm.add(load.getFraction(km).getFineMiddle());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            fm.toNativeArray()
        };
    }

    public double[][] getCoarseData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList coarse = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getCoarse() > 0d) {
                k.add(km);
                coarse.add(load.getFraction(km).getCoarse());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            coarse.toNativeArray()
        };
    }

    public double[][] getSuspSandData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList ss = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getSuspSand() > 0d) {
                k.add(km);
                ss.add(load.getFraction(km).getSuspSand());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            ss.toNativeArray()
        };
    }

    public double[][] getSuspSandBedData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList ss = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getSuspSandBed() > 0d) {
                k.add(km);
                ss.add(load.getFraction(km).getSuspSandBed());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            ss.toNativeArray()
        };
    }

    public double[][] getSuspSedimentData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList ss = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getSuspSediment() > 0d) {
                k.add(km);
                ss.add(load.getFraction(km).getSuspSediment());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            ss.toNativeArray()
        };
    }

    public double[][] getTotalLoadData() {
        Set<Double> kms = this.load.getKms();
        TDoubleArrayList k = new TDoubleArrayList();
        TDoubleArrayList ss = new TDoubleArrayList();
        for (double km : kms) {
            if (load.getFraction(km).getLoadTotal() > 0d) {
                k.add(km);
                ss.add(load.getFraction(km).getLoadTotal());
            }
        }
        return new double [][] {
            k.toNativeArray(),
            ss.toNativeArray()
        };
    }

    public boolean hasCoarseData() {
        return getCoarseData()[0].length > 0;
    }

    public boolean hasFineMiddleData() {
        return getFineMiddleData()[0].length > 0;
    }

    public boolean hasSandData() {
        return getSandData()[0].length > 0;
    }

    public boolean hasSuspSandData() {
        return getSuspSandData()[0].length > 0;
    }

    public boolean hasSuspSandBedData() {
        return getSuspSandBedData()[0].length > 0;
    }

    public boolean hasSuspSedimentData() {
        return getSuspSedimentData()[0].length > 0;
    }

    public boolean hasTotalLoadData() {
        return getTotalLoadData()[0].length > 0;
    }

    public boolean hasTotalData() {
        return getTotalData()[0].length > 0;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

