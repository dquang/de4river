/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class StyleSetting implements Serializable {

    /**The settings name.*/
    protected String name;

    /** The default value.*/
    protected String defaultValue;

    /**The display name*/
    protected String displayName;

    /**Hints.*/
    protected String hints;

    /**The type*/
    protected String type;

    /** Determines, if the property should be visible in UI or not.*/
    protected boolean hidden;


    /**
     * Create a new StyleSetting for theme attribution.
     */
    public StyleSetting() {
    }


    /**
     * Create a new StyleSetting for theme attribution.
     * @param name The attribute name.
     * @param defaultValue The current value.
     * @param displayName The name to show in a dialog.
     * @param hints Hints.
     * @param type The attribute type.
     */
    public StyleSetting(
        String  name,
        String  defaultValue,
        String  displayName,
        String  hints,
        String  type,
        boolean hidden)
    {
        this.name         = name;
        this.defaultValue = defaultValue;
        this.displayName  = displayName;
        this.hints        = hints;
        this.type         = type;
        this.hidden       = hidden;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public void setHints(String hints) {
        this.hints = hints;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getHints() {
        return this.hints;
    }

    public String getType() {
        return this.type;
    }

    public boolean isHidden() {
        return hidden;
    }
}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
