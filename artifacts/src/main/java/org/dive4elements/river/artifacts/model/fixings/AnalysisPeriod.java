/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.model.DateRange;

import java.io.Serializable;

public class AnalysisPeriod
implements   Serializable
{
    protected DateRange dateRange;
    protected QWD []    qwds;
    protected QWD []    qSectorAverages;
    protected double [] qSectorStdDevs;

    public AnalysisPeriod() {
    }

    public AnalysisPeriod(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public AnalysisPeriod(DateRange dateRange, QWD [] qwds) {
        this(dateRange);
        this.dateRange = dateRange;
        this.qwds      = qwds;
    }

    public AnalysisPeriod(
        DateRange dateRange,
        QWD []    qwds,
        QWD []    qSectorAverages,
        double [] qSectorStdDevs
    ) {
        this(dateRange, qwds);
        this.qSectorAverages = qSectorAverages;
        this.qSectorStdDevs  = qSectorStdDevs;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public QWD [] getQWDs() {
        return qwds;
    }

    public void setQWDs(QWD [] qwds) {
        this.qwds = qwds;
    }

    public QWD [] getQSectorAverages() {
        return qSectorAverages;
    }

    public void setQSectorAverages(QWD [] qSectorAverages) {
        this.qSectorAverages = qSectorAverages;
    }

    public QWD getQSectorAverage(int i) {
        return qSectorAverages[i];
    }

    public double [] getQSectorStdDevs() {
        return qSectorStdDevs;
    }

    public void setQSectorStdDevs(double [] qSectorStdDevs) {
        this.qSectorStdDevs = qSectorStdDevs;
    }

    public double getQSectorStdDev(int i) {
        return qSectorStdDevs[i];
    }

    public double getMaxQ() {
        double maxQ = -Double.MAX_VALUE;
        if (qwds != null) {
            for (QWD qwd: qwds) {
                if (qwd.getQ() > maxQ) {
                    maxQ = qwd.getQ();
                }
            }
        }
        if (qSectorAverages != null) {
            for (QWD qwd: qSectorAverages) {
                if (qwd != null && qwd.getQ() > maxQ) {
                    maxQ = qwd.getQ();
                }
            }
        }
        return maxQ;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
