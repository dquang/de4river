/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


public class GaugeImpl implements Gauge {

    private String name;

    private double lower;
    private double upper;


    public GaugeImpl() {
    }


    public GaugeImpl(String name, double lower, double upper) {
        this.name  = name;
        this.lower = lower;
        this.upper = upper;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public void setLower(double lower) {
        this.lower = lower;
    }


    public double getLower() {
        return lower;
    }


    public void setUpper(double upper) {
        this.upper = upper;
    }


    public double getUpper() {
        return upper;
    }
}
