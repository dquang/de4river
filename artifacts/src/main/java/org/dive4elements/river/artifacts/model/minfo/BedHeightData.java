/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gnu.trove.TDoubleArrayList;
import org.dive4elements.river.artifacts.model.NamedObjectImpl;

public class BedHeightData
extends NamedObjectImpl
{
    private static Logger log = LogManager.getLogger(BedHeightData.class);

    protected TDoubleArrayList heights;
    protected TDoubleArrayList station;
    protected TDoubleArrayList data_gap;
    protected TDoubleArrayList soundingWidth;
    protected Integer year;

    public BedHeightData() {
        heights = new TDoubleArrayList();
        station = new TDoubleArrayList();
        data_gap = new TDoubleArrayList();
        soundingWidth = new TDoubleArrayList();
    }

    public BedHeightData(String name) {
        super(name);
        heights = new TDoubleArrayList();
        station = new TDoubleArrayList();
        data_gap = new TDoubleArrayList();
        soundingWidth = new TDoubleArrayList();
    }

    public BedHeightData(int capacity) {
        this(capacity, "");
    }

    public BedHeightData(int capacity, String name) {
        super(name);
        heights = new TDoubleArrayList(capacity);
        station = new TDoubleArrayList(capacity);
    }

    public void add(
        double value,
        double station,
        double gap,
        double sounding,
        Integer year
    ) {
        this.heights.add(value);
        this.station.add(station);
        if (year != null) {
            this.year = year;
        }
        this.data_gap.add(gap);
        this.soundingWidth.add(sounding);
    }

    public int size() {
        return heights.size();
    }

    public double getHeight(int idx) {
        return heights.getQuick(idx);
    }

    public double [] getHeights() {
        return heights.toNativeArray();
    }

    public double [] get(int idx) {
        return get(idx, new double [3]);
    }

    public double [] get(int idx, double [] dst) {
        dst[0] = heights.getQuick(idx);
        dst[1] = station.getQuick(idx);
        return dst;
    }

   public double minHeights() {
        return heights.min();
    }

    public TDoubleArrayList getStations() {
        return this.station;
    }

    public double getHeight(double station) {
        int index = this.station.indexOf(station);
        return index >= 0 ? heights.getQuick(index) : Double.NaN;
    }

    public Integer getYear() {
        return this.year;
    }

    public double getSoundingWidth(int idx) {
        return this.soundingWidth.getQuick(idx);
    }

    public double getDataGap(int idx) {
        return this.data_gap.getQuick(idx);
    }

    public double getSoundingWidth(double station) {
        int index = this.station.indexOf(station);
        return index >= 0 ? soundingWidth.getQuick(index): Double.NaN;
    }

    public double getDataGap(double station) {
        int index = this.station.indexOf(station);
        return index >= 0 ? data_gap.getQuick(index) : Double.NaN;
    }

    public double[] getSoundingWidths() {
        return this.soundingWidth.toNativeArray();
    }


    public static void removeNaNs(TDoubleArrayList [] arrays) {

        int dest = 0;

        int A = arrays.length;
        int N = arrays[0].size();

        OUTER: for (int i = 0; i < N; ++i) {
            for (int j = 0; j < A; ++j) {
                TDoubleArrayList a = arrays[j];
                double v = a.getQuick(i);
                if (Double.isNaN(v)) {
                    continue OUTER;
                }
                a.setQuick(dest, v);
            }
            ++dest;
        }

        if (dest < N) {
            for (int i = 0; i < A; ++i) {
                arrays[i].remove(dest, N-dest);
            }
        }
    }

    public void removeNaNs() {
        removeNaNs(new TDoubleArrayList [] { heights });
    }
}
