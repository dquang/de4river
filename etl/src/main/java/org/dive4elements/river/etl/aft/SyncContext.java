/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.aft;

import org.dive4elements.river.etl.db.ConnectedStatements;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SyncContext
{
    private static Logger log = LogManager.getLogger(SyncContext.class);

    protected ConnectedStatements             aftStatements;
    protected ConnectedStatements             flysStatements;
    protected Document                        dips;

    protected Map<Long, DIPSGauge>            numberToGauge;
    protected Map<TimeInterval, TimeInterval> flysTimeIntervals;

    public SyncContext() {
    }

    public SyncContext(
        ConnectedStatements aftStatements,
        ConnectedStatements flysStatements,
        Document            dips
    ) {
        this.aftStatements  = aftStatements;
        this.flysStatements = flysStatements;
        this.dips           = dips;
    }

    public void init() throws SQLException {
        numberToGauge       = indexByNumber(dips);
        flysTimeIntervals   = loadTimeIntervals();
    }

    public ConnectedStatements getAftStatements() {
        return aftStatements;
    }

    public void setAftStatements(ConnectedStatements aftStatements) {
        this.aftStatements = aftStatements;
    }

    public ConnectedStatements getFlysStatements() {
        return flysStatements;
    }

    public void setFlysStatements(ConnectedStatements flysStatements) {
        this.flysStatements = flysStatements;
    }

    public Document getDips() {
        return dips;
    }

    public void setDips(Document dips) {
        this.dips = dips;
    }

    void close() {
        aftStatements.close();
        flysStatements.close();
    }

    public static Long numberToLong(String s) {
        try {
            return Long.valueOf(s.trim());
        }
        catch (NumberFormatException nfe) {
        }
        return null;
    }

    public Map<Long, DIPSGauge> getDIPSGauges() {
        return numberToGauge;
    }

    public Map<Long, DIPSGauge> getDIPSGauges(
        String riverName,
        double from,
        double to
    ) {
        if (from > to) {
            double t = from;
            from = to;
            to = t;
        }

        riverName = riverName.toLowerCase();

        Map<Long, DIPSGauge> result = new HashMap<Long, DIPSGauge>();

        for (Map.Entry<Long, DIPSGauge> entry: numberToGauge.entrySet()) {
            DIPSGauge gauge = entry.getValue();
            // XXX: Maybe a bit too sloppy.
            if (!riverName.contains(gauge.getRiverName().toLowerCase())) {
                continue;
            }
            double station = gauge.getStation();
            if (station >= from && station <= to) {
                result.put(entry.getKey(), gauge);
            } else {
                log.warn("DIPS: Skipping Gauge: " + gauge.getName() +
                        " because it is at Station: " + station +
                        " and the river is limited to: " + from + " - " + to);
            }
        }

        return result;
    }

    protected static Map<Long, DIPSGauge> indexByNumber(Document document) {
        Map<Long, DIPSGauge> map = new HashMap<Long, DIPSGauge>();
        NodeList nodes = document.getElementsByTagName("PEGELSTATION");
        for (int i = nodes.getLength()-1; i >= 0; --i) {
            Element element = (Element)nodes.item(i);
            String numberString = element.getAttribute("NUMMER");
            Long number = numberToLong(numberString);
            if (number != null) {
                DIPSGauge newG = new DIPSGauge(element);
                DIPSGauge oldG = map.put(number, newG);
                if (oldG != null) {
                    log.warn("DIPS: '" + newG.getName() +
                        "' collides with '" + oldG.getName() +
                        "' on gauge number " + number + ".");
                }
            }
            else {
                log.warn("DIPS: Gauge '" + element.getAttribute("NAME") +
                    "' has invalid gauge number '" + numberString + "'.");
            }
        }
        return map;
    }

    protected Map<TimeInterval, TimeInterval> loadTimeIntervals()
    throws SQLException {

        boolean debug = log.isDebugEnabled();

        Map<TimeInterval, TimeInterval> intervals =
            new TreeMap<TimeInterval, TimeInterval>();

        ResultSet rs = flysStatements
            .getStatement("select.timeintervals")
            .executeQuery();

        try {
            while (rs.next()) {
                int  id    = rs.getInt("id");
                Date start = rs.getDate("start_time");
                Date stop  = rs.getDate("stop_time");

                if (debug) {
                    log.debug("id:    " + id);
                    log.debug("start: " + start);
                    log.debug("stop:  " + stop);
                }

                TimeInterval ti = new TimeInterval(id, start, stop);
                intervals.put(ti, ti);
            }
        }
        finally {
            rs.close();
        }

        if (debug) {
            log.debug("loaded time intervals: " + intervals.size());
        }

        return intervals;
    }

    public TimeInterval fetchOrCreateFLYSTimeInterval(TimeInterval key)
    throws SQLException
    {
        TimeInterval old = flysTimeIntervals.get(key);
        if (old != null) {
            return old;
        }

        ResultSet rs = flysStatements
            .getStatement("next.timeinterval.id")
            .executeQuery();

        try {
            rs.next();
            key.setId(rs.getInt("time_interval_id"));
        }
        finally {
            rs.close();
        }

        if (log.isDebugEnabled()) {
            log.debug("FLYS: Created time interval id: " + key.getId());
            log.debug("FLYS: " + key);
        }

        flysStatements.getStatement("insert.timeinterval")
            .clearParameters()
            .setInt("id", key.getId())
            .setObject("start_time", key.getStart())
            .setObject("stop_time", key.getStop())
            .execute();

        flysTimeIntervals.put(key, key);

        return key;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
