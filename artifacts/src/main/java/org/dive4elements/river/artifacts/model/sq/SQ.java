/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.io.Serializable;

import java.util.Date;


/** Represents S/Q pairs. They are immutable! */
public class SQ implements Serializable {

    public interface Factory {
        SQ createSQ(double s, double q, Date d);
    }

    public static final Factory SQ_FACTORY = new Factory() {
        @Override
        public SQ createSQ(double s, double q, Date d) {
            return new SQ(s, q, d);
        }
    };

    public interface View {
        double getS(SQ sq);
        double getQ(SQ sq);
        Date getDate(SQ sq);
    }

    public static final View SQ_VIEW = new View() {
        @Override
        public double getS(SQ sq) {
            return sq.getS();
        }

        @Override
        public double getQ(SQ sq) {
            return sq.getQ();
        }

        @Override
        public Date getDate(SQ sq) {
            return sq.getDate();
        }
    };

    protected double s;
    protected double q;
    protected Date d;

    public SQ() {
    }

    public SQ(double s, double q, Date d) {
        this.s = s;
        this.q = q;
        this.d = d;
    }


    public double getS() {
        return s;
    }

    public double getQ() {
        return q;
    }

    public Date getDate() {
        return d;
    }

    public boolean isValid() {
        return !Double.isNaN(s) && !Double.isNaN(q);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
