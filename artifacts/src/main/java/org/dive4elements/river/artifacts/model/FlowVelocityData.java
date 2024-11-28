/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import gnu.trove.TDoubleArrayList;


public class FlowVelocityData implements Serializable {

    private TDoubleArrayList km;
    private TDoubleArrayList vMain;
    private TDoubleArrayList vTotal;
    /** Also called 'shearstress'. */
    private TDoubleArrayList tauMain;
    private TDoubleArrayList q;
    private String zone;
    private String type;

    public FlowVelocityData() {
        this.km      = new TDoubleArrayList();
        this.vMain   = new TDoubleArrayList();
        this.vTotal  = new TDoubleArrayList();
        this.tauMain = new TDoubleArrayList();
        this.q       = new TDoubleArrayList();
    }


    public void addKM(double km) {
        this.km.add(km);
    }

    public double getKM(int idx) {
        return km.get(idx);
    }

    public void addVMain(double vMain) {
        this.vMain.add(vMain);
    }

    public double getVMain(int idx) {
        return vMain.get(idx);
    }

    public void addVTotal(double vTotal) {
        this.vTotal.add(vTotal);
    }

    public double getVTotal(int idx) {
        return vTotal.get(idx);
    }

    public void addTauMain(double tauMain) {
        this.tauMain.add(tauMain);
    }

    public double getTauMain(int idx) {
        return tauMain.get(idx);
    }

    public void addQ(double q) {
        this.q.add(q);
    }

    public double getQ(int idx) {
        return q.get(idx);
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public int size() {
        return km.size();
    }


    public double[][] getMainChannelPoints() {
        double[][] points = new double[2][size()];

        for (int i = 0, n = size(); i < n; i++) {
            points[0][i] = getKM(i);
            points[1][i] = getVMain(i);
        }

        return points;
    }


    public double[][] getTotalChannelPoints() {
        double[][] points = new double[2][size()];

        for (int i = 0, n = size(); i < n; i++) {
            points[0][i] = getKM(i);
            points[1][i] = getVTotal(i);
        }

        return points;
    }


    public double[][] getQPoints() {
        double[][] points = new double[2][size()];

        for (int i = 0, n = size(); i < n; i++) {
            points[0][i] = getKM(i);
            points[1][i] = getQ(i);
        }

        return points;
    }


    public double[][] getTauPoints() {
        double[][] points = new double[2][size()];

        for (int i = 0, n = size(); i < n; i++) {
            points[0][i] = getKM(i);
            points[1][i] = getTauMain(i);
        }

        return points;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
