/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import com.smartgwt.client.widgets.grid.ListGridRecord;

/** Simple record to store points. */
public class PointRecord extends ListGridRecord {
    protected static final String ATTRIBUTE_X = "X";
    protected static final String ATTRIBUTE_Y = "Y";
    protected static final String ATTRIBUTE_NAME = "name";
    protected static final String ATTRIBUTE_ACTIVE = "active";

    /** From a JSON-encoded point, create a PointRecord. */
    public static PointRecord fromJSON(JSONArray jsonArray) {
        JSONValue   x =              jsonArray.get(0);
        JSONNumber  y = (JSONNumber) jsonArray.get(1);
        JSONString  s = (JSONString) jsonArray.get(2);
        JSONBoolean b = (JSONBoolean)jsonArray.get(3);

        if(x instanceof JSONNumber) {
            return new PointRecord(
                    b.booleanValue(), ((JSONNumber)x).doubleValue(),
                    y.doubleValue(), s.stringValue());
        }
        else {
            return new PointRecord(
                    b.booleanValue(), ((JSONString)x).stringValue(),
                    y.doubleValue(), s.stringValue());
        }
    }

    protected boolean isTimeseriesPoint = false;

    public PointRecord(boolean isActive, double x, double y, String name) {
        setActive(isActive);
        setName(name);
        setX(x);
        setY(y);
    }

    /**
     * Constructor taking the x axis value as String representing a Date value.
     * @param isActive
     * @param x
     * @param y
     * @param name
     */
    public PointRecord(boolean isActive, String x, double y, String name) {
        setActive(isActive);
        setName(name);
        setX(x);
        setY(y);

        this.isTimeseriesPoint = true;
    }

    public void setActive(boolean isActive) {
        setAttribute(ATTRIBUTE_ACTIVE, isActive);
    }

    public boolean isActive() {
        return getAttributeAsBoolean(ATTRIBUTE_ACTIVE);
    }

    public boolean isTimeseriesPoint() {
        return this.isTimeseriesPoint;
    }

    public void setName(String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    public String getName() {
        return getAttributeAsString(ATTRIBUTE_NAME);
    }

    public void setX(double x) {
        setAttribute(ATTRIBUTE_X, x);
    }

    public void setX(String date) {
        setAttribute(ATTRIBUTE_X, date);
    }

    public void setY(double y) {
        setAttribute(ATTRIBUTE_Y, y);
    }

    public double getX() {
        return getAttributeAsDouble(ATTRIBUTE_X);
    }

    public String getXAsDate() {
        return getAttributeAsString(ATTRIBUTE_X);
    }

    public double getY() {
        return getAttributeAsDouble(ATTRIBUTE_Y);
    }
}
