/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.io.Serializable;

import java.util.ArrayList;

import gnu.trove.TDoubleArrayList;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.resources.Resources;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class MiddleBedHeightData implements Serializable,
                                            Comparable<MiddleBedHeightData> {

    /** Very private log. */
    private static final Logger log = LogManager.getLogger(
        MiddleBedHeightData.class);

    public static final String I18N_SINGLE_NAME =
        "facet.bedheight_middle.single";

    private int    year;
    private String evaluatedBy;
    private String description;

    private TDoubleArrayList km;
    private TDoubleArrayList middleHeight;
    private TDoubleArrayList uncertainty;
    private TDoubleArrayList soundingWidth;
    private TDoubleArrayList dataGap;
    private String type;
    private String locationSystem;
    private String oldElevationModel;
    private String curElevationModel;
    private String riverElevationModel;
    private ArrayList empty;


    protected MiddleBedHeightData(int year, int end, String eval, String desc,
        String curElevationModel, String oldElevationModel,
        String riverElevationModel, String type,
        String locationSystem) {
        this.year   = year;
        this.evaluatedBy = eval;
        this.description = desc;
        this.curElevationModel = curElevationModel;
        this.oldElevationModel = oldElevationModel;
        this.riverElevationModel = riverElevationModel;
        this.type = type;
        this.locationSystem = locationSystem;

        this.km            = new TDoubleArrayList();
        this.middleHeight  = new TDoubleArrayList();
        this.uncertainty   = new TDoubleArrayList();
        this.soundingWidth = new TDoubleArrayList();
        this.dataGap       = new TDoubleArrayList();
        this.empty         = new ArrayList();
    }

    public void addAll(double station, double height, double uncertainty,
        double soundingWidth, double dataGap, boolean isEmpty) {
        addKM(station);
        addMiddleHeight(height);
        addUncertainty(uncertainty);
        addSoundingWidth(soundingWidth);
        addDataGap(dataGap);
        addIsEmpty(isEmpty);
    }


    public int getYear() {
        return year;
    }

    public String getEvaluatedBy() {
        return evaluatedBy;
    }

    public String getDescription() {
        return description;
    }

    public String getCurElevationModel() {
        return this.curElevationModel;
    }

    public String getOldElevationModel() {
        return this.oldElevationModel;
    }

    public String getRiverElevationModel() {
        return this.riverElevationModel;
    }

    public String getType() {
        return this.type;
    }

    public String getLocationSystem() {
        return this.locationSystem;
    }

    protected void addKM(double km) {
        this.km.add(km);
    }

    public double getKM(int idx) {
        return km.get(idx);
    }

    public TDoubleArrayList getStations() {
        return this.km;
    }

    protected void addMiddleHeight(double middleHeight) {
        this.middleHeight.add(middleHeight);
    }

    public double getMiddleHeight(int idx) {
        return middleHeight.get(idx);
    }

    protected void addUncertainty(double uncertainty) {
        this.uncertainty.add(uncertainty);
    }

    public double getUncertainty(int idx) {
        return uncertainty.get(idx);
    }

    protected void addSoundingWidth(double soundingWidth) {
        this.soundingWidth.add(soundingWidth);
    }

    public double getSoundingWidth(int idx) {
        return soundingWidth.get(idx);
    }

    protected void addDataGap(double gap) {
        this.dataGap.add(gap);
    }

    public double getDataGap(int idx) {
        return dataGap.get(idx);
    }

    protected void addIsEmpty(boolean empty) {
        this.empty.add(empty);
    }

    public boolean isEmpty(int idx) {
        return (Boolean) empty.get(idx);
    }

    public int size() {
        return km.size();
    }


    /**
     * Get the points, ready to be drawn
     * @return [[km1, km2,...],[height1,height2,...]]
     */
    public double[][] getMiddleHeightsPoints() {
        double[][] points = new double[2][size()];

        for (int i = 0, n = size(); i < n; i++) {
            if (isEmpty(i)) {
                points[0][i] = getKM(i);
                points[1][i] = Double.NaN;
            }
            else {
                points[0][i] = getKM(i);
                points[1][i] = getMiddleHeight(i);
            }
        }

        return points;
    }


    public String getSoundingName(CallContext context) {
        return Resources.getMsg(
            context.getMeta(),
            I18N_SINGLE_NAME,
            new Object[] { getYear() }
        );
    }

    @Override
    public int compareTo(MiddleBedHeightData other) {

        int descCompared = description.compareTo(other.getDescription());

        if (descCompared != 0) {
            return descCompared;
        }

        if (size() == 0 || other.size() == 0) {
            if (size() == 0 && other.size() > 0) {
                return 1;
            } else if (size() > 0 && other.size() == 0) {
                return -1;
            } else if (size() == 0 && other.size() == 0) {
               return 0;
            }
        }

        if (getKM(0) < other.getKM(0)) {
            return -1;
        } else if (getKM(0) > other.getKM(0)) {
            return 1;
        }
        return 0;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
