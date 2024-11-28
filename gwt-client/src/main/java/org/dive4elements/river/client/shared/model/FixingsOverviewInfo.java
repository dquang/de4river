/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

public class FixingsOverviewInfo implements Serializable {

    protected List<FixEvent> events;
    protected String river;
    protected double from;
    protected double to;
    protected int rid;
    protected String html;

    protected FixingsOverviewInfo() {}

    public FixingsOverviewInfo(
        int rid,
        String river,
        double from,
        double to,
        List<FixEvent> events,
        String html
    ) {
        this.rid = rid;
        this.river = river;
        this.from = from;
        this.to = to;
        this.events = new ArrayList<FixEvent>(events);
        this.html = html;
    }

    public int getRId() {
        return this.rid;
    }

    public String getRiver() {
        return this.river;
    }

    public double getFrom() {
        return this.from;
    }

    public double getTo() {
        return this.to;
    }

    public List<FixEvent> getEvents() {
        return this.events;
    }

    public FixEvent getEventByCId(String cid) {
        for (FixEvent event: events) {
            if (event.getCId().equals(cid)) {
                return event;
            }
        }
        return null;
    }

    public String getHTML() {
        return this.html;
    }


    public static class FixEvent implements Serializable {
        protected String cid;
        protected String date;
        protected String description;
        protected List<Sector> sectors;

        protected FixEvent () {}

        public FixEvent(
            String cid,
            String date,
            String description,
            List<Sector> sectors
        ) {
            this.cid = cid;
            this.date = date;
            this.description = description;
            this.sectors = new ArrayList<Sector>(sectors);
        }

        public String getCId() {
            return this.cid;
        }

        public String getDate() {
            return this.date;
        }

        public String getDescription() {
            return this.description;
        }

        public List<Sector> getSectors() {
            return this.sectors;
        }
    }

    public static class Sector implements Serializable {
        protected int cls;
        protected double from;
        protected double to;

        protected Sector () {}

        public Sector(
            int cls,
            double from,
            double to
        ) {
            this.cls = cls;
            this.from = from;
            this.to = to;
        }

        public int getCls() {
            return this.cls;
        }

        public double getFrom() {
            return this.from;
        }

        public double getTo() {
            return this.to;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
