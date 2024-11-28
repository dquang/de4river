/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class OutputSettings implements Settings, Cloneable {

    /** The output name. */
    protected String name;

    /** The categories and settings container. */
    protected HashMap<String, List<Property> > categories;


    public OutputSettings() {
        this.categories = new HashMap<String, List<Property> >();
    }


    public OutputSettings(String name) {
        this.name = name;
        this.categories = new HashMap<String, List<Property> >();
    }


    /** Set output name. */
    public void setName(String name) {
        this.name = name;
    }


    /** Get output name. */
    public String getName() {
        return this.name;
    }


    public void setSettings(String category, List<Property> settings) {
        if (this.categories == null) {
            this.categories = new HashMap<String, List<Property> >();
        }
        this.categories.put(category, settings);
    }


    public List<Property> getSettings(String category) {
        return categories.get(category);
    }


    public List<String> getCategories() {
        ArrayList<String> list = new ArrayList<String>(categories.keySet());
        return list;
    }


    public Object clone() {
        OutputSettings clone = new OutputSettings(this.getName());
        for (String s: this.getCategories()) {
            ArrayList cloneList = new ArrayList<Property>();
            for(Property p: this.getSettings(s)) {
                cloneList.add((Property)p.clone());
            }
            clone.setSettings(s, cloneList);
        }
        return clone;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

