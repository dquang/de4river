/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.HashMap;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class IntegerProperty extends PropertySetting {

    /**
     * Create a new IntegerProperty for settings.
     */
    public IntegerProperty() {
        this.attributes = new HashMap<String, String>();
    }


    /**
     * Create a new IntegerProperty.
     * @param name The attribute name.
     * @param value The current value.
     */
    public IntegerProperty(
        String name,
        Integer value)
    {
        this.name = name;
        this.value = value.toString();
        this.attributes = new HashMap<String, String>();
    }


    @Override
    public Integer getValue() {
        try {
            return Integer.valueOf(this.value);
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }


    public void setValue(Integer value) {
        this.value = value.toString();
    }

    public Object clone() {
        IntegerProperty clone = new IntegerProperty(this.getName(),
                                                    this.getValue());
        for(String s: this.getAttributeList()) {
            clone.setAttribute(s, this.getAttribute(s));
        }
        return clone;
    }

}
