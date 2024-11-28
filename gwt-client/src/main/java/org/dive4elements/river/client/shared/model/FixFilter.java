/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

/** Probably something like *Access, but from client side. */
public class FixFilter implements Serializable{

    protected String river;
    protected double fromKm;
    protected double toKm;
    protected double currentKm;
    protected int fromClass;
    protected int toClass;
    protected long fromDate;
    protected long toDate;
    protected boolean hasDate;
    protected int[] events;

    public FixFilter() {
        this.river = "";
        this.fromKm = -Double.MAX_VALUE;
        this.toKm = -1;
        this.currentKm = -1;
        this.fromClass = -1;
        this.toClass = -1;
        this.fromDate = Long.MIN_VALUE;
        this.toDate = Long.MIN_VALUE;
        this.hasDate = false;
        this.events = new int[0];
    }

    public void setRiver(String river) {
        this.river = river;
    }

    public void setFromKm(double from) {
        this.fromKm = from;
    }

    public void setToKm(double to) {
        this.toKm = to;
    }

    public void setCurrentKm(double km) {
        this.currentKm = km;
    }

    public void setFromClass(int from) {
        this.fromClass = from;
    }

    public void setToClass(int to) {
        this.toClass = to;
    }

    public void setFromDate(long from) {
        this.hasDate = true;
        this.fromDate = from;
    }

    public void setToDate(long to) {
        this.hasDate = true;
        this.toDate = to;
    }

    public void setEvents(int[] ev) {
        this.events = ev;
    }

    public String getRiver() {
        return this.river;
    }

    public double getFromKm() {
        return this.fromKm;
    }

    public double getToKm() {
        return this.toKm;
    }

    public double getCurrentKm() {
        return this.currentKm;
    }

    public int getFromClass() {
        return this.fromClass;
    }

    public int getToClass() {
        return this.toClass;
    }

    public long getFromDate() {
        return this.fromDate;
    }

    public long getToDate() {
        return this.toDate;
    }

    public int[] getEvents() {
        return this.events;
    }

    public boolean hasDate() {
        return fromDate != Long.MIN_VALUE && toDate != Long.MIN_VALUE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
