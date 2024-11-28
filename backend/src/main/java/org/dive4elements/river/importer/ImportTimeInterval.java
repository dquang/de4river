/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import org.dive4elements.river.model.TimeInterval;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.List;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ImportTimeInterval
{
    private static Logger log = LogManager.getLogger(ImportTimeInterval.class);

    protected Date startTime;
    protected Date stopTime;

    protected TimeInterval peer;

    public ImportTimeInterval() {
    }

    public ImportTimeInterval(Date startTime) {
        this(startTime, null);
    }

    public ImportTimeInterval(Date startTime, Date stopTime) {

        if (startTime != null && stopTime == null) {
            this.startTime = startTime;
            this.stopTime = null;
        }
        else if (startTime == null && stopTime != null) {
            this.startTime = stopTime;
            this.stopTime = null;
        }
        else if (startTime == null && stopTime == null) {
            throw new IllegalArgumentException("Both dates are null.");
        }
        else {
            if (startTime.after(stopTime)) {
                Date t = startTime;
                startTime = stopTime;
                stopTime = t;
            }
            this.startTime = startTime;
            this.stopTime = stopTime;
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public TimeInterval getPeer() {
        if (peer == null) {
            Session session = ImporterSession.getInstance()
                .getDatabaseSession();
            if (startTime == null) {
                log.error("Null Start time will be ignored.");
            }
            Query query;
            if (stopTime == null) {
                query = session.createQuery(
                    "from TimeInterval "
                    + "where startTime=:a and stopTime is null");
            }
            else {
                query = session.createQuery(
                    "from TimeInterval where startTime=:a and stopTime=:b");
                query.setParameter("b", stopTime);
            }
            query.setParameter("a", startTime);

            List<TimeInterval> intervals = query.list();
            if (intervals.isEmpty()) {
                peer = new TimeInterval(startTime, stopTime);
                session.save(peer);
            }
            else {
                peer = intervals.get(0);
            }
        }
        return peer;
    }

    @Override
    public String toString() {
        return "start time: " + startTime + ", stop time: " + stopTime;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
