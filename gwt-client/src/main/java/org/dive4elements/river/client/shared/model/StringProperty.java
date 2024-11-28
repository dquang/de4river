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
public class StringProperty extends PropertySetting {

    /**
     * Create a new StringProperty for settings.
     */
    public StringProperty() {
        this.attributes = new HashMap<String, String>();
    }


    /**
     * Create a new StringProperty.
     * @param name The attribute name.
     * @param value The current value.
     */
    public StringProperty(
        String name,
        String value)
    {
        this.name = name;
        this.value = value;
        this.attributes = new HashMap<String, String>();
    }


    @Override
    public String getValue() {
        return this.value;
    }


    public Object clone() {
        StringProperty clone = new StringProperty(this.getName(),
                                                    this.getValue());
        for(String s: this.getAttributeList()) {
            clone.setAttribute(s, this.getAttribute(s));
        }
        return clone;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

