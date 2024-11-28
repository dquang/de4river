/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.Date;
import com.smartgwt.client.widgets.grid.ListGridRecord;


/**
 * The WQInfoRecord is a wrapper to put  WQ Info objects into
 * a ListGrid.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class WQInfoRecord extends ListGridRecord {

    /** The artifact collection. */
    protected WQInfoObject wqInfo;


    /**
     * The default constructor.
     *
     * @param info The wq info object.
     */
    public WQInfoRecord(WQInfoObject info) {
        this.wqInfo = info;

        setName(info.getName());
        setType(info.getType());
        setValue(info.getValue());
        setStartTime(info.getStartTime());
        setStopTime(info.getStopTime());
        setOfficial(info.isOfficial() ? "X" : "");
    }


    public void setName(String name) {
        setAttribute("name", name);
    }


    public String getName() {
        return getAttributeAsString("name");
    }


    public void setType(String type) {
        setAttribute("type", type);
    }


    public String getType() {
        return getAttributeAsString("type");
    }

    public void setValue(double value) {
        setAttribute("value", value);
    }


    public double getValue() {
        return getAttributeAsDouble("value");
    }

    public void setStartTime(Date value) {
        setAttribute("starttime", value);
    }

    public Date getStartTime() {
        return getAttributeAsDate("starttime");
    }

    public void setStopTime(Date value) {
        setAttribute("stoptime", value);
    }

    public Date getStopTime() {
        return getAttributeAsDate("stoptime");
    }

    public void setOfficial(String value) {
        setAttribute("official", value);
    }

    public String getOfficial() {
        return getAttributeAsString("official");
    }


    public WQInfoObject getWQInfo() {
        return wqInfo;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
