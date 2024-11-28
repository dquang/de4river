/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.Date;

public class LogSQ extends SQ {

    public static final View LOG_SQ_VIEW = new View() {
        @Override
        public double getS(SQ sq) {
            return ((LogSQ)sq).getLogS();
        }

        @Override
        public double getQ(SQ sq) {
            return ((LogSQ)sq).getLogQ();
        }

        @Override
        public Date getDate(SQ sq) {
            return sq.getDate();
        }
    };

    public static final Factory LOG_SQ_FACTORY = new Factory() {
        @Override
        public SQ createSQ(double s, double q, Date d) {
            return new LogSQ(s, q, d);
        }
    };

    protected double logS;
    protected double logQ;

    protected boolean logTrans;

    public LogSQ() {
    }

    public LogSQ(double s, double q, Date d) {
        super(s, q, d);
    }

    /** important: We cannot process negative s/q. */
    @Override
    public boolean isValid() {
        return super.isValid() && s > 0d && q > 0d;
    }

    protected void ensureLogTrans() {
        if (!logTrans) {
            logTrans = true;
            logS = Math.log(s);
            logQ = Math.log(q);
        }
    }

    public double getLogS() {
        ensureLogTrans();
        return logS;
    }

    public double getLogQ() {
        ensureLogTrans();
        return logQ;
    }
}

