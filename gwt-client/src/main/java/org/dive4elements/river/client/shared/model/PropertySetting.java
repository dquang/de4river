/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class PropertySetting implements Property, Cloneable {

    /**The settings name.*/
    protected String name;

    /** The default value.*/
    protected String value;

    /** Additional attributes.*/
    protected HashMap<String, String> attributes;

    /**
     * Create a new StyleSetting for theme attribution.
     */
    public PropertySetting() {
        this.attributes = new HashMap<String, String>();
    }


    /**
     * Create a new PropertySet.
     * @param name The attribute name.
     * @param value The current value.
     */
    public PropertySetting(
        String name,
        String value)
    {
        this.name = name;
        this.value = value;
        this.attributes = new HashMap<String, String>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAttribute(String k, String v) {
        attributes.put(k, v);
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public List<String> getAttributeList() {
        return new ArrayList<String>(attributes.keySet());
    }

    @Override
    public Object clone() {
        PropertySetting clone = new PropertySetting(this.getName(),
                                                    this.getValue().toString());
        for(String s: this.getAttributeList()) {
            clone.setAttribute(s, this.getAttribute(s));
        }
        return clone;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
