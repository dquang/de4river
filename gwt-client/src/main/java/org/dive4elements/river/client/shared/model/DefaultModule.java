/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

public class DefaultModule implements Module {

    private String name;
    private String localname;
    private boolean selected = false;
    private List<String> rivers;

    public DefaultModule() {
    }

    public DefaultModule(
        String name,
        String localname,
        boolean selected,
        List<String> rivers) {
        this.name = name;
        this.localname = localname;
        this.selected = selected;
        this.rivers = rivers;
    }

    /**
     * Returns the name of the module
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the localized name of the module
     */
    public String getLocalizedName() {
        return this.localname;
    }

    /**
     * Returns true if the module should be selected
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * @return the rivers
     */
    public List<String> getRivers() {
        return this.rivers;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 tw=80 :
