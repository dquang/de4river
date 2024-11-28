/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


public class BBox implements Serializable {

    public double lowerX;
    public double upperX;
    public double lowerY;
    public double upperY;


    public BBox() {
    }


    public BBox(double lowerX, double lowerY, double upperX, double upperY) {
        this.lowerX = lowerX;
        this.lowerY = lowerY;
        this.upperX = upperX;
        this.upperY = upperY;
    }


    public double getLowerX() {
        return lowerX;
    }


    public double getLowerY() {
        return lowerY;
    }


    public double getUpperX() {
        return upperX;
    }


    public double getUpperY() {
        return upperY;
    }


    public String toString() {
        return
            "(" + lowerX + "," + lowerY + ")" +
            "(" + upperX + "," + upperY + ")";
    }


    public static BBox getBBoxFromString(String bbox) {
        String[] coords = bbox != null ? bbox.split(" ") : null;

        if (coords == null || coords.length < 4) {
            return null;
        }

        try {
            return new BBox(
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                Double.parseDouble(coords[2]),
                Double.parseDouble(coords[3]));
        }
        catch (NumberFormatException nfe) {
            // do nothing here
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
