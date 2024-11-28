/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;


/** Two doubles and an int index. */
public class XY
implements   Comparable<XY>
{
    public static final double X_EPSILON = 1e-4;

    protected double x;
    protected double y;
    protected int    index;

    public XY() {
    }

    public XY(XY other) {
        this(other.x, other.y, other.index);
    }

    public XY(double x, double y, int index) {
        this.x     = x;
        this.y     = y;
        this.index = index;
    }

    @Override
    public int compareTo(XY other) {
        if (x + X_EPSILON < other.x) return -1;
        if (x > other.x + X_EPSILON) return +1;
        if (index < other.index)     return -1;
        if (index > other.index)     return +1;
        return 0;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double dot(double ox, double oy) {
        return x*ox + y*oy;
    }

    public double dot(XY other) {
        return dot(other.x, other.y);
    }

    public XY sub(XY other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public XY ortho() {
        double z = x;
        x = y;
        y = -z;
        return this;
    }

    public XY normalize() {
        double len = dot(this);

        if (len > 1e-6) {
            len = 1d/Math.sqrt(len);
            x *= len;
            y *= len;
        }

        return this;
    }

    // x*nx + y*ny + d = 0 <=> d = -x*nx -y*ny
    public double lineOffset(XY p) {
         return -x*p.x -y*p.y;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
