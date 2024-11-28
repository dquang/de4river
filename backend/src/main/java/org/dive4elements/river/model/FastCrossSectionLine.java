/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.util.List;
import java.util.Comparator;

import java.io.Serializable;

import java.awt.geom.Point2D;

public class FastCrossSectionLine
implements   Serializable
{
    public static final double EPSILON = 1e-5;

    public static final Comparator<FastCrossSectionLine> KM_CMP =
        new Comparator<FastCrossSectionLine>() {
            public int compare(
                FastCrossSectionLine a,
                FastCrossSectionLine b
            ) {
                double diff = a.km - b.km;
                if (diff < -EPSILON) return -1;
                return diff > +EPSILON ? +1 : 0;
            }
        };

    protected double km;
    protected List<Point2D> points;

    public FastCrossSectionLine() {
    }

    public FastCrossSectionLine(double km) {
        this.km = km;
    }

    public FastCrossSectionLine(double km, List<Point2D> points) {
        this(km);
        this.points = points;
    }

    public FastCrossSectionLine(CrossSectionLine csl) {
        Double kmBD = csl.getKm();
        km = kmBD != null ? kmBD.doubleValue() : 0d;
        points = csl.fetchCrossSectionLinesPoints();
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public void setPoints(List<Point2D> points) {
        this.points = points;
    }

    public double [][] fetchCrossSectionProfile() {
        return CrossSectionLine.fetchCrossSectionProfile(points);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
