/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class Style implements Serializable {

    /** The theme name. */
    protected String name;

    /** The facet. */
    protected String facet;

    /** The theme index. */
    protected int index;

    /** List of theme attribute settings. */
    protected List<StyleSetting> settings;


    /**
     * Create a new style for theme attribution.
     */
    public Style() {
        settings = new ArrayList<StyleSetting>();
    }


    /**
     * Append a new style setting.
     * @param setting A theme attribution setting.
     */
    public void appendStyleSetting(StyleSetting setting) {
        settings.add(setting);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public String getFacet() {
        return this.facet;
    }

    public int getIndex() {
        return this.index;
    }


    /**
     * Getter for a theme attribution setting.
     * @return The style setting.
     */
    public StyleSetting getSetting(String name) {
        for (int i = 0; i < settings.size (); i++) {
            if (settings.get(i).getName().equals(name)) {
                return settings.get(i);
            }
        }
        return null;
    }


    /**
     * Getter for number of settings.
     * @return The size of the settings list.
     */
    public int getNumSettings () {
        return settings.size();
    }


    /**
     * Getter for style settings.
     *
     * @return The list of style settings.
     */
    public List<StyleSetting> getSettings() {
        return this.settings;
    }


    /**
     * Getter for style setting.
     * @param i The index in the settings list.
     *
     * @return The style setting at the given index.
     */
    public StyleSetting getSetting(int i) {
        return this.settings.get(i);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
