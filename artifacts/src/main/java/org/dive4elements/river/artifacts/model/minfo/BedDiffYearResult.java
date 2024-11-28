/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;


/** Result of a bed diff year calculation. */
public class BedDiffYearResult
extends BedDifferencesResult
{
    protected TDoubleArrayList diffsPerYear;
    protected TDoubleArrayList dataGap1;
    protected TDoubleArrayList dataGap2;
    protected TDoubleArrayList soundingWidth1;
    protected TDoubleArrayList soundingWidth2;
    protected Integer start;
    protected Integer end;
    protected String nameFirst;
    protected String nameSecond;
    protected int idFirst;
    protected int idSecond;

    public BedDiffYearResult () {
        super();
        this.diffsPerYear = new TDoubleArrayList();
        this.dataGap1 = new TDoubleArrayList();
        this.dataGap2 = new TDoubleArrayList();
        this.soundingWidth1 = new TDoubleArrayList();
        this.soundingWidth2 = new TDoubleArrayList();
    }

    /**
     * @param kms the stations the following parameters are connected to.
     * @param differences the height differences
     * @param heights1 the heights
     * @param heights2 the other heights
     * @param diffsPerYear the differences normalized per year in cm.
     */
    public BedDiffYearResult(
        TDoubleArrayList kms,
        TDoubleArrayList differences,
        TDoubleArrayList heights1,
        TDoubleArrayList heights2,
        TDoubleArrayList soundingWidth1,
        TDoubleArrayList soundingWidth2,
        TDoubleArrayList diffsPerYear,
        TDoubleArrayList dataGap1,
        TDoubleArrayList dataGap2,
        Integer start,
        Integer end,
        String nameFirst,
        String nameSecond,
        int idFirst,
        int idSecond
    ) {
        super(kms, differences, heights1, heights2);
        this.diffsPerYear = diffsPerYear;
        this.dataGap1 = dataGap1;
        this.dataGap2 = dataGap2;
        this.soundingWidth1 = soundingWidth1;
        this.soundingWidth2 = soundingWidth2;
        this.start = start;
        this.end = end;
        this.nameFirst = nameFirst;
        this.nameSecond = nameSecond;
        this.idFirst = idFirst;
        this.idSecond = idSecond;
    }

    public TDoubleArrayList getBedHeights() {
        return this.diffsPerYear;
    }

    public TDoubleArrayList getDataGap1() {
        return this.dataGap1;
    }

    public TDoubleArrayList getDataGap2() {
        return this.dataGap2;
    }

    public TDoubleArrayList getSoundingWidth1() {
        return this.soundingWidth1;
    }

    public TDoubleArrayList getSoundingWidth2() {
        return this.soundingWidth2;
    }

    public Integer getStart() {
        return this.start;
    }

    public void setStart(int value) {
        this.start = value;
    }

    public void setEnd(int value) {
        this.end = value;
    }

    public Integer getEnd() {
        return this.end;
    }

    /** Get name of the first BedHeight (minuend). */
    public String getNameFirst() {
        return this.nameFirst;
    }

    /** Get name of the second BedHeight (subtrahend). */
    public String getNameSecond() {
        return this.nameSecond;
    }

    /** Get id of the first BedHeight (minuend). */
    public int getIdFirst() {
        return this.idFirst;
    }

    /** Get id of the second BedHeight (subtrahend). */
    public int getIdSecond() {
        return this.idSecond;
    }

    public void addSoundingWidth1(double value) {
        this.soundingWidth1.add(value);
    }

    public void addSoundingWidth2(double value) {
        this.soundingWidth2.add(value);
    }

    public void addBedHeights(double value) {
        this.diffsPerYear.add(value);
    }

    public double[][] getSoundingWidth1Data() {
        return new double[][] {
            kms.toNativeArray(),
            soundingWidth1.toNativeArray()
        };
    }

    public double[][] getSoundingWidth2Data() {
        return new double[][] {
            kms.toNativeArray(),
            soundingWidth2.toNativeArray()
        };
    }

    public double[][] getDataGap1Data() {
        return new double[][] {
            kms.toNativeArray(),
            dataGap1.toNativeArray()
        };
    }

    public double[][] getDataGap2Data() {
        return new double[][] {
            kms.toNativeArray(),
            dataGap2.toNativeArray()
        };
    }

    public double[][] getHeightPerYearData() {
        return new double[][] {
            kms.toNativeArray(),
            diffsPerYear.toNativeArray()
        };
    }

    @Override
    public String getDiffDescription() {
        return nameFirst + " - " + nameSecond;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
