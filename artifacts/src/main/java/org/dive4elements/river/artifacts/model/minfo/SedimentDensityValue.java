/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;
import java.io.Serializable;

/** A density value at a km, year. */
public class SedimentDensityValue implements Serializable
{

    private double km;
    private double density;
    private int year;

    public SedimentDensityValue() {
        this.km = 0d;
        this.density = 0d;
        this.year = 0;
    }

    public SedimentDensityValue(double km, double density, int year) {
        this.km = km;
        this.density = density;
        this.year = year;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
