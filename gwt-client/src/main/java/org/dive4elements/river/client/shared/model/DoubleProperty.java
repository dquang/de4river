/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import java.util.HashMap;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DoubleProperty extends PropertySetting {

    /**
     * Create a new DoubleProperty for settings.
     */
    public DoubleProperty() {
        this.attributes = new HashMap<String, String>();
    }


    /**
     * Create a new DoubleProperty.
     * @param name The attribute name.
     * @param value The current value.
     */
    public DoubleProperty(
        String name,
        Double value)
    {
        this.name = name;
        this.value = value.toString();
        this.attributes = new HashMap<String, String>();
    }

    @Override
    public Double getValue() {
        try {
            Double value = Double.valueOf(this.value);
            GWT.log("returning: " + value);
            return value;
        }
        catch(NumberFormatException nfe) {
            //Should never happen, if property is used correctly.
            return null;
        }
    }


    public void setValueFromUI(String value) {
        NumberFormat nf = NumberFormat.getDecimalFormat();
        double d;
        try {
            d = nf.parse(value);
            GWT.log("setting " + value + " as " + d);
            this.value = Double.toString(d);
        }
        catch(NumberFormatException nfe) {}
    }

    public void setValue(Double value) {
        this.value = value.toString();
    }


    public String toUIString() {
        double dv;
        NumberFormat nf = NumberFormat.getDecimalFormat();
        try {
            dv = Double.parseDouble(this.value);
        }
        catch (NumberFormatException nfe) {
            return null;
        }
        return nf.format(dv);
    }

    @Override
    public Object clone() {
        DoubleProperty clone = new DoubleProperty(this.getName(),
                                                  this.getValue());
        for(String s: this.getAttributeList()) {
            clone.setAttribute(s, this.getAttribute(s));
        }
        return clone;
    }
}
