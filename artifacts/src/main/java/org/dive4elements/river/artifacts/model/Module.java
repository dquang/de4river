/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;

/**
 * Represents a Module as is is loaded from the config
 */
public class Module {

    private String name;
    List<String> rivers;
    private boolean selected;

    public Module(String name, boolean selected, List<String> rivers) {
        this.name = name;
        this.rivers = rivers;
        this.selected = selected;
    }

    public String getName() {
        return this.name;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public List<String> getRivers() {
        return this.rivers;
    }
}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 tw=80:
