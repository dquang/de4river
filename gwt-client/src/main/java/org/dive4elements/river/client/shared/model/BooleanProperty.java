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
public class BooleanProperty extends PropertySetting {

    /**
     * Create a new BooleanProperty for settings.
     */
    public BooleanProperty() {
        this.attributes = new HashMap<String, String>();
    }


    /**
     * Create a new BooleanProperty.
     * @param name The attribute name.
     * @param value The current value.
     */
    public BooleanProperty(
        String name,
        Boolean value)
    {
        this.name = name;
        this.value = value.toString();
        this.attributes = new HashMap<String, String>();
    }


    @Override
    public Boolean getValue() {
        return Boolean.valueOf(this.value);
    }


    public void setValue(Boolean value) {
        this.value = value.toString();
    }


    public Object clone() {
        BooleanProperty clone = new BooleanProperty(this.getName(),
                                                    this.getValue());
        for(String s: this.getAttributeList()) {
            clone.setAttribute(s, this.getAttribute(s));
        }
        return clone;
    }

}
