/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of properties.
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class PropertyGroup implements Property, Cloneable {

    /** The group name */
    protected String name;

    protected List<Property> properties;

    public PropertyGroup() {
    }

    public PropertyGroup(String name) {
        this.name = name;
    }

    public PropertyGroup(String name, List<Property> properties) {
        this.name = name;
        this.properties = properties;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Property getPropertyByName(String name) {
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get(i).getName().equals(name)) {
                return properties.get(i);
            }
        }
        return null;
    }

    @Override
    public Object clone() {
        PropertyGroup clone = new PropertyGroup(this.getName());
        List<Property> cloneList = new ArrayList<Property>();
        for(Property p: properties) {
            cloneList.add((Property)p.clone());
        }
        clone.setProperties(cloneList);
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for(Property p : properties) {
            buf.append(p.getName());
            buf.append("=");
            if(p instanceof PropertySetting) {
                buf.append(((PropertySetting)p).getValue().toString());
            }
            buf.append(" ");
        }

        return buf.toString();
    }
}
