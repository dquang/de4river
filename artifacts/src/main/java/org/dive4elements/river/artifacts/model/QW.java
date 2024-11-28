/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

public class QW
implements   Serializable
{
    protected double q;
    protected double w;

    public QW() {
    }

    public QW(double q, double w) {
        this.q = q;
        this.w = w;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
