/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers.tim;

/** X,Y,Z- triple. */
public class Coordinate
{
    public double x;
    public double y;
    public double z;

    public Coordinate() {
    }

    public Coordinate(Coordinate c) {
        this(c.x, c.y, c.z);
    }

    public Coordinate(double x, double y) {
        this(x, y, 0d);
    }

    public Coordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public final double distanceSqr(double ox, double oy) {
        double dx = x - ox;
        double dy = y - oy;
        return dx*dx + dy*dy;
    }

    public final double distance(double xo, double yo) {
        return Math.sqrt(distanceSqr(xo, yo));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
